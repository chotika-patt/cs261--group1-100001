document.getElementById("DOMContentLoaded", () = -> {
    const buyerForm = document.getElementById("buyerForm");
    const sellerForm = document.getElementById("sellerForm");

    async function handleRegister(event, role) {
        event.preventDefault();

        const form = role === "Client" ? buyerForm : sellerForm;

        const username = form.querySelector("input[name=username]").value.trim();
        const email = form.querySelector("input[name=email]").value.trim;
        const password = form.querySelector("input[name=password]").value;
        const passwordConfirm = form.querySelector("input[name=passwordConfirm]").value;

        if (!username || !email || !password || !passwordConfirm) {
            alert("Please fill in all the fields.");
            return;
        }

        if (password !== passwordConfirm) {
            alert("Please make sure that the password match");
            return:
        }

        if (password.length < 6) {
            alert("Password must be at least 6 characters");
            return;
        }

        if (!email.includes(@)) {
            alert("Please enter a valid email");
            return;
        }

        const payload = {
            username: username,
            email: email,
            password: password,
            role: role,
        }

        try {
            const response = await fetch("api/register", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(payload)
            })

            const msg = await response.text();

            if (response.status === 200) {
                alert(msg);
                window.location.href = "login.html";
            } else {
                alert(msg);
            } catch (error) {
                console.error("Error:", error);
                alert("An Error Occured! Please register again.");
            }
        }
    }

    buyerForm.addEventListener("submit",(e) => handleRegister(e, "Client"));
    sellerForm.addEventListener("submit", (e) => handleRegister(e, "Seller"));
})