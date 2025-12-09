package com.licoreraFx.repository;

import com.licoreraFx.model.Producto;
import com.licoreraFx.util.JsonManager;

import java.util.List;

/**
 * Repositorio de productos.
 * Encapsula acceso concurrente a los métodos de lectura/escritura de productos en JSON.
 */
public class ProductoRepository {
    private static final Object LOCK = new Object();

    /** Lista todos los productos. */
    public static List<Producto> listarProductos() {
        synchronized (LOCK) {
            return JsonManager.listarProductos();
        }
    }

    /** Guarda la lista completa de productos. */
    public static boolean guardarProductos(List<Producto> productos) {
        synchronized (LOCK) {
            return JsonManager.guardarProductos(productos);
        }
    }

    /** Agrega un producto nuevo (genera id si hace falta). */
    public static boolean agregarProducto(Producto producto) {
        synchronized (LOCK) {
            return JsonManager.agregarProducto(producto);
        }
    }

    /** Actualiza un producto identificado por su id original. */
    public static boolean actualizarProducto(String idOriginal, Producto productoActualizado) {
        synchronized (LOCK) {
            return JsonManager.actualizarProducto(idOriginal, productoActualizado);
        }
    }

    /** Elimina un producto por id. */
    public static boolean eliminarProducto(String id) {
        synchronized (LOCK) {
            return JsonManager.eliminarProducto(id);
        }
    }

    /** Busca un producto por id y devuelve un Optional. */
    public static java.util.Optional<Producto> buscarProductoPorId(String id) {
        synchronized (LOCK) {
            return JsonManager.buscarProductoPorId(id);
        }
    }
}
