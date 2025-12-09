package com.licoreraFx.repository;

import com.licoreraFx.model.Usuario;
import com.licoreraFx.util.JsonManager;

import java.util.List;

/**
 * Repositorio para usuarios.
 * Encapsula acceso sincronizado a las operaciones sobre usuarios.
 */
public class UsuarioRepository {
    private static final Object LOCK = new Object();

    /** Lista todos los usuarios. */
    public static List<Usuario> listarUsuarios() {
        synchronized (LOCK) {
            return JsonManager.listarUsuarios();
        }
    }

    /** Lista sólo los usuarios con rol VENDEDOR. */
    public static List<Usuario> listarVendedores() {
        synchronized (LOCK) {
            return JsonManager.listarVendedores();
        }
    }

    /** Guarda la lista de usuarios. */
    public static boolean guardarUsuarios(List<Usuario> usuarios) {
        synchronized (LOCK) {
            return JsonManager.guardarUsuarios(usuarios);
        }
    }

    /** Agrega un usuario. */
    public static boolean agregarUsuario(Usuario usuario) {
        synchronized (LOCK) {
            return JsonManager.agregarUsuario(usuario);
        }
    }

    /** Actualiza un usuario existente identificado por su username original. */
    public static boolean actualizarUsuario(String usernameOriginal, Usuario usuarioActualizado) {
        synchronized (LOCK) {
            return JsonManager.actualizarUsuario(usernameOriginal, usuarioActualizado);
        }
    }

    /** Elimina un usuario por username. */
    public static boolean eliminarUsuario(String username) {
        synchronized (LOCK) {
            return JsonManager.eliminarUsuario(username);
        }
    }

    /** Busca un usuario por su nombre de usuario. */
    public static java.util.Optional<Usuario> buscarUsuarioPorNombre(String username) {
        synchronized (LOCK) {
            return JsonManager.buscarUsuarioPorNombre(username);
        }
    }
}
