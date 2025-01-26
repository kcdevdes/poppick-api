package com.kcdevdes.poppick.service;

import com.kcdevdes.poppick.domain.User;
import com.kcdevdes.poppick.dto.JwtResponseDto;
import com.kcdevdes.poppick.dto.LoginRequestDto;
import com.kcdevdes.poppick.dto.SignupRequestDto;
import com.kcdevdes.poppick.exception.ResourceNotFoundException;
import com.kcdevdes.poppick.util.JwtProvider;
import com.kcdevdes.poppick.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Service Class
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public UserService(
            UserRepository userRepository,
            ModelMapper modelMapper,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider,
            AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    /// Standard Login ///
    public User registerStandardUser(SignupRequestDto dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("USER");

        return userRepository.save(user);
    }

    public JwtResponseDto login(LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getPassword() == null) {
            throw new RuntimeException("This account is linked to an OAuth provider");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole()));

        // Authentication 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                        dto.getEmail(),
                        null,
                        authorities
        );

        // JWT 토큰 생성
        return jwtProvider.generateToken(authentication);
    }

    /// OAuth Login ///
    public User registerOauthUser(String email, String provider, String oauthId, String name) {
        User user = new User();
        user.setEmail(email);
        user.setOauthProvider(provider);
        user.setOauthId(oauthId);
        user.setUsername(name);
        user.setRole("USER");

        return userRepository.save(user);
    }

    public User oauthLogin(String provider, String oauthId) {
        return userRepository.findByOauthProviderAndOauthId(provider, oauthId)
                .orElseThrow(() -> new RuntimeException("OAuth user not found"));
    }

    public User getUserById(Integer id) {
        Optional<User> userOptional = userRepository.findById(id);
        return userOptional.orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }

    public User getUserByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserByOauth(String oauthProvider, String oauthId) {
        Optional<User> userOptional = userRepository.findByOauthProviderAndOauthId(oauthProvider, oauthId);
        return userOptional.orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUser(int userId, User updatedUser) {
        // Find the existing user
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update fields
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());

        // Save updated user
        return userRepository.save(existingUser);
    }

    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }

        userRepository.deleteById(id);
    }

    ///  Util Methods ///
    public UserType getUserType(User user) {
        if (user.getOauthProvider() != null && user.getOauthId() != null) {
            return UserType.OAUTH;
        } else {
            return UserType.STANDARD;
        }
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}

enum UserType {
    STANDARD,
    OAUTH,
}
