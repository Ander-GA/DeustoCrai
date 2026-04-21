document.addEventListener('DOMContentLoaded', async function() {
    const token = localStorage.getItem('token');
    
    // Si no hay token, fuera
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    // Cargar los datos nada más entrar a la página
    cargarPrestamos();
});

// Función para obtener y pintar todos los préstamos
async function cargarPrestamos() {
    const token = localStorage.getItem('token');
    const tbody = document.getElementById('tbody-prestamos');

    try {
        const response = await fetch('/api/prestamos/todos', {
            method: 'GET',
            headers: { 'Authorization': token }
        });

        if (response.ok) {
            const prestamos = await response.json();
            pintarTabla(prestamos);
        } else if (response.status === 403) {
            // Si el backend responde 403 Forbidden, no es bibliotecario
            alert("Acceso denegado. No tienes permisos de Bibliotecario.");
            window.location.href = 'index.html';
        } else {
            tbody.innerHTML = '<tr><td colspan="6">Error al cargar los datos</td></tr>';
        }
    } catch (error) {
        console.error("Error de conexión:", error);
        tbody.innerHTML = '<tr><td colspan="6">Error de conexión con el servidor</td></tr>';
    }
}

// Función para inyectar el HTML en la tabla
function pintarTabla(prestamos) {
    const tbody = document.getElementById('tbody-prestamos');
    tbody.innerHTML = ''; // Limpiamos el mensaje de "Cargando..."

    if (prestamos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6">No hay préstamos registrados en el sistema.</td></tr>';
        return;
    }

    prestamos.forEach(p => {
        
        // --- NUEVA LÓGICA DE HISTÓRICO ---
        // Si p.recurso no es null (el libro existe), leemos el objeto real. 
        // Si es null (fue borrado), usamos el histórico.
        let nombreRecurso = "Desconocido";
        if (p.recurso) {
            nombreRecurso = p.recurso.titulo ? p.recurso.titulo : p.recurso.nombre;
        } else {
            nombreRecurso = p.nombreRecursoHistorico + " (Eliminado)";
        }
        
        let claseEstado = '';
        let botonesAccion = '';

        // Por seguridad, aseguramos que el estado existe (por si había datos antiguos en tu base de datos)
        const estadoActual = p.estado || 'PENDIENTE_ENTREGA';

        if (estadoActual === 'PENDIENTE_ENTREGA') {
            claseEstado = 'estado-pendiente';
            botonesAccion = `<button class="btn-accion btn-entregar" onclick="cambiarEstado(${p.id}, 'ENTREGADO')">Dar al Estudiante</button>`;
        } else if (estadoActual === 'ENTREGADO') {
            claseEstado = 'estado-entregado';
            botonesAccion = `<button class="btn-accion btn-devolver" onclick="cambiarEstado(${p.id}, 'DEVUELTO')">Marcar Devuelto</button>`;
        } else if (estadoActual === 'DEVUELTO') {
            claseEstado = 'estado-devuelto';
            botonesAccion = `<em>Finalizado</em>`;
        }

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${p.usuario.email}</td>
            <td>${nombreRecurso}</td>
            <td>${p.fechaPrestamo}</td>
            <td>${p.fechaDevolucionPrevista}</td>
            <td class="${claseEstado}">${estadoActual.replace('_', ' ')}</td>
            <td>${botonesAccion}</td>
        `;
        tbody.appendChild(tr);
    });
}

// Función para actualizar el estado cuando el bibliotecario hace clic en un botón
async function cambiarEstado(prestamoId, nuevoEstado) {
    const token = localStorage.getItem('token');
    
    // Confirmación simple
    if (!confirm(`¿Seguro que quieres marcar este préstamo como ${nuevoEstado}?`)) return;

    try {
        const response = await fetch(`/api/prestamos/${prestamoId}/estado?nuevoEstado=${nuevoEstado}`, {
            method: 'PUT',
            headers: { 'Authorization': token }
        });

        if (response.ok) {
            // Si se actualizó bien, volvemos a cargar la tabla para ver los cambios
            cargarPrestamos();
        } else {
            alert("Hubo un error al actualizar el estado.");
        }
    } catch (error) {
        console.error("Error al cambiar estado:", error);
    }
}