package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.User;

import java.security.Principal;

@RestController
@CrossOrigin
@RequestMapping("/cart")
public class ShoppingCartController
{
    private final ShoppingCartDao shoppingCartDao;
    private final UserDao userDao;

    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao)
    {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
    }

    // get cart
    @GetMapping
    public ResponseEntity<?> getCart(Principal principal)
    {
        User user = getUserOrNull(principal);
        if (user == null) return unauthorized();

        ShoppingCart cart = shoppingCartDao.getByUserId(user.getId());
        return ResponseEntity.ok(cart);
    }

    // add product
    @PostMapping("/products/{productId}")
    public ResponseEntity<?> addProduct(@PathVariable int productId, Principal principal)
    {
        User user = getUserOrNull(principal);
        if (user == null) return unauthorized();

        shoppingCartDao.addProduct(user.getId(), productId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // update qty
    @PutMapping("/products/{productId}")
    public ResponseEntity<?> updateQuantity(@PathVariable int productId,
                                            @RequestBody QuantityRequest body,
                                            Principal principal)
    {
        User user = getUserOrNull(principal);
        if (user == null) return unauthorized();

        shoppingCartDao.updateQuantity(user.getId(), productId, body.getQuantity());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // clear cart
    @DeleteMapping
    public ResponseEntity<?> clearCart(Principal principal)
    {
        User user = getUserOrNull(principal);
        if (user == null) return unauthorized();

        shoppingCartDao.clearCart(user.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // helper
    private User getUserOrNull(Principal principal)
    {
        if (principal == null) return null;

        String username = principal.getName();
        if (username == null || username.isBlank()) return null;

        return userDao.getByUserName(username);
    }

    // helper
    private ResponseEntity<?> unauthorized()
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }

    // request body
    public static class QuantityRequest
    {
        private int quantity;

        public int getQuantity()
        {
            return quantity;
        }

        public void setQuantity(int quantity)
        {
            this.quantity = quantity;
        }
    }
}
