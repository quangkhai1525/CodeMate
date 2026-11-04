package org.example.dao;

import org.example.model.PomodoroStatus;
import org.example.model.TimeManagement;
import org.example.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TimeManagementDAO extends JpaDaoSupport {
    public void saveCompletedSession(String taskName, LocalDateTime startTime,
                                     LocalDateTime endTime, int durationSeconds) {
        Long userId = currentUserId();
        if (durationSeconds <= 0 || startTime == null || endTime == null || endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("Thông tin phiên Pomodoro không hợp lệ.");
        }

        withEntityManager(entityManager -> inTransaction(entityManager, em -> {
            String normalizedTask = taskName == null || taskName.isBlank() ? null : taskName.trim();
            TimeManagement session = new TimeManagement(
                    normalizedTask, startTime, endTime, durationSeconds, PomodoroStatus.COMPLETED,
                    em.getReference(User.class, userId));
            em.persist(session);
            return null;
        }));
    }

    public Map<LocalDate, Integer> getWorkMinutesLast7Days() {
        return getWorkMinutesForWeek(LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));
    }

    public Map<LocalDate, Integer> getWorkMinutesForWeek(LocalDate monday) {
        Long userId = currentUserId();
        LocalDateTime from = monday.atStartOfDay();
        LocalDateTime until = monday.plusDays(7).atStartOfDay();

        return withEntityManager(entityManager -> {
            List<Object[]> rows = entityManager.createQuery(
                            "select t.startTime, t.durationSeconds from TimeManagement t " +
                                    "where t.user.id = :userId and t.sessionStatus = :status " +
                                    "and t.startTime >= :from and t.startTime < :until", Object[].class)
                    .setParameter("userId", userId)
                    .setParameter("status", PomodoroStatus.COMPLETED)
                    .setParameter("from", from)
                    .setParameter("until", until)
                    .getResultList();

            Map<LocalDate, Long> secondsByDay = new LinkedHashMap<>();
            for (Object[] row : rows) {
                LocalDate day = ((LocalDateTime) row[0]).toLocalDate();
                secondsByDay.merge(day, ((Number) row[1]).longValue(), Long::sum);
            }

            Map<LocalDate, Integer> result = new LinkedHashMap<>();
            for (int offset = 0; offset < 7; offset++) {
                LocalDate day = monday.plusDays(offset);
                long seconds = secondsByDay.getOrDefault(day, 0L);
                result.put(day, Math.toIntExact((seconds + 30) / 60));
            }
            return result;
        });
    }

    public Map<LocalDate, Integer> getRecentWorkMinutes(int daysBack) {
        Long userId = currentUserId();
        LocalDate startDate = LocalDate.now().minusDays(daysBack);
        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime until = LocalDate.now().plusDays(1).atStartOfDay();

        return withEntityManager(entityManager -> {
            List<Object[]> rows = entityManager.createQuery(
                            "select t.startTime, t.durationSeconds from TimeManagement t " +
                                    "where t.user.id = :userId and t.sessionStatus = :status " +
                                    "and t.startTime >= :from and t.startTime < :until", Object[].class)
                    .setParameter("userId", userId)
                    .setParameter("status", PomodoroStatus.COMPLETED)
                    .setParameter("from", from)
                    .setParameter("until", until)
                    .getResultList();

            Map<LocalDate, Long> secondsByDay = new LinkedHashMap<>();
            for (Object[] row : rows) {
                LocalDate day = ((LocalDateTime) row[0]).toLocalDate();
                secondsByDay.merge(day, ((Number) row[1]).longValue(), Long::sum);
            }

            Map<LocalDate, Integer> result = new LinkedHashMap<>();
            for (int offset = 0; offset <= daysBack; offset++) {
                LocalDate day = startDate.plusDays(offset);
                long seconds = secondsByDay.getOrDefault(day, 0L);
                result.put(day, Math.toIntExact((seconds + 30) / 60));
            }
            return result;
        });
    }
}
