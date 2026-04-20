# DeustoCraiApp — Sistema de Gestión de Biblioteca y Salas

## Descripción del Proyecto

DeustoCraiApp es una aplicación basada en Spring Boot diseñada para digitalizar la gestión de servicios en el CRAI de la Universidad de Deusto. El sistema permite la gestión integral de usuarios, el catálogo de libros, préstamos de recursos y la reserva de salas de estudio con control de disponibilidad en tiempo real.

---

## Requisitos Previos

Para construir y ejecutar este proyecto desde cero, necesitarás:

- Java JDK 21
- MySQL Server (u otro entorno compatible con JDBC)
- Gradle (incluido a través del `gradlew` wrapper)

---

## Configuración de la Base de Datos

El proyecto utiliza una base de datos MySQL llamada `deustocrai_db`.

1. Asegúrate de que tu servidor MySQL esté en ejecución.

2. Crea la base de datos y el usuario ejecutando el script proporcionado en `/sql/create-deustocrai.sql` o manualmente:
```sql
-- DB Name: deustocrai_db
-- User:    spq
-- Password: spq
```

---

## Instrucciones de Construcción y Ejecución

Sigue estos pasos para compilar y lanzar la aplicación:

### 1. Clonar y Compilar

Abre una terminal en la raíz del proyecto y ejecuta:
```bash
# En Windows
gradlew.bat build

# En Linux/macOS
./gradlew build
```

### 2. Ejecutar la Aplicación

Una vez compilado, puedes iniciar el servidor con:
```bash
# Usando Gradle directamente
./gradlew bootRun
```

La aplicación estará disponible en: http://localhost:8080

---

## Características Principales (Sprint 1)

- **Seguridad y Acceso:** Sistema de autenticación con tokens y roles (ESTUDIANTE, BIBLIOTECARIO, ADMIN).
- **Catálogo de Libros:** Búsqueda y gestión de libros por título, autor e ISBN.
- **Reservas de Salas:** Gestión de aulas del CRAI con validación de conflictos horarios.
- **Préstamos:** Registro de préstamos de libros y devoluciones.
- **Gestión de Material:** Registro de recursos técnicos como portátiles o cámaras.

---

## Documentación de la API (Swagger)

El proyecto incluye documentación interactiva de los endpoints. Una vez que la aplicación esté en ejecución, puedes acceder a ella en:

http://localhost:8080/swagger-ui/index.html

---

## Datos de Prueba Iniciales

Al arrancar la aplicación, el sistema carga automáticamente un conjunto de datos iniciales que incluye:

- **Usuarios de prueba:** Cuentas para Ander, Iñigo, Emilio, Gaizka y Jacqueline, además de una cuenta de administrador (email: 1, pass: 1).
- **Salas:** Aula 01, Aula 02 y Aula 03.
- **Libros:** Títulos de referencia como "Clean Code" y "Design Patterns".

---

## Tecnologías Utilizadas

- **Backend:** Java 21, Spring Boot 3.4.0, Spring Data JPA
- **Base de Datos:** MySQL
- **Frontend:** HTML5, CSS, JavaScript
- **Testing:** JUnit 5 y Hamcrest
