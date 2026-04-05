package com.finance.dashboard.repository;

import com.finance.dashboard.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findAllByStatus(com.finance.dashboard.model.UserStatus status);
}
