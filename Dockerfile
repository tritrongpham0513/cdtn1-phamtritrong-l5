# =============================================================================
# Multi-stage Dockerfile cho Mekong Mobile Smart CRM (Module L5)
# Thành phần 4 - Mức đánh giá cao (High Distinction)
# =============================================================================

# Giai đoạn 1: Build mã nguồn với Maven
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy Maven Wrapper và cấu hình pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Tải trước các phụ thuộc (cache layer)
RUN ./mvnw dependency:go-offline -B || true

# Copy mã nguồn và thư mục dữ liệu mẫu
COPY src/ src/
COPY seeds/ seeds/

# Đóng gói file JAR (bỏ qua unit test vì test chạy độc lập ở CI)
RUN ./mvnw clean package -DskipTests

# Giai đoạn 2: Runtime image tối giản, bảo mật
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Cài đặt curl để hỗ trợ container healthcheck
RUN apk add --no-cache curl

# Tạo user không phải root để đảm bảo an toàn vận hành
RUN addgroup -S mekonggroup && adduser -S mekonguser -G mekonggroup
USER mekonguser

# Copy file JAR và thư mục dữ liệu mẫu từ stage builder
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/seeds/ ./seeds/

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=dev
ENV PORT=8080

ENTRYPOINT ["java", "-jar", "app.jar"]

