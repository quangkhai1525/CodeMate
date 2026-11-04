package org.example.dao;

import org.example.model.TodoItem;
import org.example.model.User;

import java.time.LocalDate;
import java.util.List;

public class TodoDAO extends JpaDaoSupport {
    public TodoItem add(String title, String priority, LocalDate dueDate) {
        Long userId = currentUserId();
        String normalizedTitle = normalizeTitle(title);
        String normalizedPriority = normalizePriority(priority);
        return withEntityManager(entityManager -> inTransaction(entityManager, em -> {
            TodoItem item = new TodoItem(normalizedTitle, false, dueDate, normalizedPriority,
                    em.getReference(User.class, userId));
            em.persist(item);
            return item;
        }));
    }

    public TodoItem add(String title, LocalDate dueDate) {
        return add(title, "URGENT", dueDate);
    }

    public void rename(Long itemId, String title) {
        Long userId = currentUserId();
        String normalizedTitle = normalizeTitle(title);
        withEntityManager(entityManager -> inTransaction(entityManager, em -> {
            TodoItem item = findOwnedItem(em, userId, itemId);
            item.setTitle(normalizedTitle);
            return null;
        }));
    }

    public void delete(Long itemId) {
        Long userId = currentUserId();
        withEntityManager(entityManager -> inTransaction(entityManager, em -> {
            int deleted = em.createQuery("delete from TodoItem t where t.id = :itemId and t.user.id = :userId")
                    .setParameter("itemId", itemId)
                    .setParameter("userId", userId)
                    .executeUpdate();
            if (deleted == 0) {
                throw new IllegalArgumentException("Không tìm thấy công việc cần xóa.");
            }
            return null;
        }));
    }

    public void setCompleted(Long itemId, boolean completed) {
        Long userId = currentUserId();
        withEntityManager(entityManager -> inTransaction(entityManager, em -> {
            TodoItem item = findOwnedItem(em, userId, itemId);
            item.setCompleted(completed);
            return null;
        }));
    }

    public List<TodoItem> getForDate(LocalDate date) {
        Long userId = currentUserId();
        return withEntityManager(entityManager -> entityManager.createQuery(
                        "select t from TodoItem t where t.user.id = :userId and t.dueDate = :date " +
                                "order by t.completed, t.id", TodoItem.class)
                .setParameter("userId", userId)
                .setParameter("date", date)
                .getResultList());
    }

    public int moveIncompleteToTomorrow(LocalDate date) {
        Long userId = currentUserId();
        return withEntityManager(entityManager -> inTransaction(entityManager, em ->
                em.createQuery("update TodoItem t set t.dueDate = :tomorrow " +
                                "where t.user.id = :userId and t.dueDate = :date and t.completed = false")
                        .setParameter("tomorrow", date.plusDays(1))
                        .setParameter("userId", userId)
                        .setParameter("date", date)
                        .executeUpdate()));
    }

    private TodoItem findOwnedItem(jakarta.persistence.EntityManager entityManager, Long userId, Long itemId) {
        return entityManager.createQuery(
                        "select t from TodoItem t where t.id = :itemId and t.user.id = :userId", TodoItem.class)
                .setParameter("itemId", itemId)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy công việc của tài khoản này."));
    }

    private String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Tên công việc không được để trống.");
        }
        String normalized = title.trim();
        if (normalized.length() > 200) {
            throw new IllegalArgumentException("Tên công việc không được dài quá 200 ký tự.");
        }
        return normalized;
    }

    private String normalizePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return "URGENT";
        }
        String p = priority.trim().toUpperCase();
        if (p.equals("IMPORTANT") || p.equals("NORMAL")) {
            return p;
        }
        return "URGENT";
    }
}
