package org.yearup.data;

import org.yearup.models.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductDao {

    // get all products
    List<Product> getAll();

    // get a product by id
    Product getById(int id);

    // search products (used by /products/search)
    List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String subCategory);

    // ✅ FIX: used by CategoriesController: /categories/{categoryId}/products
    // This is a SAFE default method that just reuses search().
    default List<Product> listByCategoryId(int categoryId) {
        return search(categoryId, null, null, null);
    }

    // create product
    Product create(Product product);

    // update product
    void update(int id, Product product);

    // delete product
    void delete(int id);
}
