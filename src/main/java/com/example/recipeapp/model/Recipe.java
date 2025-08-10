package com.example.recipeapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "タイトルは必須です")
    private String title;

    @Column(length = 1000)
    private String ingredients;

    @Column(length = 2000)
    private String instructions;

    // お気に入り
    @Column(nullable = false)
    private boolean favorite = false;

    /*参考サイト*/
    @Column(length = 1000)
    private String reference;

    // カテゴリ - 修正点: cascade設定とfetch戦略の明示
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "recipe_category",
            joinColumns = @JoinColumn(name = "recipe_id")
    )
    @Column(name = "category", length = 50) // カラム長を明示的に設定
    private Set<String> categories = new HashSet<>();

    private String imagePath;

    // カテゴリ用のヘルパーメソッド
    public void addCategory(String category) {
        if (categories == null) {
            categories = new HashSet<>();
        }
        categories.add(category);
    }

    public void removeCategory(String category) {
        if (categories != null) {
            categories.remove(category);
        }
    }

    public void clearCategories() {
        if (categories != null) {
            categories.clear();
        }
    }
}