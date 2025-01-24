package com.kcdevdes.poppick.util;

import com.kcdevdes.poppick.dto.JwtResponseDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;


@Component
public class JwtProvider {

    private final Key key;
    private static final long ACCESS_TOKEN_EXPIRATION = 86400000L; // 1일
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7일
    private final PasswordEncoder passwordEncoder;

    public JwtProvider(JwtProperties jwtProperties, PasswordEncoder passwordEncoder) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.passwordEncoder = passwordEncoder;
    }

    // Generate a new token
    public JwtResponseDto generateToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")); // 권한을 쉼표로 구분하여 저장

        String accessToken = createToken(authentication.getName(), authorities, ACCESS_TOKEN_EXPIRATION);
        String refreshToken = createToken(null, null, REFRESH_TOKEN_EXPIRATION);

        return JwtResponseDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    // Validate the token
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

    // Create an authentication object with the access and refresh tokens
    private String createToken(String subject, String authorities, long expirationTime) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .claim("auth", authorities)
                .setExpiration(new Date(now + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Retrieve the authentication object from the token
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse token: " + e.getMessage());
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        // 권한 파싱
        String authorities = claims.get("auth", String.class);
        if (authorities == null || authorities.isEmpty()) {
            throw new RuntimeException("No authority information in the token.");
        }

        Collection<GrantedAuthority> grantedAuthorities = Arrays.stream(authorities.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 사용자 정보 설정
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                claims.getSubject(), "", grantedAuthorities);
        return new UsernamePasswordAuthenticationToken(userDetails, "", grantedAuthorities);
    }

    public String getEmailFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }
}