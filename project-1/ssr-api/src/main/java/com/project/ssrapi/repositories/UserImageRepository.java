package com.project.ssrapi.repositories;

import com.example.shinsiri.entities.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserImageRepository extends JpaRepository<UserImage, Integer> {

    List<UserImage> findAllByUserId(int userId);

    @Query("SELECT ui FROM UserImage ui WHERE ui.type = 'PROFILE' AND ui.userId IN :userIds ")
    List<UserImage> findAllProfileByUserIds(List<Integer> userIds);

    UserImage findOneByUserIdAndType(int userId, String type);

}
