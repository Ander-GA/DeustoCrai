# DeustoCrai - Library & Resource Management System 📚

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=Gradle&logoColor=white)](https://gradle.org/)

## 👥 Contexto de Desarrollo

Este sistema ha sido diseñado e implementado como proyecto académico de Ingeniería Informática mediante el trabajo colaborativo de un equipo de 5 estudiantes.

## 📝 Descripción del Proyecto

**DeustoCrai** es un sistema integral de gestión diseñado para optimizar la administración de los recursos del Centro de Recursos para el Aprendizaje y la Investigación (CRAI) de la Universidad de Deusto. 

El proyecto se centra en proporcionar una solución robusta para el control de inventario, la reserva de espacios y el préstamo de materiales, garantizando una experiencia de usuario fluida y una gestión de datos segura.

## ⚙️ Arquitectura y Patrones de Diseño

El backend ha sido diseñado bajo una **arquitectura multicapa**, priorizando la escalabilidad, la separación de responsabilidades (SoC) y el bajo acoplamiento. Se han implementado las siguientes metodologías y patrones:

*   **Arquitectura Multicapa (MVC):** Separación lógica en capas de Controladores (gestión de peticiones HTTP), Servicios (lógica de negocio) y Repositorios (acceso a datos).
*   **API RESTful:** Diseño de endpoints estandarizados para la comunicación entre el cliente y el servidor, utilizando correctamente los verbos HTTP y códigos de estado.
*   **Inyección de Dependencias (IoC):** Aprovechando el contenedor de Spring Boot para gestionar el ciclo de vida de los componentes y reducir el acoplamiento.
*   **DTO (Data Transfer Object):** Para encapsular y transportar datos entre la API y el cliente, aislando y protegiendo el modelo de dominio interno.
*   **DAO (Data Access Object):** Para abstraer y centralizar todas las operaciones de acceso y persistencia en la base de datos MySQL.
*   **Facade:** Para proporcionar una interfaz unificada y simplificada a los subsistemas más complejos, mejorando la legibilidad y el mantenimiento del código.

## 🚀 Funcionalidades Principales

El desarrollo se enfocó en módulos críticos para la operativa diaria de la biblioteca:

*   **Gestión de Préstamos:** Sistema completo para el registro, seguimiento y devolución de material bibliográfico y tecnológico.
*   **Reserva de Salas:** Módulo de asignación de espacios de estudio con control de disponibilidad en tiempo real.
*   **Listas de Espera:** Lógica de encolado para gestionar la alta demanda de recursos específicos, notificando a los usuarios según prioridad.

## 🛠️ Instalación y Despliegue

Sigue estos pasos para desplegar el entorno de desarrollo local:

1. **Clonar el repositorio:**
```bash
   git clone [https://github.com/Ander-GA/DeustoCrai.git](https://github.com/Ander-GA/DeustoCrai.git)
```
2. **Configurar la Base de Datos:**
*  Crear un esquema en MySQL llamado deustocrai_db.
*  Actualizar las credenciales en el archivo application.properties (o application.yml).

3. **Compilar y Ejecutar:**
*  Utilizando el wrapper de Gradle:
```bash
   ./gradlew bootRun
```
