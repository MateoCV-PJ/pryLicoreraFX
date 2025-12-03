package com.licoreraFx.controller;

import com.licoreraFx.model.Usuario;
import com.licoreraFx.util.JsonManager;
import com.licoreraFx.util.SesionActual;
import com.licoreraFx.util.Validador;
import com.licoreraFx.view.LoginView;
import com.licoreraFx.view.MenuAdministradorView;
import com.licoreraFx.view.MenuVendedorView;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.Optional;

public class LoginController {

    private final LoginView view;

    public LoginController(LoginView view) {
        this.view = view;
    }

    public void onLoginAction() {
        String usuario = Validador.normalizarUsuario(view.getUsuario());
        String password = view.getPassword();

        if (!Validador.camposNoVacios(usuario, password)) {
            view.mostrarAlerta("Error", "Por favor completa todos los campos", Alert.AlertType.ERROR);
            return;
        }

        try {
            Optional<Usuario> opt = JsonManager.buscarUsuarioPorNombre(usuario);
            if (opt.isEmpty()) {
                view.mostrarAlerta("Error", "Usuario o contraseña incorrectos", Alert.AlertType.ERROR);
                return;
            }

            Usuario u = opt.get();
            if (!password.equals(u.getPassword())) {
                view.mostrarAlerta("Error", "Usuario o contraseña incorrectos", Alert.AlertType.ERROR);
                return;
            }

            // Autenticación exitosa
            SesionActual.setUsuario(u);

            // Navegar según rol
            String rol = u.getRol() != null ? u.getRol().toUpperCase().trim() : "";
            if (rol.equals("ADMIN") || rol.equals("ADMINISTRADOR")) {
                // Abrir MenuAdministradorView
                Platform.runLater(() -> new MenuAdministradorView().mostrar(new Stage()));
            } else if (rol.equals("VENDEDOR") || rol.equals("VENDOR")) {
                Platform.runLater(() -> new MenuVendedorView().mostrar(new Stage()));
            } else {
                view.mostrarAlerta("Error", "Rol de usuario no reconocido: " + rol, Alert.AlertType.ERROR);
            }

            // Cerrar ventana de login
            Platform.runLater(() -> view.getStage().close());

        } catch (Exception e) {
            e.printStackTrace();
            view.mostrarAlerta("Error", "Ocurrió un error al autenticar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
