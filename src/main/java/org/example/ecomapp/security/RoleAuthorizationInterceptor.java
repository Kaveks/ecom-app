package org.example.ecomapp.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.ecomapp.model.User;
import org.example.ecomapp.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RoleAuthorizationInterceptor implements HandlerInterceptor {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRoles requireRoles =
                handlerMethod.getMethodAnnotation(RequireRoles.class);

        if (requireRoles == null) {
            return true;
        }

        String userIdHeader = request.getHeader(USER_ID_HEADER);

        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "X-User-Id header is required."
            );
        }

        long userId;

        try {
            userId = Long.parseLong(userIdHeader);
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "X-User-Id must be a valid user ID."
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "The specified user was not found."
                ));

        boolean allowed = java.util.Arrays.stream(requireRoles.value())
                .anyMatch(role -> role == user.getRole());

        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to perform this operation."
            );
        }

        return true;
    }
}