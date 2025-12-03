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
        // Nueva venta
        Button bNuevaVentaTop = new Button("Nueva venta");
        // Agrupar facturas en una sola opción
        Button bFacturasTop = new Button("Facturas");

        Button btnCerrarSesionTop = new Button("Cerrar sesión");
        btnCerrarSesionTop.setStyle("-fx-background-color: transparent; -fx-underline: true; -fx-text-fill: #333;");

        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(8, 12, 8, 12));
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Espaciador para empujar el botón de cerrar sesión a la derecha
        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        topBar.getChildren().addAll(bVendedoresTop, bClientesTop, bProveedoresTop, bInventarioTop, bVentasTop, bNuevaVentaTop, bFacturasTop, topSpacer, btnCerrarSesionTop);

        // -- Selección visual para botones superiores --
        Button[] topButtons = new Button[] { bVendedoresTop, bClientesTop, bProveedoresTop, bInventarioTop, bVentasTop, bNuevaVentaTop, bFacturasTop };
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

            Button btnGuardar = new Button("Guardar");
            Button btnEliminar = new Button("Eliminar");
            // Añadir: solo para Clientes, Vendedores y Proveedores
            Button btnAnadir = null;
            if ("Clientes".equals(nombreGestion) || "Vendedores".equals(nombreGestion) || "Proveedores".equals(nombreGestion)) {
                btnAnadir = new Button("Añadir");
                btnAnadir.setOnAction(ev -> ejecutarAccion("Añadir", nombreGestion));
            }

            // Modificar: omitir para 'Nueva Venta'
            Button btnModificar = null;
            if (!"Nueva Venta".equals(nombreGestion)) {
                btnModificar = new Button("Modificar");
                btnModificar.setOnAction(ev -> ejecutarAccion("Modificar", nombreGestion));
            }

            // Guardar y Eliminar siempre
            btnGuardar.setOnAction(ev -> ejecutarAccion("Guardar", nombreGestion));
            btnEliminar.setOnAction(ev -> ejecutarAccion("Eliminar", nombreGestion));

            // Añadir los botones en orden: Guardar, (Añadir), (Modificar), Eliminar
            actions.getChildren().add(btnGuardar);
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
        bVendedoresTop.setOnAction(e -> seleccionarGestion.accept("Vendedores", bVendedoresTop));
        bClientesTop.setOnAction(e -> seleccionarGestion.accept("Clientes", bClientesTop));
        bProveedoresTop.setOnAction(e -> seleccionarGestion.accept("Proveedores", bProveedoresTop));
        bInventarioTop.setOnAction(e -> seleccionarGestion.accept("Inventario", bInventarioTop));
        bVentasTop.setOnAction(e -> seleccionarGestion.accept("Ventas", bVentasTop));

        // Handler para Nueva venta: muestra formulario/placeholder para crear venta
        bNuevaVentaTop.setOnAction(e -> {
            seleccionarGestion.accept("Nueva Venta", bNuevaVentaTop);
            // (el content se actualiza dentro seleccionarGestion)
        });

        // Nuevo comportamiento para 'Facturas': mostrar selector de tipo dentro del contentArea
        bFacturasTop.setOnAction(e -> {
            // marcar la opción Facturas en la barra
            setSelectedTop.accept(bFacturasTop);

            contentTitle.setText("Facturas");
            contentArea.getChildren().clear();
            Label encabezado = new Label("Gestión: Facturas");
            encabezado.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            Label info = new Label("Elija el tipo de factura: Clientes o Proveedores.");
            info.setWrapText(true);

            Button opcionClientes = new Button("Facturas de Clientes");
            Button opcionProveedores = new Button("Facturas de Proveedores");
            opcionClientsSetup(opcionClientes, contentArea, gestionActual);
            opcionProvidersSetup(opcionProveedores, contentArea, gestionActual);

            HBox opciones = new HBox(10, opcionClientes, opcionProveedores);
            opciones.setAlignment(Pos.CENTER);

            Region mockRegion = new Region();
            mockRegion.setPrefHeight(220);
            mockRegion.setStyle("-fx-border-color: #DDD; -fx-border-style: solid; -fx-background-color: #FAFAFA;");

            contentArea.getChildren().addAll(encabezado, info, opciones, mockRegion);
            // Nota: las acciones de Guardar/Modificar/Eliminar se agregan dentro de los helpers de facturas
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

        // Mostrar 'Nueva Venta' por defecto al abrir el menú (y marcar el botón)
        seleccionarGestion.accept("Nueva Venta", bNuevaVentaTop);

        // Usar BorderPane para posición superior/centro/inferior (sin pie global: acciones se agregan por gestión)
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(contentArea);

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Menú Administrador");
        stage.setScene(scene);
        stage.show();
    }

    // Helper para configurar botón 'Facturas de Clientes'
    private void opcionClientsSetup(Button opcionClientes, VBox contentArea, String[] gestionActual) {
        opcionClientes.setOnAction(ev -> {
            gestionActual[0] = "Facturas - Clientes";
            contentArea.getChildren().clear();
            Label encabezado = new Label("Facturas de Clientes");
            encabezado.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            Label info = new Label("Aquí aparecerá la lista de facturas de clientes, filtros y detalles.");
            info.setWrapText(true);
            Region mockRegion = new Region();
            mockRegion.setPrefHeight(260);
            mockRegion.setStyle("-fx-border-color: #DDD; -fx-border-style: solid; -fx-background-color: #FFFDF6;");
            contentArea.getChildren().addAll(encabezado, info, mockRegion);
            // Agregar barra de acciones para facturas de clientes
            HBox actions = new HBox(8);
            actions.setPadding(new Insets(8, 0, 0, 0));
            actions.setAlignment(Pos.CENTER_RIGHT);
            Button btnGuardar = new Button("Guardar");
            Button btnModificar = new Button("Modificar");
            Button btnEliminar = new Button("Eliminar");
            btnGuardar.setOnAction(e -> ejecutarAccion("Guardar", "Facturas - Clientes"));
            btnModificar.setOnAction(e -> ejecutarAccion("Modificar", "Facturas - Clientes"));
            btnEliminar.setOnAction(e -> ejecutarAccion("Eliminar", "Facturas - Clientes"));
            actions.getChildren().addAll(btnGuardar, btnModificar, btnEliminar);
            contentArea.getChildren().add(actions);
        });
    }

    // Helper para configurar botón 'Facturas de Proveedores'
    private void opcionProvidersSetup(Button opcionProveedores, VBox contentArea, String[] gestionActual) {
        opcionProveedores.setOnAction(ev -> {
            gestionActual[0] = "Facturas - Proveedores";
            contentArea.getChildren().clear();
            Label encabezado = new Label("Facturas de Proveedores");
            encabezado.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            Label info = new Label("Aquí aparecerá la lista de facturas de proveedores, filtros y detalles.");
            info.setWrapText(true);
            Region mockRegion = new Region();
            mockRegion.setPrefHeight(260);
            mockRegion.setStyle("-fx-border-color: #DDD; -fx-border-style: solid; -fx-background-color: #FFFDF6;");
            contentArea.getChildren().addAll(encabezado, info, mockRegion);
            // Agregar barra de acciones para facturas de proveedores
            HBox actions = new HBox(8);
            actions.setPadding(new Insets(8, 0, 0, 0));
            actions.setAlignment(Pos.CENTER_RIGHT);
            Button btnGuardar = new Button("Guardar");
            Button btnModificar = new Button("Modificar");
            Button btnEliminar = new Button("Eliminar");
            btnGuardar.setOnAction(e -> ejecutarAccion("Guardar", "Facturas - Proveedores"));
            btnModificar.setOnAction(e -> ejecutarAccion("Modificar", "Facturas - Proveedores"));
            btnEliminar.setOnAction(e -> ejecutarAccion("Eliminar", "Facturas - Proveedores"));
            actions.getChildren().addAll(btnGuardar, btnModificar, btnEliminar);
            contentArea.getChildren().add(actions);
        });
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
                message = accion + " (placeholder) en Vendedores.";
                break;
            case "Proveedores":
                message = accion + " (placeholder) en Proveedores.";
                break;
            case "Inventario":
                message = accion + " (placeholder) en Inventario de productos.";
                break;
            case "Nueva Venta":
                message = accion.equals("Guardar") ? "Venta registrada (placeholder)." : accion + " (placeholder) en Venta.";
                break;
            case "Ventas":
                message = accion + " (placeholder) en Ventas (listado).";
                break;
            case "Facturas - Clientes":
                message = accion + " (placeholder) en Facturas de Clientes.";
                break;
            case "Facturas - Proveedores":
                message = accion + " (placeholder) en Facturas de Proveedores.";
                break;
            default:
                message = accion + " (placeholder) en " + gestion + ".";
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
