package es.deusto.spq.deustocrai.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;


@Entity
@Table(name = "user")
public class User {

    // Definimos el Enum dentro o fuera de la clase
    public enum Role {
        ESTUDIANTE, BIBLIOTECARIO, ADMIN
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    //@NotBlank(message = "Los apellidos son obligatorios")
    @Column(nullable = false)
    private String apellidos;

    //@NotBlank(message = "La contraseña es obligatoria")
    //@Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Column(nullable = false)
    private String password;

    //@NotBlank(message = "El email es obligatorio")
    //@Email(message = "El formato del email no es válido")
    @Column(nullable = false, unique = true)
    private String email;

    @NotNull(message = "El rol es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @Column(nullable = false)
    private boolean bloqueado = false;
    
    private java.time.LocalDateTime fechaFinPenalizacion;
    
    public User() {}
    
    public User(String nombre, String apellidos, String password, String email, Role role) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    public boolean isBloqueado() { return bloqueado; }
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }
    
    public java.time.LocalDateTime getFechaFinPenalizacion() { return fechaFinPenalizacion; }
    public void setFechaFinPenalizacion(java.time.LocalDateTime fechaFinPenalizacion) { this.fechaFinPenalizacion = fechaFinPenalizacion; }
    @Override
    public int hashCode() { return Objects.hash(email); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User other = (User) obj;
        return Objects.equals(email, other.email);
    }
}