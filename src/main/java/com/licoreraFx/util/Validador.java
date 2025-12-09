package com.licoreraFx.util;

/**
 * Utilitario simple para validaciones cortas.
 */
public class Validador {

    /**
     * Verifica que todos los campos no sean nulos ni vacíos.
     */
    public static boolean camposNoVacios(String... campos) {
        if (campos == null) return false;
        for (String c : campos) {
            if (c == null || c.trim().isEmpty()) return false;
        }
        return true;
    }

    /**
     * Normaliza el usuario (quita espacios alrededor).
     */
    public static String normalizarUsuario(String usuario) {
        if (usuario == null) return null;
        return usuario.trim();
    }
}
