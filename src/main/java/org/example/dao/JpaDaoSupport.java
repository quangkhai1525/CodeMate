package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.model.User;
import org.example.model.UserSession;
import org.example.utils.DBConnection;

import java.util.function.Function;

abstract class JpaDaoSupport {
    @FunctionalInterface
    protected interface EntityManagerAction<T> {
        T apply(EntityManager entityManager);
    }

    protected final Long currentUserId() {
        User user = UserSession.getCurrentUser();
        if (user == null || user.getId() == null) {
            throw new IllegalStateException("Phiên đăng nhập đã kết thúc. Vui lòng đăng nhập lại.");
        }
        return user.getId();
    }

    protected final <T> T withEntityManager(EntityManagerAction<T> action) {
        EntityManager entityManager = DBConnection.getEntityManager();
        try {
            return action.apply(entityManager);
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    protected final <T> T inTransaction(EntityManager entityManager, Function<EntityManager, T> action) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            T result = action.apply(entityManager);
            transaction.commit();
            return result;
        } catch (RuntimeException | Error exception) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw exception;
        }
    }
}
