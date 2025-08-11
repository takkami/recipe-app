package com.example.recipeapp.config;

import jakarta.servlet.ServletContext;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * Tomcat のマルチパート解析器の fileCountMax（受け付けるパート数）を拡張。
 * Spring Boot 3.5.x には max-file-count のプロパティが無いため、
 * ServletContext 属性 "org.apache.tomcat.util.http.fileupload.fileCountMax" を直接設定します。
 */
@Configuration
public class TomcatFileUploadConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    /** 受け付けるパート数の上限（必要なら -1 で無制限） */
    private static final int FILE_COUNT_MAX = 100;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addContextCustomizers(context -> {
            ServletContext sc = context.getServletContext();
            if (sc != null) {
                // ★ここがポイント：ServletContext に属性を置く
                sc.setAttribute("org.apache.tomcat.util.http.fileupload.fileCountMax", Integer.valueOf(FILE_COUNT_MAX));

                // 必要に応じてサイズ系もここで調整可能（Springのmultipart設定と併用可）
                // sc.setAttribute("org.apache.tomcat.util.http.fileupload.maxFileSize", Long.valueOf(-1L));
                // sc.setAttribute("org.apache.tomcat.util.http.fileupload.maxRequestSize", Long.valueOf(-1L));
            }
        });
    }
}
