package com.licoreraFx.repository;

import com.licoreraFx.model.Producto;
import com.licoreraFx.util.JsonManager;

import java.util.List;

public class ProductoRepository {
    private static final Object LOCK = new Object();

    public static List<Producto> listarProductos() {
        synchronized (LOCK) {
            return JsonManager.listarProductos();
        }
    }

    public static boolean guardarProductos(List<Producto> productos) {
        synchronized (LOCK) {
            return JsonManager.guardarProductos(productos);
        }
    }

    public static boolean agregarProducto(Producto producto) {
        synchronized (LOCK) {
            return JsonManager.agregarProducto(producto);
        }
    }

    public static boolean actualizarProducto(String idOriginal, Producto productoActualizado) {
        synchronized (LOCK) {
            return JsonManager.actualizarProducto(idOriginal, productoActualizado);
        }
    }

    public static boolean eliminarProducto(String id) {
        synchronized (LOCK) {
            return JsonManager.eliminarProducto(id);
        }
    }

    public static java.util.Optional<Producto> buscarProductoPorId(String id) {
        synchronized (LOCK) {
            return JsonManager.buscarProductoPorId(id);
        }
    }
}

