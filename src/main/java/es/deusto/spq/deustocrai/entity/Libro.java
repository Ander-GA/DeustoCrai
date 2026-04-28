package es.deusto.spq.deustocrai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank; // Para validar que no esté vacío

@Entity
@Table(name = "libro")
public class Libro extends AbstractRecurso {

    //@NotBlank(message = "El ISBN es obligatorio")
    @Column(nullable = false, unique = true)
    private String isbn;

    //@NotBlank(message = "El autor es obligatorio")
    @Column(nullable = false)
    private String autor;

    public Libro() {
        super();
    }

    public Libro(String titulo, String isbn, String autor) {
        super(titulo); 
        this.isbn = isbn;
        this.autor = autor;
    }

    // Getters y Setters
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
}