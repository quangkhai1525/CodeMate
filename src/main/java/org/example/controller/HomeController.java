package org.example.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.example.dao.TimeManagementDAO;
import org.example.dao.TodoDAO;
import org.example.dao.WaterLogDAO;
import org.example.model.TodoItem;
import org.example.model.User;
import org.example.model.UserSession;
import org.example.utils.ChangeFXML;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeController {
    private static final int DAILY_WATER_GOAL_ML = 2000;
    private static final DateTimeFormatter CHART_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM");

    @FXML private javafx.scene.layout.BorderPane root;
    @FXML private Label userGreeting;

    // Pomodoro Timer Elements
    @FXML private Arc timerProgressArc;
    @FXML private Label timerLabel;
    @FXML private Label timerStatusBadge;
    @FXML private Label currentTaskBadge;
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button cancelButton;

    // Quick Duration Buttons
    @FXML private Button btnPomo25;
    @FXML private Button btnPomo30;
    @FXML private Button btnBreak5;
    @FXML private Button btnBreak15;

    // Main Navigation Tabs
    @FXML private TabPane mainTabPane;

    // Task Date Switcher & Stats
    @FXML private Button btnViewToday;
    @FXML private Button btnViewTomorrow;
    @FXML private DatePicker taskDatePicker;
    @FXML private Label todoCountLabel;
    @FXML private ProgressBar todoProgress;
    @FXML private Label todoProgressLabel;

    // Inline Task Creator
    @FXML private TextField newTodoInput;
    @FXML private Button btnPriorityUrgent;
    @FXML private Button btnPriorityImportant;
    @FXML private Button btnPriorityNormal;

    // 3 Priority Containers
    @FXML private VBox urgentSection;
    @FXML private VBox importantSection;
    @FXML private VBox normalSection;
    @FXML private VBox urgentListContainer;
    @FXML private VBox importantListContainer;
    @FXML private VBox normalListContainer;
    @FXML private Label emptyTodoNotice;

    // Water Tracker Elements
    @FXML private Label waterTotalLabel;
    @FXML private Label waterGoalLabel;
    @FXML private Label waterPercentLabel;
    @FXML private ProgressBar waterProgress;
    @FXML private Label homeWaterSummary;
    @FXML private ProgressBar homeWaterProgress;
    @FXML private Label waterTipLabel;

    // Charts
    @FXML private BarChart<String, Number> workChart;
    @FXML private CategoryAxis workXAxis;
    @FXML private NumberAxis workYAxis;
    @FXML private BarChart<String, Number> waterChart;
    @FXML private CategoryAxis waterXAxis;
    @FXML private NumberAxis waterYAxis;

    // KPI Summary Labels (Stats Tab)
    @FXML private Label kpiTotalHours;
    @FXML private Label kpiAvgWater;
    @FXML private Label kpiStreakDays;

    // Week Navigation Controls (Stats Tab)
    @FXML private Label weekRangeLabel;
    @FXML private Button btnThisWeek;
    private LocalDate selectedWeekMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

    private final TodoDAO todoDAO = new TodoDAO();
    private final WaterLogDAO waterLogDAO = new WaterLogDAO();
    private final TimeManagementDAO timeManagementDAO = new TimeManagementDAO();
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "codemate-database");
        thread.setDaemon(true);
        return thread;
    });

    private Timeline timerTimeline;
    private Instant targetEndTime;
    private Instant workStartTime;
    private String sessionTaskName;
    private int totalSessionDurationSeconds = 25 * 60;
    private int remainingSecondsOnPause = 0;
    private TimerMode timerMode = TimerMode.IDLE;
    private boolean isPaused = false;
    private volatile boolean disposed;

    // Todo view state
    private LocalDate activeDate = LocalDate.now();
    private String selectedPriority = "URGENT"; // URGENT, IMPORTANT, NORMAL

    @FXML
    private void initialize() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            userGreeting.setText("Chưa đăng nhập");
            return;
        }
        userGreeting.setText(currentUser.getFullname());

        // Set default timer
        setTimerDurationMinutes(25, TimerMode.WORK);

        // Task priority buttons initialization
        selectPriority("URGENT");

        // Charts configuration
        if (workChart != null) workChart.setAnimated(false);
        if (waterChart != null) waterChart.setAnimated(false);
        if (workYAxis != null) workYAxis.setForceZeroInRange(true);
        if (waterYAxis != null) waterYAxis.setForceZeroInRange(true);

        if (taskDatePicker != null) {
            taskDatePicker.setValue(activeDate);
        }

        // Tab selection change listener to refresh charts immediately when user clicks tab
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                refreshDashboard();
            });
        }

        // Add F5 key accelerator on scene for instant manual refresh
        Platform.runLater(() -> {
            if (root != null && root.getScene() != null) {
                root.getScene().getAccelerators().put(
                        new KeyCodeCombination(KeyCode.F5),
                        this::handleRefreshAll
                );
            }
        });

        updateDateSwitcherButtons();
        refreshDashboard();
    }

    // ==========================================
    // POMODORO TIMER LOGIC
    // ==========================================

    @FXML
    private void selectPomo25() {
        if (timerMode != TimerMode.IDLE && !isPaused) return;
        setTimerDurationMinutes(25, TimerMode.WORK);
    }

    @FXML
    private void selectPomo30() {
        if (timerMode != TimerMode.IDLE && !isPaused) return;
        setTimerDurationMinutes(30, TimerMode.WORK);
    }

    @FXML
    private void selectBreak5() {
        if (timerMode != TimerMode.IDLE && !isPaused) return;
        setTimerDurationMinutes(5, TimerMode.BREAK);
    }

    @FXML
    private void selectBreak15() {
        if (timerMode != TimerMode.IDLE && !isPaused) return;
        setTimerDurationMinutes(15, TimerMode.BREAK);
    }

    private void setTimerDurationMinutes(int minutes, TimerMode mode) {
        stopTimerTimeline();
        timerMode = TimerMode.IDLE;
        isPaused = false;
        totalSessionDurationSeconds = minutes * 60;
        timerLabel.setText(formatTime(totalSessionDurationSeconds));
        if (timerProgressArc != null) {
            timerProgressArc.setLength(360.0);
            if (mode == TimerMode.BREAK) {
                timerProgressArc.setStroke(Color.web("#10b981"));
            } else {
                timerProgressArc.setStroke(Color.web("#38bdf8"));
            }
        }
        if (mode == TimerMode.BREAK) {
            timerStatusBadge.setText("☕ SẴN SÀNG NGHỈ NGƠI (" + minutes + " PHÚT)");
            timerStatusBadge.setStyle("-fx-text-fill: #10b981; -fx-background-color: rgba(16, 185, 129, 0.15);");
        } else {
            timerStatusBadge.setText("🎯 SẴN SÀNG TẬP TRUNG (" + minutes + " PHÚT)");
            timerStatusBadge.setStyle("-fx-text-fill: #38bdf8; -fx-background-color: rgba(56, 189, 248, 0.15);");
        }
        if (startButton != null) startButton.setDisable(false);
        if (pauseButton != null) pauseButton.setDisable(true);
        if (cancelButton != null) cancelButton.setDisable(true);
    }

    @FXML
    private void startTimerAction() {
        if (isPaused) {
            // Resume from pause
            isPaused = false;
            targetEndTime = Instant.now().plusSeconds(remainingSecondsOnPause);
            resumeTimerTimeline();
            startButton.setDisable(true);
            pauseButton.setDisable(false);
            cancelButton.setDisable(false);
            timerStatusBadge.setText(timerMode == TimerMode.BREAK ? "☕ ĐANG NGHỈ NGƠI..." : "🚀 ĐANG TẬP TRUNG...");
            return;
        }

        if (timerMode != TimerMode.IDLE || disposed) return;

        timerMode = (timerStatusBadge.getText().contains("NGHỈ")) ? TimerMode.BREAK : TimerMode.WORK;
        workStartTime = Instant.now();
        targetEndTime = Instant.now().plusSeconds(totalSessionDurationSeconds);

        startButton.setDisable(true);
        pauseButton.setDisable(false);
        cancelButton.setDisable(false);

        if (timerProgressArc != null) {
            if (timerMode == TimerMode.BREAK) {
                timerProgressArc.setStroke(Color.web("#10b981"));
            } else {
                timerProgressArc.setStroke(Color.web("#38bdf8"));
            }
        }

        if (timerMode == TimerMode.WORK) {
            timerStatusBadge.setText("🚀 ĐANG TẬP TRUNG LÀM VIỆC");
            timerStatusBadge.setStyle("-fx-text-fill: #38bdf8; -fx-background-color: rgba(56, 189, 248, 0.2);");
        } else {
            timerStatusBadge.setText("☕ ĐANG NGHỈ NGƠI THƯ GIÃN");
            timerStatusBadge.setStyle("-fx-text-fill: #10b981; -fx-background-color: rgba(16, 185, 129, 0.2);");
        }

        resumeTimerTimeline();
    }

    @FXML
    private void pauseTimerAction() {
        if (timerMode == TimerMode.IDLE || targetEndTime == null || isPaused) return;

        isPaused = true;
        stopTimerTimeline();
        long millisRemaining = Math.max(0, targetEndTime.toEpochMilli() - Instant.now().toEpochMilli());
        remainingSecondsOnPause = Math.toIntExact((millisRemaining + 999) / 1000);

        startButton.setDisable(false);
        pauseButton.setDisable(true);
        timerStatusBadge.setText("⏸ ĐÃ TẠM DỪNG");
        timerStatusBadge.setStyle("-fx-text-fill: #f59e0b; -fx-background-color: rgba(245, 158, 11, 0.2);");
    }

    @FXML
    private void cancelTimerAction() {
        stopTimerTimeline();
        timerMode = TimerMode.IDLE;
        isPaused = false;
        targetEndTime = null;
        workStartTime = null;
        sessionTaskName = null;
        currentTaskBadge.setText("Chưa chọn công việc cụ thể");
        setTimerDurationMinutes(25, TimerMode.WORK);
    }

    public void setFocusTask(String taskTitle) {
        this.sessionTaskName = taskTitle;
        if (currentTaskBadge != null) {
            currentTaskBadge.setText("🎯 Đang làm: " + taskTitle);
        }
    }

    private void resumeTimerTimeline() {
        stopTimerTimeline();
        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateTimerTick()));
        timerTimeline.setCycleCount(Timeline.INDEFINITE);
        timerTimeline.playFromStart();
        updateTimerTick();
    }

    private void updateTimerTick() {
        if (targetEndTime == null) return;
        long millisRemaining = targetEndTime.toEpochMilli() - Instant.now().toEpochMilli();
        long secondsRemaining = Math.max(0, (millisRemaining + 999) / 1000);

        timerLabel.setText(formatTime(secondsRemaining));

        // Update circular arc progress (360 degrees full down to 0)
        if (timerProgressArc != null && totalSessionDurationSeconds > 0) {
            double fraction = (double) secondsRemaining / totalSessionDurationSeconds;
            timerProgressArc.setLength(360.0 * Math.max(0.0, Math.min(1.0, fraction)));
        }

        if (secondsRemaining == 0) {
            stopTimerTimeline();
            TimerMode completedMode = timerMode;
            Instant completedAt = targetEndTime;
            timerMode = TimerMode.IDLE;
            targetEndTime = null;

            if (completedMode == TimerMode.WORK) {
                handleWorkCompleted(completedAt);
            } else {
                handleBreakCompleted();
            }
        }
    }

    private void handleWorkCompleted(Instant completedAt) {
        Instant started = workStartTime;
        int duration = totalSessionDurationSeconds;
        String task = sessionTaskName;
        workStartTime = null;

        timerStatusBadge.setText("🎉 HOÀN THÀNH PHIÊN!");
        timerStatusBadge.setStyle("-fx-text-fill: #10b981; -fx-background-color: rgba(16, 185, 129, 0.25);");

        databaseExecutor.execute(() -> {
            try {
                LocalDateTime start = LocalDateTime.ofInstant(started == null ? Instant.now().minusSeconds(duration) : started, ZoneId.systemDefault());
                LocalDateTime end = LocalDateTime.ofInstant(completedAt, ZoneId.systemDefault());
                timeManagementDAO.saveCompletedSession(task, start, end, duration);
            } catch (Exception ignored) {}

            Platform.runLater(() -> {
                if (disposed) return;
                refreshDashboard();
                showPomodoroFinishedDialog();
            });
        });
    }

    private void handleBreakCompleted() {
        timerStatusBadge.setText("☕ ĐÃ HẾT GIỜ NGHỈ!");
        setTimerDurationMinutes(25, TimerMode.WORK);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hết giờ nghỉ");
        alert.setHeaderText("Đã hết thời gian nghỉ ngơi!");
        alert.setContentText("Bạn đã sẵn sàng cho một phiên tập trung mới chưa?");
        alert.showAndWait();
    }

    private void showPomodoroFinishedDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Hoàn thành Pomodoro!");
        dialog.setHeaderText("🎉 Xuất sắc! Bạn đã hoàn thành trọn vẹn một phiên tập trung.");

        ButtonType btnBreak = new ButtonType("☕ Nghỉ ngơi 5 phút", ButtonBar.ButtonData.YES);
        ButtonType btnContinue = new ButtonType("🚀 Tiếp tục làm việc", ButtonBar.ButtonData.NO);
        dialog.getDialogPane().getButtonTypes().setAll(btnBreak, btnContinue);

        CheckBox cbWater = new CheckBox("💧 Bổ sung ngay 1 ly nước (250 ml)");
        cbWater.setSelected(true);
        cbWater.setStyle("-fx-font-size: 14px; -fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");

        Label tip = new Label("Hãy cho mắt và cơ thể nghỉ ngơi một chút để nạp lại năng lượng nhé.");
        tip.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px;");
        VBox content = new VBox(12, tip, cbWater);
        dialog.getDialogPane().setContent(content);

        // Apply dark styling to dialog pane
        try {
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/view/home.css").toExternalForm());
        } catch (Exception ignored) {}

        Platform.runLater(() -> {
            Button breakBtn = (Button) dialog.getDialogPane().lookupButton(btnBreak);
            if (breakBtn != null) {
                breakBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: #0b132b; -fx-font-weight: 800; -fx-background-radius: 12px; -fx-padding: 10 20; -fx-cursor: hand;");
            }
            Button continueBtn = (Button) dialog.getDialogPane().lookupButton(btnContinue);
            if (continueBtn != null) {
                continueBtn.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #38bdf8; -fx-border-color: #38bdf8; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (cbWater.isSelected()) {
            runDatabaseWrite(() -> waterLogDAO.addMl(250), "Không thể ghi nhận nước.");
        }

        if (result.isPresent() && result.get() == btnBreak) {
            setTimerDurationMinutes(5, TimerMode.BREAK);
            startTimerAction();
        } else {
            setTimerDurationMinutes(25, TimerMode.WORK);
        }
    }

    // ==========================================
    // 3-TIER SMART TASKS & DATE SWITCHER LOGIC
    // ==========================================

    @FXML
    private void handleRefreshAll() {
        refreshDashboard();
    }

    @FXML
    private void onDatePicked() {
        if (taskDatePicker != null && taskDatePicker.getValue() != null) {
            activeDate = taskDatePicker.getValue();
            updateDateSwitcherButtons();
            refreshDashboard();
        }
    }

    @FXML
    private void viewTodayTasks() {
        activeDate = LocalDate.now();
        if (taskDatePicker != null) taskDatePicker.setValue(activeDate);
        updateDateSwitcherButtons();
        refreshDashboard();
    }

    @FXML
    private void viewTomorrowTasks() {
        activeDate = LocalDate.now().plusDays(1);
        if (taskDatePicker != null) taskDatePicker.setValue(activeDate);
        updateDateSwitcherButtons();
        refreshDashboard();
    }

    private void updateDateSwitcherButtons() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        if (btnViewToday != null && btnViewTomorrow != null) {
            if (activeDate.equals(today)) {
                btnViewToday.getStyleClass().setAll("date-tab-active");
                btnViewTomorrow.getStyleClass().setAll("date-tab-inactive");
            } else if (activeDate.equals(tomorrow)) {
                btnViewToday.getStyleClass().setAll("date-tab-inactive");
                btnViewTomorrow.getStyleClass().setAll("date-tab-active");
            } else {
                btnViewToday.getStyleClass().setAll("date-tab-inactive");
                btnViewTomorrow.getStyleClass().setAll("date-tab-inactive");
            }
        }
        if (taskDatePicker != null && !activeDate.equals(taskDatePicker.getValue())) {
            taskDatePicker.setValue(activeDate);
        }
    }

    @FXML
    private void setPriorityUrgent() { selectPriority("URGENT"); }

    @FXML
    private void setPriorityImportant() { selectPriority("IMPORTANT"); }

    @FXML
    private void setPriorityNormal() { selectPriority("NORMAL"); }

    private void selectPriority(String p) {
        this.selectedPriority = p;
        if (btnPriorityUrgent == null) return;
        btnPriorityUrgent.getStyleClass().setAll("priority-btn");
        btnPriorityImportant.getStyleClass().setAll("priority-btn");
        btnPriorityNormal.getStyleClass().setAll("priority-btn");

        if ("URGENT".equals(p)) {
            btnPriorityUrgent.getStyleClass().add("priority-btn-urgent-active");
        } else if ("IMPORTANT".equals(p)) {
            btnPriorityImportant.getStyleClass().add("priority-btn-important-active");
        } else {
            btnPriorityNormal.getStyleClass().add("priority-btn-normal-active");
        }
    }

    @FXML
    private void addTodoInline() {
        String title = newTodoInput.getText();
        if (title == null || title.trim().isEmpty()) {
            showError("Vui lòng nhập tên công việc cần làm!");
            return;
        }
        String priorityToSave = selectedPriority;
        newTodoInput.clear();

        runDatabaseWrite(() -> todoDAO.add(title.trim(), priorityToSave, activeDate), "Không thể thêm công việc.");
    }

    @FXML
    private void moveIncompleteToTomorrow() {
        if (disposed) return;
        databaseExecutor.execute(() -> {
            try {
                int moved = todoDAO.moveIncompleteToTomorrow(activeDate);
                Platform.runLater(() -> {
                    if (disposed) return;
                    showInformation(moved == 0
                            ? "Không có công việc chưa hoàn thành nào cần chuyển."
                            : "Đã chuyển " + moved + " công việc chưa hoàn thành sang ngày mai!");
                    refreshDashboard();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Lỗi khi chuyển công việc: " + e.getMessage()));
            }
        });
    }

    private void renderTodos(List<TodoItem> todos) {
        urgentListContainer.getChildren().clear();
        importantListContainer.getChildren().clear();
        normalListContainer.getChildren().clear();

        int completedCount = 0;
        int urgentCount = 0;
        int importantCount = 0;
        int normalCount = 0;

        for (TodoItem item : todos) {
            if (item.isCompleted()) completedCount++;
            String p = item.getPriority() == null ? "URGENT" : item.getPriority().toUpperCase();

            HBox card = createModernTaskCard(item);
            switch (p) {
                case "NORMAL" -> {
                    normalListContainer.getChildren().add(card);
                    normalCount++;
                }
                case "IMPORTANT" -> {
                    importantListContainer.getChildren().add(card);
                    importantCount++;
                }
                default -> {
                    urgentListContainer.getChildren().add(card);
                    urgentCount++;
                }
            }
        }

        // Show/hide sections based on count
        urgentSection.setVisible(urgentCount > 0);
        urgentSection.setManaged(urgentCount > 0);
        importantSection.setVisible(importantCount > 0);
        importantSection.setManaged(importantCount > 0);
        normalSection.setVisible(normalCount > 0);
        normalSection.setManaged(normalCount > 0);

        boolean isEmpty = todos.isEmpty();
        emptyTodoNotice.setVisible(isEmpty);
        emptyTodoNotice.setManaged(isEmpty);
        if (isEmpty) {
            emptyTodoNotice.setText(activeDate.equals(LocalDate.now())
                    ? "✨ Chưa có công việc nào cho hôm nay. Hãy nhập vào ô bên trên để thêm việc đầu tiên!"
                    : "📅 Chưa có công việc nào được lên lịch cho ngày mai.");
        }

        // Update progress counters
        int total = todos.size();
        todoCountLabel.setText(total + " công việc (" + completedCount + " hoàn thành)");
        double progress = total == 0 ? 0.0 : (double) completedCount / total;
        todoProgress.setProgress(progress);
        todoProgressLabel.setText(Math.round(progress * 100) + "%");
    }

    private HBox createModernTaskCard(TodoItem item) {
        HBox card = new HBox(12.0);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("modern-task-card");

        String p = item.getPriority() == null ? "URGENT" : item.getPriority().toUpperCase();
        Label badge = new Label();
        badge.getStyleClass().add("task-priority-badge");
        switch (p) {
            case "NORMAL" -> {
                badge.setText("📌 Lúc rảnh");
                badge.getStyleClass().add("badge-normal");
                card.getStyleClass().add("card-normal");
            }
            case "IMPORTANT" -> {
                badge.setText("⚡ Quan trọng");
                badge.getStyleClass().add("badge-important");
                card.getStyleClass().add("card-important");
            }
            default -> {
                badge.setText("🔥 Làm ngay");
                badge.getStyleClass().add("badge-urgent");
                card.getStyleClass().add("card-urgent");
            }
        }

        CheckBox checkBox = new CheckBox(item.getTitle());
        checkBox.setSelected(item.isCompleted());
        checkBox.setMaxWidth(Double.MAX_VALUE);
        checkBox.getStyleClass().add("task-title-checkbox");
        if (item.isCompleted()) {
            checkBox.setStyle("-fx-strikethrough: true; -fx-opacity: 0.55;");
        }
        checkBox.selectedProperty().addListener((obs, oldV, isChecked) -> {
            runDatabaseWrite(() -> todoDAO.setCompleted(item.getId(), isChecked), "Lỗi cập nhật trạng thái việc.");
        });

        // Play button to set as current Pomodoro focus task
        Button playBtn = new Button("▶ Tập trung");
        playBtn.getStyleClass().add("task-btn-focus");
        playBtn.setOnAction(e -> {
            setFocusTask(item.getTitle());
            showInformation("Đã gán công việc: \"" + item.getTitle() + "\" vào đồng hồ Pomodoro!");
        });

        // Delete button
        Button delBtn = new Button("✕");
        delBtn.getStyleClass().add("task-btn-delete");
        delBtn.setOnAction(e -> {
            runDatabaseWrite(() -> todoDAO.delete(item.getId()), "Lỗi xóa công việc.");
        });

        HBox.setHgrow(checkBox, Priority.ALWAYS);
        card.getChildren().addAll(badge, checkBox, playBtn, delBtn);
        return card;
    }

    // ==========================================
    // WATER TRACKER LOGIC
    // ==========================================

    @FXML private void addWater250() { changeWater(250); }
    @FXML private void addWater500() { changeWater(500); }
    @FXML private void addWater100() { changeWater(100); }
    @FXML private void subtractWater250() { changeWater(-250); }

    private void changeWater(int amount) {
        runDatabaseWrite(() -> waterLogDAO.addMl(amount), "Không thể cập nhật lượng nước.");
    }

    private void renderWater(int totalMl) {
        double progress = Math.min(1.0, Math.max(0.0, totalMl / (double) DAILY_WATER_GOAL_ML));
        waterTotalLabel.setText(totalMl + " ml");
        waterPercentLabel.setText(Math.round(progress * 100) + "%");
        waterGoalLabel.setText("Mục tiêu 2,000 ml · Còn lại " + Math.max(0, DAILY_WATER_GOAL_ML - totalMl) + " ml");

        waterProgress.setProgress(progress);
        if (homeWaterSummary != null) homeWaterSummary.setText(totalMl + " ml / 2000 ml");
        if (homeWaterProgress != null) homeWaterProgress.setProgress(progress);

        if (totalMl >= DAILY_WATER_GOAL_ML) {
            waterTipLabel.setText("🎉 Tuyệt vời! Bạn đã hoàn thành trọn vẹn mục tiêu 2L nước hôm nay!");
        } else if (totalMl >= 1000) {
            waterTipLabel.setText("💧 Đã đi được hơn nửa chặng đường rồi! Tiếp tục nạp năng lượng nào.");
        } else if (totalMl > 0) {
            waterTipLabel.setText("💧 Rất tốt! Tiếp tục uống đều đặn để giữ vững sự tỉnh táo nhé.");
        } else {
            waterTipLabel.setText("💡 Khởi đầu ngày mới: Uống 1 ly nước ấm để đánh thức cơ thể và não bộ!");
        }
    }

    // ==========================================
    // CHARTS & DASHBOARD REFRESH
    // ==========================================

    @FXML
    private void handlePrevWeek() {
        selectedWeekMonday = selectedWeekMonday.minusWeeks(1);
        refreshDashboard();
    }

    @FXML
    private void handleCurrentWeek() {
        selectedWeekMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        refreshDashboard();
    }

    @FXML
    private void handleNextWeek() {
        selectedWeekMonday = selectedWeekMonday.plusWeeks(1);
        refreshDashboard();
    }

    private void updateWeekNav() {
        if (weekRangeLabel == null) return;
        LocalDate currentWeekMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekSunday = selectedWeekMonday.plusDays(6);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        boolean isCurrentWeek = selectedWeekMonday.equals(currentWeekMonday);
        String rangeStr = "Tuần: " + selectedWeekMonday.format(dtf) + " - " + weekSunday.format(dtf);
        if (isCurrentWeek) {
            rangeStr += " (Tuần này)";
        }
        weekRangeLabel.setText(rangeStr);
        if (btnThisWeek != null) {
            if (isCurrentWeek) {
                if (!btnThisWeek.getStyleClass().contains("btn-week-current-active")) {
                    btnThisWeek.getStyleClass().add("btn-week-current-active");
                }
            } else {
                btnThisWeek.getStyleClass().remove("btn-week-current-active");
            }
        }
    }

    private void refreshDashboard() {
        if (disposed || UserSession.getCurrentUser() == null) return;
        databaseExecutor.execute(() -> {
            try {
                List<TodoItem> todos = todoDAO.getForDate(activeDate);
                int todayWater = waterLogDAO.getTodayTotalMl();
                Map<LocalDate, Integer> workMin = timeManagementDAO.getWorkMinutesForWeek(selectedWeekMonday);
                Map<LocalDate, Integer> waterLog = waterLogDAO.getWaterMlForWeek(selectedWeekMonday);
                Map<LocalDate, Integer> recentWork = timeManagementDAO.getRecentWorkMinutes(60);
                Map<LocalDate, Integer> recentWater = waterLogDAO.getRecentWaterMl(60);

                Platform.runLater(() -> {
                    if (disposed) return;
                    renderTodos(todos);
                    renderWater(todayWater);
                    renderChart(workChart, workXAxis, workMin, "Phút làm việc", true);
                    renderChart(waterChart, waterXAxis, waterLog, "ml nước", false);
                    updateKpis(workMin, waterLog, recentWork, recentWater);
                    updateWeekNav();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (!disposed) showError("Lỗi tải dữ liệu cá nhân: " + e.getMessage());
                });
            }
        });
    }

    private void updateKpis(Map<LocalDate, Integer> workMin, Map<LocalDate, Integer> waterLog,
                            Map<LocalDate, Integer> recentWork, Map<LocalDate, Integer> recentWater) {
        if (workMin == null || waterLog == null) return;

        // Total time worked in viewed week (formatted as X giờ Y phút)
        int totalMinutes = workMin.values().stream().mapToInt(Integer::intValue).sum();
        int hrs = totalMinutes / 60;
        int mins = totalMinutes % 60;
        String timeStr;
        if (totalMinutes == 0) {
            timeStr = "0 phút";
        } else if (hrs == 0) {
            timeStr = mins + " phút";
        } else if (mins == 0) {
            timeStr = hrs + " giờ";
        } else {
            timeStr = hrs + " giờ " + mins + " phút";
        }
        if (kpiTotalHours != null) {
            kpiTotalHours.setText(timeStr);
        }

        // Average water per day in viewed week
        int activeDays = (int) waterLog.values().stream().filter(v -> v > 0).count();
        int totalWater = waterLog.values().stream().mapToInt(Integer::intValue).sum();
        int avgWater = activeDays > 0 ? (totalWater / activeDays) : (totalWater / 7);
        if (kpiAvgWater != null) {
            kpiAvgWater.setText(String.format("%,d ml/ngày", avgWater));
        }

        // Streak: consecutive active days ending at today/yesterday
        LocalDate today = LocalDate.now();
        int todayWork = recentWork != null ? recentWork.getOrDefault(today, 0) : 0;
        int todayWater = recentWater != null ? recentWater.getOrDefault(today, 0) : 0;
        boolean todayCompleted = (todayWork >= 25 || todayWater >= 1500);

        int streak = 0;
        LocalDate checkDate;
        if (todayCompleted) {
            // Hôm nay đã hoàn thành -> tính 1 ngày cho hôm nay và kiểm tra lùi về hôm qua
            streak = 1;
            checkDate = today.minusDays(1);
        } else {
            // Hôm nay mới bắt đầu hoặc chưa xong -> giữ nguyên thành quả chuỗi từ hôm qua
            checkDate = today.minusDays(1);
        }

        if (recentWater != null && recentWork != null) {
            for (int i = 0; i < 60; i++) {
                int w = recentWater.getOrDefault(checkDate, 0);
                int m = recentWork.getOrDefault(checkDate, 0);
                if (w >= 1500 || m >= 25) {
                    streak++;
                    checkDate = checkDate.minusDays(1);
                } else {
                    break;
                }
            }
        }

        if (kpiStreakDays != null) {
            kpiStreakDays.setText(streak + " ngày liên tiếp");
        }
    }

    private void renderChart(BarChart<String, Number> chart, CategoryAxis xAxis,
                             Map<LocalDate, Integer> values, String seriesName, boolean isWorkChart) {
        if (chart == null || values == null) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);
        for (Map.Entry<LocalDate, Integer> entry : values.entrySet()) {
            LocalDate date = entry.getKey();
            boolean isToday = date.equals(LocalDate.now());
            String dayName = switch (date.getDayOfWeek()) {
                case MONDAY -> "T2";
                case TUESDAY -> "T3";
                case WEDNESDAY -> "T4";
                case THURSDAY -> "T5";
                case FRIDAY -> "T6";
                case SATURDAY -> "T7";
                case SUNDAY -> "CN";
            };
            String dateLabel = dayName + " (" + date.format(CHART_DATE_FORMAT) + (isToday ? " - Nay" : "") + ")";
            int val = entry.getValue();
            XYChart.Data<String, Number> data = new XYChart.Data<>(dateLabel, val);

            // Install tooltip when bar node is attached
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    String todayTag = isToday ? " [Hôm nay]" : "";
                    if (isWorkChart) {
                        int h = val / 60;
                        int m = val % 60;
                        String detail = h > 0 ? (h + " giờ " + (m > 0 ? m + " phút" : "")) : (m + " phút");
                        String tipText = dayName + " (" + date.format(CHART_DATE_FORMAT) + ")" + todayTag + "\n" + val + " phút (" + detail + " tập trung)";
                        Tooltip tt = new Tooltip(tipText);
                        tt.setStyle("-fx-font-size: 12px; -fx-background-color: #0b132b; -fx-text-fill: #10b981; -fx-font-weight: bold; -fx-border-color: #10b981; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8px 12px;");
                        Tooltip.install(newNode, tt);
                    } else {
                        int pct = Math.round((float) val / DAILY_WATER_GOAL_ML * 100);
                        String tipText = dayName + " (" + date.format(CHART_DATE_FORMAT) + ")" + todayTag + "\n" + String.format("%,d ml nước (%d%% mục tiêu)", val, pct);
                        Tooltip tt = new Tooltip(tipText);
                        tt.setStyle("-fx-font-size: 12px; -fx-background-color: #0b132b; -fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-border-color: #38bdf8; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8px 12px;");
                        Tooltip.install(newNode, tt);
                    }
                }
            });

            series.getData().add(data);
        }
        chart.getData().clear();
        chart.getData().add(series);
        chart.setCategoryGap(24.0);
        chart.setBarGap(4.0);
        if (xAxis != null) xAxis.setAutoRanging(true);
    }

    private void runDatabaseWrite(Runnable action, String errMsg) {
        if (disposed) return;
        databaseExecutor.execute(() -> {
            try {
                action.run();
                Platform.runLater(() -> {
                    if (!disposed) refreshDashboard();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (!disposed) showError(errMsg + "\n" + e.getMessage());
                });
            }
        });
    }

    @FXML
    private void logout(ActionEvent event) {
        disposed = true;
        stopTimerTimeline();
        databaseExecutor.shutdownNow();
        UserSession.logout();
        ChangeFXML.changeFXML(event, "/view/login.fxml");
    }

    private void stopTimerTimeline() {
        if (timerTimeline != null) {
            timerTimeline.stop();
            timerTimeline = null;
        }
    }

    private String formatTime(long totalSec) {
        long min = Math.max(0, totalSec) / 60;
        long sec = Math.max(0, totalSec) % 60;
        return String.format("%02d:%02d", min, sec);
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Thông báo");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInformation(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Thông báo");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private enum TimerMode { IDLE, WORK, BREAK }
}
