document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("token");
    if (!token) {
        window.location.href = "login.html";
        return;
    }

    cargarSalas();

    document.getElementById("formBloqueo").addEventListener("submit", (e) => {
        e.preventDefault();
        enviarBloqueo();
    });
});

async function cargarSalas() {
    try {
        const response = await fetch("/api/salas", {
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
    let fechaInicio = document.getElementById("fecha-inicio").value;
    let fechaFin = document.getElementById("fecha-fin").value;
    const motivo = document.getElementById("motivo-bloqueo").value;

    if (new Date(fechaInicio) >= new Date(fechaFin)) {
        alert("La fecha de fin debe ser posterior a la fecha de inicio.");
        return;
    }

    // Comprobación de seguridad: añadimos los segundos solo si el navegador no los puso (YYYY-MM-DDThh:mm tiene longitud 16)
    if (fechaInicio.length === 16) fechaInicio += ":00";
    if (fechaFin.length === 16) fechaFin += ":00";

    const payload = {
        fechaInicio: fechaInicio, 
        fechaFin: fechaFin,
        motivo: motivo
    };

    try {
        const response = await fetch(`/api/salas/${aulaId}/bloquear`, {
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