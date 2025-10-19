const popup = document.getElementById("add-product-popup");
const openBtn = document.querySelector(".add-product-btn");
const closeBtn = document.getElementById("close-popup");
const form = document.getElementById("add-product-form");

openBtn.addEventListener("click", () => popup.classList.add("active"));
closeBtn.addEventListener("click", () => popup.classList.remove("active"));

form.addEventListener("submit", async (e) => {
  e.preventDefault();

  const formData = new FormData();
  formData.append("name", document.getElementById("product-name").value);
  formData.append("category", document.getElementById("product-category").value);
  formData.append("price", document.getElementById("product-price").value);
  formData.append("stock", document.getElementById("product-stock").value);
  formData.append("description", document.getElementById("product-description").value);

  const fileInput = document.getElementById("product-image");
  if(fileInput.files.length > 0){
    formData.append("main_image", fileInput.files[0]);
  }

  try {
    const res = await fetch("/api/add", {
      method: "POST",
      body: formData
    });

    if (res.ok) {
      alert("เพิ่มสินค้าสำเร็จ!");
      form.reset();
      location.reload();
    } else {
      const msg = await res.text();
      alert("เกิดข้อผิดพลาด: " + msg);
    }
  } catch (err) {
    console.error(err);
    alert("ไม่สามารถเชื่อมต่อกับเซิร์ฟเวอร์ได้");
  }
});