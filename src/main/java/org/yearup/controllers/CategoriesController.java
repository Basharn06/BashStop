package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.CategoryDao;
import org.yearup.data.ProductDao;
import org.yearup.models.Category;
import org.yearup.models.Product;

import java.util.List;

@RestController
@RequestMapping("/categories")
@CrossOrigin
public class CategoriesController
{
    private final CategoryDao categoryDao;
    private final ProductDao productDao;

    @Autowired
    public CategoriesController(CategoryDao categoryDao, ProductDao productDao)
    {
        this.categoryDao = categoryDao;
        this.productDao = productDao;
    }

    // GET /categories
    @GetMapping("")
    public List<Category> getAll()
    {
        return categoryDao.getAll();
    }

    // GET /categories/{id}
    @GetMapping("/{id}")
    public Category getById(@PathVariable int id)
    {
        Category category = categoryDao.getById(id);

        if (category == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return category;
    }

    // get products by category id
    @GetMapping("{categoryId}/products")
    public List<Product> getProductsByCategoryId(@PathVariable int categoryId)
    {
        return productDao.listByCategoryId(categoryId);
    }

    // create category (admin only)
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Category addCategory(@RequestBody Category category)
    {
        return categoryDao.create(category);
    }

    // update category (admin only)
    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateCategory(@PathVariable int id, @RequestBody Category category)
    {
        categoryDao.update(id, category);
    }

    // delete category (admin only)
    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteCategory(@PathVariable int id)
    {
        categoryDao.delete(id);
    }
}
