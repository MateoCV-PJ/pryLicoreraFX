package com.licoreraFx.view;

import com.licoreraFx.controller.LoginController;
import com.licoreraFx.util.SesionActual;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MenuAdministradorView {
    public void mostrar(Stage stage) {
        Label title = new Label("Menú Administrador");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // Barra horizontal superior con botones de gestión y botón Cerrar sesión a la derecha
        Button bVendedoresTop = new Button("Vendedores");
        Button bClientesTop = new Button("Clientes");
        Button bProveedoresTop = new Button("Proveedores");
        Button bInventarioTop = new Button("Inventario");
        // Ventas (gestión solicitada)
        Button bVentasTop = new Button("Ventas");
        // Compras (gestión solicitada)
        Button bComprasTop = new Button("Compras");

        Button btnCerrarSesionTop = new Button("Cerrar sesión");
        btnCerrarSesionTop.setStyle("-fx-background-color: transparent; -fx-underline: true; -fx-text-fill: #333;");

        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(8, 12, 8, 12));
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Espaciador para empujar el botón de cerrar sesión a la derecha
        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        topBar.getChildren().addAll(bVendedoresTop, bClientesTop, bProveedoresTop, bInventarioTop, bVentasTop, bComprasTop, topSpacer, btnCerrarSesionTop);

        // -- Selección visual para botones superiores --
        Button[] topButtons = new Button[] { bVendedoresTop, bClientesTop, bProveedoresTop, bInventarioTop, bVentasTop, bComprasTop };
        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: #333;";
        String selectedStyle = "-fx-background-color: #E6E6E6; -fx-text-fill: #000; -fx-background-radius: 6;";
        for (Button tb : topButtons) tb.setStyle(defaultStyle);
        java.util.function.Consumer<Button> setSelectedTop = btn -> {
            for (Button tb : topButtons) {
                if (tb == btn) tb.setStyle(selectedStyle);
                else tb.setStyle(defaultStyle);
            }
        };
        // -- end selección visual --

        // Area central donde se mostrará la gestión seleccionada
        VBox contentArea = new VBox(10);
        contentArea.setPadding(new Insets(20));
        contentArea.setAlignment(Pos.TOP_CENTER);
        Label contentTitle = new Label("Seleccione una gestión");
        contentTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label contentPlaceholder = new Label("Aquí se mostrará la información y controles de la gestión seleccionada.");
        contentPlaceholder.setWrapText(true);
        contentArea.getChildren().addAll(contentTitle, contentPlaceholder);

        // Variable mutable para rastrear la gestión actual
        final String[] gestionActual = new String[1];

        // Helper local para crear la barra de acciones dentro del contentArea
        java.util.function.BiConsumer<String, VBox> agregarBarraAcciones = (nombreGestion, area) -> {
            HBox actions = new HBox(8);
            actions.setPadding(new Insets(8, 0, 0, 0));
            actions.setAlignment(Pos.CENTER_RIGHT);

            // Decide qué botones mostrar: por requerimiento, quitar 'Guardar' en ciertas gestiones
            boolean mostrarGuardar = true;
            // quitar Guardar para estas gestiones (incluye sub-gestiones de Facturas)
            if ("Vendedores".equals(nombreGestion)
                    || "Clientes".equals(nombreGestion)
                    || "Proveedores".equals(nombreGestion)
                    || "Inventario".equals(nombreGestion)
                    || "Ventas".equals(nombreGestion)
                    || nombreGestion.startsWith("Facturas")) {
                mostrarGuardar = false;
            }

            // Botones posibles
            Button btnGuardar = null;
            Button btnModificar = null;
            Button btnEliminar = new Button("Eliminar");
            Button btnAnadir = null;

            // Añadir: incluir para Clientes, Vendedores, Proveedores y también para Inventario
            if ("Clientes".equals(nombreGestion) || "Vendedores".equals(nombreGestion) || "Proveedores".equals(nombreGestion) || "Inventario".equals(nombreGestion)) {
                btnAnadir = new Button("Añadir");
                btnAnadir.setOnAction(ev -> ejecutarAccion("Añadir", nombreGestion));
            }

            // Modificar: omitir para 'Nueva Venta' (aunque ya no existe esa opción aquí)
            if (!"Nueva Venta".equals(nombreGestion)) {
                btnModificar = new Button("Modificar");
                btnModificar.setOnAction(ev -> ejecutarAccion("Modificar", nombreGestion));
            }

            if (mostrarGuardar) {
                btnGuardar = new Button("Guardar");
                btnGuardar.setOnAction(ev -> ejecutarAccion("Guardar", nombreGestion));
            }

            btnEliminar.setOnAction(ev -> ejecutarAccion("Eliminar", nombreGestion));

            // Orden: (Guardar opcional), Añadir opcional, Modificar opcional, Eliminar
            if (btnGuardar != null) actions.getChildren().add(btnGuardar);
            if (btnAnadir != null) actions.getChildren().add(btnAnadir);
            if (btnModificar != null) actions.getChildren().add(btnModificar);
            actions.getChildren().add(btnEliminar);

            area.getChildren().add(actions);
        };

        // Handler que actualiza el contentArea cuando se selecciona una gestión
        java.util.function.BiConsumer<String, Button> seleccionarGestion = (nombre, boton) -> {
            gestionActual[0] = nombre;
            // marcar botón seleccionado
            setSelectedTop.accept(boton);
            contentTitle.setText(nombre);
            contentArea.getChildren().clear();
            Label encabezado = new Label("Gestión: " + nombre);
            encabezado.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            Label info = new Label("Aquí aparecerán los datos, filtros y tablas relacionados con '" + nombre + "'.");
            info.setWrapText(true);

            // Ejemplo de espacio para una tabla o formulario (placeholder)
            Region mockRegion = new Region();
            mockRegion.setPrefHeight(260);
            mockRegion.setStyle("-fx-border-color: #DDD; -fx-border-style: solid; -fx-background-color: #FAFAFA;");

            contentArea.getChildren().addAll(encabezado, info, mockRegion);
            // Agregar barra de acciones específica para esta gestión
            agregarBarraAcciones.accept(nombre, contentArea);
        };

        // Asociar botones superiores a la selección
        bVendedoresTop.setOnAction(e -> {
            setSelectedTop.accept(bVendedoresTop);
            // Mostrar la vista real de Vendedores (la propia vista incluye sus botones de acción)
            com.licoreraFx.view.VendedoresView vv = new com.licoreraFx.view.VendedoresView();
            vv.mostrar(contentArea);
        });
        bClientesTop.setOnAction(e -> {
            setSelectedTop.accept(bClientesTop);
            // Mostrar la vista real de Clientes
            com.licoreraFx.view.ClientesView cv = new com.licoreraFx.view.ClientesView();
            cv.mostrar(contentArea);
        });
        bProveedoresTop.setOnAction(e -> {
            setSelectedTop.accept(bProveedoresTop);
            // Mostrar la vista real de Proveedores
            com.licoreraFx.view.ProveedoresView pv = new com.licoreraFx.view.ProveedoresView();
            pv.mostrar(contentArea);
        });
        bInventarioTop.setOnAction(e -> {
            setSelectedTop.accept(bInventarioTop);
            // Mostrar la vista real de Inventario
            com.licoreraFx.view.InventarioView iv = new com.licoreraFx.view.InventarioView();
            iv.mostrar(contentArea);
        });
        bVentasTop.setOnAction(e -> {
            setSelectedTop.accept(bVentasTop);
            // Mostrar la vista real de Ventas
            com.licoreraFx.view.VentasView vv = new com.licoreraFx.view.VentasView();
            vv.mostrar(contentArea);
        });
        bComprasTop.setOnAction(e -> {
            setSelectedTop.accept(bComprasTop);
            // Mostrar la vista real de Compras
            com.licoreraFx.view.ComprasView cv = new com.licoreraFx.view.ComprasView();
            cv.mostrar(contentArea);
        });


        // Cerrar sesión: volver al LoginView
        btnCerrarSesionTop.setOnAction(e -> {
            // Limpiar la sesión actual
            SesionActual.clear();
            // Crear la vista y controlador del login y conectarlos (igual que en MainApp)
            LoginView loginView = new LoginView();
            LoginController loginController = new LoginController(loginView);
            // Usar referencia de método en lugar de lambda (más limpio)
            loginView.setOnLoginHandler(loginController::onLoginAction);

            // Mostrar el login en una nueva Stage y cerrar sólo la ventana actual
            javafx.application.Platform.runLater(() -> {
                Stage loginStage = new Stage();
                loginView.mostrar(loginStage);
                // Cerrar la ventana actual (menú)
                stage.close();
            });
        });

        // Mostrar 'Vendedores' por defecto al abrir el menú: ejecutar el handler del botón
        bVendedoresTop.fire();

        // Usar BorderPane para posición superior/centro/inferior (sin pie global: acciones se agregan por gestión)
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(contentArea);

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Menú Administrador");
        stage.setScene(scene);
        stage.show();
    }


    // Ejecuta la acción solicitada según la gestión actual (placeholders específicos)
    private void ejecutarAccion(String accion, String gestion) {
        String title = accion + " - " + gestion;
        String message;
        switch (gestion) {
            case "Clientes":
                message = accion.equals("Guardar") ? "Se guardó (placeholder) un cliente nuevo." :
                          accion.equals("Modificar") ? "Se modificó (placeholder) el cliente seleccionado." :
                          "Se eliminó (placeholder) el cliente seleccionado.";
                break;
            case "Vendedores":
                message = accion + " (placeholder) in Vendedores.";
                break;
            case "Proveedores":
                message = accion + " (placeholder) in Proveedores.";
                break;
            case "Inventario":
                message = accion + " (placeholder) in Inventario de productos.";
                break;
            case "Nueva Venta":
                message = accion.equals("Guardar") ? "Venta registrada (placeholder)." : accion + " (placeholder) en Venta.";
                break;
            case "Ventas":
                message = accion + " (placeholder) in Ventas (listado).";
                break;
            case "Compras":
                message = accion + " (placeholder) in Compras a Proveedores.";
                break;
            default:
                message = accion + " (placeholder) in " + gestion + ".";
        }

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    private void mostrarPlaceholder(String nombre) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Funcionalidad");
        alert.setHeaderText(null);
        alert.setContentText("La acción: '" + nombre + "' aún no está implementada en esta vista.");
        alert.showAndWait();
    }
}
