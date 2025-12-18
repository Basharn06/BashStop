package org.yearup.data.mysql;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MySqlShoppingCartDao implements ShoppingCartDao
{
    private final JdbcTemplate jdbcTemplate;
    private final ProductDao productDao;

    public MySqlShoppingCartDao(JdbcTemplate jdbcTemplate, ProductDao productDao)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.productDao = productDao;
    }

    // get cart
    @Override
    public ShoppingCart getByUserId(int userId)
    {
        String sql = """
                SELECT product_id, quantity
                FROM shopping_cart
                WHERE user_id = ?
                """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userId);

        Map<Integer, ShoppingCartItem> items = new HashMap<>();

        for (Map<String, Object> row : rows)
        {
            int productId = ((Number) row.get("product_id")).intValue();
            int quantity = ((Number) row.get("quantity")).intValue();

            Product product = productDao.getById(productId);

            ShoppingCartItem item = new ShoppingCartItem();
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setDiscountPercent(BigDecimal.ZERO);

            items.put(productId, item);
        }

        ShoppingCart cart = new ShoppingCart();
        cart.setItems(items);

        return cart;
    }

    // add product
    @Override
    public void addProduct(int userId, int productId)
    {
        String checkSql = """
                SELECT COUNT(*)
                FROM shopping_cart
                WHERE user_id = ? AND product_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, productId);

        if (count != null && count > 0)
        {
            String updateSql = """
                    UPDATE shopping_cart
                    SET quantity = quantity + 1
                    WHERE user_id = ? AND product_id = ?
                    """;

            jdbcTemplate.update(updateSql, userId, productId);
            return;
        }

        String insertSql = """
                INSERT INTO shopping_cart (user_id, product_id, quantity)
                VALUES (?, ?, 1)
                """;

        jdbcTemplate.update(insertSql, userId, productId);
    }

    // update quantity
    @Override
    public void updateQuantity(int userId, int productId, int quantity)
    {
        String checkSql = """
                SELECT COUNT(*)
                FROM shopping_cart
                WHERE user_id = ? AND product_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, productId);

        if (count == null || count == 0)
        {
            return;
        }

        String sql = """
                UPDATE shopping_cart
                SET quantity = ?
                WHERE user_id = ? AND product_id = ?
                """;

        jdbcTemplate.update(sql, quantity, userId, productId);
    }

    // clear cart
    @Override
    public void clearCart(int userId)
    {
        String sql = """
                DELETE FROM shopping_cart
                WHERE user_id = ?
                """;

        jdbcTemplate.update(sql, userId);
    }
}
