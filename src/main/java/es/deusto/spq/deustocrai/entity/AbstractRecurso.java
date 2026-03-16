package es.deusto.spq.deustocrai.entity;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED) // Crea tablas separadas para Libro y Material
public abstract class AbstractRecurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    private boolean disponible = true;

    public AbstractRecurso() {}

    public AbstractRecurso(String titulo) {
        this.titulo = titulo;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
}