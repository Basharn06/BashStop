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
        super(dataSource);
    }

    @Override
    public List<Product> getAll()
    {
        return search(null, null, null, null, null);
    }

    @Override
    public List<Product> search(Integer categoryId,
                                BigDecimal minPrice,
                                BigDecimal maxPrice,
                                String subCategory,
                                String name)
    {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet row = statement.executeQuery())
        {
            while (row.next())
            {
                Product p = mapRow(row);

                if (!matchesCategory(p, categoryId)) continue;
                if (!matchesMinPrice(p, minPrice)) continue;
                if (!matchesMaxPrice(p, maxPrice)) continue;
                if (!matchesSubCategory(p, subCategory)) continue;
                if (!matchesName(p, name)) continue;

                products.add(p);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return products;
    }

    @Override
    public Product getById(int id)
    {
        String sql = "SELECT * FROM products WHERE product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, id);

            try (ResultSet row = statement.executeQuery())
            {
                if (row.next()) return mapRow(row);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public Product create(Product product)
    {
        String sql =
                "INSERT INTO products (name, price, category_id, description, image_url) " +
                        "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, safeGetImageUrl(product));

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys())
            {
                if (keys.next())
                {
                    int newId = keys.getInt(1);
                    return getById(newId);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public void update(int id, Product product)
    {
        String sql =
                "UPDATE products " +
                        "SET name = ?, price = ?, category_id = ?, description = ?, image_url = ? " +
                        "WHERE product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, safeGetImageUrl(product));
            statement.setInt(6, id);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id)
    {
        String sql = "DELETE FROM products WHERE product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Product mapRow(ResultSet row) throws SQLException
    {
        Product product = new Product();

        product.setProductId(getIntIfExists(row, "product_id"));
        product.setName(getStringIfExists(row, "name"));
        product.setPrice(getBigDecimalIfExists(row, "price"));
        product.setCategoryId(getIntIfExists(row, "category_id"));
        product.setDescription(getStringIfExists(row, "description"));

        if (hasColumn(row, "image_url"))
        {
            product.setImageUrl(row.getString("image_url"));
        }

        if (hasColumn(row, "subcategory"))
        {
            product.setSubCategory(row.getString("subcategory"));
        }
        else if (hasColumn(row, "sub_category"))
        {
            product.setSubCategory(row.getString("sub_category"));
        }
        else if (hasColumn(row, "genre"))
        {
            product.setSubCategory(row.getString("genre"));
        }

        return product;
    }

    private boolean matchesCategory(Product p, Integer categoryId)
    {
        if (categoryId == null) return true;
        return p.getCategoryId() == categoryId;
    }

    private boolean matchesMinPrice(Product p, BigDecimal minPrice)
    {
        if (minPrice == null) return true;
        if (p.getPrice() == null) return false;
        return p.getPrice().compareTo(minPrice) >= 0;
    }

    private boolean matchesMaxPrice(Product p, BigDecimal maxPrice)
    {
        if (maxPrice == null) return true;
        if (p.getPrice() == null) return false;
        return p.getPrice().compareTo(maxPrice) <= 0;
    }

    private boolean matchesSubCategory(Product p, String subCategory)
    {
        if (subCategory == null || subCategory.isBlank()) return true;
        if (subCategory.equalsIgnoreCase("Show All")) return true;

        String actual = safeGetSubCategory(p);
        return actual.toLowerCase().contains(subCategory.trim().toLowerCase());
    }

    private boolean matchesName(Product p, String name)
    {
        if (name == null || name.isBlank()) return true;

        String actual = (p.getName() == null) ? "" : p.getName();
        return actual.toLowerCase().contains(name.trim().toLowerCase());
    }

    private String safeGetSubCategory(Product p)
    {
        try
        {
            String sc = p.getSubCategory();
            return (sc == null) ? "" : sc;
        }
        catch (Exception e)
        {
            return "";
        }
    }

    private String safeGetImageUrl(Product p)
    {
        try
        {
            String url = p.getImageUrl();
            return (url == null) ? "" : url;
        }
        catch (Exception e)
        {
            return "";
        }
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException
    {
        ResultSetMetaData meta = rs.getMetaData();
        int count = meta.getColumnCount();

        for (int i = 1; i <= count; i++)
        {
            String label = meta.getColumnLabel(i);
            if (label != null && label.equalsIgnoreCase(columnName)) return true;

            String name = meta.getColumnName(i);
            if (name != null && name.equalsIgnoreCase(columnName)) return true;
        }

        return false;
    }

    private int getIntIfExists(ResultSet rs, String column) throws SQLException
    {
        if (!hasColumn(rs, column)) return 0;
        return rs.getInt(column);
    }

    private String getStringIfExists(ResultSet rs, String column) throws SQLException
    {
        if (!hasColumn(rs, column)) return null;
        return rs.getString(column);
    }

    private BigDecimal getBigDecimalIfExists(ResultSet rs, String column) throws SQLException
    {
        if (!hasColumn(rs, column)) return null;
        return rs.getBigDecimal(column);
    }
}