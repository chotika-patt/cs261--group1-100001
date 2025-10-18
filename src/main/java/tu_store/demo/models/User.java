package tu_store.demo.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 20, nullable = true)
    private String phone;

    @Column(length = 20, nullable = true)
    private String studentID;  // เฉพาะ seller

    @Column(length = 255, nullable = true)
    private String verify_document;  // path หรือ URL ของไฟล์ยืนยัน (.jpg / .pdf)

    @Column(nullable = true)
    private Boolean verified;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;  // client, seller, admin

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ----- Constructors -----
    public User() {}

    public User(String username, String email, String password, String phone,
                String studentID, String verify_document, UserRole role,  Boolean verified) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.studentID = studentID;
        this.verify_document = verify_document;
        this.role = role;
        this.verified = verified;
    }

    // ----- Getters & Setters -----
    public Long getUser_id() { return userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStudentID() { return studentID; }
    public void setStudentID(String studentID) { this.studentID = studentID; }

    public String getVerify_document() { return verify_document; }
    public void setVerify_document(String verify_document) { this.verify_document = verify_document; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
}