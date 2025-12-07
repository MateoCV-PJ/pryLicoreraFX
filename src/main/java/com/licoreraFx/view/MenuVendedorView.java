package com.licoreraFx.view;

import com.licoreraFx.controller.LoginController;
import com.licoreraFx.repository.ClienteRepository;
import com.licoreraFx.model.Cliente;

import com.licoreraFx.util.SesionActual;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

@SuppressWarnings("unused")
public class MenuVendedorView {
    public void mostrar(Stage stage) {
        Label title = new Label("Menú Vendedor");

        // Controlador cliente para acciones puntuales (nueva venta)
        // Nota: se crea y usa directamente cuando se necesita en otras vistas, por eso no se mantiene aquí.

        // Barra horizontal superior con botones de gestión (solo vista) y botón Cerrar sesión a la derecha
        Button bClientesTop = new Button("Clientes");
        Button bInventarioTop = new Button("Inventario");
        Button bVentasTop = new Button("Ventas");

        Button btnCerrarSesionTop = new Button("Cerrar sesión");

        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(8, 12, 8, 12));
        topBar.setAlignment(Pos.CENTER_LEFT);

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        topBar.getChildren().addAll(bClientesTop, bInventarioTop, bVentasTop, topSpacer, btnCerrarSesionTop);

        // -- Selección visual para botones superiores (vendedor) --
        Button[] topButtons = new Button[] { bClientesTop, bInventarioTop, bVentasTop };
        // Eliminadas las reglas CSS hardcodeadas; se mantiene lógica para marcar selección aplicando estilos por defecto
        java.util.function.Consumer<Button> setSelectedTop = btn -> {
            for (Button tb : topButtons) {
                // limpiar estilos personalizados
                tb.setStyle("");
            }
            // marcar el seleccionado con estilo nativo (sin forzar reglas)
            btn.setStyle("");
        };
        // -- end selección visual --

        // Area central donde se mostrará la gestión seleccionada
        VBox contentArea = new VBox(10);
        contentArea.setPadding(new Insets(20));
        contentArea.setAlignment(Pos.TOP_CENTER);
        Label contentTitle = new Label("Seleccione una gestión");
        Label contentPlaceholder = new Label("Aquí se mostrará la información en modo vendedor (igual al administrador).");
        contentPlaceholder.setWrapText(true);
        contentArea.getChildren().addAll(contentTitle, contentPlaceholder);

        // Asociaciones generales: cargar las mismas vistas que el administrador
        bInventarioTop.setOnAction(e -> {
            setSelectedTop.accept(bInventarioTop);
            com.licoreraFx.view.InventarioView iv = new com.licoreraFx.view.InventarioView();
            // cargar inventario en modo vendedor (sin botones Añadir/Modificar/Eliminar)
            iv.mostrar(contentArea, true);
        });

        bVentasTop.setOnAction(e -> {
            setSelectedTop.accept(bVentasTop);
            com.licoreraFx.view.VentasView vv = new com.licoreraFx.view.VentasView();
            vv.mostrar(contentArea);
        });

        bClientesTop.setOnAction(e -> {
            setSelectedTop.accept(bClientesTop);
            com.licoreraFx.view.ClientesView cv = new com.licoreraFx.view.ClientesView();
            cv.mostrar(contentArea, true); // cargar la vista de clientes en modo vendedor
        });


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

        // Mostrar 'Clientes' por defecto
        setSelectedTop.accept(bClientesTop);
        com.licoreraFx.view.ClientesView cv = new com.licoreraFx.view.ClientesView();
        cv.mostrar(contentArea, true);

        // No footer CRUD for vendedor (la vista cargada ya incluye sus acciones permitidas)
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(contentArea);

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Menú Vendedor");
        stage.setScene(scene);
        stage.show();
    }

    // Mantengo los diálogos de añadir/modificar cliente para compatibilidad, pero los controladores muestran la UI completa.
    private void showAgregarClienteDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Agregar cliente");

        VBox root = new VBox(8); root.setPadding(new Insets(12));
        javafx.scene.control.TextField tfNombre = new javafx.scene.control.TextField(); tfNombre.setPromptText("Nombre completo");
        javafx.scene.control.TextField tfEmail = new javafx.scene.control.TextField(); tfEmail.setPromptText("Email");
        javafx.scene.control.TextField tfDireccion = new javafx.scene.control.TextField(); tfDireccion.setPromptText("Dirección");
        javafx.scene.control.TextField tfDocumento = new javafx.scene.control.TextField(); tfDocumento.setPromptText("Documento");
        Label lblError = new Label();

        Button btnSave = new Button("Guardar"); Button btnCancel = new Button("Cancelar");
        btnSave.setOnAction(e -> {
            String nombre = tfNombre.getText().trim(); String email = tfEmail.getText().trim(); String direccion = tfDireccion.getText().trim(); String documento = tfDocumento.getText().trim();
            if (nombre.isEmpty() || email.isEmpty() || direccion.isEmpty() || documento.isEmpty()) { lblError.setText("Todos los campos son obligatorios."); return; }
            Cliente nuevo = new Cliente(null, nombre, email, direccion, documento);
            boolean ok = ClienteRepository.agregarCliente(nuevo);
            if (ok) { dialog.close(); new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Cliente agregado.").showAndWait(); }
            else { lblError.setText("No se pudo agregar (id duplicado o error)."); }
        });
        btnCancel.setOnAction(ev -> dialog.close());

        HBox btns = new HBox(8, btnSave, btnCancel); btns.setAlignment(Pos.CENTER_RIGHT);
        root.getChildren().addAll(new Label("Nombre:"), tfNombre, new Label("Email:"), tfEmail, new Label("Dirección:"), tfDireccion, new Label("Documento:"), tfDocumento, lblError, btns);
        Scene sceneAdd = new Scene(root, 420, 360);
        dialog.setScene(sceneAdd); dialog.showAndWait();
    }

    // Muestra diálogo para modificar cliente existente
    private void showModificarClienteDialog(Stage owner) {
        List<Cliente> clientes = ClienteRepository.listarClientes();
        if (clientes.isEmpty()) { new Alert(Alert.AlertType.WARNING, "No hay clientes para modificar.").showAndWait(); return; }
        Stage dlg = new Stage(); dlg.initOwner(owner); dlg.initModality(Modality.APPLICATION_MODAL); dlg.setTitle("Modificar cliente");
        VBox root = new VBox(8); root.setPadding(new Insets(12));
        ComboBox<Cliente> cb = new ComboBox<>(); cb.getItems().addAll(clientes);
        cb.setConverter(new javafx.util.StringConverter<>() { public String toString(Cliente c){ return c==null?"":(c.getNombre()!=null?c.getNombre():c.getId()); } public Cliente fromString(String s){return null;} });
        TextField tfNombre = new TextField(); TextField tfEmail = new TextField(); TextField tfDireccion = new TextField(); TextField tfDocumento = new TextField();
        Label lblError = new Label();
        cb.setOnAction(ev -> {
            Cliente sel = cb.getValue(); if (sel != null) { tfNombre.setText(sel.getNombre()); tfEmail.setText(sel.getEmail()); tfDireccion.setText(sel.getDireccion()); tfDocumento.setText(sel.getDocumento()); }
        });
        Button btnSave = new Button("Guardar"); Button btnCancel = new Button("Cancelar");
        btnSave.setOnAction(ev -> {
            Cliente sel = cb.getValue(); if (sel == null) { lblError.setText("Selecciona un cliente."); return; }
            String nombre = tfNombre.getText().trim(); String email = tfEmail.getText().trim(); String direccion = tfDireccion.getText().trim(); String documento = tfDocumento.getText().trim();
            if (nombre.isEmpty() || email.isEmpty() || direccion.isEmpty() || documento.isEmpty()) { lblError.setText("Todos los campos son obligatorios."); return; }
            Cliente actualizado = new Cliente(sel.getId(), nombre, email, direccion, documento);
            boolean ok = ClienteRepository.actualizarCliente(sel.getId(), actualizado);
            if (ok) { dlg.close(); new Alert(Alert.AlertType.INFORMATION, "Cliente actualizado.").showAndWait(); }
            else { lblError.setText("No se pudo actualizar."); }
        });
        btnCancel.setOnAction(ev -> dlg.close());
        HBox btns = new HBox(8, btnSave, btnCancel); btns.setAlignment(Pos.CENTER_RIGHT);
        root.getChildren().addAll(new Label("Selecciona cliente:"), cb, new Label("Nombre:"), tfNombre, new Label("Email:"), tfEmail, new Label("Dirección:"), tfDireccion, new Label("Documento:"), tfDocumento, lblError, btns);
        Scene sceneMod = new Scene(root, 520, 420);
        dlg.setScene(sceneMod); dlg.showAndWait();
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
