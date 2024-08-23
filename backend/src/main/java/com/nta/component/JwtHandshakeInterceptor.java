package com.nta.component;

import com.nta.exception.AppException;
import com.nta.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
    private final AuthenticationService authenticationService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String uri = request.getURI().toString();
        String token = null;

        if (uri.contains("token=")) {
            String[] parts = uri.split("token=");
            if (parts.length > 1) {
                token = parts[1];
            }
        }
        if(token == null) return false;
        try{
            authenticationService.verifyToken(token);
            return true;
        } catch (AppException e) {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
