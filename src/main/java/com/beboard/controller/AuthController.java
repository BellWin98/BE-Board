package com.beboard.controller;

import com.beboard.dto.AuthDto;
import com.beboard.dto.UserDto;
import com.beboard.entity.User;
import com.beboard.service.AuthService;
import com.beboard.service.UserService;
import com.beboard.util.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;


    @PostMapping("/login")
    public ResponseEntity<AuthDto.LoginResponse> login(@Valid @RequestBody AuthDto.LoginRequest request) {
        AuthDto.LoginResponse loginResponse = authService.login(request);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto.Response> register(@Valid @RequestBody UserDto.RegisterRequest request) {
        UserDto.Response registeredUser = userService.register(request);

        return ResponseEntity.ok(registeredUser);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto.Response> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(UserDto.Response.from(currentUser));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDto.Response> updateProfile(
            @RequestBody UserDto.UpdateProfileRequest request,
            @AuthenticationPrincipal User currentUser) {

        UserDto.Response user = userService.updateProfile(currentUser.getId(), request);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/change-password")
    public ResponseEntity<UserDto.Response> changePassword(
            @RequestBody UserDto.ChangePasswordRequest request,
            @AuthenticationPrincipal User currentUser) {

        UserDto.Response user = userService.changePassword(currentUser.getId(), request);

        return ResponseEntity.ok(user);
    }


}
