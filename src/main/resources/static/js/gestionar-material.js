document.addEventListener('DOMContentLoaded', async function() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }
    cargarPrestamosMaterial();
});

async function cargarPrestamosMaterial() {
    const token = localStorage.getItem('token');
    const tbody = document.getElementById('tbody-prestamos-material');

    try {
        const response = await fetch('/api/prestamos/todos', {
            method: 'GET',
            headers: { 'Authorization': token }
        });

        if (response.ok) {
            const prestamos = await response.json();

            // Filtramos SOLO préstamos de material (material !== null)
            const soloMaterial = prestamos.filter(p => p.material != null);

            pintarTablaMaterial(soloMaterial);
        } else if (response.status === 403) {
            alert("Acceso denegado. No tienes permisos de Bibliotecario.");
            window.location.href = 'index.html';
        } else {
            tbody.innerHTML = '<tr><td colspan="7">Error al cargar los datos</td></tr>';
        }
    } catch (error) {
        console.error("Error de conexión:", error);
        tbody.innerHTML = '<tr><td colspan="7">Error de conexión con el servidor</td></tr>';
    }
}

function pintarTablaMaterial(prestamos) {
    const tbody = document.getElementById('tbody-prestamos-material');
    tbody.innerHTML = '';

    if (prestamos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:#888;">No hay préstamos de material registrados.</td></tr>';
        return;
    }

    prestamos.forEach(p => {
        // Nombre del material con protección ante nulls
        let nombreMaterial = "Desconocido";
        if (p.material) {
            nombreMaterial = p.material.nombre || p.material.titulo || "Sin nombre";
        } else if (p.nombreRecursoHistorico) {
            nombreMaterial = p.nombreRecursoHistorico + " (Eliminado)";
        }

        const estadoActual = p.estado || 'PENDIENTE_ENTREGA';
        let claseEstado = '';
        let botonesAccion = '';

        if (estadoActual === 'PENDIENTE_ENTREGA') {
            claseEstado = 'estado-pendiente';
            botonesAccion = `<button class="btn-action btn-entregar" onclick="cambiarEstadoMaterial(${p.id}, 'ENTREGADO')">Dar al Estudiante</button>`;
        } else if (estadoActual === 'ENTREGADO') {
            claseEstado = 'estado-entregado';
            botonesAccion = `<button class="btn-action btn-devolver" onclick="cambiarEstadoMaterial(${p.id}, 'DEVUELTO')">Marcar Devuelto</button>`;
        } else if (estadoActual === 'DEVUELTO') {
            claseEstado = 'estado-devuelto';
            botonesAccion = `<em>Finalizado</em>`;
        }

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${p.id}</td>
            <td>${p.usuario?.email || p.usuario?.nombre || '—'}</td>
            <td>${nombreMaterial}</td>
            <td>${p.fechaPrestamo || '—'}</td>
            <td>${p.fechaDevolucionPrevista || '—'}</td>
            <td class="${claseEstado}">${estadoActual.replace('_', ' ')}</td>
            <td>${botonesAccion}</td>
        `;
        tbody.appendChild(tr);
    });
}

async function cambiarEstadoMaterial(prestamoId, nuevoEstado) {
    const token = localStorage.getItem('token');
    if (!confirm(`¿Seguro que quieres marcar este préstamo como "${nuevoEstado.replace('_', ' ')}"?`)) return;

    try {
        const response = await fetch(`/api/prestamos/${prestamoId}/estado?nuevoEstado=${nuevoEstado}`, {
            method: 'PUT',
            headers: { 'Authorization': token }
        });

        if (response.ok) {
            cargarPrestamosMaterial(); // Recarga la tabla sin hacer location.reload()
        } else {
            alert("Hubo un error al actualizar el estado. Código: " + response.status);
        }
    } catch (error) {
        console.error("Error al cambiar estado:", error);
        alert("Error de conexión al actualizar el estado.");
    }
}