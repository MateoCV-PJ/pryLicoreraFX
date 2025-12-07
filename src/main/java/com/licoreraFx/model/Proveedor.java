package com.licoreraFx.model;

public class Proveedor {
    private String id;
    private String nombreEmpresa;
    private String email;
    private String direccion;
    private String rut;

    public Proveedor(String id, String nombreEmpresa, String email, String direccion, String rut) {
        this.id = id;
        this.nombreEmpresa = nombreEmpresa;
        this.email = email;
        this.direccion = direccion;
        this.rut = rut;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }
}
