# ========================
# GIAI ĐOẠN BUILD (sử dụng Maven)
# ========================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy toàn bộ project vào container build
COPY . .

# Build ứng dụng (bỏ qua test để tiết kiệm thời gian)
RUN mvn clean package -DskipTests

# ========================
# GIAI ĐOẠN RUN (sử dụng JDK nhẹ)
# ========================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy file jar từ container build
COPY --from=build /app/target/*.jar app.jar

# Render sẽ tự inject PORT vào biến môi trường
EXPOSE 8080

# Lệnh khởi động ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
