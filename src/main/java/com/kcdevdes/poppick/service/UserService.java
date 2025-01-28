package com.kcdevdes.poppick.service;

import com.kcdevdes.poppick.dto.request.OauthSignupRequestDto;
import com.kcdevdes.poppick.entity.Role;
import com.kcdevdes.poppick.entity.User;
import com.kcdevdes.poppick.dto.response.JwtResponseDto;
import com.kcdevdes.poppick.dto.request.LoginRequestDto;
import com.kcdevdes.poppick.dto.request.SignupRequestDto;
import com.kcdevdes.poppick.util.JwtProvider;
import com.kcdevdes.poppick.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    /////////////////////////////////////////////////////////////
    //////////////// Standard Login /////////////////////////////
    /////////////////////////////////////////////////////////////

    /**
     * Register a new user
     *
     * @param dto
     * @return the saved entity
     * @throws ResponseStatusException if email already exists
     */
    public User registerUser(SignupRequestDto dto) {
        // Check if email already exists
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email Already Exists");
        }

        // Create user object
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setUsername(dto.getUsername());
        user.setRole(Role.USER);

        return userRepository.save(user);
    }

    /**
     * issue JWT for a user - same as login
     *
     * @param dto
     * @return JWT token
     * @throws ResponseStatusException when user not found / password incorrect / user signed up with OAuth
     */
    public JwtResponseDto issueJWT(LoginRequestDto dto) {
        // Retrieve user info from database
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong Credentials"));

        // Check if user signed up as a standard auth user
        if (isOAuthUser(user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "OAuth Login Required");
        }

        // Check if password is correct
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong Credentials");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                dto.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority(user.getRole().getKey()))
        );

        // Create JWT token and return it
        return jwtProvider.generateToken(authentication);
    }

    /// //////////////////////////////////////////////////////////
    /// /////////////   OAuth Login  /////////////////////////////
    /// //////////////////////////////////////////////////////////

    /**
     * Register a new user with OAuth
     *
     * @param dto
     * @return the saved entity
     * @throws ResponseStatusException if email already exists / OAuth credentials mismatch
     */
    public User registerOauthUser(OauthSignupRequestDto dto) {
        // Check if email already exists
        User user = getUserByOauth(dto.getEmail(), dto.getOauthProvider(), dto.getOauthId());
        if (user != null) {
            // Update existing user to OAuth details
            user.setOauthProvider(dto.getOauthProvider());
            user.setOauthId(dto.getOauthId());
            user.setPassword(null);

        } else {
            // Create new user
            user = new User();
            user.setEmail(dto.getEmail());
            user.setUsername(dto.getUsername());
            user.setOauthProvider(dto.getOauthProvider());
            user.setOauthId(dto.getOauthId());
            user.setRole(Role.USER);
            user.setProfileImage(dto.getProfileImage());

        }
        return userRepository.save(user);
    }


    /**
     * Issue JWT for a user - same as OAuth login
     *
     * @param email    user's email
     * @param provider OAuth provider
     * @param oauthId  OAuth ID
     * @return JWT token
     * @throws ResponseStatusException when user not found, mismatched OAuth details, or user signed up with standard auth
     */
    public JwtResponseDto oauthLogin(String email, String provider, String oauthId) {
        // Find user by email
        User user = getUserByOauth(email, provider, oauthId);

        // Create authentication object
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority(user.getRole().getKey()))
        );

        // Generate JWT token
        return jwtProvider.generateToken(authentication);
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
     * @param user
     * @return true if user is an OAuth user
     */
    private boolean isOAuthUser(User user) {
        return user.getOauthProvider() != null && user.getOauthId() != null;
    }

    /**
     * Compare OAuth details
     * @param user
     * @param provider
     * @param oauthId
     * @return true if OAuth details match
     */
    private boolean compareOauthDetails(User user, String provider, String oauthId) {
        return user.getOauthProvider().equals(provider) && user.getOauthId().equals(oauthId);
    }
}