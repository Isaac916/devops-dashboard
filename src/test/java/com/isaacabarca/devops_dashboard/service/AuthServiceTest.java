package com.isaacabarca.devops_dashboard.service;

import com.isaacabarca.devops_dashboard.dto.request.LoginRequest;
import com.isaacabarca.devops_dashboard.dto.request.SignUpRequest;
import com.isaacabarca.devops_dashboard.dto.response.JwtResponse;
import com.isaacabarca.devops_dashboard.entity.User;
import com.isaacabarca.devops_dashboard.enums.Role;
import com.isaacabarca.devops_dashboard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private SignUpRequest signUpRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        signUpRequest = SignUpRequest.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password("Test12345")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("Test12345")
                .build();

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .enabled(true)
                .verified(true)
                .role(Role.USER)
                .build();
    }

    @Test
    void signUp_ShouldSaveUser_WhenEmailNotExists() {
        when(userRepository.existsByEmail(signUpRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signUpRequest.getPassword())).thenReturn("encodedPassword");
        when(jwtService.generateAccessToken(any(), any())).thenReturn("verificationToken");

        authService.signUp(signUpRequest);

        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(eq("test@example.com"), eq("verificationToken"));
    }

    @Test
    void signUp_ShouldThrowException_WhenEmailExists() {
        when(userRepository.existsByEmail(signUpRequest.getEmail())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.signUp(signUpRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_ShouldReturnJwtResponse_WhenCredentialsValid() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateAccessToken(user.getEmail(), user.getRole().name())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user.getEmail())).thenReturn("refreshToken");
        when(jwtService.getJwtExpiration()).thenReturn(900000L);

        JwtResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void login_ShouldThrowException_WhenPasswordInvalid() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }
}
