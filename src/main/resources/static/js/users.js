document.addEventListener('DOMContentLoaded', async () => {
    const userList = document.getElementById('user-list');

    try {
        const response = await fetch('http://localhost:8080/api/users');
        
        if (response.ok) {
            const users = await response.json();
            users.forEach(user => {
                const listItem = document.createElement('li');
                listItem.textContent = user.nome;
                userList.appendChild(listItem);
            });
        } else {
            console.error('Failed to fetch users');
        }
    } catch (error) {
        console.error('An error occurred:', error);
    }
});
