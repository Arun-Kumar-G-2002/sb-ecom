package com.ecommerce.project.security.controller;

import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.request.LoginRequestDTO;
import com.ecommerce.project.security.request.SignupRequestDTO;
import com.ecommerce.project.security.response.UserInfoResponseDTO;
import com.ecommerce.project.security.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDTO loginRequest) {
        return authService.authenticateUser(loginRequest);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequestDTO signUpRequest) {
        return authService.registerUser(signUpRequest);
    }

    @GetMapping("/username")
    public String getCurrentUserName(Authentication authentication) {
        return authService.currentUserName(authentication);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserDetails(Authentication authentication) {
        UserInfoResponseDTO response = authService.getUserDetails(authentication);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signOutUser() {
        ResponseCookie cookie = authService.signOutUser();
        return ResponseEntity.ok().header("Set-Cookie", cookie.toString())
                .body("User signed out successfully.");
    }
}
