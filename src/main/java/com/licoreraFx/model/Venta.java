package com.licoreraFx.model;

import java.util.List;

/**
 * Modelo que representa una venta.
 * Contiene detalles de los productos y totales.
 */
public class Venta {
    private String id;
    private String clienteId;
    private String nombreCliente;
    private List<DetalleVenta> detalles;
    private double total;

    // Campos para el vendedor / usuario que creó la venta
    private String vendedorId;
    private String vendedorNombre;
    private String vendedorRol;
    // Fecha de creación de la venta en formato ISO (ej. 2025-12-09T17:00:00)
    private String fecha;

    /**
     * Crea una venta con los datos básicos.
     */
    public Venta(String id, String clienteId, String nombreCliente, List<DetalleVenta> detalles, double total) {
        this.id = id;
        this.clienteId = clienteId;
        this.nombreCliente = nombreCliente;
        this.detalles = detalles;
        this.total = total;
    }

    // Getters y setters simples
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    // Getters/Setters para campos de vendedor
    public String getVendedorId() { return vendedorId; }
    public void setVendedorId(String vendedorId) { this.vendedorId = vendedorId; }
    public String getVendedorNombre() { return vendedorNombre; }
    public void setVendedorNombre(String vendedorNombre) { this.vendedorNombre = vendedorNombre; }
    public String getVendedorRol() { return vendedorRol; }
    public void setVendedorRol(String vendedorRol) { this.vendedorRol = vendedorRol; }

    @Override
    public String toString() {
        return "Venta{" +
                "id='" + id + '\'' +
                ", clienteId='" + clienteId + '\'' +
                ", nombreCliente='" + nombreCliente + '\'' +
                ", total=" + total +
                ", vendedorId='" + vendedorId + '\'' +
                ", vendedorNombre='" + vendedorNombre + '\'' +
                ", vendedorRol='" + vendedorRol + '\'' +
                ", fecha='" + fecha + '\'' +
                '}';
    }
}
