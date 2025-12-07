package com.licoreraFx.repository;

import com.licoreraFx.model.Usuario;
import com.licoreraFx.util.JsonManager;

import java.util.List;

public class UsuarioRepository {
    private static final Object LOCK = new Object();

    public static List<Usuario> listarUsuarios() {
        synchronized (LOCK) {
            return JsonManager.listarUsuarios();
        }
    }

    public static List<Usuario> listarVendedores() {
        synchronized (LOCK) {
            return JsonManager.listarVendedores();
        }
    }

    public static boolean guardarUsuarios(List<Usuario> usuarios) {
        synchronized (LOCK) {
            return JsonManager.guardarUsuarios(usuarios);
        }
    }

    public static boolean agregarUsuario(Usuario usuario) {
        synchronized (LOCK) {
            return JsonManager.agregarUsuario(usuario);
        }
    }

    public static boolean actualizarUsuario(String usernameOriginal, Usuario usuarioActualizado) {
        synchronized (LOCK) {
            return JsonManager.actualizarUsuario(usernameOriginal, usuarioActualizado);
        }
    }

    public static boolean eliminarUsuario(String username) {
        synchronized (LOCK) {
            return JsonManager.eliminarUsuario(username);
        }
    }

    public static java.util.Optional<Usuario> buscarUsuarioPorNombre(String username) {
        synchronized (LOCK) {
            return JsonManager.buscarUsuarioPorNombre(username);
        }
    }
}

