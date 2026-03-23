/*
	 This code is based on solutions provided by ChatGPT 4o and 
	 adapted using GitHub Copilot. It has been thoroughly reviewed 
	 and validated to ensure correctness and that it is free of errors.
*/
async function checkAuth() {
    const token = localStorage.getItem('token');
    if (!token) return;

    try {
        const response = await fetch('/auth/me', {
            headers: { 'Authorization': token }
        });

        if (response.ok) {
            const user = await response.json();
            
            // Si el usuario está validado, cambiamos el header
            const loginLink = document.getElementById('login-link');
            const userMenu = document.getElementById('user-menu');
            const usernameDisplay = document.getElementById('username-display');
            
            if(loginLink) loginLink.style.display = 'none';
            if(userMenu) userMenu.style.display = 'block';
            if(usernameDisplay) usernameDisplay.innerText = user.nombre || 'Usuario';

            // Mostramos los botones de accesos directos en el index.html
            const quickLinks = document.getElementById('quick-links');
            if(quickLinks) quickLinks.style.display = 'flex';
        } else {
            cerrarSesion();
        }
    } catch (e) { 
        console.error("Error de sesión", e); 
    }
}

// Abrir el menú desplegable del usuario
function toggleMenu() { 
    document.getElementById("myDropdown").classList.toggle("show"); 
}

// Cierra el menú desplegable si se hace clic fuera de él
window.onclick = function(event) {
    if (!event.target.matches('.dropbtn') && !event.target.closest('.dropbtn')) {
        let dropdowns = document.getElementsByClassName("dropdown-content");
        for (let i = 0; i < dropdowns.length; i++) {
            let openDropdown = dropdowns[i];
            if (openDropdown.classList.contains('show')) {
                openDropdown.classList.remove('show');
            }
        }
    }
}

function cerrarSesion() {
    localStorage.removeItem('token');
    window.location.href = "index.html";
}

// Inyecta el header.html de forma dinámica al cargar la página
async function init() {
    const headerPlaceholder = document.getElementById('header-placeholder');
    if(headerPlaceholder) {
        try {
            const resp = await fetch('header.html');
            if(resp.ok) {
                headerPlaceholder.innerHTML = await resp.text();
            }
        } catch (e) {
            console.error("No se pudo cargar el header", e);
        }
    }
    
    // Comprobar la sesión una vez que el header está insertado
    checkAuth();
}

window.onload = init;