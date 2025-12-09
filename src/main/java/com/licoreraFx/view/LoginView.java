package com.licoreraFx.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;

/**
 * Vista de inicio de sesión.
 * Muestra los campos de usuario y contraseña y los botones de acción.
 */
public class LoginView {

    // Stage principal de la vista
    private Stage stage;
    // Campo para el nombre de usuario
    private TextField txtUsuario;
    // Campo para la contraseña
    private PasswordField txtPassword;
    // Botón para iniciar sesión
    private Button btnIniciarSesion;
    // Etiqueta para mostrar mensajes de validación
    private Label lblIndicador; // indicador de validación

    // Handler que será llamado al presionar el botón
    private Runnable onLoginHandler;

    /**
     * Muestra la ventana de login en el stage dado.
     * @param primaryStage Stage donde se mostrará la vista.
     */
    public void mostrar(Stage primaryStage) {
        this.stage = primaryStage;

        // Crear contenedor principal sin imagen de fondo
        StackPane root = new StackPane();
        root.getStyleClass().add("app-root");

        // Crear panel central de login
        VBox panelLogin = crearPanelLogin();

        root.getChildren().add(panelLogin);

        // Crear escena
        Scene scene = new Scene(root, 1000, 650);

        // Capturar la tecla ENTER desde cualquier parte de la escena
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (btnIniciarSesion != null) {
                    btnIniciarSesion.fire();
                    event.consume();
                }
            }
        });

        // Configurar stage
        stage.setTitle("Sistema - Inicio de Sesión");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();

        // Pedir foco en el campo usuario al mostrar la ventana
        if (txtUsuario != null) {
            txtUsuario.requestFocus();
        }
    }

    /**
     * Crea el panel central con los controles de login.
     * @return VBox con los controles de inicio de sesión.
     */
    private VBox crearPanelLogin() {
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(40));
        panel.setMaxWidth(420);
        panel.setMaxHeight(520);

        panel.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.95);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: rgba(0,0,0,0.06);" +
                        "-fx-border-width: 1;"
        );

        // Marca (nombre de la licorera) simple, sin logo
        Label lblMarca = new Label("Licorera - Quién Dijo Miedo");
        lblMarca.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblMarca.setTextFill(Color.web("#2b6ea3"));

        // Título
        Label lblTitulo = new Label("INICIAR SESIÓN");
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        lblTitulo.setTextFill(Color.web("#1f2d3a"));

        // Subtítulo
        Label lblSubtitulo = new Label("Accede a tu cuenta");
        lblSubtitulo.setFont(Font.font("Segoe UI", 14));
        lblSubtitulo.setTextFill(Color.web("#5b6770"));

        // Campo de usuario
        Label lblUsuario = new Label("Usuario");
        lblUsuario.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        lblUsuario.setTextFill(Color.web("#333333"));

        txtUsuario = new TextField();
        txtUsuario.setPromptText("Ingresa tu usuario");
        txtUsuario.setPrefHeight(40);
        txtUsuario.setMaxWidth(360);
        txtUsuario.getStyleClass().add("text-field");

        // Campo de contraseña
        Label lblPassword = new Label("Contraseña");
        lblPassword.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        lblPassword.setTextFill(Color.web("#333333"));

        txtPassword = new PasswordField();
        txtPassword.setPromptText("Ingresa tu contraseña");
        txtPassword.setPrefHeight(40);
        txtPassword.setMaxWidth(360);
        txtPassword.getStyleClass().add("password-field");

        // Indicador de validación (invisible por defecto)
        lblIndicador = new Label("");
        lblIndicador.setFont(Font.font("Segoe UI", 12));
        lblIndicador.setTextFill(Color.web("#b23b3b"));
        lblIndicador.setVisible(false);
        lblIndicador.setWrapText(true);
        lblIndicador.setMaxWidth(360);
        lblIndicador.managedProperty().bind(lblIndicador.visibleProperty());

        txtUsuario.textProperty().addListener((obs, oldVal, newVal) -> { if (lblIndicador.isVisible()) lblIndicador.setVisible(false); });
        txtPassword.textProperty().addListener((obs, oldVal, newVal) -> { if (lblIndicador.isVisible()) lblIndicador.setVisible(false); });

        // Botón iniciar sesión azul sobrio
        btnIniciarSesion = new Button("Iniciar Sesión");
        btnIniciarSesion.setPrefWidth(360);
        btnIniciarSesion.setPrefHeight(44);
        btnIniciarSesion.getStyleClass().add("dialog-button");

        btnIniciarSesion.setOnAction(e -> {
            String usuario = txtUsuario.getText() != null ? txtUsuario.getText().trim() : "";
            String password = txtPassword.getText() != null ? txtPassword.getText().trim() : "";
            if (usuario.isEmpty()) {
                lblIndicador.setText("⚠ Por favor ingresa el usuario.");
                lblIndicador.setVisible(true);
                txtUsuario.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                lblIndicador.setText("⚠ Por favor ingresa la contraseña.");
                lblIndicador.setVisible(true);
                txtPassword.requestFocus();
                return;
            }
            lblIndicador.setVisible(false);
            if (onLoginHandler != null) onLoginHandler.run();
        });

        btnIniciarSesion.setDefaultButton(true);

        // Botón 'Salir'
        Button btnSalir = new Button("Salir");
        btnSalir.setPrefWidth(150);
        btnSalir.setPrefHeight(44);
        btnSalir.getStyleClass().add("button-secundario");
        btnSalir.setOnAction(e -> Platform.exit());

        HBox botones = new HBox(12, btnIniciarSesion, btnSalir);
        botones.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(
                lblMarca,
                lblTitulo,
                lblSubtitulo,
                lblUsuario,
                txtUsuario,
                lblPassword,
                txtPassword,
                lblIndicador,
                botones
        );

        return panel;
    }

    /**
     * Muestra una alerta simple con el título y mensaje dados.
     * @param titulo Título de la alerta.
     * @param mensaje Mensaje a mostrar.
     * @param tipo Tipo de alerta.
     */
    public void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        DialogPane dialogPane = alerta.getDialogPane();
        // no stylesheet applied
        alerta.showAndWait();
    }

    // Getters y setters
    public String getUsuario() { return txtUsuario.getText(); }
    public String getPassword() { return txtPassword.getText(); }
    public Stage getStage() { return stage; }
    public void setOnLoginHandler(Runnable onLoginHandler) { this.onLoginHandler = onLoginHandler; }
}
