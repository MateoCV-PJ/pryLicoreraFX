package com.licoreraFx.repository;

import com.licoreraFx.model.Proveedor;
import com.licoreraFx.util.JsonManager;

import java.util.List;

public class ProveedorRepository {
    private static final Object LOCK = new Object();

    public static List<Proveedor> listarProveedores() {
        synchronized (LOCK) {
            return JsonManager.listarProveedores();
        }
    }

    public static boolean guardarProveedores(List<Proveedor> proveedores) {
        synchronized (LOCK) {
            return JsonManager.guardarProveedores(proveedores);
        }
    }

    public static boolean agregarProveedor(Proveedor proveedor) {
        synchronized (LOCK) {
            return JsonManager.agregarProveedor(proveedor);
        }
    }

    public static boolean actualizarProveedor(String idOriginal, Proveedor proveedorActualizado) {
        synchronized (LOCK) {
            return JsonManager.actualizarProveedor(idOriginal, proveedorActualizado);
        }
    }

    public static boolean eliminarProveedor(String id) {
        synchronized (LOCK) {
            return JsonManager.eliminarProveedor(id);
        }
    }
}

