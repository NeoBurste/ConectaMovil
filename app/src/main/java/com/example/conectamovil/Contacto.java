package com.example.conectamovil;

public class Contacto {
    private String nombre;
    private String telefono;

    public Contacto() {
        // Constructor vacío necesario para Firebase
    }

    public Contacto(String nombre, String telefono) {
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    @Override
    public String toString() {
        return "Nombre: " + nombre + ", Teléfono: " + telefono;
    }

}
