package es.deusto.spq.deustocrai.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "instalacion_deportiva")
public class InstalacionDeportiva {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre; // Ej: "Pista Padel 1", "Campo de Fútbol"
    
    @Column(nullable = false)
    private String tipo; // Ej: "PADEL", "FUTBOL"

    public InstalacionDeportiva() {}
    public InstalacionDeportiva(String nombre, String tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
}
