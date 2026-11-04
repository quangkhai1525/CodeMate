package org.example.dao;

import jakarta.persistence.LockModeType;
import org.example.model.User;
import org.example.model.WaterLog;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WaterLogDAO extends JpaDaoSupport {
    public int getTodayTotalMl() {
        Long userId = currentUserId();
        LocalDate today = LocalDate.now();
        return withEntityManager(entityManager -> entityManager.createQuery(
                        "select w.totalMl from WaterLog w where w.user.id = :userId and w.logDate = :date", Integer.class)
                .setParameter("userId", userId)
                .setParameter("date", today)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(0));
    }

    public int addGlass250Ml() {
        return addMl(250);
    }

    public int addMl(int amount) {
        Long userId = currentUserId();
        LocalDate today = LocalDate.now();
        return withEntityManager(entityManager -> inTransaction(entityManager, em -> {
            User user = em.find(User.class, userId, LockModeType.PESSIMISTIC_WRITE);
            if (user == null) {
                throw new IllegalStateException("Không tìm thấy tài khoản đang đăng nhập.");
            }
            List<WaterLog> logs = em.createQuery(
                            "select w from WaterLog w where w.user.id = :userId and w.logDate = :date", WaterLog.class)
                    .setParameter("userId", userId)
                    .setParameter("date", today)
                    .getResultList();
            if (logs.isEmpty()) {
                int initial = Math.max(0, amount);
                WaterLog log = new WaterLog(today, initial, user);
                em.persist(log);
                return initial;
            }
            WaterLog log = logs.get(0);
            int newTotal = Math.max(0, log.getTotalMl() + amount);
            log.setTotalMl(newTotal);
            return newTotal;
        }));
    }

    public Map<LocalDate, Integer> getWaterMlLast7Days() {
        return getWaterMlForWeek(LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));
    }

    public Map<LocalDate, Integer> getWaterMlForWeek(LocalDate monday) {
        Long userId = currentUserId();
        LocalDate lastDay = monday.plusDays(6);
        return withEntityManager(entityManager -> {
            List<Object[]> rows = entityManager.createQuery(
                            "select w.logDate, w.totalMl from WaterLog w where w.user.id = :userId " +
                                    "and w.logDate >= :from and w.logDate <= :until order by w.logDate", Object[].class)
                    .setParameter("userId", userId)
                    .setParameter("from", monday)
                    .setParameter("until", lastDay)
                    .getResultList();
            Map<LocalDate, Integer> valuesByDay = new LinkedHashMap<>();
            for (Object[] row : rows) {
                valuesByDay.put((LocalDate) row[0], ((Number) row[1]).intValue());
            }
            Map<LocalDate, Integer> result = new LinkedHashMap<>();
            for (int offset = 0; offset < 7; offset++) {
                LocalDate day = monday.plusDays(offset);
                result.put(day, valuesByDay.getOrDefault(day, 0));
            }
            return result;
        });
    }

    public Map<LocalDate, Integer> getRecentWaterMl(int daysBack) {
        Long userId = currentUserId();
        LocalDate startDate = LocalDate.now().minusDays(daysBack);
        LocalDate today = LocalDate.now();
        return withEntityManager(entityManager -> {
            List<Object[]> rows = entityManager.createQuery(
                            "select w.logDate, w.totalMl from WaterLog w where w.user.id = :userId " +
                                    "and w.logDate >= :from and w.logDate <= :until order by w.logDate", Object[].class)
                    .setParameter("userId", userId)
                    .setParameter("from", startDate)
                    .setParameter("until", today)
                    .getResultList();
            Map<LocalDate, Integer> valuesByDay = new LinkedHashMap<>();
            for (Object[] row : rows) {
                valuesByDay.put((LocalDate) row[0], ((Number) row[1]).intValue());
            }
            Map<LocalDate, Integer> result = new LinkedHashMap<>();
            for (int offset = 0; offset <= daysBack; offset++) {
                LocalDate day = startDate.plusDays(offset);
                result.put(day, valuesByDay.getOrDefault(day, 0));
            }
            return result;
        });
    }
}
