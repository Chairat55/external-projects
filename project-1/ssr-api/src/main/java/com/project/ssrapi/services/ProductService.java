package com.project.ssrapi.services;

import com.example.shinsiri.dtos.req.product.ReqCreateProductDTO;
import com.example.shinsiri.dtos.req.product.ReqSearchProductDTO;
import com.example.shinsiri.dtos.res.product.ResProductDTO;
import com.example.shinsiri.dtos.res.product.ResSearchProductDTO;
import com.example.shinsiri.entities.Product;
import com.example.shinsiri.entities.ProductImage;
import com.example.shinsiri.exceptions.BadRequestException;
import com.example.shinsiri.repositories.ProductImageRepository;
import com.example.shinsiri.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    public static final List<String> PRODUCT_TYPES = Arrays.asList("EXOL", "MILLERS OILS", "Cataclean", "PureBrite", "X-BRITE", "D-Brite", "Ecripta");
    public static final List<String> SSR_TYPES = Arrays.asList("PureBrite", "X-BRITE", "D-Brite", "Ecripta");
    public static final List<String> LUBRICANTS_TYPES = Arrays.asList("EXOL", "MILLERS OILS", "Cataclean");

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EntityManager em;

    public ResProductDTO getProductById(int productId) {
        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty()) {
            throw new BadRequestException("productId: " + productId + " ไม่มีในระบบ");
        }

        ResProductDTO resDto = new ResProductDTO();
        modelMapper.map(product.get(), resDto);

        resDto.setProductImages(productImageRepository.findAllByProductId(productId));
        return resDto;
    }

    public ResSearchProductDTO searchProduct(ReqSearchProductDTO dto) {
        int pageNo = dto.getPageNo() == null ? 1 : dto.getPageNo();
        int pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();

        String sql = "SELECT * FROM products WHERE 1=1 ";
        String sqlCount = "SELECT COUNT(id) FROM products WHERE 1=1 ";
        String sqlCountAll = "SELECT COUNT(id) as totalAll, " +
                "(SELECT COUNT(id) FROM products WHERE stock > 0) as totalInStock, " +
                "(SELECT COUNT(id) FROM products WHERE stock = 0) as totalOutOfStock " +
                "FROM products ";

        if (dto.getType() != null && !dto.getType().equals("")) {
            sql += "    AND type = :type ";
            sqlCount += "    AND type = :type ";
        }

        if (dto.getName() != null && !dto.getName().equals("")) {
            sql += "    AND name LIKE :name ";
            sqlCount += "    AND name LIKE :name ";
        }

        Query query = em.createNativeQuery(sql, Product.class);
        Query queryCount = em.createNativeQuery(sqlCount);
        Query queryCountAll = em.createNativeQuery(sqlCountAll);

        if (dto.getType() != null && !dto.getType().equals("")) {
            query.setParameter("type", dto.getType());
            queryCount.setParameter("type", dto.getType());
        }

        if (dto.getName() != null && !dto.getName().equals("")) {
            query.setParameter("name", "%" + dto.getName() + "%");
            queryCount.setParameter("name", "%" + dto.getName() + "%");
        }

        query.setFirstResult((pageNo - 1) * pageSize);
        query.setMaxResults(pageSize);

        int totalItems = ((BigInteger) queryCount.getSingleResult()).intValue();

        Object[] resultAll = (Object[]) queryCountAll.getSingleResult();
        int totalAll = ((BigInteger) resultAll[0]).intValue();
        int totalInStock = ((BigInteger) resultAll[1]).intValue();
        int totalOutOfStock = ((BigInteger) resultAll[2]).intValue();

        List<Product> products = query.getResultList();
        List<ResProductDTO> resProductDTOS = new ArrayList<>();

        // หารูป Cover ไปแสดง
        if (products.size() > 0) {
            List<Integer> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
            List<ProductImage> productImages = productImageRepository.findAllLimit1ByProductIds(productIds);

            products.forEach(product -> {
                ResProductDTO resDTO = new ResProductDTO();
                modelMapper.map(product, resDTO);

                resDTO.setCoverImage(
                        productImages.stream()
                                .filter(productImage -> productImage.getProductId() == resDTO.getId())
                                .findFirst().orElse(null)
                );

                resProductDTOS.add(resDTO);
            });
        }

        ResSearchProductDTO resDto = new ResSearchProductDTO();
        resDto.setPageNo(pageNo);
        resDto.setPageSize(pageSize);
        resDto.setTotalPages((int) Math.ceil(totalItems / (pageSize + 0.0)));
        resDto.setTotalItems(totalItems);
        resDto.setTotalAll(totalAll);
        resDto.setTotalInStock(totalInStock);
        resDto.setTotalOutOfStock(totalOutOfStock);
        resDto.setItems(resProductDTOS);

        return resDto;
    }

    public Product createProduct(ReqCreateProductDTO dto) {
        checkProductType(dto.getType());
        checkProductDuplicate(dto.getName());

        Product product = new Product();
        modelMapper.map(dto, product);

        productRepository.saveAndFlush(product);
        return product;
    }

    public Product updateProduct(int productId, ReqCreateProductDTO dto) {
        checkProductType(dto.getType());

        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            if (!product.getName().equals(dto.getName())) {
                checkProductDuplicate(dto.getName());

                modelMapper.map(dto, product);

                productRepository.saveAndFlush(product);
            }
        } else {
            throw new BadRequestException("Product id: " + productId + " ไม่มีในระบบ");
        }
        return product;
    }

    public void checkProductDuplicate(String name) {
        Product product = productRepository.findOneByName(name);
        if (product != null) throw new BadRequestException("Product มีอยู่แล้วในระบบ");
    }

    private void checkProductType(String type) {
        if (!PRODUCT_TYPES.contains(type)) {
            throw new BadRequestException("Type ต้องเป็นอยู่ในรายการนี้เท่านั้น " + String.join(", ", PRODUCT_TYPES));
        }
    }

}
