package com.kcdevdes.poppick.domain;

import com.kcdevdes.poppick.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class UserTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Save two different users with the same email address and expect an exception")
    void testUniqueConstraintViolation() {
        // Given
        User user1 = new User();
        user1.setEmail("duplicate@example.com");
        user1.setUsername("user1");

        User user2 = new User();
        user2.setEmail("duplicate@example.com"); // Same Email address used
        user2.setUsername("user2");

        // When
        userRepository.save(user1);

        // Then
        assertThrows(Exception.class, () -> userRepository.save(user2)); // Throw Exception
    }

    @Test
    @DisplayName("Save a user with a null email address and expect an exception")
    void testNullableConstraintViolation() {
        // Given
        User user = new User();
        user.setEmail(null); // 이메일 누락
        user.setUsername("testuser");

        // Then
        assertThrows(Exception.class, () -> userRepository.save(user));
    }

    @Test
    @DisplayName("Save a user, update the user info and verify createdAt and updatedAt")
    void testPrePersistAndPreUpdate() {
        // Create
        User user = new User();
        user.setEmail("pretest@example.com");
        user.setUsername("pretestuser");
        User savedUser = userRepository.save(user);

        // Verify createdAt and updatedAt
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();

        // Update
        savedUser.setUsername("updateduser");
        User updatedUser = userRepository.save(savedUser);

        // Verify updatedAt changes
        assertThat(updatedUser.getUpdatedAt()).isAfter(savedUser.getCreatedAt());
    }

}
