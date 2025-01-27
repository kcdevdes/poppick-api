package com.kcdevdes.poppick.config;

import com.kcdevdes.poppick.util.JwtAuthenticationFilter;
import com.kcdevdes.poppick.util.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/v1/users/signup", "/v1/users/login").permitAll()
            .requestMatchers("/v1/users/oauth/google/redirect", "/v1/users/oauth/google/fail").permitAll()
            .requestMatchers("/v1/users/me").authenticated()
            .anyRequest().authenticated()
        )
        .oauth2Login(oauth2 -> oauth2
            .successHandler((request, response, authentication) -> {
                // Redirect to success URL
                response.sendRedirect("/v1/users/oauth/google/success");
            })
            .failureHandler((request, response, exception) -> {
                // Redirect to failure URL
                response.sendRedirect("/v1/users/oauth/google/fail");
            })
            .redirectionEndpoint(redirection -> redirection
                .baseUri("/v1/users/oauth/google/redirect")
            )
            .userInfoEndpoint(userInfo -> userInfo
                .userAuthoritiesMapper(grantedAuthoritiesMapper())
            )
        )
        .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
    }

    private GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                    mappedAuthorities.add(new OidcUserAuthority(
                        "ROLE_OIDC_USER", oidcUserAuthority.getIdToken(), oidcUserAuthority.getUserInfo()
                    ));
                } else if (authority instanceof OAuth2UserAuthority oauth2UserAuthority) {
                    mappedAuthorities.add(new OAuth2UserAuthority(
                        "ROLE_OAUTH2_USER", oauth2UserAuthority.getAttributes()
                    ));
                } else {
                    mappedAuthorities.add(authority);
                }
            });

            return mappedAuthorities;
        };
    }
}