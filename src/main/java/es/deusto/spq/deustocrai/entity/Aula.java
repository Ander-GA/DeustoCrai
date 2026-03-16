package es.deusto.spq.deustocrai.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "aula")
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre; // Ej: "Sala de Juntas", "Aula 102"

    private int capacidad;

    private boolean tieneProyector;

    public Aula() {}

    public Aula(String nombre, int capacidad, boolean tieneProyector) {
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.tieneProyector = tieneProyector;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    public boolean isTieneProyector() { return tieneProyector; }
    public void setTieneProyector(boolean tieneProyector) { this.tieneProyector = tieneProyector; }
}