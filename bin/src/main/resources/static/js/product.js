const productContainer = document.getElementById("product");

fetch(`http://localhost:8080/api/products/${Id}`)
  .then(response => {
    if (!response.ok) throw new Error("Network response was not ok");
    return response.json();
  })
  .then(data => {
    // แสดงชื่อสินค้า
    const name = document.createElement("h2");
    name.textContent = data.name;
    productContainer.appendChild(name);

    // แสดง description
    const desc = document.createElement("p");
    desc.textContent = data.description;
    productContainer.appendChild(desc);

    // แสดงรูป
    data.imageUrls.forEach(url => {
      const img = document.createElement("img");
      img.src = url;
      img.style.width = "100px";
      productContainer.appendChild(img);
    });

    // แสดง variants
    const variantsDiv = document.createElement("div");
    data.variants.forEach(v => {
      const variant = document.createElement("p");
      variant.textContent = `Color: ${v.color}, Size: ${v.size}, Price: ${v.price}, Stock: ${v.stock}`;
      variantsDiv.appendChild(variant);
    });
    productContainer.appendChild(variantsDiv);
  })
  .catch(error => console.error("Error fetching product:", error));