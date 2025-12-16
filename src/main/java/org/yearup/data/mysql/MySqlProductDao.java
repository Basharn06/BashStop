package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlProductDao extends MySqlDaoBase implements ProductDao
{
    public MySqlProductDao(DataSource dataSource)
    {
        super(dataSource); // parent setup
    }

    @Override // must match
    public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice)
    {
        List<Product> products = new ArrayList<>(); // results list

        String sql =
                "SELECT product_id, name, price, category_id, description, subcategory, stock, image_url, featured " +
                        "FROM products " +
                        "WHERE (? = -1 OR category_id = ?) " +
                        "  AND (? = -1 OR price >= ?) " +
                        "  AND (? = -1 OR price <= ?)"; // filter query

        int cat = (categoryId == null) ? -1 : categoryId; // null default
        BigDecimal min = (minPrice == null) ? new BigDecimal("-1") : minPrice; // null default
        BigDecimal max = (maxPrice == null) ? new BigDecimal("-1") : maxPrice; // null default

        try (Connection connection = getConnection(); // db connect
             PreparedStatement statement = connection.prepareStatement(sql)) // prep stmt
        {
            statement.setInt(1, cat); // cat flag
            statement.setInt(2, cat); // cat val

            statement.setBigDecimal(3, min); // min flag
            statement.setBigDecimal(4, min); // min val

            statement.setBigDecimal(5, max); // max flag
            statement.setBigDecimal(6, max); // max val

            try (ResultSet row = statement.executeQuery()) // run query
            {
                while (row.next()) // loop rows
                {
                    products.add(mapRow(row)); // map row
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e); // bubble error
        }

        return products; // return list
    }

    @Override // by category
    public List<Product> listByCategoryId(int categoryId)
    {
        List<Product> products = new ArrayList<>(); // results list

        String sql =
                "SELECT product_id, name, price, category_id, description, subcategory, stock, image_url, featured " +
                        "FROM products " +
                        "WHERE category_id = ?"; // category filter

        try (Connection connection = getConnection(); // db connect
             PreparedStatement statement = connection.prepareStatement(sql)) // prep stmt
        {
            statement.setInt(1, categoryId); // set id

            try (ResultSet row = statement.executeQuery()) // run query
            {
                while (row.next()) // loop rows
                {
                    products.add(mapRow(row)); // map row
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e); // bubble error
        }

        return products; // return list
    }

    @Override // get one
    public Product getById(int productId)
    {
        String sql =
                "SELECT product_id, name, price, category_id, description, subcategory, stock, image_url, featured " +
                        "FROM products " +
                        "WHERE product_id = ?"; // id query

        try (Connection connection = getConnection(); // db connect
             PreparedStatement statement = connection.prepareStatement(sql)) // prep stmt
        {
            statement.setInt(1, productId); // set id

            try (ResultSet row = statement.executeQuery()) // run query
            {
                if (row.next()) // found row
                {
                    return mapRow(row); // map row
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e); // bubble error
        }

        return null; // not found
    }

    @Override // create row
    public Product create(Product product)
    {
        String sql =
                "INSERT INTO products (name, price, category_id, description, subcategory, stock, image_url, featured) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"; // insert sql

        try (Connection connection = getConnection(); // db connect
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) // gen keys
        {
            statement.setString(1, product.getName()); // set name
            statement.setBigDecimal(2, product.getPrice()); // set price
            statement.setInt(3, product.getCategoryId()); // set cat
            statement.setString(4, product.getDescription()); // set desc
            statement.setString(5, product.getSubCategory()); // set subcat
            statement.setInt(6, product.getStock()); // set stock
            statement.setString(7, product.getImageUrl()); // set image
            statement.setBoolean(8, product.isFeatured()); // set featured

            statement.executeUpdate(); // run insert

            try (ResultSet keys = statement.getGeneratedKeys()) // read keys
            {
                if (keys.next()) // has key
                {
                    int newId = keys.getInt(1); // new id
                    return getById(newId); // return saved
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e); // bubble error
        }

        return null; // insert failed
    }

    @Override // update row
    public void update(int productId, Product product)
    {
        String sql =
                "UPDATE products " +
                        "SET name = ?, price = ?, category_id = ?, description = ?, subcategory = ?, stock = ?, image_url = ?, featured = ? " +
                        "WHERE product_id = ?"; // update sql

        try (Connection connection = getConnection(); // db connect
             PreparedStatement statement = connection.prepareStatement(sql)) // prep stmt
        {
            statement.setString(1, product.getName()); // set name
            statement.setBigDecimal(2, product.getPrice()); // set price
            statement.setInt(3, product.getCategoryId()); // set cat
            statement.setString(4, product.getDescription()); // set desc
            statement.setString(5, product.getSubCategory()); // set subcat
            statement.setInt(6, product.getStock()); // set stock
            statement.setString(7, product.getImageUrl()); // set image
            statement.setBoolean(8, product.isFeatured()); // set featured
            statement.setInt(9, productId); // where id

            statement.executeUpdate(); // run update
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e); // bubble error
        }
    }

    @Override // delete row
    public void delete(int productId)
    {
        String sql = "DELETE FROM products WHERE product_id = ?"; // delete sql

        try (Connection connection = getConnection(); // db connect
             PreparedStatement statement = connection.prepareStatement(sql)) // prep stmt
        {
            statement.setInt(1, productId); // set id
            statement.executeUpdate(); // run delete
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e); // bubble error
        }
    }

    protected static Product mapRow(ResultSet row) throws SQLException
    {
        Product product = new Product(); // empty obj

        product.setProductId(row.getInt("product_id")); // set id
        product.setName(row.getString("name")); // set name
        product.setPrice(row.getBigDecimal("price")); // set price
        product.setCategoryId(row.getInt("category_id")); // set cat
        product.setDescription(row.getString("description")); // set desc
        product.setSubCategory(row.getString("subcategory")); // set subcat
        product.setStock(row.getInt("stock")); // set stock
        product.setImageUrl(row.getString("image_url")); // set image
        product.setFeatured(row.getBoolean("featured")); // set featured

        return product; // return obj
    }
}
