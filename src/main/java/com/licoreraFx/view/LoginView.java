package com.licoreraFx.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;

public class LoginView {

    private Stage stage;
    private TextField txtUsuario;
    private PasswordField txtPassword;
    private Button btnIniciarSesion;
    private Label lblIndicador; // indicador de validación

    // Handler que será llamado al presionar el botón
    private Runnable onLoginHandler;

    public void mostrar(Stage primaryStage) {
        this.stage = primaryStage;

        // Crear contenedor principal con imagen de fondo
        StackPane root = new StackPane();

        // Agregar imagen de fondo
        agregarImagenFondo(root);

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
        stage.setTitle("¡Quién dijo Miedo! - Inicio de Sesión");
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
     * Agrega una imagen de fondo al contenedor
     */
    private void agregarImagenFondo(StackPane root) {
        try {
            ImageView imageView = new ImageView();

            // Intentar cargar el recurso de forma segura
            var is = getClass().getResourceAsStream("/img/fondo-login.jpg");
            if (is != null) {
                try (var stream = is) {
                    Image imagen = new Image(stream);
                    imageView.setImage(imagen);
                    imageView.setFitWidth(1000);
                    imageView.setFitHeight(650);
                    imageView.setPreserveRatio(false);
                    root.getChildren().add(imageView);
                    return;
                } catch (Exception e) {
                    System.out.println("Error al leer la imagen de fondo, usando degradado: " + e.getMessage());
                }
            } else {
                System.out.println("No se encontró la imagen de fondo en recursos, usando color degradado");
            }

            // Si llegó aquí, usar fondo degradado
            aplicarFondoDegradadoRojo(root);

        } catch (Exception e) {
            System.err.println("Error al cargar imagen de fondo: " + e.getMessage());
            aplicarFondoDegradadoRojo(root);
        }
    }

    /**
     * Aplica un fondo degradado rojo si no hay imagen
     */
    private void aplicarFondoDegradadoRojo(StackPane root) {
        BackgroundFill backgroundFill = new BackgroundFill(
                new javafx.scene.paint.LinearGradient(
                        0, 0, 1, 1, true,
                        javafx.scene.paint.CycleMethod.NO_CYCLE,
                        new javafx.scene.paint.Stop(0, Color.web("#8B0000")), // Rojo oscuro
                        new javafx.scene.paint.Stop(1, Color.web("#DC143C"))  // Crimson
                ),
                CornerRadii.EMPTY,
                Insets.EMPTY
        );
        root.setBackground(new Background(backgroundFill));
    }

    /**
     * Crea el panel central de login
     */
    private VBox crearPanelLogin() {
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(40));
        panel.setMaxWidth(400);
        panel.setMaxHeight(500);

        // Estilo del panel: semitransparente para legibilidad sobre el fondo
        panel.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.88);" + // Blanco casi opaco para buen contraste
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-color: rgba(0,0,0,0.12);" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 20, 0, 0, 8);"
        );

        // Logo o imagen de la licorera
        Node logo = crearLogo();
        VBox.setMargin(logo, new Insets(0, 0, 6, 0));

        // Marca (nombre de la licorera)
        Label lblMarca = new Label("¡Quién dijo Miedo!");
        lblMarca.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblMarca.setTextFill(Color.web("#B22222"));

        // Título
        Label lblTitulo = new Label("INICIAR SESIÓN");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        lblTitulo.setTextFill(Color.web("#333333"));

        // Subtítulo
        Label lblSubtitulo = new Label("Accede a tu cuenta");
        lblSubtitulo.setFont(Font.font("Arial", 14));
        lblSubtitulo.setTextFill(Color.web("#666666"));

        // Campo de usuario
        Label lblUsuario = new Label("Usuario");
        lblUsuario.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
        lblUsuario.setTextFill(Color.web("#333333"));

        txtUsuario = new TextField();
        txtUsuario.setPromptText("Ingresa tu usuario");
        txtUsuario.setPrefHeight(40);
        txtUsuario.setMaxWidth(320);
        txtUsuario.setStyle(
                "-fx-background-color: rgba(255,255,255,0.98);" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #DADADA;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;" +
                        "-fx-font-size: 14;"
        );

        // Campo de contraseña
        Label lblPassword = new Label("Contraseña");
        lblPassword.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
        lblPassword.setTextFill(Color.web("#333333"));

        txtPassword = new PasswordField();
        txtPassword.setPromptText("Ingresa tu contraseña");
        txtPassword.setPrefHeight(40);
        txtPassword.setMaxWidth(320);
        txtPassword.setStyle(
                "-fx-background-color: rgba(255,255,255,0.98);" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #DADADA;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;" +
                        "-fx-font-size: 14;"
        );

        // Indicador de validación (invisible por defecto)
        lblIndicador = new Label("");
        lblIndicador.setFont(Font.font("Arial", 12));
        lblIndicador.setTextFill(Color.web("#B22222"));
        lblIndicador.setVisible(false);
        lblIndicador.setWrapText(true);
        lblIndicador.setMaxWidth(320);
        // No ocupar espacio cuando está oculto
        lblIndicador.managedProperty().bind(lblIndicador.visibleProperty());

        // Ocultar indicador cuando el usuario empiece a escribir
        txtUsuario.textProperty().addListener((obs, oldVal, newVal) -> {
            if (lblIndicador.isVisible()) lblIndicador.setVisible(false);
        });
        txtPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            if (lblIndicador.isVisible()) lblIndicador.setVisible(false);
        });

        // Botón iniciar sesión (ROJO)
        btnIniciarSesion = new Button("INICIAR SESIÓN");
        btnIniciarSesion.setPrefWidth(320);
        btnIniciarSesion.setPrefHeight(45);
        btnIniciarSesion.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btnIniciarSesion.setStyle(
                "-fx-background-color: #DC143C;" +  // Rojo crimson
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 6, 0, 0, 3);"
        );

        // Efecto hover para el botón
        btnIniciarSesion.setOnMouseEntered(e ->
                btnIniciarSesion.setStyle(
                        "-fx-background-color: #B22222;" +  // Rojo más oscuro al pasar el mouse
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.28), 8, 0, 0, 3);"
                )
        );

        btnIniciarSesion.setOnMouseExited(e ->
                btnIniciarSesion.setStyle(
                        "-fx-background-color: #DC143C;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 6, 0, 0, 3);"
                )
        );

        // Accion del botón delegada al handler externo
        btnIniciarSesion.setOnAction(e -> {
            // Validación simple: campos no vacíos
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

            // Ocultar indicador si pasa validación
            lblIndicador.setVisible(false);

            if (onLoginHandler != null) onLoginHandler.run();
        });

        // Permite que la tecla Enter active el botón por defecto
        btnIniciarSesion.setDefaultButton(true);

        // Crear botón 'Salir' junto al botón de iniciar sesión
        Button btnSalir = new Button("SALIR");
        btnSalir.setPrefWidth(150);
        btnSalir.setPrefHeight(45);
        btnSalir.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
        btnSalir.setStyle(
                "-fx-background-color: #F0F0F0; -fx-text-fill: #333; -fx-background-radius: 8; -fx-border-color: #DADADA;"
        );
        btnSalir.setOnMouseEntered(e -> btnSalir.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333; -fx-background-radius: 8; -fx-border-color: #DADADA;"));
        btnSalir.setOnMouseExited(e -> btnSalir.setStyle("-fx-background-color: #F0F0F0; -fx-text-fill: #333; -fx-background-radius: 8; -fx-border-color: #DADADA;"));
        btnSalir.setOnAction(e -> Platform.exit());

        // Agrupar botones en un HBox para alinearlos horizontalmente
        HBox botones = new HBox(10, btnIniciarSesion, btnSalir);
        botones.setAlignment(Pos.CENTER);

        // Agregar todos los elementos al panel
        panel.getChildren().addAll(
                logo,
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
     * Crea el logo de la aplicación. Devuelve un Node con imagen o fallback.
     */
    private Node crearLogo() {
        var is = getClass().getResourceAsStream("/img/logo.png");
        if (is != null) {
            try (var stream = is) {
                ImageView logoView = new ImageView();
                Image logo = new Image(stream);
                logoView.setImage(logo);
                logoView.setFitWidth(80);
                logoView.setFitHeight(80);
                logoView.setPreserveRatio(true);
                return logoView;
            } catch (Exception e) {
                System.out.println("Error al leer el logo, usando placeholder: " + e.getMessage());
            }
        } else {
            System.out.println("No se encontró el logo en recursos, usando placeholder");
        }

        // Fallback visual: círculo rojo con letra "Q"
        StackPane placeholder = new StackPane();
        Circle circle = new Circle(40, Color.web("#DC143C"));
        Label letter = new Label("Q");
        letter.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        letter.setTextFill(Color.WHITE);
        placeholder.getChildren().addAll(circle, letter);
        return placeholder;
    }

    /**
     * Muestra un mensaje de alerta
     */
    public void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        // Estilo del botón de la alerta
        DialogPane dialogPane = alerta.getDialogPane();
        try {
            // Comprobar URL del CSS antes de usar
            var cssUrl = getClass().getResource("/css/styles.css");
            if (cssUrl != null) {
                dialogPane.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception ignored) {
        }

        alerta.showAndWait();
    }

    // Getters para que el controlador pueda obtener los valores
    public String getUsuario() {
        return txtUsuario.getText();
    }

    public String getPassword() {
        return txtPassword.getText();
    }

    public Stage getStage() {
        return stage;
    }

    public void setOnLoginHandler(Runnable onLoginHandler) {
        this.onLoginHandler = onLoginHandler;
    }
}
