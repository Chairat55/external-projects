package com.project.ssrapi.repositories;

import com.example.shinsiri.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Integer> {

    Tag findOneByName(String name);

    @Query(value = "SELECT t.* " +
            "FROM tags t " +
            "JOIN customer_tags ct ON t.id = ct.tag_id " +
            "WHERE ct.customer_id = :customerId ", nativeQuery = true)
    List<Tag> findAllByCustomerId(int customerId);

}
