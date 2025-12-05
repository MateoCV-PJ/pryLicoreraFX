package com.licoreraFx.model;

public class Proveedor {
    private String id;
    private String nombreEmpresa;
    private String email;
    private String telefono;
    private String rut;

    public Proveedor(String id, String nombreEmpresa, String email, String telefono, String rut) {
        this.id = id;
        this.nombreEmpresa = nombreEmpresa;
        this.email = email;
        this.telefono = telefono;
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }
}


