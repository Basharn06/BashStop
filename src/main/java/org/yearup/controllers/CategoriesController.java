package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private CategoryDao categoryDao;
    private ProductDao productDao;

    @Autowired // auto inject
    public CategoriesController(CategoryDao categoryDao, ProductDao productDao)
    {
        this.categoryDao = categoryDao; // save dao
        this.productDao = productDao;   // save dao
    }

    // add the appropriate annotation for a get action
    @GetMapping // get all
    public List<Category> getAll()
    {
        // find and return all categories
        return categoryDao.getAllCategories(); // dao fetch
    }

    // add the appropriate annotation for a get action
    @GetMapping("{id}") // get one
    public Category getById(@PathVariable int id)
    {
        // get the category by id
        Category category = categoryDao.getById(id); // dao fetch

        if(category == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND); // not found

        return category; // return cat
    }

    // the url to return all products in category 1 would look like this
    // https://localhost:8080/categories/1/products
    @GetMapping("{categoryId}/products") // cat products
    public List<Product> getProductsById(@PathVariable int categoryId)
    {
        // get a list of product by categoryId
        return productDao.listByCategoryId(categoryId); // dao fetch
    }

    // add annotation to call this method for a POST action
    // add annotation to ensure that only an ADMIN can call this function
    @PostMapping // create
    @PreAuthorize("hasRole('ROLE_ADMIN')") // admin only
    public Category addCategory(@RequestBody Category category)
    {
        // insert the category
        return categoryDao.create(category); // create cat
    }

    @PutMapping("{id}") // update
    @PreAuthorize("hasRole('ROLE_ADMIN')") // admin only
    public void updateCategory(@PathVariable int id, @RequestBody Category category)
    {
        categoryDao.update(id, category); // update cat
    }

    @DeleteMapping("{id}") // delete
    @PreAuthorize("hasRole('ROLE_ADMIN')") // admin only
    public void deleteCategory(@PathVariable int id)
    {
        categoryDao.delete(id); // delete cat
    }
}
