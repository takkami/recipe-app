package com.example.recipeapp.controller;

import com.example.recipeapp.model.Recipe;
import com.example.recipeapp.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.NoSuchElementException;

@Controller
public class RecipeController {

    @Autowired
    private RecipeRepository recipeRepository;

    @GetMapping("/home")
    public String showHome(Model model) {
        model.addAttribute("recipes", recipeRepository.findAll());
        model.addAttribute("favoritesPage", false);
        return "home";
    }

    // レシピ作成画面を表示
    @GetMapping("/recipes/new")
    public String showRecipeForm(Model model) {
        model.addAttribute("recipe", new Recipe());
        return "recipe_form";
    }

    // レシピを新規登録
    @PostMapping("/recipes/new")
    public String submitRecipe(@RequestParam String title,
                               @RequestParam String ingredients,
                               @RequestParam String instructions,
                               @RequestParam(name = "favorite", defaultValue = "false") boolean favorite,
                               @RequestParam(required = false) String reference,
                               @RequestParam(value = "categories", required = false) List<String> categories,
                               @RequestParam("image") MultipartFile imageFile) throws IOException {

        System.out.println("カテゴリ: " + categories);
        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        recipe.setIngredients(ingredients);
        recipe.setInstructions(instructions);

        // カテゴリの処理：nullチェックを追加
        if (categories != null && !categories.isEmpty()) {
            recipe.setCategories(new HashSet<>(categories));
        } else {
            recipe.setCategories(new HashSet<>());
        }

        recipe.setFavorite(favorite);
        recipe.setReference(reference);

        // 画像アップロード処理（プロジェクト直下の uploads ディレクトリへ）
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(fileName);
            imageFile.transferTo(filePath.toFile());

            recipe.setImagePath("/uploads/" + fileName); // HTMLで表示する相対パス
        }

        recipeRepository.save(recipe);
        return "redirect:/home?loading=true";
    }

    // 編集画面を表示
    @GetMapping("/recipes/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid recipe ID: " + id));
        model.addAttribute("recipe", recipe);
        return "recipe_form";
    }

    // 編集内容を保存
    @PostMapping("/recipes/update")
    public String updateRecipe(@RequestParam Long id,
                               @RequestParam String title,
                               @RequestParam String ingredients,
                               @RequestParam String instructions,
                               @RequestParam(name = "favorite", defaultValue = "false") boolean favorite,
                               @RequestParam(required = false) String reference,
                               @RequestParam(value = "categories", required = false) List<String> categories,
                               @RequestParam("image") MultipartFile imageFile) throws IOException {

        Recipe existingRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid recipe ID: " + id));

        existingRecipe.setTitle(title);
        existingRecipe.setIngredients(ingredients);
        existingRecipe.setInstructions(instructions);
        existingRecipe.setFavorite(favorite);
        existingRecipe.setReference(reference);

        // カテゴリの処理：nullチェックを追加
        if (categories != null && !categories.isEmpty()) {
            existingRecipe.setCategories(new HashSet<>(categories));
        } else {
            existingRecipe.setCategories(new HashSet<>());
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(fileName);
            imageFile.transferTo(filePath.toFile());

            existingRecipe.setImagePath("/uploads/" + fileName);
        }

        recipeRepository.save(existingRecipe);
        return "redirect:/home?loading=true";
    }

    // レシピ削除処理（フォームからのPOST - 既存のページ遷移用）
    @PostMapping("/recipes/{id}/delete")
    public String deleteRecipe(@PathVariable Long id,
                               @RequestParam(required = false) Boolean from,
                               @RequestParam(required = false) String category) {
        recipeRepository.deleteById(id);

        if (Boolean.TRUE.equals(from)) {
            return "redirect:/recipes/favorites";
        }

        if (category != null && !category.isEmpty()) {
            try {
                String encodedCategory = URLEncoder.encode(category, "UTF-8");
                return "redirect:/recipes/category/" + encodedCategory;
            } catch (UnsupportedEncodingException e) {
                return "redirect:/home";
            }
        }

        return "redirect:/home";
    }

    // レシピ削除処理（AJAX用 - その場削除）
    @DeleteMapping("/recipes/{id}/delete")
    @ResponseBody
    public ResponseEntity<Void> deleteRecipeAjax(@PathVariable Long id) {
        try {
            if (!recipeRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            recipeRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // お気に入りのみ表示
    @GetMapping("/recipes/favorites")
    public String showFavoriteRecipes(Model model) {
        model.addAttribute("recipes", recipeRepository.findByFavoriteTrue());
        model.addAttribute("favoritesPage", true);
        return "home";
    }

    // カテゴリ別表示
    @GetMapping("/recipes/category/{category}")
    public String showRecipesByCategory(@PathVariable String category, Model model) {
        List<Recipe> recipes = recipeRepository.findByCategory(category);
        model.addAttribute("recipes", recipes);
        model.addAttribute("categoryName", category);
        return "home";
    }

    // お気に入りトグル
    @PostMapping("/recipes/{id}/toggleFavorite")
    @ResponseBody
    public ResponseEntity<Boolean> toggleFavorite(@PathVariable Long id) {
        try {
            Recipe recipe = recipeRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("指定されたレシピが見つかりません ID: " + id));
            recipe.setFavorite(!recipe.isFavorite());
            recipeRepository.save(recipe);
            return ResponseEntity.ok(recipe.isFavorite());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}