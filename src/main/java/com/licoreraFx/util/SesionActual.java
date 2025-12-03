package com.licoreraFx.util;

import com.licoreraFx.model.Usuario;

public class SesionActual {
    private static Usuario usuario;

    public static Usuario getUsuario() {
        return usuario;
    }

    public static void setUsuario(Usuario usuario) {
        SesionActual.usuario = usuario;
    }

    public static void clear() {
        SesionActual.usuario = null;
    }
}
