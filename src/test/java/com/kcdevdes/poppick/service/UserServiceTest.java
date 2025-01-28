package com.kcdevdes.poppick.service;

import com.kcdevdes.poppick.entity.User;
import com.kcdevdes.poppick.dto.request.LoginRequestDto;
import com.kcdevdes.poppick.dto.request.SignupRequestDto;
import com.kcdevdes.poppick.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final ModelMapper modelMapper = new ModelMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, modelMapper, passwordEncoder); // ModelMapper 주입
    }

    @Test
    void registerUser() {
        // Given
        SignupRequestDto signupRequestDto = new SignupRequestDto("test@example.com", "John Doe", "password123");
        User mockUser = new User(1, "test@example.com", "John Doe", null, "USER", null, null, null, null, null);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // When
        User createdUser = userService.registerUser(signupRequestDto);

        // Then
        assertThat(createdUser.getEmail()).isEqualTo("test@example.com");
        assertThat(createdUser.getUsername()).isEqualTo("John Doe");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void issueJWTStandardUser() {
        // Given
        LoginRequestDto loginRequestDto = new LoginRequestDto("test@example.com",  "password123");
        User mockUser = new User(1, "test@example.com", "John Doe", "password123", "USER", null, null, null, null, null);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);


        // When
        User createdUser = userService.issueJWT(loginRequestDto);

        // Then
        assertThat(createdUser.getEmail()).isEqualTo("test@example.com");
        assertThat(createdUser.getUsername()).isEqualTo("John Doe");
        verify(userRepository, times(1)).save(any(User.class));

    }

    @Test
    void getUserById() {
        // Given
        User user = new User(1, "test@example.com", "testuser", null, "USER", null, null, null, null, null);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        // When
        Optional<User> foundUserOptional = userService.getUserById(1);
        User foundUser = foundUserOptional.get();

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(1);
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    void getUserByEmail() {
        // Given
        User user = new User(1, "test@example.com", "testuser", null, "USER", null, null, null, null, null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // When
        Optional<User> foundUserOptional = userRepository.findByEmail("test@example.com");
        User foundUser = foundUserOptional.get();

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void getUserByOauth() {
        // Given
        User user = new User(1, "test@example.com", "testuser", null, "USER", null, "google", "12345", null, null);
        when(userRepository.findByOauthProviderAndOauthId("google", "12345")).thenReturn(Optional.of(user));

        // When
        Optional<User> foundUserOptional = userService.getUserByOauth("google", "12345");
        User foundUser = foundUserOptional.get();

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getOauthProvider()).isEqualTo("google");
        assertThat(foundUser.getOauthId()).isEqualTo("12345");
        verify(userRepository, times(1)).findByOauthProviderAndOauthId("google", "12345");
    }

    @Test
    void updateUser() {
        // Given
        User existingUser = new User(1, "test@example.com", "testuser", null, "USER", null, null, null, null, null);
        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));

        User updatedUser = new User(1, "test@example.com", "updateuser", null, "USER", null, null, null, null, null);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        User result = userService.updateUser(1, updatedUser);

        // Then
        assertThat(result.getUsername()).isEqualTo("updateduser");
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deleteUser() {
        // Given
        when(userRepository.existsById(1)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1);

        // When
        userService.deleteUser(1);

        // Then
        verify(userRepository, times(1)).existsById(1);
        verify(userRepository, times(1)).deleteById(1);
    }

    @Test
    void testPasswordHashing() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 비밀번호 해싱
        String rawPassword = "securepassword";
        String hashedPassword = encoder.encode(rawPassword);

        // 해싱 결과는 항상 달라야 함
        assertThat(hashedPassword).isNotEqualTo(rawPassword);

        // 비밀번호 검증
        boolean matches = encoder.matches(rawPassword, hashedPassword);
        assertThat(matches).isTrue();
    }
}
