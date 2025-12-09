package com.licoreraFx.util;

import com.licoreraFx.model.Usuario;

/**
 * Guarda la sesión actual en memoria (simple Singleton estático).
 */
public class SesionActual {
    private static Usuario usuario;

    /**
     * Obtiene el usuario en sesión.
     */
    public static Usuario getUsuario() {
        return usuario;
    }

    /**
     * Establece el usuario en sesión.
     */
    public static void setUsuario(Usuario usuario) {
        SesionActual.usuario = usuario;
    }

    /**
     * Borra la sesión actual.
     */
    public static void clear() {
        SesionActual.usuario = null;
    }
}
