# CodeMate

Ứng dụng desktop Java 17 dùng JavaFX/FXML, Hibernate/JPA và MySQL. Ứng dụng không cần web server.

## Chuẩn bị MySQL

1. Chạy `database/schema.sql` trong MySQL để tạo database `codemate` và các bảng.
2. Tạo một tài khoản MySQL cục bộ có quyền đọc/ghi trên database `codemate`.
3. Đặt các biến môi trường kết nối trong PowerShell trước khi chạy:

```powershell
$env:CODEMATE_DB_URL = 'jdbc:mysql://localhost:3306/codemate?useSSL=false&serverTimezone=Asia/Bangkok&characterEncoding=UTF-8'
$env:CODEMATE_DB_USER = 'ten_tai_khoan_mysql'
$env:CODEMATE_DB_PASSWORD = 'mat_khau_mysql_cua_ban'
```

`CODEMATE_DB_URL` có giá trị mặc định như ví dụ trên nếu không đặt. Hai biến `CODEMATE_DB_USER` và `CODEMATE_DB_PASSWORD` là bắt buộc. `persistence.xml` dùng chế độ `validate`, vì vậy cần chạy script SQL trước khi mở ứng dụng.

## Email OTP (tùy chọn)

Đăng nhập, đăng ký và các tính năng Pomodoro không cần cấu hình email. Nếu muốn dùng chức năng quên mật khẩu, đặt tài khoản SMTP Gmail và mật khẩu ứng dụng riêng trong biến môi trường cục bộ:

```powershell
$env:CODEMATE_MAIL_USERNAME = 'dia_chi_email_cua_ban'
$env:CODEMATE_MAIL_PASSWORD = 'mat_khau_ung_dung_email_cua_ban'
```

Không đưa các giá trị này vào Git hoặc chia sẻ chúng trong báo cáo.

## Chạy ứng dụng

Trong thư mục `CodeMate`, chạy:

```powershell
   .\mvnw.cmd javafx:run
```

Giao diện đăng nhập hiện có mở trước; đăng nhập thành công sẽ chuyển sang màn hình Pomodoro, công việc, nước uống và thống kê.
