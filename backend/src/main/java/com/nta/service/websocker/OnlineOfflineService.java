package com.nta.service.websocker;

import com.nta.entity.User;
import com.nta.model.AuthenticatedUserDetail;
import com.nta.repository.UserRepository;
import com.nta.service.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class OnlineOfflineService {
    private final Set<String> onlineUsers;
    UserRepository userRepository;
    SimpMessageSendingOperations simpMessageSendingOperations;
    AuthenticationService authenticationService;
    protected Map<String, Set<String>> userSubscribed;

    public void addOnlineUser(Principal user) {
        if (user == null) return;
        var userDetails = authenticationService.getUserDetail(user);
        log.info("{} is online", userDetails.getUsername());
        onlineUsers.add(userDetails.getId());
    }

    public void removeOnlineUser(Principal user) {
        if (user != null) {
            var userDetails = authenticationService.getUserDetail(user);
            log.info("{} went offline", userDetails.getUsername());
            onlineUsers.remove(userDetails.getId());
            userSubscribed.remove(userDetails.getId());
        }
    }

    public boolean isUserOnline(String userId) {
        return onlineUsers.contains(userId);
    }

    public Map<String, Set<String>> getUserSubscribed() {
        Map<String, Set<String>> result = new HashMap<>();
        List<User> users = userRepository.findAllById(userSubscribed.keySet());
        users.forEach(user -> result.put(user.getUsername(), userSubscribed.get(user.getId())));
        return result;
    }

    public void addUserSubscribed(Principal user, String subscribedChannel) {
        var userDetails = authenticationService.getUserDetail(user);
        log.info("{} subscribed to {}", userDetails.getUsername(), subscribedChannel);
        Set<String> subscriptions = userSubscribed.getOrDefault(userDetails.getId(), new HashSet<>());
        subscriptions.add(subscribedChannel);
        userSubscribed.put(userDetails.getId(), subscriptions);
    }

    public void removeUserSubscribed(Principal user, String subscribedChannel) {
        var userDetails = authenticationService.getUserDetail(user);
        log.info("unsubscription! {} unsubscribed {}", userDetails.getUsername(), subscribedChannel);
        Set<String> subscriptions = userSubscribed.getOrDefault(userDetails.getId(), new HashSet<>());
        subscriptions.remove(subscribedChannel);
        userSubscribed.put(userDetails.getId(), subscriptions);
    }

    public boolean isUserSubscribed(String username, String subscription) {
        Set<String> subscriptions = userSubscribed.getOrDefault(username, new HashSet<>());
        return subscriptions.contains(subscription);
    }


}
