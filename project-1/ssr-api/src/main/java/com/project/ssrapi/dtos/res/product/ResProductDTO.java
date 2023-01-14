package com.project.ssrapi.dtos.res.product;

import com.example.shinsiri.entities.Product;
import com.example.shinsiri.entities.ProductImage;
import lombok.Data;

import java.util.List;

@Data
public class ResProductDTO extends Product {

    ProductImage coverImage;
    List<ProductImage> productImages;

}
