package com.example.recipeapp.repository;

import com.example.recipeapp.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByFavoriteTrue();

    // ⬇️ 安定して動作するカテゴリ検索（完全一致）
    @Query("SELECT r FROM Recipe r WHERE :category MEMBER OF r.categories")
    List<Recipe> findByCategory(@Param("category") String category);

}
