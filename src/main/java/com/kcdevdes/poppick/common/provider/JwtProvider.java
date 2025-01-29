package com.kcdevdes.poppick.common.provider;

import com.kcdevdes.poppick.dto.response.JwtResponseDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;


import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

    private final Key key;
    private static final long ACCESS_TOKEN_EXPIRATION = 3600000L; // 1 hour
    private static final long REFRESH_TOKEN_EXPIRATION = 30L * 24 * 60 * 60 * 1000; // 30 days

    public JwtProvider(JwtProperties jwtProperties, PasswordEncoder passwordEncoder) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a new access token and refresh token
     *
     * @param authentication
     * @param isRefresh
     * @return JwtResponseDto object
     */
    public JwtResponseDto generateToken(Authentication authentication, boolean isRefresh) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        String accessToken = createToken(authentication.getName(), authorities, ACCESS_TOKEN_EXPIRATION);
        String refreshToken = createToken(authentication.getName(), authorities, REFRESH_TOKEN_EXPIRATION);

        if (isRefresh) {
            return JwtResponseDto.builder()
                    .grantType("Bearer")
                    .accessToken(accessToken)
                    .build();
        }

        return JwtResponseDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Validate the token
     *
     * @param token
     * @return true if valid, false if invalid
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Create a new token
     *
     * @param subject
     * @param authorities
     * @param expirationTime
     * @return token as a string
     */
    private String createToken(String subject, String authorities, long expirationTime) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .claim("auth", authorities)
                .setExpiration(new Date(now + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Parse the claims from a token
     *
     * @param token
     * @return Claims object
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Parse Error");
        }
    }

    /**
     * Get the authentication object from a token
     *
     * @param token
     * @return Authentication object
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        String authorities = claims.get("auth", String.class);
        if (authorities == null || authorities.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization Required");
        }

        Collection<GrantedAuthority> grantedAuthorities = Arrays.stream(authorities.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                claims.getSubject(), "", grantedAuthorities);
        return new UsernamePasswordAuthenticationToken(userDetails, "", grantedAuthorities);
    }

    /**
     * Get the email from a token
     *
     * @param token
     * @return email as a string
     */
    public String getEmailFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }
}

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
class JwtProperties {
    private String secret;
}