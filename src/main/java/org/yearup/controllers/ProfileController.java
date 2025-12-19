package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;
import org.yearup.models.User;

import java.security.Principal;
import java.sql.ResultSet;

@RestController
@RequestMapping("/profile")
@CrossOrigin
public class ProfileController
{
    private final JdbcTemplate jdbcTemplate;
    private final UserDao userDao;

    public ProfileController(JdbcTemplate jdbcTemplate, UserDao userDao)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.userDao = userDao;
    }

    // get profile
    @GetMapping
    public ResponseEntity<?> getProfile(Principal principal)
    {
        User user = getUserOrNull(principal);
        if (user == null) return unauthorized();

        Integer userId = getUserId(user);
        if (userId == null) return unauthorized();

        String sql = """
                SELECT user_id, first_name, last_name, phone, email, address, city, state, zip
                FROM profiles
                WHERE user_id = ?
                """;

        Profile profile = jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) return null;
            return mapProfile(rs);
        }, userId);

        if (profile == null)
        {
            profile = new Profile();
            setProfileUserId(profile, userId);
        }

        return ResponseEntity.ok(profile);
    }

    // update profile
    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody Profile profile, Principal principal)
    {
        User user = getUserOrNull(principal);
        if (user == null) return unauthorized();

        Integer userId = getUserId(user);
        if (userId == null) return unauthorized();

        String existsSql = """
                SELECT COUNT(*)
                FROM profiles
                WHERE user_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(existsSql, Integer.class, userId);
        boolean exists = count != null && count > 0;

        if (exists)
        {
            String updateSql = """
                    UPDATE profiles
                    SET first_name = ?,
                        last_name  = ?,
                        phone      = ?,
                        email      = ?,
                        address    = ?,
                        city       = ?,
                        state      = ?,
                        zip        = ?
                    WHERE user_id = ?
                    """;

            jdbcTemplate.update(updateSql,
                    profile.getFirstName(),
                    profile.getLastName(),
                    profile.getPhone(),
                    profile.getEmail(),
                    profile.getAddress(),
                    profile.getCity(),
                    profile.getState(),
                    profile.getZip(),
                    userId
            );
        }
        else
        {
            String insertSql = """
                    INSERT INTO profiles (user_id, first_name, last_name, phone, email, address, city, state, zip)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;

            jdbcTemplate.update(insertSql,
                    userId,
                    profile.getFirstName(),
                    profile.getLastName(),
                    profile.getPhone(),
                    profile.getEmail(),
                    profile.getAddress(),
                    profile.getCity(),
                    profile.getState(),
                    profile.getZip()
            );
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // helper
    private Profile mapProfile(ResultSet rs) throws java.sql.SQLException
    {
        Profile p = new Profile();
        setProfileUserId(p, rs.getInt("user_id"));
        p.setFirstName(rs.getString("first_name"));
        p.setLastName(rs.getString("last_name"));
        p.setPhone(rs.getString("phone"));
        p.setEmail(rs.getString("email"));
        p.setAddress(rs.getString("address"));
        p.setCity(rs.getString("city"));
        p.setState(rs.getString("state"));
        p.setZip(rs.getString("zip"));
        return p;
    }

    // helper
    private ResponseEntity<?> unauthorized()
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }

    // helper
    private User getUserOrNull(Principal principal)
    {
        if (principal == null) return null;

        String username = principal.getName();
        if (username == null || username.isBlank()) return null;

        try
        {
            return (User) userDao.getClass()
                    .getMethod("getByUsername", String.class)
                    .invoke(userDao, username);
        }
        catch (Exception ignored)
        {
        }

        try
        {
            return (User) userDao.getClass()
                    .getMethod("getByUserName", String.class)
                    .invoke(userDao, username);
        }
        catch (Exception ignored)
        {
        }

        return null;
    }

    // helper
    private Integer getUserId(User user)
    {
        try
        {
            Object id = user.getClass().getMethod("getUserId").invoke(user);
            if (id instanceof Number n) return n.intValue();
        }
        catch (Exception ignored)
        {
        }

        try
        {
            Object id = user.getClass().getMethod("getId").invoke(user);
            if (id instanceof Number n) return n.intValue();
        }
        catch (Exception ignored)
        {
        }

        return null;
    }

    // helper
    private void setProfileUserId(Profile profile, int userId)
    {
        try
        {
            profile.getClass().getMethod("setUserId", int.class).invoke(profile, userId);
        }
        catch (Exception ignored)
        {
        }
    }
}
