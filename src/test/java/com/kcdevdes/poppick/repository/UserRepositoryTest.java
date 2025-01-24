package com.kcdevdes.poppick.repository;

import com.kcdevdes.poppick.domain.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @AfterEach()
    void deleteAll() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Save and Find User")
    void testSaveAndFindUser() {
        // Given
        User user = new User(null, "test@example.com", "testuser", null, "USER", null, null, null, null, null);
        User savedUser = userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getCreatedAt()).isNotNull();
        assertThat(foundUser.get().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Update User")
    void testUpdateUser() {
        // Given
        User user = new User(null, "test@example.com", "testuser", null, "USER", null, null, null, null, null);
        User savedUser = userRepository.save(user);

        // When
        savedUser.setEmail("test2@example.com");
        User updatedUser = userRepository.save(savedUser);

        // Then
        assertThat(updatedUser.getEmail()).isEqualTo("test2@example.com");
    }
}