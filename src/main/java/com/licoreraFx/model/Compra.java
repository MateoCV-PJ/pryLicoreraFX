package com.licoreraFx.model;

import java.util.ArrayList;
import java.util.List;

public class Compra {
    private String id;            // autoincremental en JSON
    private String proveedorId;   // id del proveedor
    private String numeroFactura; // número de factura
    private String metodoPago;    // Efectivo/Transferencia/Tarjeta/Crédito
    private double total;         // total calculado
    private String notas;         // opcional
    private List<Item> items = new ArrayList<>(); // detalle de productos

    public static class Item {
        private String productoId;
        private String nombreProducto;
        private int cantidad;
        private double precio;

        public Item() {}
        public Item(String productoId, String nombreProducto, int cantidad, double precio) {
            this.productoId = productoId;
            this.nombreProducto = nombreProducto;
            this.cantidad = cantidad;
            this.precio = precio;
        }
        public String getProductoId() { return productoId; }
        public void setProductoId(String productoId) { this.productoId = productoId; }
        public String getNombreProducto() { return nombreProducto; }
        public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        public double getPrecio() { return precio; }
        public void setPrecio(double precio) { this.precio = precio; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProveedorId() { return proveedorId; }
    public void setProveedorId(String proveedorId) { this.proveedorId = proveedorId; }
    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items != null ? items : new ArrayList<>(); }
}
