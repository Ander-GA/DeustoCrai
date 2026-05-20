package es.deusto.spq.deustocrai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "control_calidad")
public class ControlCalidad {

    public enum EstadoControl {
        APTO,
        ROTO,
        REPARACION
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Material material;

    @ManyToOne
    private Prestamo prestamo;

    @ManyToOne(optional = false)
    private User bibliotecario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoControl estado;

    @Column(length = 1000)
    private String observaciones;

    @Column(nullable = false)
    private LocalDateTime fechaRevision;

    public ControlCalidad() {
    }

    public ControlCalidad(Material material, Prestamo prestamo, User bibliotecario, EstadoControl estado, String observaciones) {
        this.material = material;
        this.prestamo = prestamo;
        this.bibliotecario = bibliotecario;
        this.estado = estado;
        this.observaciones = observaciones;
        this.fechaRevision = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }

    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
    }

    public User getBibliotecario() {
        return bibliotecario;
    }

    public void setBibliotecario(User bibliotecario) {
        this.bibliotecario = bibliotecario;
    }

    public EstadoControl getEstado() {
        return estado;
    }

    public void setEstado(EstadoControl estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFechaRevision() {
        return fechaRevision;
    }

    public void setFechaRevision(LocalDateTime fechaRevision) {
        this.fechaRevision = fechaRevision;
    }
}