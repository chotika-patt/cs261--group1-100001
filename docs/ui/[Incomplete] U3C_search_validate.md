


# 🧩 U3C - Search Page : Input Validation Documentation


## 🎯 Objective
อธิบายระบบ **Input Validation** ของหน้า “ค้นหาสินค้า” เพื่อป้องกันข้อมูลผิดพลาด  
เช่น ราคาติดลบ, minPrice > maxPrice, คะแนนรีวิวเกิน 5 และช่วยให้ผู้ใช้เข้าใจข้อผิดพลาดด้วยข้อความเตือนที่ชัดเจน

---

## 📍 Scope
- SearchBar (ช่อง keyword)  
- FilterPanel (หมวดหมู่, ราคา, คะแนน, สต็อก)  
- ปุ่ม Apply / Search  
- Inline Error Message  
- Banner Error (API Response)

---

## ⚙️ Validation Rules

| ฟิลด์ | เงื่อนไข | พฤติกรรมเมื่อไม่ผ่าน |
|:--|:--|:--|
| `q` | ห้ามว่างหรือช่องว่างล้วน | แสดง “กรุณากรอกคำค้นก่อนกดค้นหา” |
| `minPrice` | ≥ 0 | “ราคาต้องเป็นค่าบวก” |
| `maxPrice` | ≥ 0 และ ≥ minPrice | “ราคาต่ำสุดต้องน้อยกว่าราคาสูงสุด” |
| `ratingGte` | 0 ≤ rating ≤ 5 | “คะแนนรีวิวควรอยู่ระหว่าง 0–5” |
| `page`, `pageSize` | page ≥ 1, pageSize ≤ 100 | reset เป็นค่า default |
| `category` | string ถูกต้องตามรายการ | reset เป็น “ทั้งหมด” หากไม่ถูกต้อง |

---

## 🧠 UX Behavior

| สถานะ | การแสดงผล |
|:--|:--|
| ✅ Valid | ปุ่ม Apply / Search active |
| ⚠️ Invalid | ช่อง input border แดง + ข้อความเตือน |
| ⛔ API Error | Banner สีแดง + ปุ่ม Retry |
| 🏷 Empty | “ไม่พบสินค้า” + ปุ่ม “ล้างตัวกรอง” |

---

## 💬 Error Message Design

| สถานการณ์ | ข้อความ | Style |
|:--|:--|:--|
| minPrice > maxPrice | “กรุณากรอกราคาให้ถูกต้อง (ราคาต่ำสุดต้องน้อยกว่าราคาสูงสุด)” | สีแดง #C32032 |
| rating > 5 | “คะแนนรีวิวควรอยู่ระหว่าง 0–5” | สีแดง |
| keyword ว่าง | “กรุณากรอกคำค้นก่อนกดค้นหา” | สีแดง |
| API 400 | “รูปแบบข้อมูลไม่ถูกต้อง กรุณาลองใหม่อีกครั้ง” | Banner แดง |
| API 500 | “ไม่สามารถค้นหาได้ในขณะนี้ กรุณาลองใหม่อีกครั้ง” | Banner แดง + Retry |

---

## 🔄 Functional Flow

```text
1. ผู้ใช้กรอกข้อมูลในช่อง SearchBar / FilterPanel
2. เมื่อกด Enter หรือ Apply → เรียก validateInput()
3. ถ้าข้อมูลผิด → แสดงข้อความเตือน และไม่ยิง API
4. ถ้าผ่าน → เรียก API /api/products/search
5. หาก API error → แสดง Banner สีแดงด้านบน
