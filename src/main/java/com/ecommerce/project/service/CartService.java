package com.ecommerce.project.service;

import com.ecommerce.project.dto.cart.CartDTO;

public interface CartService {
    CartDTO addProductToCart(Long productId, Integer quantity);
}
