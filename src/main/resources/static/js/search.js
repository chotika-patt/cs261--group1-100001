document.getElementById("search-input").addEventListener("keydown", (e) => {
  if (e.key === "Enter") {
    e.preventDefault();
    searchProducts();
  }
})
  async function searchProducts() {
    const keyword = document.getElementById("search-input").value.trim();
    const resultDiv = document.getElementById("search-results");
    resultDiv.innerHTML = "<p>กำลังค้นหา...</p>";

    // ถ้าไม่มีการกรอก ไม่ต้องค้นหา
    if (!keyword) {
      resultDiv.innerHTML = "<p>กรุณาพิมพ์คำที่ต้องการค้นหา</p>";
      return;
    }

    try {
      const response = await fetch("/api/search", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        // ส่งเฉพาะชื่อสินค้า (name) ส่วนอื่น null ก็ได้
        body: JSON.stringify({
          name: keyword,
          category: null,
          status: null,
          minPrice: null,
          maxPrice: null
        })
      });

      const data = await response.json();
      //ด้านจะเป็นส่วนเกี่ยวกับการ return ซึ้งยังเป็นแค่ตัวอย่าง
      // ถ้ามี message แปลว่าไม่เจอสินค้า
      if (data.message) {
        resultDiv.innerHTML = `<p>${data.message}</p>`;
      } else {
        renderResults(data);
      }
    } catch (error) {
      console.error("Error:", error);
      resultDiv.innerHTML = "<p>เกิดข้อผิดพลาดในการค้นหา</p>";
    }
  }
 /*
   function renderResults(products) {
    const resultDiv = document.getElementById("search-results");
    resultDiv.innerHTML = "";

    products.forEach(p => {
      const item = document.createElement("div");
      item.classList.add("product-item");
      item.innerHTML = `
        <h3>${p.name}</h3>
        <p>หมวดหมู่: ${p.category || "-"}</p>
        <p>ราคา: ${p.price || "-"}</p>
        <p>สถานะ: ${p.status || "-"}</p>
      `;
      resultDiv.appendChild(item);
    });
  }
  const input = document.getElementById("search-input");
  document.getElementById("search-close").addEventListener("click",()=>{
    input.value = '';
  })
  
*/
  
const closeSearch = document.getElementById('search-close');
const inputSearch = document.getElementById('search-input');

closeSearch.addEventListener("click",()=>{
    inputSearch.value="";
})
