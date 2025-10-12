document.addEventListener("DOMContentLoaded", async () => {
    const buyerForm = document.getElementById("buyerForm");
    const sellerForm = document.getElementById("sellerForm");
    const buyerSubmit = document.getElementById("buyerSubmit");
    const sellerSubmit = document.getElementById("sellerSubmit");

    const csrfToken = await getCsrfToken();

    if (buyerSubmit) {
        buyerSubmit.addEventListener("click", async (event) => {
            event.preventDefault();
            await handleBuyerRegister(buyerForm, "/api/register/buyer", csrfToken);
        });
    }

    if (sellerSubmit) {
        sellerSubmit.addEventListener("click", async (event) => {
            event.preventDefault();
            await handleSellerRegister(sellerForm, "/api/register/seller", csrfToken);
        });
    }
});

async function getCsrfToken() {
    try {
        const res = await fetch("/csrf", { credentials: "include" });
        if (!res.ok) throw new Error("Failed to fetch CSRF token");
        const data = await res.json();
        return data.token;
    } catch (err) {
        console.error("CSRF fetch failed:", err);
        return null;
    }
}

async function handleBuyerRegister(form, url, csrfToken) {
    const formData = new FormData(form);
    const payload = Object.fromEntries(formData.entries());

    try {
        const res = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-CSRF-TOKEN": csrfToken
            },
            body: JSON.stringify(payload),
            credentials: "include"
        });

        if (res.ok) {
            window.location.href = "login.html";
        } else {
            alert("Registration failed.");
        }

    } catch (err) {
        console.error(err);
        alert("Unexpected error during registration.");
    }
}
