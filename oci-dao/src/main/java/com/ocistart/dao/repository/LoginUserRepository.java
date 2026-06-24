package com.ocistart.dao.repository;

import com.ocistart.dao.entity.LoginUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginUserRepository extends JpaRepository<LoginUser, Long> {
    Optional<LoginUser> findByUsername(String username);
    Optional<LoginUser> findByEmail(String email);
    boolean existsByUsername(String username);
}
