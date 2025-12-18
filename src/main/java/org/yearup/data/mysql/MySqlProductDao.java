package org.yearup.data.mysql;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlProductDao implements ProductDao
{
    private final JdbcTemplate jdbcTemplate;

    public MySqlProductDao(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    // get all
    @Override
    public List<Product> getAll()
    {
        String sql = """
                SELECT product_id, name, price, category_id, description, subcategory, stock, image_url, featured
                FROM products
                ORDER BY product_id
                """;

        return jdbcTemplate.query(sql, productRowMapper());
    }

    // search
    @Override
    public List<Product> search(Integer categoryId,
                                BigDecimal minPrice,
                                BigDecimal maxPrice,
                                String subCategory,
                                String name)
    {
        StringBuilder sql = new StringBuilder("""
                SELECT product_id, name, price, category_id, description, subcategory, stock, image_url, featured
                FROM products
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (categoryId != null)
        {
            sql.append(" AND category_id = ? ");
            params.add(categoryId);
        }

        if (minPrice != null)
        {
            sql.append(" AND price >= ? ");
            params.add(minPrice);
        }

        if (maxPrice != null)
        {
            sql.append(" AND price <= ? ");
            params.add(maxPrice);
        }

        if (subCategory != null && !subCategory.isBlank())
        {
            sql.append(" AND subcategory = ? ");
            params.add(subCategory);
        }

        if (name != null && !name.isBlank())
        {
            sql.append(" AND name LIKE ? ");
            params.add("%" + name + "%");
        }

        sql.append(" ORDER BY product_id ");

        return jdbcTemplate.query(sql.toString(), productRowMapper(), params.toArray());
    }

    // get by id
    @Override
    public Product getById(int id)
    {
        String sql = """
                SELECT product_id, name, price, category_id, description, subcategory, stock, image_url, featured
                FROM products
                WHERE product_id = ?
                """;

        return jdbcTemplate.queryForObject(sql, productRowMapper(), id);
    }

    // create
    @Override
    public Product create(Product product)
    {
        throw new UnsupportedOperationException();
    }

    // update
    @Override
    public void update(int id, Product product)
    {
        throw new UnsupportedOperationException();
    }

    // delete
    @Override
    public void delete(int id)
    {
        throw new UnsupportedOperationException();
    }

    // mapper
    private RowMapper<Product> productRowMapper()
    {
        return new RowMapper<Product>()
        {
            @Override
            public Product mapRow(ResultSet rs, int rowNum) throws SQLException
            {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setPrice(rs.getBigDecimal("price"));
                p.setCategoryId(rs.getInt("category_id"));
                p.setDescription(rs.getString("description"));
                p.setSubCategory(rs.getString("subcategory"));
                p.setStock(rs.getInt("stock"));
                p.setImageUrl(rs.getString("image_url"));
                p.setFeatured(rs.getBoolean("featured"));
                return p;
            }
        };
    }
}
