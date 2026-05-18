document.addEventListener("DOMContentLoaded", () => {
    // Verificar si el usuario está autenticado y tiene rol adecuado (Librarian/Admin)
    const token = localStorage.getItem("token");
    if (!token) {
        window.location.href = "login.html";
        return;
    }

    cargarSalas();

    // Manejar el envío del formulario de bloqueo
    document.getElementById("formBloqueo").addEventListener("submit", (e) => {
        e.preventDefault();
        enviarBloqueo();
    });
});

async function cargarSalas() {
    try {
        // Llama al endpoint existente en tu AulaController para listar las salas
        const response = await fetch("/api/aulas", {
            headers: {
                "Authorization": localStorage.getItem("token")
            }
        });

        if (!response.ok) throw new Error("No se pudieron cargar las salas.");

        const salas = await response.json();
        const tbody = document.getElementById("lista-salas-body");
        tbody.innerHTML = "";

        salas.forEach(sala => {
            const tr = document.createElement("tr");
            tr.style.borderBottom = "1px solid #ddd";
            tr.innerHTML = `
                <td style="padding: 10px;">${sala.id}</td>
                <td style="padding: 10px; font-weight: bold;">${sala.nombre || 'Sala ' + sala.id}</td>
                <td style="padding: 10px;">${sala.capacidad || 'N/A'} personas</td>
                <td style="padding: 10px;">
                    <button class="btn-bloquear" onclick="abrirModal(${sala.id}, '${sala.nombre || 'Sala ' + sala.id}')">
                        ⚠️ Bloquear fechas
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (error) {
        console.error(error);
        alert("Error al cargar el catálogo de salas.");
    }
}

function abrirModal(id, nombre) {
    document.getElementById("bloqueo-aula-id").value = id;
    document.getElementById("modal-nombre-sala").innerText = nombre;
    document.getElementById("modalBloqueo").style.display = "block";
}

function cerrarModal() {
    document.getElementById("modalBloqueo").style.display = "none";
    document.getElementById("formBloqueo").reset();
}

async function enviarBloqueo() {
    const aulaId = document.getElementById("bloqueo-aula-id").value;
    const fechaInicio = document.getElementById("fecha-inicio").value; // Formato ISO local
    const fechaFin = document.getElementById("fecha-fin").value;
    const motivo = document.getElementById("motivo-bloqueo").value;

    if (new Date(fechaInicio) >= new Date(fechaFin)) {
        alert("La fecha de fin debe ser posterior a la fecha de inicio.");
        return;
    }

    const payload = {
        fechaInicio: fechaInicio + ":00", // Añadimos los segundos para el formato LocalDateTime de Java
        fechaFin: fechaFin + ":00",
        motivo: motivo
    };

    try {
        // Enlace directo con el endpoint @PostMapping("/{aulaId}/bloquear") del AulaController
        const response = await fetch(`/api/aulas/${aulaId}/bloquear`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": localStorage.getItem("token")
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            alert("¡Éxito! La sala ha sido bloqueada y no aceptará reservas en ese rango de tiempo.");
            cerrarModal();
        } else {
            const errorText = await response.text();
            alert("No se pudo aplicar el bloqueo: " + errorText);
        }
    } catch (error) {
        console.error("Error de red:", error);
        alert("Error de conexión con el servidor.");
    }
}