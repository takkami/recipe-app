package com.example.recipeapp;

import com.example.recipeapp.model.User;
import com.example.recipeapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class RecipeappApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecipeappApplication.class, args);
    }

    // 初期ユーザー登録
    @Bean
    CommandLineRunner init(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            // 管理者ユーザーが存在しない場合のみ作成
            if (repo.findByUsername("admin") == null) {
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setPassword(encoder.encode("password"));
                adminUser.setRole("ROLE_ADMIN");
                repo.save(adminUser);
                System.out.println("初期管理者ユーザー 'admin' を作成しました（パスワード: password）");
            }

            // 一般ユーザーも作成する場合
            if (repo.findByUsername("user") == null) {
                User normalUser = new User();
                normalUser.setUsername("user");
                normalUser.setPassword(encoder.encode("password"));
                normalUser.setRole("ROLE_USER");
                repo.save(normalUser);
                System.out.println("初期一般ユーザー 'user' を作成しました（パスワード: password）");
            }
        };
    }
}