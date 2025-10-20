# ใช้ base image ของ JDK
FROM openjdk:17-jdk-slim

# ตั้ง working directory ใน container
WORKDIR /app

# คัดลอกไฟล์ .jar ที่ build แล้วเข้า container
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar
# เปิด port ให้เข้าถึงจากภายนอก
EXPOSE 8080

# คำสั่งเริ่มโปรแกรม
ENTRYPOINT ["java", "-jar", "app.jar"]