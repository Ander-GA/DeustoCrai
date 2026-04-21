document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    cargarLibros();

    // Evento para el formulario de añadir libro
    document.getElementById('form-nuevo-libro').addEventListener('submit', function(e) {
        e.preventDefault(); // Evitamos que la página se recargue
        anadirLibro();
    });
});

// 1. Obtener y mostrar todos los libros
async function cargarLibros() {
    const tbody = document.getElementById('tbody-libros');
    const token = localStorage.getItem('token');

    try {
        // AJUSTA ESTA RUTA según tu LibroController.java
        const response = await fetch('/api/libros', {
            method: 'GET',
            headers: { 'Authorization': token }
        });

        if (response.ok) {
            const libros = await response.json();
            pintarTabla(libros);
        } else {
            tbody.innerHTML = '<tr><td colspan="6">Error al cargar el inventario.</td></tr>';
        }
    } catch (error) {
        console.error("Error al cargar libros:", error);
    }
}

function pintarTabla(libros) {
    const tbody = document.getElementById('tbody-libros');
    tbody.innerHTML = '';

    if (libros.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6">No hay libros en el catálogo.</td></tr>';
        return;
    }

    libros.forEach(libro => {
        const estado = libro.disponible 
            ? '<span class="badge-disponible">Disponible</span>' 
            : '<span class="badge-prestado">Prestado/No disponible</span>';

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${libro.id}</td>
            <td><strong>${libro.titulo || libro.nombre}</strong></td>
            <td>${libro.autor || '-'}</td>
            <td>${libro.isbn || '-'}</td>
            <td>${estado}</td>
            <td>
                <button class="btn-borrar" onclick="borrarLibro(${libro.id})">Borrar</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// 2. Función para añadir un libro
async function anadirLibro() {
    const token = localStorage.getItem('token');
    const titulo = document.getElementById('input-titulo').value;
    const autor = document.getElementById('input-autor').value;
    const isbn = document.getElementById('input-isbn').value;

    const nuevoLibro = {
        titulo: titulo, // O "nombre" dependiendo de cómo se llame en tu AbstractRecurso.java
        autor: autor,
        isbn: isbn,
        disponible: true
    };

    try {
        // AJUSTA ESTA RUTA según tu LibroController.java
        const response = await fetch('/api/libros', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': token 
            },
            body: JSON.stringify(nuevoLibro)
        });

        if (response.ok) {
            alert("Libro añadido correctamente.");
            document.getElementById('form-nuevo-libro').reset(); // Limpiar el formulario
            cargarLibros(); // Recargar la tabla
        } else {
            alert("Hubo un error al añadir el libro.");
        }
    } catch (error) {
        console.error("Error al añadir libro:", error);
    }
}

// 3. Función para borrar un libro
async function borrarLibro(id) {
    if (!confirm("¿Estás seguro de que quieres eliminar este libro del catálogo?")) return;

    const token = localStorage.getItem('token');

    try {
        // AJUSTA ESTA RUTA según tu LibroController.java
        const response = await fetch(`/api/libros/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': token }
        });

        if (response.ok) {
            cargarLibros(); // Recargar la tabla
        } else {
            alert("No se pudo borrar el libro. (Puede que esté asociado a un préstamo activo).");
        }
    } catch (error) {
        console.error("Error al borrar libro:", error);
    }
}