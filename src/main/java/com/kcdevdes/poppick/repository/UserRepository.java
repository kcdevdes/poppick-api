package com.kcdevdes.poppick.repository;

import com.kcdevdes.poppick.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByOauthProviderAndOauthId(String oauthProvider, String oauthId);
}