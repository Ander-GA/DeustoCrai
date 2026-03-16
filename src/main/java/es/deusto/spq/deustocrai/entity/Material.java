package es.deusto.spq.deustocrai.entity;

import jakarta.persistence.Entity;

@Entity
public class Material extends AbstractRecurso {

    private String numeroSerie;
    private String tipo; // Ejemplo: "Portatil", "Camara"

    public Material() {}

    public Material(String titulo, String numeroSerie, String tipo) {
        super(titulo);
        this.numeroSerie = numeroSerie;
        this.tipo = tipo;
    }

    // Getters y Setters
    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}