document.addEventListener('DOMContentLoaded', async function() {
    // 1. Obtenemos el token tal y como lo guardas en tu login.html
    const token = localStorage.getItem('token');

    if (!token) {
        // Si no hay token en absoluto, redirigimos al login
        window.location.href = 'login.html';
        return;
    }

    try {
        // 2. Hacemos la llamada al endpoint que tienes en AuthController.java
        const response = await fetch('/auth/me', {
            headers: { 'Authorization': token }
        });

        if (response.ok) {
            // 3. El backend nos devuelve la entidad User de Java en formato JSON
            const user = await response.json();
            
            // 4. Rellenamos el HTML con los atributos reales de tu User.java
            document.getElementById('perfil-nombre').textContent = user.nombre || 'No disponible';
            document.getElementById('perfil-apellidos').textContent = user.apellidos || 'No disponible';
            document.getElementById('perfil-email').textContent = user.email || 'No disponible';
            document.getElementById('perfil-rol').textContent = user.role || 'No disponible';
        } else {
            // Si el token es inválido o ha expirado, forzamos cierre de sesión
            cerrarSesion(); 
        }
    } catch (e) { 
        console.error("Error al obtener los datos del perfil", e); 
        cerrarSesion();
    }
});