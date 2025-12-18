package org.yearup.data;

import org.yearup.models.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductDao
{
    List<Product> getAll();

    List<Product> search(
            Integer categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String subCategory,
            String name
    );

    Product getById(int id);

    Product create(Product product);

    void update(int id, Product product);

    void delete(int id);
}
