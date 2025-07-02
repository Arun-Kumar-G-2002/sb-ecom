package com.ecommerce.project.repositories.cart;

import com.ecommerce.project.model.cart.CartItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CartItemsRepository extends JpaRepository<CartItems, Long> {
    @Query("SELECT ci FROM CartItems ci WHERE ci.product.id = ?1 AND ci.cart.id = ?2")
    CartItems findCartItemByProductIdAndCartId(Long productId, Long cartId);
}
