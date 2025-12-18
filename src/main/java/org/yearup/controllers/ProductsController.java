package org.yearup.controllers;

import org.springframework.web.bind.annotation.*;
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

    // get products
    @GetMapping
    public List<Product> getAll(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer cat,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String subCategory,
            @RequestParam(required = false) String name
    )
    {
        Integer resolvedCategoryId = categoryId;

        if (resolvedCategoryId == null && cat != null)
        {
            resolvedCategoryId = cat;
        }

        if (resolvedCategoryId != null && resolvedCategoryId == 0)
        {
            resolvedCategoryId = null;
        }

        if (subCategory != null && subCategory.isBlank())
        {
            subCategory = null;
        }

        if (name != null && name.isBlank())
        {
            name = null;
        }

        return productDao.search(
                resolvedCategoryId,
                minPrice,
                maxPrice,
                subCategory,
                name
        );
    }

    // get by id
    @GetMapping("/{id}")
    public Product getById(@PathVariable int id)
    {
        return productDao.getById(id);
    }
}
