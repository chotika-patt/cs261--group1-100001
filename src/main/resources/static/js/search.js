const searchInput = document.getElementById("search-input");
const searchResults = document.getElementById("search-results");
const searchClose = document.getElementById("search-close");

// ====== Event: Enter to search ======
searchInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
        e.preventDefault();
        doSearch();
    }
});

// ====== Event: Clear search ======
searchClose.addEventListener("click", () => {
    searchInput.value = "";
    searchResults.innerHTML = "";
});

// ====== Main search function ======
async function doSearch() {
    const query = searchInput.value.trim();
    searchResults.innerHTML = "<p>กำลังค้นหา...</p>";

    if (!query) {
        searchResults.innerHTML = "<p>กรุณาพิมพ์คำที่ต้องการค้นหา</p>";
        return;
    }

    try {
        const res = await fetch("/api/search", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                name: query,
                category: null,
                status: null,
                minPrice: null,
                maxPrice: null
            })
        });

        let data;
        try {
            data = await res.json();
        } catch {
            throw new Error("Invalid server response");
        }

        if (data.message) {
            searchResults.innerHTML = `<p>${data.message}</p>`;
        } else if (Array.isArray(data) && data.length > 0) {
            render(data);
        } else {
            searchResults.innerHTML = "<p>ไม่พบสินค้า</p>";
        }

    } catch (err) {
        console.error("Search error:", err);
        searchResults.innerHTML = "<p>เกิดข้อผิดพลาดในการค้นหา</p>";
    }
}

// ====== Render results ======
function render(products) {
    searchResults.innerHTML = "";
    products.forEach(p => {
        const div = document.createElement("div");
        div.classList.add("product-item");
        div.innerHTML = `
            <h3>${p.name}</h3>
            <p>หมวดหมู่: ${p.category || "-"}</p>
            <p>ราคา: ${p.price || "-"}</p>
            <p>สถานะ: ${p.status || "-"}</p>
        `;
        searchResults.appendChild(div);
    });
}