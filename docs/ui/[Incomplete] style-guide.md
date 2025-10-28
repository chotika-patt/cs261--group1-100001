# 🎨 TU Store — UI / UX Design & Style Guide  
*Version: 1.0 | Updated: 2025-10-27*

---

## 🔹 1. Brand Identity

| Item | Description |
|:------|:-------------|
| **System Name** | TU Store |
| **Mood & Tone** | Modern, Friendly, Bright, and Trustworthy |


---

## 🔹 2. Typography

| Usage | Font | Size | Weight | Line Height |
|:------|:------|:------|:------|:-------------|
| Heading 1 | IBM Plex Sans Thai |  px |  |  |
| Heading 2 | IBM Plex Sans Thai |  px |  |  |
| Heading 3 | IBM Plex Sans Thai |  px |  |  |
| Body | IBM Plex Sans Thai |  px |  |  |
| Caption / Label | IBM Plex Sans Thai |  px |  |  |

**Notes:**
- ใช้หน่วย `rem` หรือ `%` สำหรับ responsive font  
- สีข้อความหลัก: #ใส่สีด้วยคับ / สีรอง: #ใส่สีด้วยคับ

---

## 🔹 3. Color Palette

| Category | Color Name | Hex | Usage |
|:----------|:------------|:------|:---------|
| Primary | TU Gold | #ใส่สีด้วยคับ | ปุ่มหลัก / highlight |
| Secondary | TU Red | #ใส่สีด้วยคับ | ปุ่มรอง / Error State |
| Accent | TU Blue | #ใส่สีด้วยคับ | ลิงก์ / focus outline |
| Neutral Dark | Text Primary | #ใส่สีด้วยคับ | ข้อความหลัก |
| Neutral Light | Background | #ใส่สีด้วยคับ | พื้นหลังรอง |
| Success | Green | #ใส่สีด้วยคับ | แจ้งเตือนสำเร็จ |
| Error | Red |#ใส่สีด้วยคับ | แจ้งเตือนข้อผิดพลาด |

---

## 🔹 4. Grid & Spacing

| Item | Specification |
|:------|:---------------|
| **Grid System** | 12 คอลัมน์ |
| **Gutter Width** | 16 px (mobile) → 24 px (desktop) |
| **Container Max Width** | 1200 px |
| **Spacing Scale (px)** | 4, 8, 12, 16, 24, 32, 48 |
| **Section Padding** | 64 px top-bottom |
| **Card Padding** | 16 px inside |

---

## 🔹 5. Components Guideline

| Component | Description | Key Properties |
|:-----------|:-------------|:----------------|
| **Navbar** | มีโลโก้ TU ตรงกลาง, เมนูขวา (Login, Cart) | ความสูง 72 px, พื้นหลังขาว, เงา shadow-sm |
| **Button** | ใช้สี Primary / Secondary ตามประเภท | Radius 8 px, Hover เข้มขึ้น 10%, Disabled opacity 0.5 |
| **Search Bar** | กล่องโค้ง 12 px, ไอคอนด้านขวา | Transition 0.3s, มี placeholder |
| **Filter Panel** | มี dropdown + ช่องกรอกเลข + ปุ่ม Apply | ช่องห่าง 12 px, background เทาอ่อน |
| **Product Card** | แสดงรูป, ชื่อ, ราคา, สต็อก, คะแนน | Shadow-sm, Radius 12 px, Hover scale 1.02 |
| **Modal** | Overlay 50%, Center screen | Close icon มุมขวาบน, ปุ่มยืนยัน-ยกเลิก |

---

## 🔹 6. Responsive Breakpoints

| Device | Width Range | Layout Behavior |
|:--------|:--------------|:----------------|
| Mobile | ≤ 640 px | 1 คอลัมน์, ฟิลเตอร์เป็น Drawer |
| Tablet | 641–1024 px | 2–3 คอลัมน์, Navbar ซ่อนโลโก้ |
| Desktop | ≥ 1025 px | 4 คอลัมน์, Full Navigation Bar |

---

## 🔹 7. Interaction & Animation

| State | Behavior | Transition |
|:--------|:-----------|:-------------|
| **Hover** | สีพื้นเข้มขึ้น 10% หรือ scale 1.02 | 0.3s ease-in-out |
| **Focus** | เส้นขอบ 2px สี `#005BBB` | ทันที |
| **Active / Click** | ลด opacity ลง 0.8 | 0.1s |
| **Disabled** | Opacity 0.5, cursor not-allowed | ไม่มี |
| **Loading** | ใช้ skeleton หรือ spinner สีเทาอ่อน | 0.2s fade-in |

---

## 🔹 8. UX Principles

| Principle | Guideline |
|:------------|:------------|
| **Consistency** | ปุ่ม, สี, ฟอนต์เหมือนกันทุกหน้า |
| **Clarity** | ใช้คำที่สั้น เข้าใจง่าย เช่น “ค้นหา”, “เพิ่มลงตะกร้า” |
| **Feedback** | ทุกการกดมีผลตอบสนอง เช่น toast / highlight |
| **Accessibility** | รองรับคีย์บอร์ด, aria-label, focus outline |
| **Responsiveness** | ทุกหน้าต้องปรับตามขนาดจอได้ |
| **Error Handling** | แสดงข้อความเตือนแบบ inline และชัดเจน |

---

## 🔹 9. Iconography & Imagery

| Item | Specification |
|:------|:---------------|
| **Icon Set** | Material Symbols Outlined / FontAwesome 6 |
| **Icon Size** | 24 px (ทั่วไป), 32 px (desktop) |
| **Icon Color** | `#1D1D1F` หรือเทา `#777777` |
| **Illustration Style** | Flat / Minimal / สีตาม palette |
| **Image Shape** | สี่เหลี่ยมมุมโค้ง 12 px, object-fit: cover |

---

## 🔹 10. Example Layout Reference

| Page | Layout Description |
|:------|:-------------------|
| **Login Page** | Logo กลางจอ, ฟอร์มตรงกลาง, ปุ่มสีทอง |
| **Search Page** | Search bar ด้านบน, Filter ซ้าย, Product Grid ขวา |
| **Product Detail** | รูปสินค้าใหญ่ซ้าย, ข้อมูลขวา, ปุ่มเพิ่มตะกร้าด้านล่าง |
| **Cart Page** | ตารางสินค้า + ปุ่ม Checkout สีทอง |
| **Error Page (404)** | ภาพประกอบ + ปุ่ม “กลับหน้าหลัก” |

---

## 🔹 11. References

| Type | Description | Link |
|:------|:-------------|:------|
| **Figma File** | TU Store UI Kit Prototype | [Figma Link (Internal)](https://www.figma.com/file/xxxxx/TU-Store-UI-Kit) |
| **Font** | IBM Plex Sans Thai | [Google Fonts](https://fonts.google.com/specimen/IBM+Plex+Sans+Thai) |
| **Icon Library** | Material Symbols Outlined | [Material Icons](https://fonts.google.com/icons) |

---

*End of Style Guide*
