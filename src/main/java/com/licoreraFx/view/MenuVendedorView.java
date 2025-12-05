package com.licoreraFx.view;

import com.licoreraFx.controller.LoginController;
import com.licoreraFx.util.SesionActual;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MenuVendedorView {
    public void mostrar(Stage stage) {
        Label title = new Label("Menú Vendedor");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // Barra horizontal superior con botones de gestión (solo vista) y botón Cerrar sesión a la derecha
        Button bClientesTop = new Button("Clientes");
        Button bInventarioTop = new Button("Inventario");
        Button bVentasTop = new Button("Ventas");
        Button bFacturasTop = new Button("Facturas");

        Button btnCerrarSesionTop = new Button("Cerrar sesión");
        btnCerrarSesionTop.setStyle("-fx-background-color: transparent; -fx-underline: true; -fx-text-fill: #333;");

        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(8, 12, 8, 12));
        topBar.setAlignment(Pos.CENTER_LEFT);

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        topBar.getChildren().addAll(bClientesTop, bInventarioTop, bVentasTop, bFacturasTop, topSpacer, btnCerrarSesionTop);

        // -- Selección visual para botones superiores (vendedor) --
        Button[] topButtons = new Button[] { bClientesTop, bInventarioTop, bVentasTop, bFacturasTop };
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
        Label contentPlaceholder = new Label("Aquí se mostrará la información en modo solo vista para la selección.");
        contentPlaceholder.setWrapText(true);
        contentArea.getChildren().addAll(contentTitle, contentPlaceholder);

        // Helper dinámico para crear la barra de acciones (usa el Stage owner para dialogs)
        java.util.function.BiConsumer<String, VBox> agregarBarraAcciones = (nombreGestion, area) -> {
            // Para Inventario el vendedor solo ve (sin botones de acción)
            if ("Inventario".equals(nombreGestion)) {
                return;
            }
            HBox actions = new HBox(8);
            actions.setPadding(new Insets(8, 0, 0, 0));
            actions.setAlignment(Pos.CENTER_RIGHT);

            Button btnGuardar = new Button("Guardar");
            Button btnEliminar = new Button("Eliminar");
            Button btnAnadir = null;
            Button btnModificar = null;

            // Añadir solo para Clientes, Vendedores, Proveedores
            if ("Clientes".equals(nombreGestion) || "Vendedores".equals(nombreGestion) || "Proveedores".equals(nombreGestion)) {
                btnAnadir = new Button("Añadir");
                // Para Clientes, abrir diálogo real; para otros mostrar placeholder
                btnAnadir.setOnAction(ev -> {
                    if ("Clientes".equals(nombreGestion)) {
                        showAgregarClienteDialog(stage);
                    } else {
                        ejecutarAccion("Añadir", nombreGestion);
                    }
                });
            }

            // Modificar: omitir para 'Nueva Venta'
            if (!"Nueva Venta".equals(nombreGestion)) {
                btnModificar = new Button("Modificar");
                btnModificar.setOnAction(ev -> ejecutarAccion("Modificar", nombreGestion));
            }

            btnGuardar.setOnAction(ev -> ejecutarAccion("Guardar", nombreGestion));
            btnEliminar.setOnAction(ev -> ejecutarAccion("Eliminar", nombreGestion));

            actions.getChildren().add(btnGuardar);
            if (btnAnadir != null) actions.getChildren().add(btnAnadir);
            if (btnModificar != null) actions.getChildren().add(btnModificar);
            actions.getChildren().add(btnEliminar);

            area.getChildren().add(actions);
        };

        // Selector general para mostrar contenido (solo vista)
        java.util.function.Consumer<String> mostrarVista = nombre -> {
            contentTitle.setText(nombre);
            contentArea.getChildren().clear();
            Label encabezado = new Label("Vista: " + nombre);
            encabezado.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            Label info = new Label("Modo solo vista: aquí aparecerán listas, filtros y detalles, sin posibilidad de edición desde esta pantalla.");
            info.setWrapText(true);

            Region mockRegion = new Region();
            mockRegion.setPrefHeight(300);
            mockRegion.setStyle("-fx-border-color: #DDD; -fx-border-style: solid; -fx-background-color: #FAFAFA;");

            contentArea.getChildren().addAll(encabezado, info, mockRegion);
            // Agregar barra de acciones dinámica
            agregarBarraAcciones.accept(nombre, contentArea);
        };

        // Asociaciones generales
        bInventarioTop.setOnAction(e -> { setSelectedTop.accept(bInventarioTop); mostrarVista.accept("Inventario"); });
        bVentasTop.setOnAction(e -> { setSelectedTop.accept(bVentasTop); mostrarVista.accept("Ventas"); });

        // Facturas: mostrar selector dentro del contentArea
        bFacturasTop.setOnAction(e -> {
            // marcar visualmente el botón Facturas
            setSelectedTop.accept(bFacturasTop);
            contentTitle.setText("Facturas");
            contentArea.getChildren().clear();
            Label encabezado = new Label("Facturas");
            encabezado.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            // Mostrar directamente la opción de facturas de clientes para el vendedor
            Button opcionClientes = new Button("Facturas de Clientes");

            opcionClientes.setOnAction(ev -> {
                // mantener marcado el botón Facturas mientras se muestran las sub-opciones
                setSelectedTop.accept(bFacturasTop);
                contentArea.getChildren().clear();
                Label h = new Label("Facturas de Clientes (solo vista)");
                h.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                Label t = new Label("Listado de facturas de venta. Aquí puede ver detalles pero no editar.");
                t.setWrapText(true);
                Region r = new Region(); r.setPrefHeight(260); r.setStyle("-fx-border-color: #DDD; -fx-background-color: #FFFDF6;");
                contentArea.getChildren().addAll(h, t, r);
                // agregar acciones para esta sub-gestión
                agregarBarraAcciones.accept("Facturas - Clientes", contentArea);
            });

            HBox opciones = new HBox(10, opcionClientes);
            opciones.setAlignment(Pos.CENTER);

            Region mockRegion = new Region(); mockRegion.setPrefHeight(200); mockRegion.setStyle("-fx-border-color: #DDD; -fx-background-color: #FAFAFA;");
            contentArea.getChildren().addAll(encabezado, opciones, mockRegion);
        });

        // Clientes: mostrar tabla placeholder y botones Agregar/Modificar
        bClientesTop.setOnAction(e -> { setSelectedTop.accept(bClientesTop); showClientesView(contentArea); agregarBarraAcciones.accept("Clientes", contentArea); });

        // Cerrar sesión: limpiar sesión y abrir Login en nueva Stage
        btnCerrarSesionTop.setOnAction(e -> {
            SesionActual.clear();
            LoginView loginView = new LoginView();
            LoginController loginController = new LoginController(loginView);
            loginView.setOnLoginHandler(loginController::onLoginAction);
            javafx.application.Platform.runLater(() -> {
                Stage loginStage = new Stage();
                loginView.mostrar(loginStage);
                stage.close();
            });
        });

        // Mostrar 'Clientes' por defecto al abrir el menú (en lugar de 'Nueva Venta')
        setSelectedTop.accept(bClientesTop);
        mostrarVista.accept("Clientes");

        // No footer CRUD for vendedor (vista sólo)
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(contentArea);

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Menú Vendedor");
        stage.setScene(scene);
        stage.show();
    }

    // Muestra la vista de clientes con botones para agregar o modificar
    private void showClientesView(VBox contentArea) {
         contentArea.getChildren().clear();
         Label encabezado = new Label("Clientes");
         encabezado.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
         Label info = new Label("Listado de clientes. Selecciona uno para modificar o presiona 'Agregar cliente' para crear uno nuevo.");
         info.setWrapText(true);

         Region tableMock = new Region();
         tableMock.setPrefHeight(260);
         tableMock.setStyle("-fx-border-color: #DDD; -fx-background-color: #FFFDF6;");

         // Las acciones (Guardar/Añadir/Modificar/Eliminar) se agregan dinámicamente desde la vista principal
         contentArea.getChildren().addAll(encabezado, info, tableMock);
     }

    // Diálogo modal simple para agregar cliente (placeholder)
    private void showAgregarClienteDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Agregar cliente");

        VBox root = new VBox(10);
        root.setPadding(new Insets(12));

        Label lblName = new Label("Nombre:");
        TextField tfName = new TextField();
        Label lblPhone = new Label("Teléfono:");
        TextField tfPhone = new TextField();
        Label lblEmail = new Label("Email:");
        TextField tfEmail = new TextField();

        Button btnSave = new Button("Guardar");
        Button btnCancel = new Button("Cancelar");

        HBox btns = new HBox(8, btnSave, btnCancel);
        btns.setAlignment(Pos.CENTER_RIGHT);

        btnSave.setOnAction(e -> {
            // Placeholder: aquí podrías validar y guardar en JSON usando JsonManager
            String nombre = tfName.getText().trim();
            String telefono = tfPhone.getText().trim();
            String email = tfEmail.getText().trim();
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Cliente agregado");
            a.setHeaderText(null);
            a.setContentText("Cliente '" + nombre + "' agregado (placeholder).\nTel: " + telefono + "\nEmail: " + email);
            a.showAndWait();
            dialog.close();
        });

        btnCancel.setOnAction(e -> dialog.close());

        root.getChildren().addAll(lblName, tfName, lblPhone, tfPhone, lblEmail, tfEmail, btns);

        Scene scene = new Scene(root, 360, 300);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // Ejecuta la acción solicitada según la gestión actual (placeholders específicos)
    private void ejecutarAccion(String accion, String gestion) {
        String title = accion + " - " + gestion;
        String message = accion + " (placeholder) en " + gestion + ".";
        if ("Nueva Venta".equals(gestion) && "Guardar".equals(accion)) {
            message = "Venta registrada (placeholder).";
        }
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
