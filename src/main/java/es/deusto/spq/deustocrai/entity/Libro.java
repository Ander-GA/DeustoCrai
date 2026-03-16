package es.deusto.spq.deustocrai.entity;

import jakarta.persistence.Entity;

@Entity
public class Libro extends AbstractRecurso {

    private String isbn;
    private String autor;

    public Libro() {}

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