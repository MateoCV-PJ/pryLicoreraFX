package com.licoreraFx.repository;

import com.licoreraFx.model.Proveedor;
import com.licoreraFx.util.JsonManager;

import java.util.List;

/**
 * Repositorio para proveedores.
 * Encapsula las operaciones CRUD sobre proveedores con sincronización.
 */
public class ProveedorRepository {
    private static final Object LOCK = new Object();

    /**
     * Lista todos los proveedores.
     *
     * @return Lista de proveedores.
     */
    public static List<Proveedor> listarProveedores() {
        synchronized (LOCK) {
            return JsonManager.listarProveedores();
        }
    }

    /**
     * Guarda la lista de proveedores.
     *
     * @param proveedores Lista de proveedores a guardar.
     * @return Verdadero si la operación fue exitosa, falso en caso contrario.
     */
    public static boolean guardarProveedores(List<Proveedor> proveedores) {
        synchronized (LOCK) {
            return JsonManager.guardarProveedores(proveedores);
        }
    }

    /**
     * Agrega un proveedor nuevo.
     *
     * @param proveedor Proveedor a agregar.
     * @return Verdadero si la operación fue exitosa, falso en caso contrario.
     */
    public static boolean agregarProveedor(Proveedor proveedor) {
        synchronized (LOCK) {
            return JsonManager.agregarProveedor(proveedor);
        }
    }

    /**
     * Actualiza un proveedor existente.
     *
     * @param idOriginal          ID del proveedor a actualizar.
     * @param proveedorActualizado Proveedor con la información actualizada.
     * @return Verdadero si la operación fue exitosa, falso en caso contrario.
     */
    public static boolean actualizarProveedor(String idOriginal, Proveedor proveedorActualizado) {
        synchronized (LOCK) {
            return JsonManager.actualizarProveedor(idOriginal, proveedorActualizado);
        }
    }

    /**
     * Elimina un proveedor.
     *
     * @param id ID del proveedor a eliminar.
     * @return Verdadero si la operación fue exitosa, falso en caso contrario.
     */
    public static boolean eliminarProveedor(String id) {
        synchronized (LOCK) {
            return JsonManager.eliminarProveedor(id);
        }
    }
}
