package com.licoreraFx.util;

public class Validador {

    public static boolean camposNoVacios(String... campos) {
        if (campos == null) return false;
        for (String c : campos) {
            if (c == null || c.trim().isEmpty()) return false;
        }
        return true;
    }

    public static String normalizarUsuario(String usuario) {
        if (usuario == null) return null;
        return usuario.trim();
    }
}
