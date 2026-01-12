package com.secj3303.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.secj3303.model.User;

public interface UserRepository extends JpaRepository<User, String> {
}