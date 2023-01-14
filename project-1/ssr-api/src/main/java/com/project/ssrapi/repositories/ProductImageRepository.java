package com.project.ssrapi.repositories;

import com.example.shinsiri.entities.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {

    @Query(value = "SELECT pi.* " +
            "FROM product_images pi " +
            "WHERE pi.id IN ( " +
            "   SELECT MIN(id) " +
            "   FROM product_images " +
            "   WHERE id IN :productIds " +
            "       AND type = 'COVER' " +
            "   GROUP BY id " +
            ")", nativeQuery = true)
    List<ProductImage> findAllLimit1ByProductIds(List<Integer> productIds);

    List<ProductImage> findAllByProductId(int productId);

    ProductImage findOneByProductIdAndType(int productId, String type);

}
