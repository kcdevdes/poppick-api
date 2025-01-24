package com.kcdevdes.poppick.service;

import com.kcdevdes.poppick.domain.User;
import com.kcdevdes.poppick.dto.LoginRequestDto;
import com.kcdevdes.poppick.dto.SignupRequestDto;
import com.kcdevdes.poppick.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

// Service Class
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
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

    public User login(LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getPassword() == null) {
            throw new RuntimeException("This account is linked to an OAuth provider");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
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
        User user = userRepository.findByOauthProviderAndOauthId(provider, oauthId)
                .orElseThrow(() -> new RuntimeException("OAuth user not found"));

        return user;
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserByOauth(String oauthProvider, String oauthId) {
        return userRepository.findByOauthProviderAndOauthId(oauthProvider, oauthId);
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
