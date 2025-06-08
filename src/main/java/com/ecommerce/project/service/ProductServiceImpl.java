package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private ModelMapper modelMapper;

    @Value("${project.image}")
    private String path;

    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {

        if (categoryId == null) {
            throw new ResourceNotFoundException("Category", "categoryId", categoryId);
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId));

        Product productFromDb = productRepository.findByCategoryAndProductName(category, productDTO.getProductName());
        if (productFromDb != null) {
            throw new APIException("Product with name " + productDTO.getProductName() + " already exists.");
        }

        Product product = modelMapper.map(productDTO, Product.class);

        product.setImage("default.png");
        product.setCategory(category);
        double specialPrice = product.getPrice() -
                ((product.getDiscount() / 100) * product.getPrice());
        product.setSpecialPrice(specialPrice);

        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductResponse getAllProducts(int pageNumber, int pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        if (pageNumber < 0 || pageSize <= 0) {
            throw new APIException("Page number cannot be negative and size must be greater than zero.");
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findAll(pageable);

        List<Product> products = productPage.getContent();
        if (products.isEmpty()) {
            throw new ResourceNotFoundException("Product", "pagination", "No products found for the given page and size");
        }

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLast(productPage.isLast());

        return productResponse;
    }

    @Override
    public ProductResponse getProductByCategory(Long categoryId, int pageNumber, int pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        if (categoryId == null) {
            throw new ResourceNotFoundException("Category", "categoryId", categoryId);
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId));

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageDetails = productRepository.findByCategoryOrderByPriceAsc(category, pageable);

        List<Product> products = pageDetails.getContent();
        if (products.isEmpty()) {
            throw new ResourceNotFoundException("Product", "categoryId", categoryId);
        }

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageDetails.getNumber());
        productResponse.setPageSize(pageDetails.getSize());
        productResponse.setTotalElements(pageDetails.getTotalElements());
        productResponse.setTotalPages(pageDetails.getTotalPages());
        productResponse.setLast(pageDetails.isLast());

        return productResponse;
    }

    @Override
    public ProductResponse getProductsByKeyword(String keyword, int pageNumber, int pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        if (keyword == null || keyword.isEmpty()) {
            throw new ResourceNotFoundException("Product", "keyword", keyword);
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageDetails = productRepository.findByProductNameLikeIgnoreCase("%" + keyword + "%", pageable);

        List<Product> products = pageDetails.getContent();
        if (products == null || products.isEmpty()) {
            throw new ResourceNotFoundException("Product", "keyword", keyword);
        }

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageDetails.getNumber());
        productResponse.setPageSize(pageDetails.getSize());
        productResponse.setTotalElements(pageDetails.getTotalElements());
        productResponse.setTotalPages(pageDetails.getTotalPages());
        productResponse.setLast(pageDetails.isLast());
        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDTO, Long productId) {
        if (productId == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        Product product = modelMapper.map(productDTO, Product.class);
        existingProduct.setProductName(product.getProductName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setDiscount(product.getDiscount());
        double specialPrice = product.getPrice() - ((product.getDiscount() / 100) * product.getPrice());
        existingProduct.setSpecialPrice(specialPrice);

        Product updatedProduct = productRepository.save(existingProduct);
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        if (productId == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        productRepository.delete(product);
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateImage(Long productId, MultipartFile image) throws IOException {
        if (productId == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        String imageName = fileService.uploadImage(path, image);

        if (imageName == null) {
            throw new RuntimeException("Failed to upload image");
        }

        product.setImage(imageName);
        Product updatedProduct = productRepository.save(product);
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

}
