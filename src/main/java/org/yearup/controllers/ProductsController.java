package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import java.math.BigDecimal;
import java.util.List;

@RestController // REST endpoints
@RequestMapping("/products") // base route
@CrossOrigin // allow frontend calls
public class ProductsController {

    private final ProductDao productDao; // dao reference

    @Autowired // constructor injection
    public ProductsController(ProductDao productDao) {
        this.productDao = productDao; // assign dao
    }

    @GetMapping("") // list all products
    public List<Product> getAll() {
        return productDao.getAll(); // fetch all
    }

    @GetMapping("/search") // search products
    public List<Product> search(
            @RequestParam(required = false) Integer categoryId, // category filter
            @RequestParam(required = false) BigDecimal minPrice, // min filter
            @RequestParam(required = false) BigDecimal maxPrice, // max filter
            @RequestParam(required = false) String subCategory // genre filter
    ) {
        return productDao.search(categoryId, minPrice, maxPrice, subCategory); // run search
    }

    @GetMapping("/{id}") // get by id
    public Product getById(@PathVariable int id) {
        Product product = productDao.getById(id); // fetch one
        if (product == null) { // missing product
            throw new ResponseStatusException(HttpStatus.NOT_FOUND); // return 404
        }
        return product; // return product
    }

    @PostMapping("") // create product
    public Product create(@RequestBody Product product) {
        return productDao.create(product); // insert row
    }

    @PutMapping("/{id}") // update product
    public void update(@PathVariable int id, @RequestBody Product product) {
        productDao.update(id, product); // update row
    }

    @DeleteMapping("/{id}") // delete product
    public void delete(@PathVariable int id) {
        productDao.delete(id); // delete row
    }
}