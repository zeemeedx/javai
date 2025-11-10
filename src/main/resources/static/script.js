const form = document.getElementById('login-form');
const message = document.getElementById('message');

form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const email = form.email.value;
    const password = form.password.value;

    try {
        const response = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, senha: password })
        });

        if (response.ok) {
            message.textContent = 'Login successful!';
            message.style.color = 'green';
        } else {
            message.textContent = 'Login failed. Please check your credentials.';
            message.style.color = 'red';
        }
    } catch (error) {
        message.textContent = 'An error occurred. Please try again later.';
        message.style.color = 'red';
    }
});
