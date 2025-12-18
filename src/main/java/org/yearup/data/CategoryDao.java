package org.yearup.data;

import org.yearup.models.Category;

import java.util.List;

public interface CategoryDao
{
    List<Category> getAll();

    Category getById(int id);

    Category create(Category category);

    void update(int id, Category category);

    void delete(int id);
}
