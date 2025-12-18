package org.yearup.controllers;

import org.springframework.web.bind.annotation.*;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.User;

import java.security.Principal;

@RestController
@RequestMapping("/cart")
@CrossOrigin
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
    public ShoppingCart getCart(Principal principal)
    {
        User user = getUser(principal);
        return shoppingCartDao.getByUserId(user.getId());
    }

    // add product
    @PostMapping("/products/{productId}")
    public ShoppingCart addProduct(@PathVariable int productId, Principal principal)
    {
        User user = getUser(principal);
        shoppingCartDao.addProduct(user.getId(), productId);
        return shoppingCartDao.getByUserId(user.getId());
    }

    // update quantity
    @PutMapping("/products/{productId}")
    public ShoppingCart updateQuantity(
            @PathVariable int productId,
            @RequestBody ShoppingCartItemRequest body,
            Principal principal)
    {
        User user = getUser(principal);
        shoppingCartDao.updateQuantity(user.getId(), productId, body.getQuantity());
        return shoppingCartDao.getByUserId(user.getId());
    }

    // clear cart
    @DeleteMapping
    public ShoppingCart clearCart(Principal principal)
    {
        User user = getUser(principal);
        shoppingCartDao.clearCart(user.getId());
        return shoppingCartDao.getByUserId(user.getId());
    }

    // helper
    private User getUser(Principal principal)
    {
        String username = principal.getName();
        return userDao.getByUserName(username);
    }
}
