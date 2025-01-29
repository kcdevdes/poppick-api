package com.kcdevdes.poppick.service;

import com.kcdevdes.poppick.entity.User;
import com.kcdevdes.poppick.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(
            UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get a user by ID
     *
     * @param id
     * @return User
     * @throws ResponseStatusException if user not found
     */
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found"));
    }

    /**
     * Get a user by email
     *
     * @param email
     * @return User
     * @throws ResponseStatusException if user not found
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found"));
    }

    /**
     * Get a user by OAuth provider and ID
     *
     * @param email
     * @param oauthProvider
     * @param oauthId
     * @return User
     * @throws ResponseStatusException if user not found or OAuth details mismatch
     */
    public User getUserByOauth(String email, String oauthProvider, String oauthId) {
        // Check if user exists and OAuth details match
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found"));
        if (!isOAuthUser(user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Standard Auth Required");
        }

        if (!compareOauthDetails(user, oauthProvider, oauthId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong OAuth credentials");
        }

        return user;
    }

    /**
     * Update a user by ID
     *
     * @param userId
     * @param updatedUser
     * @return updated user
     */
    public User updateUserById(int userId, User updatedUser) {
        User user = getUserById(userId);

        // Update user details
        user.setUsername(updatedUser.getUsername());
        user.setProfileImage(updatedUser.getProfileImage());

        return userRepository.save(user);
    }

    /**
     * Update a user by email
     *
     * @param email
     * @param updatedUser
     * @return updated user
     */
    public User updateUserByEmail(String email, User updatedUser) {
        User user = getUserByEmail(email);

        // Update user details
        user.setUsername(updatedUser.getUsername());
        user.setProfileImage(updatedUser.getProfileImage());

        return userRepository.save(user);
    }

    /**
     * Delete a user by ID
     *
     * @param id
     */
    public void deleteUserById(int id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    /**
     * Delete a user by email
     *
     * @param email
     */
    public void deleteUserByEmail(String email) {
        User user = getUserByEmail(email);
        userRepository.delete(user);
    }

    /**
     * Check if a user is an OAuth user
     *
     * @param user
     * @return true if user is an OAuth user
     */
    private boolean isOAuthUser(User user) {
        return user.getOauthProvider() != null && user.getOauthId() != null;
    }

    /**
     * Compare OAuth details
     *
     * @param user
     * @param provider
     * @param oauthId
     * @return true if OAuth details match
     */
    private boolean compareOauthDetails(User user, String provider, String oauthId) {
        return user.getOauthProvider().equals(provider) && user.getOauthId().equals(oauthId);
    }
}