package com.isaacabarca.devops_dashboard.service;

import com.isaacabarca.devops_dashboard.dto.request.LoginRequest;
import com.isaacabarca.devops_dashboard.dto.request.SignUpRequest;
import com.isaacabarca.devops_dashboard.dto.response.JwtResponse;
import com.isaacabarca.devops_dashboard.entity.User;
import com.isaacabarca.devops_dashboard.enums.Role;
import com.isaacabarca.devops_dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;  // ← NUEVO

    public void signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .verified(false)
                .role(Role.USER)
                .build();

        userRepository.save(user);

        String verificationToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
    }

    public JwtResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Email o contraseña incorrectos");
        }

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        // ← NUEVO: Guardar refresh token en Redis
        refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken);

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    public JwtResponse refreshToken(String refreshToken) {
        String email = jwtService.extractEmail(refreshToken);

        if (!refreshTokenService.isValidRefreshToken(email, refreshToken)) {
            throw new BadCredentialsException("Refresh token inválido o expirado");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        String newAccessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail());

        // Rotar refresh token: eliminar el viejo y guardar el nuevo
        refreshTokenService.deleteRefreshToken(email);
        refreshTokenService.saveRefreshToken(email, newRefreshToken);

        return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    public void logout(String email) {
        refreshTokenService.deleteRefreshToken(email);
    }

    public void verifyEmail(String token) {
        String email = jwtService.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.isVerified()) {
            throw new RuntimeException("El email ya está verificado");
        }

        user.setVerified(true);
        userRepository.save(user);
    }
}