package com.licoreraFx;

import javafx.application.Application;
import javafx.stage.Stage;
import com.licoreraFx.view.LoginView;
import com.licoreraFx.controller.LoginController;

/**
 * Clase principal de la aplicación.
 * Lanza la aplicación JavaFX y muestra la pantalla de inicio de sesión.
 */
public class MainApp extends Application {
    /**
     * Punto de entrada de la aplicación.
     * Llama a JavaFX para iniciar la aplicación.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Inicia la interfaz gráfica y muestra la vista de login.
     * Crea el controlador y enlaza el manejador de login.
     */
    @Override
    public void start(Stage primaryStage) {
        LoginView view = new LoginView();
        LoginController controller = new LoginController(view);
        view.setOnLoginHandler(() -> controller.onLoginAction());
        view.mostrar(primaryStage);
    }
}
