package com.kcdevdes.poppick.service;

import com.kcdevdes.poppick.dto.request.LoginRequestDto;
import com.kcdevdes.poppick.dto.request.OauthSignupRequestDto;
import com.kcdevdes.poppick.dto.request.SignupRequestDto;
import com.kcdevdes.poppick.dto.response.JwtResponseDto;
import com.kcdevdes.poppick.entity.Role;
import com.kcdevdes.poppick.entity.User;
import com.kcdevdes.poppick.repository.UserRepository;
import com.kcdevdes.poppick.common.provider.JwtProvider;
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
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(
            UserService userService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider) {
        this.userService = userService;
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
        return jwtProvider.generateToken(authentication, false);
    }

    /**
     * Validate and refresh access token
     *
     * @param refreshToken Refresh token from request
     * @return New JWT response with access token
     */
    public JwtResponseDto refreshToken(String refreshToken) {
        // Validate refresh token
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token");
        }

        // Extract user information from refresh token
        String email = jwtProvider.getEmailFromToken(refreshToken);

        // Find user in the database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User Not Found"));

        // Generate a new access token
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null, List.of(new SimpleGrantedAuthority(user.getRole().getKey()))
        );
        return jwtProvider.generateToken(authentication, true);
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
        User user = userService.getUserByOauth(dto.getEmail(), dto.getOauthProvider(), dto.getOauthId());
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
        User user = userService.getUserByOauth(email, provider, oauthId);

        // Create authentication object
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority(user.getRole().getKey()))
        );

        // Generate JWT token
        return jwtProvider.generateToken(authentication, false);
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
}
