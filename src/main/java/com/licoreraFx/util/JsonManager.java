package com.licoreraFx.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.licoreraFx.model.Usuario;

import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonManager {

    private static final String USUARIOS_PATH = "data/usuarios.json";
    private static final Gson gson = new Gson();

    public static List<Usuario> listarUsuarios() {
        try {
            Path path = Path.of(USUARIOS_PATH);
            if (!Files.exists(path)) {
                return new ArrayList<>();
            }
            try (Reader reader = new FileReader(path.toFile())) {
                Type listType = new TypeToken<List<Usuario>>() {}.getType();
                List<Usuario> usuarios = gson.fromJson(reader, listType);
                return usuarios != null ? usuarios : new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error leyendo usuarios JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static Optional<Usuario> buscarUsuarioPorNombre(String username) {
        if (username == null) return Optional.empty();
        List<Usuario> usuarios = listarUsuarios();
        return usuarios.stream()
                .filter(u -> username.equals(u.getUsername()))
                .findFirst();
    }
}
