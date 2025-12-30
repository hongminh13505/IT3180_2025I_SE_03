# Hệ thống Quản lý Chung cư

Ứng dụng web quản lý chung cư được xây dựng bằng Spring Boot 3.5.0, Spring Security 6, PostgreSQL và Thymeleaf.

## 1. Công nghệ sử dụng

1. **Backend**: Spring Boot 3.5.0
2. **Security**: Spring Security 6
3. **Database**: PostgreSQL
4. **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript
5. **Build Tool**: Maven
6. **ORM**: Spring Data JPA (Hibernate)

## 2. Yêu cầu hệ thống

- Java 17 hoặc cao hơn
- Maven 3.6 hoặc cao hơn
- PostgreSQL 12 hoặc cao hơn

## 3. Cài đặt

### 3.1. Clone repository

```bash
git clone <repository-url>
cd 2025-1\ CNPM
```

### 3.2. Cấu hình PostgreSQL

Tạo database mới trong PostgreSQL:

```sql
CREATE DATABASE apartment_db;
```

Chạy file SQL để tạo các bảng:

```bash
psql -U postgres -d apartment_db -f database.sql
```

### 3.3. Cấu hình application

Mở file `src/main/resources/application.yml` và cập nhật thông tin kết nối database:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/apartment_db
    username: postgres
    password: your_password
```

### 3.4. Build và chạy ứng dụng

```bash
mvn clean install
mvn spring-boot:run
```

Hoặc chạy file jar:

```bash
java -jar target/apartment-management-1.0.0.jar
```

## 4. Truy cập ứng dụng

URL: https://apartment-management-system-se03-1-wwe1.onrender.com/

## 5. Tài khoản mặc định

Sau khi chạy SQL script, cần tạo tài khoản admin đầu tiên:

```sql
-- Tạo tài khoản admin (mật khẩu: admin123)
INSERT INTO doi_tuong (cccd, mat_khau, vai_tro, la_cu_dan, ho_va_ten, ngay_sinh, trang_thai_tai_khoan) 
VALUES ('001234567890', '$2a$10$xXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXx', 'ban_quan_tri', false, 'Quản trị viên', '1990-01-01', 'hoat_dong');
```

**Lưu ý**: Mật khẩu đã được mã hóa bằng BCrypt. Bạn cần sử dụng BCrypt encoder để tạo mật khẩu mới.

## 6. Cấu trúc dự án

```
apartment-management/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/apartment/
│   │   │       ├── controller/         # Controllers
│   │   │       ├── entity/             # Entity classes
│   │   │       ├── repository/         # JPA Repositories
│   │   │       ├── security/           # Security configuration
│   │   │       ├── service/            # Service layer
│   │   │       └── ApartmentManagementApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/               # CSS files
│   │       │   └── js/                # JavaScript files
│   │       ├── templates/             # Thymeleaf templates
│   │       │   ├── admin/
│   │       │   ├── layout/
│   │       │   └── login.html
│   │       └── application.yml
│   └── test/
├── database.sql
├── pom.xml
└── README.md
```

## 7. Phân quyền

Hệ thống hỗ trợ các vai trò sau:

1. **Ban quản trị** (`ban_quan_tri`): Quản lý toàn bộ hệ thống
2. **Kế toán** (`ke_toan`): Quản lý hóa đơn, thanh toán
3. **Cơ quan chức năng** (`co_quan_chuc_nang`): Xử lý báo cáo sự cố
4. **Người dùng thường** (`nguoi_dung_thuong`): Cư dân chung cư

## 8. Chức năng chính

### 8.1. Quản lý cư dân

1. Thêm, sửa, xóa thông tin cư dân
2. Tìm kiếm cư dân
3. Quản lý trạng thái tài khoản

### 8.2. Quản lý hộ gia đình

1. Quản lý thông tin hộ gia đình
2. Gán căn hộ cho hộ gia đình
3. Theo dõi thành viên hộ

### 8.3. Quản lý hóa đơn

1. Tạo hóa đơn dịch vụ
2. Theo dõi thanh toán
3. Thống kê thu chi
4. Quản lý công nợ

### 8.4. Quản lý báo cáo sự cố

1. Tiếp nhận báo cáo sự cố
2. Phân loại mức độ ưu tiên
3. Theo dõi xử lý
4. Lưu kết quả xử lý

### 8.5. Quản lý thông báo

1. Gửi thông báo cho cư dân
2. Phân loại thông báo
3. Quản lý đối tượng nhận

### 8.6. Dashboard

1. Thống kê tổng quan
2. Biểu đồ thu chi
3. Báo cáo sự cố mới
4. Thông báo quan trọng

