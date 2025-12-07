package com.licoreraFx.repository;

import com.licoreraFx.model.Compra;
import com.licoreraFx.util.JsonManager;

import java.util.List;

public class CompraRepository {
    private static final Object LOCK = new Object();

    public static List<Compra> listarCompras() {
        synchronized (LOCK) {
            return JsonManager.listarCompras();
        }
    }

    public static boolean guardarCompras(List<Compra> compras) {
        synchronized (LOCK) {
            return JsonManager.guardarCompras(compras);
        }
    }

    public static boolean agregarCompra(Compra compra) {
        synchronized (LOCK) {
            return JsonManager.agregarCompra(compra);
        }
    }

    public static boolean eliminarCompra(String id) {
        synchronized (LOCK) {
            return JsonManager.eliminarCompra(id);
        }
    }

    public static String generarIdCompra() {
        synchronized (LOCK) {
            return JsonManager.generarIdCompra();
        }
    }
}

