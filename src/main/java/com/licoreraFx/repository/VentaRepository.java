package com.licoreraFx.repository;

import com.licoreraFx.model.Venta;
import com.licoreraFx.util.JsonManager;

import java.util.List;

public class VentaRepository {
    private static final Object LOCK = new Object();

    public static List<Venta> listarVentas() {
        synchronized (LOCK) {
            return JsonManager.listarVentas();
        }
    }

    public static boolean guardarVentas(List<Venta> ventas) {
        synchronized (LOCK) {
            return JsonManager.guardarVentas(ventas);
        }
    }

    public static boolean agregarVenta(Venta venta) {
        synchronized (LOCK) {
            return JsonManager.agregarVenta(venta);
        }
    }

    public static boolean eliminarVenta(String id) {
        synchronized (LOCK) {
            return JsonManager.eliminarVenta(id);
        }
    }

    public static String generarIdVenta() {
        synchronized (LOCK) {
            return JsonManager.generarIdVenta();
        }
    }
}

