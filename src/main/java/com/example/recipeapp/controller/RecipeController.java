package com.example.recipeapp.controller;

import com.example.recipeapp.model.Recipe;
import com.example.recipeapp.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

    private static final int MAX_CATEGORIES = 3;

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

    // カテゴリ数をバリデーションするヘルパーメソッド（強化版）
    private ValidationResult validateCategories(List<String> categories) {
        ValidationResult result = new ValidationResult();

        if (categories == null || categories.isEmpty()) {
            result.categories = new HashSet<>();
            result.isValid = true;
            return result;
        }

        // 重複を除去し、空文字列を除外
        Set<String> uniqueCategories = categories.stream()
                .filter(cat -> cat != null && !cat.trim().isEmpty())
                .collect(Collectors.toSet());

        // カテゴリ数の制限チェック（厳格）
        if (uniqueCategories.size() > MAX_CATEGORIES) {
            result.categories = uniqueCategories;
            result.isValid = false;
            result.errorMessage = "カテゴリは" + MAX_CATEGORIES + "つまでしか選択できません。現在" + uniqueCategories.size() + "つ選択されています。";
            System.out.println("カテゴリ制限エラー: " + uniqueCategories.size() + " > " + MAX_CATEGORIES);
            return result;
        }

        result.categories = uniqueCategories;
        result.isValid = true;
        return result;
    }

    // バリデーション結果を格納するクラス
    private static class ValidationResult {
        Set<String> categories;
        boolean isValid;
        String errorMessage;
    }

    // レシピを新規登録（強化版）
    @PostMapping("/recipes/new")
    public String submitRecipe(@RequestParam String title,
                               @RequestParam String ingredients,
                               @RequestParam String instructions,
                               @RequestParam(name = "favorite", defaultValue = "false") boolean favorite,
                               @RequestParam(required = false) String reference,
                               @RequestParam(value = "categories", required = false) List<String> categories,
                               @RequestParam("image") MultipartFile imageFile,
                               RedirectAttributes redirectAttributes) throws IOException {

        System.out.println("受信したカテゴリ: " + categories);
        System.out.println("カテゴリ数: " + (categories != null ? categories.size() : 0));

        // カテゴリの厳格なバリデーション
        ValidationResult validationResult = validateCategories(categories);
        if (!validationResult.isValid) {
            redirectAttributes.addFlashAttribute("errorMessage", validationResult.errorMessage);
            redirectAttributes.addFlashAttribute("recipe", createRecipeFromParams(title, ingredients, instructions, favorite, reference, validationResult.categories));
            return "redirect:/recipes/new";
        }

        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        recipe.setIngredients(ingredients);
        recipe.setInstructions(instructions);
        recipe.setCategories(validationResult.categories);
        recipe.setFavorite(favorite);
        recipe.setReference(reference);

        System.out.println("設定されたカテゴリ: " + validationResult.categories);

        // 画像アップロード処理（プロジェクト直下の uploads ディレクトリへ）
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(fileName);
            imageFile.transferTo(filePath.toFile());

            recipe.setImagePath("/uploads/" + fileName); // HTMLで表示する相対パス
        }

        try {
            Recipe savedRecipe = recipeRepository.save(recipe);
            System.out.println("保存されたレシピID: " + savedRecipe.getId());
            System.out.println("保存されたカテゴリ: " + savedRecipe.getCategories());
            redirectAttributes.addFlashAttribute("successMessage", "レシピが正常に登録されました。");
        } catch (Exception e) {
            System.err.println("レシピ保存エラー: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "レシピの保存に失敗しました。");
            return "redirect:/recipes/new";
        }

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

    // 編集内容を保存（強化版）
    @PostMapping("/recipes/update")
    public String updateRecipe(@RequestParam Long id,
                               @RequestParam String title,
                               @RequestParam String ingredients,
                               @RequestParam String instructions,
                               @RequestParam(name = "favorite", defaultValue = "false") boolean favorite,
                               @RequestParam(required = false) String reference,
                               @RequestParam(value = "categories", required = false) List<String> categories,
                               @RequestParam("image") MultipartFile imageFile,
                               @RequestParam(name = "deleteCurrentImage", defaultValue = "false") boolean deleteCurrentImage,
                               RedirectAttributes redirectAttributes
    ) throws IOException {

        System.out.println("更新 - 受信したカテゴリ: " + categories);
        System.out.println("更新 - カテゴリ数: " + (categories != null ? categories.size() : 0));

        // カテゴリの厳格なバリデーション
        ValidationResult validationResult = validateCategories(categories);
        if (!validationResult.isValid) {
            redirectAttributes.addFlashAttribute("errorMessage", validationResult.errorMessage);
            return "redirect:/recipes/edit/" + id;
        }

        Recipe existingRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid recipe ID: " + id));

        existingRecipe.setTitle(title);
        existingRecipe.setIngredients(ingredients);
        existingRecipe.setInstructions(instructions);
        existingRecipe.setFavorite(favorite);
        existingRecipe.setReference(reference);
        existingRecipe.setCategories(validationResult.categories);

        System.out.println("更新 - 設定されたカテゴリ: " + validationResult.categories);

        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");
        Files.createDirectories(uploadPath);

        // 画像アップロード or 画像削除
        if (imageFile != null && !imageFile.isEmpty()) {
            // 古い画像があれば削除
            if (existingRecipe.getImagePath() != null) {
                Path oldPath = uploadPath.resolve(Paths.get(existingRecipe.getImagePath()).getFileName());
                Files.deleteIfExists(oldPath);
            }

            // 新しい画像を保存
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            imageFile.transferTo(filePath.toFile());

            existingRecipe.setImagePath("/uploads/" + fileName);
        } else if (deleteCurrentImage) {
            // 新しい画像が無く、削除フラグが立っている場合
            if (existingRecipe.getImagePath() != null) {
                Path oldPath = uploadPath.resolve(Paths.get(existingRecipe.getImagePath()).getFileName());
                Files.deleteIfExists(oldPath);
            }
            existingRecipe.setImagePath(null);
        }

        try {
            Recipe savedRecipe = recipeRepository.save(existingRecipe);
            System.out.println("更新されたレシピID: " + savedRecipe.getId());
            System.out.println("更新されたカテゴリ: " + savedRecipe.getCategories());
            redirectAttributes.addFlashAttribute("successMessage", "レシピが正常に更新されました。");
        } catch (Exception e) {
            System.err.println("レシピ更新エラー: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "レシピの更新に失敗しました。");
            return "redirect:/recipes/edit/" + id;
        }

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

    // パラメータからレシピオブジェクトを作成するヘルパーメソッド
    private Recipe createRecipeFromParams(String title, String ingredients, String instructions, boolean favorite, String reference, Set<String> categories) {
        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        recipe.setIngredients(ingredients);
        recipe.setInstructions(instructions);
        recipe.setFavorite(favorite);
        recipe.setReference(reference);
        recipe.setCategories(categories);
        return recipe;
    }
}