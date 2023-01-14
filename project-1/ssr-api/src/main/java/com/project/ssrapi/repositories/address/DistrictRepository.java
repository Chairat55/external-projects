package com.project.ssrapi.repositories.address;

import com.example.shinsiri.entities.address.District;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DistrictRepository extends JpaRepository<District, Integer> {
    List<District> findByProvinceId(int provinceId);
}
