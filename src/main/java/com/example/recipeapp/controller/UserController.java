package com.example.recipeapp.controller;

import com.example.recipeapp.model.User;
import com.example.recipeapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute User user,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

        // バリデーションエラーがある場合
        if (bindingResult.hasErrors()) {
            System.out.println("バリデーションエラー: " + bindingResult.getAllErrors());
            return "register";
        }

        // ユーザー名の詳細チェック
        String username = user.getUsername();
        if (username == null || username.trim().length() < 3 || username.trim().length() > 20) {
            redirectAttributes.addFlashAttribute("errorMessage", "ユーザー名は3文字以上20文字以下で入力してください。");
            return "redirect:/register";
        }

        // パスワードの詳細チェック
        String password = user.getPassword();
        if (password == null || password.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "パスワードは6文字以上で入力してください。");
            return "redirect:/register";
        }

        // 既存ユーザー名のチェック
        if (userRepository.findByUsername(username.trim()) != null) {
            System.out.println("ユーザー名重複エラー: " + username);
            redirectAttributes.addFlashAttribute("errorMessage", "そのユーザー名は既に使用されています。別のユーザー名を選択してください。");
            return "redirect:/register";
        }

        try {
            // ユーザー名をトリムして設定
            user.setUsername(username.trim());

            // パスワードをハッシュ化して保存
            user.setPassword(passwordEncoder.encode(password));
            user.setRole("ROLE_USER");

            User savedUser = userRepository.save(user);
            System.out.println("新規ユーザー登録完了: " + savedUser.getUsername() + " (ID: " + savedUser.getId() + ")");

            redirectAttributes.addFlashAttribute("successMessage", "ユーザー登録が完了しました。ログインしてください。");
            return "redirect:/login";

        } catch (Exception e) {
            System.err.println("ユーザー登録エラー: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "ユーザー登録中にエラーが発生しました。もう一度お試しください。");
            return "redirect:/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "login";
    }

    // ユーザー情報確認用のエンドポイント（デバッグ用）
    @GetMapping("/admin/users")
    @ResponseBody
    public String listUsers() {
        StringBuilder sb = new StringBuilder();
        sb.append("登録済みユーザー一覧:\n");

        userRepository.findAll().forEach(user -> {
            sb.append("ID: ").append(user.getId())
                    .append(", ユーザー名: ").append(user.getUsername())
                    .append(", 役割: ").append(user.getRole())
                    .append("\n");
        });

        return sb.toString();
    }
}