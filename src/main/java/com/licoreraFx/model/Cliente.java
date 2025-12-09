package com.licoreraFx.model;

/**
 * Modelo que representa un cliente.
 * Contiene datos de contacto y documento.
 */
public class Cliente {
    private String id;
    private String nombre;
    private String email;
    private String direccion; // dirección del cliente
    private String documento;

    public Cliente() {}

    public Cliente(String id, String nombre, String email, String direccion, String documento) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.direccion = direccion;
        this.documento = documento;
    }

    /**
     * Obtiene el ID del cliente.
     * @return el ID del cliente.
     */
    public String getId() { return id; }

    /**
     * Establece el ID del cliente.
     * @param id el nuevo ID del cliente.
     */
    public void setId(String id) { this.id = id; }

    /**
     * Obtiene el nombre del cliente.
     * @return el nombre del cliente.
     */
    public String getNombre() { return nombre; }

    /**
     * Establece el nombre del cliente.
     * @param nombre el nuevo nombre del cliente.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Obtiene el email del cliente.
     * @return el email del cliente.
     */
    public String getEmail() { return email; }

    /**
     * Establece el email del cliente.
     * @param email el nuevo email del cliente.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Obtiene la dirección del cliente.
     * @return la dirección del cliente.
     */
    public String getDireccion() { return direccion; }

    /**
     * Establece la dirección del cliente.
     * @param direccion la nueva dirección del cliente.
     */
    public void setDireccion(String direccion) { this.direccion = direccion; }

    /**
     * Obtiene el documento del cliente.
     * @return el documento del cliente.
     */
    public String getDocumento() { return documento; }

    /**
     * Establece el documento del cliente.
     * @param documento el nuevo documento del cliente.
     */
    public void setDocumento(String documento) { this.documento = documento; }

    @Override
    public String toString() {
        return "Cliente{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", direccion='" + direccion + '\'' +
                ", documento='" + documento + '\'' +
                '}';
    }
}
