package org.example.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public final class DBConnection {
    private static EntityManagerFactory entityManagerFactory;

    private DBConnection() {
    }

    private static EntityManagerFactory buildEntityManagerFactory() {
        try {
            String username = System.getenv("CODEMATE_DB_USER");
            if (username == null || username.isBlank()) {
                username = "root";
            }
            String password = System.getenv("CODEMATE_DB_PASSWORD");
            if (password == null) {
                password = "";
            }

            String defaultUrl = "jdbc:mysql://localhost:3306/codemate?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Bangkok&characterEncoding=UTF-8";
            String url = System.getenv().getOrDefault("CODEMATE_DB_URL", defaultUrl);

            Map<String, Object> properties = new HashMap<>();
            properties.put("jakarta.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
            properties.put("jakarta.persistence.jdbc.url", url);
            properties.put("jakarta.persistence.jdbc.user", username);
            properties.put("jakarta.persistence.jdbc.password", password);
            return Persistence.createEntityManagerFactory("codemate", properties);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Không thể khởi tạo kết nối MySQL. Vui lòng bật MySQL trong XAMPP và kiểm tra database 'codemate'.", e);
        }
    }

    public static synchronized EntityManager getEntityManager() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            entityManagerFactory = buildEntityManagerFactory();
        }
        return entityManagerFactory.createEntityManager();
    }

    public static synchronized void closeEntityManager() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}
