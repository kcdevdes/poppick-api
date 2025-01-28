package com.kcdevdes.poppick.config;

import com.kcdevdes.poppick.util.JwtAuthenticationFilter;
import com.kcdevdes.poppick.util.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    public SecurityConfig(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider);
    }

    /**
     * Main Security Configuration
     * Defines authorization rules, OAuth2 settings, and integrates the JWT filter.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless APIs
                .csrf(AbstractHttpConfigurer::disable)

                // Define authorization rules
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints
                        .requestMatchers("/v1/auth/signup", "/v1/auth/login").permitAll()
                        .requestMatchers("/v1/oauth/google/redirect", "/v1/oauth/google/failure").permitAll()
                        // Protected endpoint
                        .requestMatchers("/v1/users/me").authenticated()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

                // Configure OAuth2 Login
                .oauth2Login(oauth2 -> oauth2
                        .successHandler((request, response, authentication) -> {
                            // Handle successful OAuth2 login
                            response.sendRedirect("/v1/auth/google/success");
                        })
                        .failureHandler((request, response, exception) -> {
                            // Handle OAuth2 login failure
                            response.sendRedirect("/v1/auth/google/failure");
                        })
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/v1/auth/google/redirect")
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userAuthoritiesMapper(grantedAuthoritiesMapper())
                        )
                );

        return http.build();
    }

    /**
     * Custom mapper for granted authorities.
     * Maps OIDC and OAuth2 authorities to custom roles.
     */
    private GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                    // Map OIDC user authorities to a custom role
                    mappedAuthorities.add(new OidcUserAuthority(
                            "ROLE_OIDC_USER", oidcUserAuthority.getIdToken(), oidcUserAuthority.getUserInfo()
                    ));
                } else if (authority instanceof OAuth2UserAuthority oauth2UserAuthority) {
                    // Map OAuth2 user authorities to a custom role
                    mappedAuthorities.add(new OAuth2UserAuthority(
                            "ROLE_OAUTH2_USER", oauth2UserAuthority.getAttributes()
                    ));
                } else {
                    // Preserve default authorities
                    mappedAuthorities.add(authority);
                }
            });

            return mappedAuthorities;
        };
    }
}
