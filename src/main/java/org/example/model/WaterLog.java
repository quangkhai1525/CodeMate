package org.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

@Entity
@Table(name = "water_log", uniqueConstraints = {
        @UniqueConstraint(name = "uk_water_user_day", columnNames = {"user_id", "log_date"})
})
public class WaterLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "total_ml", nullable = false)
    private int totalMl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public WaterLog() {
    }

    public WaterLog(LocalDate logDate, int totalMl, User user) {
        this.logDate = logDate;
        this.totalMl = totalMl;
        this.user = user;
    }

    public Long getId() { return id; }
    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }
    public int getTotalMl() { return totalMl; }
    public void setTotalMl(int totalMl) { this.totalMl = totalMl; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
