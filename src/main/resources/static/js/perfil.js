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
			
			cargarEstadisticas(token); // Cargar estadísticas de préstamos	
        } else {
            // Si el token es inválido o ha expirado, forzamos cierre de sesión
            cerrarSesion(); 
        }
    } catch (e) { 
        console.error("Error al obtener los datos del perfil", e); 
        cerrarSesion();
    }
});

async function cargarEstadisticas(token) {
    try {
        const response = await fetch('/api/prestamos/mis-estadisticas', {
            headers: { 'Authorization': token }
        });
        if (response.ok) {
            const stats = await response.json();
            document.getElementById('stat-total').textContent = stats.totalPrestamos || 0;
            document.getElementById('stat-activos').textContent = stats.prestamosActivos || 0;
            document.getElementById('stat-tiempo').textContent = stats.devueltosATiempo || 0;
            document.getElementById('stat-retraso').textContent = stats.devueltosConRetraso || 0;
        }
    } catch (e) {
        console.error("Error al cargar estadísticas", e);
    }
}