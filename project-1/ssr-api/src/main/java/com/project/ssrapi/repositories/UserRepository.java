package com.project.ssrapi.repositories;

import com.example.shinsiri.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findOneByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.id IN :userIds")
    List<User> findAllByUserIds(List<Integer> userIds);

}
