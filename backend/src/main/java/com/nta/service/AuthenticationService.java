package com.nta.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nta.dto.request.AuthenticationRequest;
import com.nta.dto.request.IntrospectRequest;
import com.nta.dto.request.LogoutRequest;
import com.nta.dto.response.AuthenticationResponse;
import com.nta.dto.response.IntrospectResponse;
import com.nta.entity.User;
import com.nta.enums.TokenType;
import com.nta.exception.AppException;
import com.nta.enums.ErrorCode;
import com.nta.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
@PropertySource("classpath:application-dev.properties")
public class AuthenticationService {

    UserRepository userRepository;
    RedisService redisService;
    Environment env;
    PasswordEncoder passwordEncoder;
    public AuthenticationResponse authenticated(AuthenticationRequest authenticationRequest) throws JOSEException {
        var user = userRepository.findByUsername(authenticationRequest.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if(!passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword())){
            throw new AppException(ErrorCode.INCORRECT_PASSWORD);
        }

        var accessToken = generateToken(user,TokenType.ACCESS_TOKEN);
        var refreshToken = generateToken(user,TokenType.FRESH_TOKEN);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    String generateToken(User user, TokenType type) throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        Date expirationTime = TokenType.ACCESS_TOKEN.equals(type) ? new Date(Instant.now().plus(1,ChronoUnit.HOURS).toEpochMilli()) :
                new Date(Instant.now().plus(3,ChronoUnit.DAYS).toEpochMilli());
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("nta.com") // chỉ định token đợc issue từ ai
                .issueTime(new Date())
                .expirationTime(expirationTime)
                .jwtID(UUID.randomUUID().toString())
                .claim("scope",user.getRole()) // tuân theo code convention oauth2: tên claim -> scope, role cách nhau 1 khoảng trang
                .build();


        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header,payload); // sẽ cần nhận vào 2 đối số là header và payload

        try {
            String SIGNER_KEY = env.getProperty("jwt.signer");
            jwsObject.sign(new MACSigner(SIGNER_KEY));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot craete token",e);
            throw new RuntimeException(e);
        }
    }

    // Dùng để verify token trong controller
    public IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException
    {
        var token = request.getToken();
        boolean isValid = true;
        try {
            verifyToken(token); // Nếu token hết hạn or token sai thì sẽ throw error
        }catch (AppException e) {
            isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    public void logout(LogoutRequest request) {
        if(redisService.isRedisLive()) {
            redisService.set(request.getToken(),"1");
        }
    }

    private SignedJWT verifyToken(String token) throws ParseException, JOSEException {
        String SIGNER_KEY = env.getProperty("jwt.signer");
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);

        if(!(verified && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Check if token is a logouted token (in black list in redis)
        if(redisService.isRedisLive() && redisService.hasValue(token,"1")) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return signedJWT;
    }

}
