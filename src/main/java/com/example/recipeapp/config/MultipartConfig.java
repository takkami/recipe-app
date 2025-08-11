package com.example.recipeapp.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.MultipartConfigElement;

/**
 * マルチパートファイルアップロードの詳細設定
 */
@Configuration
public class MultipartConfig {

    /**
     * マルチパートリゾルバーのカスタム設定
     */
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    /**
     * マルチパート設定要素の詳細設定
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        // ファイルサイズ制限
        factory.setMaxFileSize(DataSize.ofMegabytes(10));

        // リクエスト全体のサイズ制限
        factory.setMaxRequestSize(DataSize.ofMegabytes(20));

        // ファイルがメモリに書き込まれる閾値
        factory.setFileSizeThreshold(DataSize.ofKilobytes(0));

        // 一時ディレクトリ
        factory.setLocation(System.getProperty("java.io.tmpdir"));

        return factory.createMultipartConfig();
    }
}