package com.licoreraFx.repository;

import com.licoreraFx.model.Venta;
import com.licoreraFx.util.JsonManager;

import java.util.List;

/**
 * Repositorio para ventas.
 * Encapsula acceso sincronizado a las operaciones de ventas.
 */
public class VentaRepository {
    private static final Object LOCK = new Object();

    /** Lista todas las ventas. */
    public static List<Venta> listarVentas() {
        synchronized (LOCK) {
            return JsonManager.listarVentas();
        }
    }

    /** Guarda todas las ventas. */
    public static boolean guardarVentas(List<Venta> ventas) {
        synchronized (LOCK) {
            return JsonManager.guardarVentas(ventas);
        }
    }

    /** Agrega una venta nueva. */
    public static boolean agregarVenta(Venta venta) {
        synchronized (LOCK) {
            return JsonManager.agregarVenta(venta);
        }
    }

    /** Elimina una venta por id. */
    public static boolean eliminarVenta(String id) {
        synchronized (LOCK) {
            return JsonManager.eliminarVenta(id);
        }
    }

    /** Genera un id para una venta. */
    public static String generarIdVenta() {
        synchronized (LOCK) {
            return JsonManager.generarIdVenta();
        }
    }
}
