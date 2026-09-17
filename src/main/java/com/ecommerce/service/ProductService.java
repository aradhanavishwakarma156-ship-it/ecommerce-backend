package com.ecommerce.service;


import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;




@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;

    public ProductService(
            ProductRepository productRepository,
            CloudinaryService cloudinaryService
    ) {
        this.productRepository = productRepository;
        this.cloudinaryService = cloudinaryService;
    }


    public Product createProduct(
            String name,
            String description,
            BigDecimal price,
            Integer stock,
            List<MultipartFile> images
    ) {

        validateProductDetails(
                name,
                price,
                stock
        );

        validateImages(
                images,
                true
        );


        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile image : images) {

            String imageUrl =
                    cloudinaryService.uploadImage(image);

            imageUrls.add(imageUrl);
        }


        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .imageUrls(imageUrls)
                .build();


        return productRepository.save(product);
    }
    public Page<Product> getAllProducts(
            String keyword,
            int page,
            int size
    ) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }

        if (size <= 0) {
            throw new IllegalArgumentException(
                    "Page size must be greater than 0"
            );
        }

        Pageable pageable =
                PageRequest.of(page, size);


        if (keyword == null || keyword.isBlank()) {

            return productRepository.findAll(pageable);
        }


        return productRepository
                .findByNameContainingIgnoreCase(
                        keyword.trim(),
                        pageable
                );
    }
    public Product getProductById(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found")
                );
    }


    public Product updateProduct(
            Long id,
            String name,
            String description,
            BigDecimal price,
            Integer stock,
            List<MultipartFile> images
    ) {

        validateProductDetails(
                name,
                price,
                stock
        );

        validateImages(
                images,
                false
        );


        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found"
                        )
                );


        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);


        if (images != null && !images.isEmpty()) {

            List<String> imageUrls =
                    new ArrayList<>();

            for (MultipartFile image : images) {

                String imageUrl =
                        cloudinaryService.uploadImage(image);

                imageUrls.add(imageUrl);
            }

            product.setImageUrls(imageUrls);
        }


        return productRepository.save(product);
    }
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found")
                );

        productRepository.delete(product);
    }




    private void validateProductDetails(
            String name,
            BigDecimal price,
            Integer stock
    ) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Product name is required"
            );
        }

        if (price == null ||
                price.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Product price must be greater than 0"
            );
        }

        if (stock == null || stock < 0) {

            throw new IllegalArgumentException(
                    "Product stock cannot be negative"
            );
        }
    }


    private void validateImages(
            List<MultipartFile> images,
            boolean required
    ) {

        if (required &&
                (images == null || images.isEmpty())) {

            throw new IllegalArgumentException(
                    "At least one product image is required"
            );
        }


        if (images != null) {

            for (MultipartFile image : images) {

                if (image.isEmpty()) {

                    throw new IllegalArgumentException(
                            "Image file cannot be empty"
                    );
                }


                String fileName = image.getOriginalFilename();

                if (fileName == null) {

                    throw new IllegalArgumentException(
                            "Invalid image file"
                    );
                }


                String lowerFileName =
                        fileName.toLowerCase();


                if (!(lowerFileName.endsWith(".jpg") ||
                        lowerFileName.endsWith(".jpeg") ||
                        lowerFileName.endsWith(".png") ||
                        lowerFileName.endsWith(".webp"))) {

                    throw new IllegalArgumentException(
                            "Only JPG, JPEG, PNG and WEBP images are allowed"
                    );
                }
            }
        }
    }

}