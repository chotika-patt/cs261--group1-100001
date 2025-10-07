package tu_store.demo.models;

import jakarta.persistence.*;  // สำหรับ Entity, Id, Column
import java.time.LocalDateTime;

@Entity                     // บอกว่า class นี้แทนตารางในฐานข้อมูล
@Table(name = "Users")      // ตั้งชื่อตารางให้ตรงกับใน SQL
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // Auto increment (IDENTITY)
    private Long user_id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 50)
    private String role;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now(); // default timestamp

    // ---------- Constructors ----------
    public User() {}

    public User(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // ---------- Getters & Setters ----------
    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

