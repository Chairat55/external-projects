package com.project.ssrapi.controllers;

import com.example.shinsiri.dtos.req.product.ReqCreateProductDTO;
import com.example.shinsiri.dtos.req.product.ReqSearchProductDTO;
import com.example.shinsiri.dtos.res.product.ResProductDTO;
import com.example.shinsiri.dtos.res.product.ResSearchProductDTO;
import com.example.shinsiri.entities.Product;
import com.example.shinsiri.repositories.ProductRepository;
import com.example.shinsiri.services.AuthenticationService;
import com.example.shinsiri.services.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private ProductService productService;


    @GetMapping("")
    public List<Product> getProducts() {
        logger.info("[GET] /products");
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResProductDTO getProductById(
            @PathVariable int id
    ) {
        logger.info("[GET] /products/{}", id);
        return productService.getProductById(id);
    }

    @PostMapping("/search")
    public ResSearchProductDTO searchProduct(
            @RequestBody ReqSearchProductDTO dto
    ) {
        logger.info("[POST] /products/search with dto: {}", dto);
        return productService.searchProduct(dto);
    }

    @PostMapping("")
    public Product createProduct(
            Authentication authentication,
            @Valid @RequestBody ReqCreateProductDTO dto
    ) {
        logger.info("[POST] /products with dto: {}", dto);
        authenticationService.checkAuthentication(authentication);
        return productService.createProduct(dto);
    }

    @PutMapping("/{id}")
    public Product updateProduct(
            Authentication authentication,
            @PathVariable int id,
            @Valid @RequestBody ReqCreateProductDTO dto
    ) {
        logger.info("[PUT] /products/{} with dto: {}", id, dto);
        authenticationService.checkAuthentication(authentication);
        return productService.updateProduct(id, dto);
    }

}
