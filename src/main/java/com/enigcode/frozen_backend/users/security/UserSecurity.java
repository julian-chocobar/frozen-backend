package com.enigcode.frozen_backend.users.security;

import com.enigcode.frozen_backend.users.service.UserService;
import com.enigcode.frozen_backend.users.DTO.UserDetailDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {

    private final UserService userService;

    public boolean isSelf(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            return false;
        }
        UserDetailDTO me = userService.getUserByUsername(auth.getName());
        return me != null && me.getId() != null && me.getId().equals(userId);
    }
}
