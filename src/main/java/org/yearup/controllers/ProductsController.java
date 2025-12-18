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

    @GetMapping("")
    public List<Product> getAll()
    {
        return productDao.getAll();
    }

    @GetMapping("/search")
    public List<Product> search(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String subCategory,
            @RequestParam(required = false) String name
    )
    {
        return productDao.search(categoryId, minPrice, maxPrice, subCategory, name);
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable int id)
    {
        Product product = productDao.getById(id);

        if (product == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return product;
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Product create(@RequestBody Product product)
    {
        return productDao.create(product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void update(@PathVariable int id, @RequestBody Product product)
    {
        productDao.update(id, product);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void delete(@PathVariable int id)
    {
        productDao.delete(id);
    }
}