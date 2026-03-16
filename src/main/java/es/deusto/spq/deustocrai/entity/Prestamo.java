package es.deusto.spq.deustocrai.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "prestamo")
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User usuario;

    @ManyToOne
    @JoinColumn(name = "recurso_id")
    private AbstractRecurso recurso;

    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucionPrevista;

    public Prestamo() {}

    public Prestamo(User usuario, AbstractRecurso recurso) {
        this.usuario = usuario;
        this.recurso = recurso;
        this.fechaPrestamo = LocalDate.now();
        this.fechaDevolucionPrevista = LocalDate.now().plusDays(7); // Por defecto 7 días
    }

    // Getters y Setters
    public Long getId() { return id; }
    public User getUsuario() { return usuario; }
    public AbstractRecurso getRecurso() { return recurso; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public LocalDate getFechaDevolucionPrevista() { return fechaDevolucionPrevista; }
}