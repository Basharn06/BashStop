package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
@CrossOrigin
public class ProductsController
{
    private final ProductDao productDao;

    public ProductsController(ProductDao productDao)
    {
        this.productDao = productDao;
    }

    // GET http://localhost:8080/products
    @GetMapping("")
    @PreAuthorize("permitAll()")

    public List<Product> getAll()
    {
        // ProductDao doesn't have getAll(), so we use search with all nulls (returns everything)
        return productDao.search(null, null, null);
    }

    // GET http://localhost:8080/products/search?cat=1&minPrice=10&maxPrice=100
    @GetMapping("/search")
    @PreAuthorize("permitAll()")
    public List<Product> search(
            @RequestParam(name = "cat", required = false) Integer categoryId,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice
    )
    {
        // ProductDao.search expects 3 arguments (NOT 4)
        return productDao.search(categoryId, minPrice, maxPrice);
    }

    // GET http://localhost:8080/products/1
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public Product getById(@PathVariable int id)
    {
        Product product = productDao.getById(id);

        if (product == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return product;
    }

    // POST http://localhost:8080/products  (admin only)
    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Product create(@RequestBody Product product)
    {
        return productDao.create(product);
    }

    // PUT http://localhost:8080/products/1  (admin only)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void update(@PathVariable int id, @RequestBody Product product)
    {
        productDao.update(id, product);
    }

    // DELETE http://localhost:8080/products/1  (admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void delete(@PathVariable int id)
    {
        productDao.delete(id);
    }
}
