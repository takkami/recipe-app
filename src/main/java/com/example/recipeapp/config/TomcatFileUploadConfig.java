package com.example.recipeapp.config;

import jakarta.servlet.ServletContext;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;

/**
 * Tomcat のマルチパート解析器の fileCountMax（受け付けるパート数）を拡張。
 * Spring Boot 3.5.x には max-file-count のプロパティが無いため、
 * 複数の方法でパート数制限を設定します。
 */
@Configuration
public class TomcatFileUploadConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    /** 受け付けるパート数の上限 */
    private static final int FILE_COUNT_MAX = 200;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addContextCustomizers(context -> {
            ServletContext sc = context.getServletContext();
            if (sc != null) {
                // 方法1: ServletContext 属性で設定
                sc.setAttribute("org.apache.tomcat.util.http.fileupload.fileCountMax", FILE_COUNT_MAX);

                // 方法2: システムプロパティでも設定
                System.setProperty("org.apache.tomcat.util.http.fileupload.fileCountMax", String.valueOf(FILE_COUNT_MAX));

                // 追加のマルチパート設定
                sc.setAttribute("org.apache.tomcat.util.http.fileupload.maxFileSize", Long.valueOf(10L * 1024 * 1024)); // 10MB
                sc.setAttribute("org.apache.tomcat.util.http.fileupload.maxRequestSize", Long.valueOf(20L * 1024 * 1024)); // 20MB
            }
        });

        // Tomcatコネクタレベルでの設定
        factory.addConnectorCustomizers(connector -> {
            // HTTP POST の最大サイズを設定
            connector.setMaxPostSize(20 * 1024 * 1024); // 20MB
        });
    }
}