package com.licoreraFx.repository;

import com.licoreraFx.model.Cliente;
import com.licoreraFx.util.JsonManager;

import java.util.List;

public class ClienteRepository {
    // Simple lock to avoid concurrent writes
    private static final Object LOCK = new Object();

    public static List<Cliente> listarClientes() {
        synchronized (LOCK) {
            return JsonManager.listarClientes();
        }
    }

    public static boolean guardarClientes(List<Cliente> clientes) {
        synchronized (LOCK) {
            return JsonManager.guardarClientes(clientes);
        }
    }

    public static boolean agregarCliente(Cliente cliente) {
        synchronized (LOCK) {
            return JsonManager.agregarCliente(cliente);
        }
    }

    public static boolean actualizarCliente(String idOriginal, Cliente clienteActualizado) {
        synchronized (LOCK) {
            return JsonManager.actualizarCliente(idOriginal, clienteActualizado);
        }
    }

    public static boolean eliminarCliente(String id) {
        synchronized (LOCK) {
            return JsonManager.eliminarCliente(id);
        }
    }

    public static java.util.Optional<Cliente> buscarClientePorId(String id) {
        synchronized (LOCK) {
            return JsonManager.buscarClientePorId(id);
        }
    }
}

