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

    // カテゴリ
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_category", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "category")
    private Set<String> categories = new HashSet<>();

    private String imagePath;
}