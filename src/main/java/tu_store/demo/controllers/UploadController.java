package tu_store.demo.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tu_store.demo.models.User;
import tu_store.demo.models.UserRole;
import tu_store.demo.repositories.UserRepository;

import java.io.IOException;
import java.nio.file.*;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${file.upload-dir}")
    private String uploadDir;  // ที่เก็บไฟล์ (ตั้งค่าใน application.properties)

    private final UserRepository userRepository;

    public UploadController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ Upload เอกสารยืนยัน เฉพาะ Seller
    @PostMapping("/seller/{id}")
    public ResponseEntity<String> uploadSellerDocument(
            @PathVariable Long id,
            @RequestParam("application_document") MultipartFile file) {

        try {
            // 🧩 1. ค้นหา user จาก id
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 🧩 2. ตรวจสอบว่าเป็น seller เท่านั้นที่อัปโหลดได้
            if (user.getRole() != UserRole.SELLER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Only sellers can upload verification documents.");
            }

            // 🧩 3. ตรวจสอบประเภทไฟล์
            String fileName = file.getOriginalFilename();
            if (fileName == null || 
                !(fileName.endsWith(".pdf") || fileName.endsWith(".jpg") || fileName.endsWith(".png"))) {
                return ResponseEntity.badRequest().body("Invalid file type. Allowed: PDF, JPG, PNG.");
            }

            // 🧩 4. สร้างโฟลเดอร์ถ้ายังไม่มี
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 🧩 5. ตั้งชื่อไฟล์ไม่ซ้ำ (เช่น seller123.pdf)
            String newFileName = "seller_" + user.getUser_id() + "_" + fileName;
            Path filePath = uploadPath.resolve(newFileName);

            // 🧩 6. บันทึกไฟล์ลงโฟลเดอร์
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            // 🧩 7. บันทึก path ลงในฐานข้อมูล
            user.setVerify_document(filePath.toString());
            userRepository.save(user);

            return ResponseEntity.ok("Upload success: " + newFileName);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }
}
