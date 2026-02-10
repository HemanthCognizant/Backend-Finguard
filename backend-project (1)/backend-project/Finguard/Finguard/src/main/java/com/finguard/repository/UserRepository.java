package com.finguard.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.finguard.entity.User;
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String username);
}