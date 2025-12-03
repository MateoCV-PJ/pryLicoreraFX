package com.licoreraFx;

import javafx.application.Application;
import javafx.stage.Stage;
import com.licoreraFx.view.LoginView;
import com.licoreraFx.controller.LoginController;

public class MainApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        LoginView view = new LoginView();
        LoginController controller = new LoginController(view);
        view.setOnLoginHandler(() -> controller.onLoginAction());
        view.mostrar(primaryStage);
    }
}
