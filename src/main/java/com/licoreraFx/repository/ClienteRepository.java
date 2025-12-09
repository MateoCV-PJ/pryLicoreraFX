package com.licoreraFx.repository;

import com.licoreraFx.model.Cliente;
import com.licoreraFx.util.JsonManager;

import java.util.List;

/**
 * Repositorio para clientes.
 * Proporciona acceso sincronizado a operaciones CRUD sobre clientes en JSON.
 */
public class ClienteRepository {
    // Simple lock to avoid concurrent writes
    private static final Object LOCK = new Object();

    /** Lista todos los clientes. */
    public static List<Cliente> listarClientes() {
        synchronized (LOCK) {
            return JsonManager.listarClientes();
        }
    }

    /** Guarda la lista completa de clientes. */
    public static boolean guardarClientes(List<Cliente> clientes) {
        synchronized (LOCK) {
            return JsonManager.guardarClientes(clientes);
        }
    }

    /** Agrega un cliente nuevo (genera id si hace falta). */
    public static boolean agregarCliente(Cliente cliente) {
        synchronized (LOCK) {
            return JsonManager.agregarCliente(cliente);
        }
    }

    /** Actualiza un cliente por su id original. */
    public static boolean actualizarCliente(String idOriginal, Cliente clienteActualizado) {
        synchronized (LOCK) {
            return JsonManager.actualizarCliente(idOriginal, clienteActualizado);
        }
    }

    /** Elimina un cliente por id. */
    public static boolean eliminarCliente(String id) {
        synchronized (LOCK) {
            return JsonManager.eliminarCliente(id);
        }
    }

    /** Busca un cliente por id. */
    public static java.util.Optional<Cliente> buscarClientePorId(String id) {
        synchronized (LOCK) {
            return JsonManager.buscarClientePorId(id);
        }
    }
}
