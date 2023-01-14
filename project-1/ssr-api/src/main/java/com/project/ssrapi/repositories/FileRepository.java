package com.project.ssrapi.repositories;

import com.example.shinsiri.entities.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Integer> {

    File findOneByName(String name);

}
