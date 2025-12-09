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

    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}
