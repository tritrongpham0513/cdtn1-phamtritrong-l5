package com.mekongmobile.inventory.repository;

import com.mekongmobile.inventory.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository tài khoản đăng nhập (mật khẩu lưu dạng BCrypt - Tiêu chí S3).
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);
}
