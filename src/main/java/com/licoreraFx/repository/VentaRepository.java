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
    // Lista de listeners que serán notificados cuando cambien las ventas (UI puede registrarse)
    private static final java.util.List<Runnable> changeListeners = new java.util.ArrayList<>();

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
            boolean ok = JsonManager.agregarVenta(venta);
            if (ok) notifyChangeListeners();
            return ok;
        }
    }

    /** Elimina una venta por id. */
    public static boolean eliminarVenta(String id) {
        synchronized (LOCK) {
            boolean ok = JsonManager.eliminarVenta(id);
            if (ok) notifyChangeListeners();
            return ok;
        }
    }

    /** Genera un id para una venta. */
    public static String generarIdVenta() {
        synchronized (LOCK) {
            return JsonManager.generarIdVenta();
        }
    }

    public static void addChangeListener(Runnable r) {
        synchronized (LOCK) { if (r != null) changeListeners.add(r); }
    }

    public static void removeChangeListener(Runnable r) {
        synchronized (LOCK) { changeListeners.remove(r); }
    }

    private static void notifyChangeListeners() {
        java.util.List<Runnable> copy;
        synchronized (LOCK) { copy = new java.util.ArrayList<>(changeListeners); }
        for (Runnable r : copy) {
            try { javafx.application.Platform.runLater(r); } catch (Exception ignored) {}
        }
    }
}
