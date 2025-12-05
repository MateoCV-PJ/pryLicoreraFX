package com.licoreraFx.view;

import com.licoreraFx.model.Proveedor;
import com.licoreraFx.util.JsonManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.regex.Pattern;

public class ProveedoresView {

    private TableView<Proveedor> table;
    private ObservableList<Proveedor> masterData;
    private FilteredList<Proveedor> filtered;

    public Node createView() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Proveedores");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField tfSearch = new TextField();
        tfSearch.getStyleClass().add("search-field");
        tfSearch.setPromptText("Buscar proveedor por nombre de empresa, email, teléfono o RUT...");
        tfSearch.setMaxWidth(Double.MAX_VALUE);
        HBox searchBox = new HBox(8, tfSearch);
        HBox.setHgrow(tfSearch, Priority.ALWAYS);

        List<Proveedor> proveedores = JsonManager.listarProveedores();
        masterData = FXCollections.observableArrayList(proveedores);

        table = new TableView<>();
        table.getStyleClass().add("proveedores-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(320);
        table.setMinHeight(200);
        table.setMaxHeight(Region.USE_PREF_SIZE);

        TableColumn<Proveedor, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));

        TableColumn<Proveedor, String> colNombreEmpresa = new TableColumn<>("Nombre Empresa");
        colNombreEmpresa.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombreEmpresa()));

        TableColumn<Proveedor, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmail()));

        TableColumn<Proveedor, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTelefono()));

        TableColumn<Proveedor, String> colRut = new TableColumn<>("RUT");
        colRut.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRut()));

        //noinspection unchecked
        table.getColumns().addAll(colId, colNombreEmpresa, colEmail, colTelefono, colRut);

        TableColumn<Proveedor, Void> colAccion = new TableColumn<>("Acción");
        colAccion.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Nueva Compra");

            {
                btn.setOnAction(event -> {
                    Proveedor p = getTableView().getItems().get(getIndex());
                    showNuevaCompraDialog(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        //noinspection unchecked
        table.getColumns().add(colAccion);

        filtered = new FilteredList<>(masterData, p -> true);
        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal == null ? "" : newVal.trim().toLowerCase();
            filtered.setPredicate(p -> {
                if (q.isEmpty()) return true;
                return (p.getNombreEmpresa() != null && p.getNombreEmpresa().toLowerCase().contains(q)) ||
                        (p.getEmail() != null && p.getEmail().toLowerCase().contains(q)) ||
                        (p.getTelefono() != null && p.getTelefono().toLowerCase().contains(q)) ||
                        (p.getRut() != null && p.getRut().toLowerCase().contains(q)) ||
                        (p.getId() != null && p.getId().toLowerCase().contains(q));
            });
        });

        SortedList<Proveedor> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        Button btnAnadir = new Button("Añadir");
        Button btnModificar = new Button("Modificar");
        Button btnEliminar = new Button("Eliminar");

        btnAnadir.setOnAction(e -> showAgregarProveedorDialog());
        btnModificar.setOnAction(e -> showModificarProveedorDialog());
        btnEliminar.setOnAction(e -> {
            Proveedor sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                new Alert(Alert.AlertType.WARNING, "Selecciona un proveedor para eliminar.", ButtonType.OK).showAndWait();
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar el proveedor '" + sel.getNombreEmpresa() + "'?", ButtonType.YES, ButtonType.NO);
            confirm.initModality(Modality.APPLICATION_MODAL);
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    boolean ok = JsonManager.eliminarProveedor(sel.getId());
                    if (ok) {
                        cargarDatos();
                        new Alert(Alert.AlertType.INFORMATION, "Proveedor eliminado.", ButtonType.OK).showAndWait();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "No se pudo eliminar el proveedor.", ButtonType.OK).showAndWait();
                    }
                }
            });
        });

        HBox actions = new HBox(8, btnAnadir, btnModificar, btnEliminar);
        actions.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(titulo, searchBox, table, actions);
        VBox.setVgrow(table, Priority.ALWAYS);

        return root;
    }

    public void mostrar(VBox contentArea) {
        Node view = createView();
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }

    private void cargarDatos() {
        List<Proveedor> proveedores = JsonManager.listarProveedores();
        masterData.setAll(proveedores);
    }

    private void showAgregarProveedorDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Agregar Proveedor");

        javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
        root.setPadding(new Insets(12));

        TextField tfNombreEmpresa = new TextField();
        tfNombreEmpresa.setPromptText("Nombre de la empresa");
        TextField tfEmail = new TextField();
        tfEmail.setPromptText("Email");
        TextField tfTelefono = new TextField();
        tfTelefono.setPromptText("Teléfono");
        TextField tfRut = new TextField();
        tfRut.setPromptText("RUT");

        Button btnSave = new Button("Guardar");
        Button btnCancel = new Button("Cancelar");

        Label lblError = new Label();
        lblError.getStyleClass().add("dialog-error");

        btnSave.setOnAction(e -> {
            String nombreEmpresa = tfNombreEmpresa.getText().trim();
            String email = tfEmail.getText().trim();
            String telefono = tfTelefono.getText().trim();
            String rut = tfRut.getText().trim();

            if (nombreEmpresa.isEmpty() || email.isEmpty() || telefono.isEmpty() || rut.isEmpty()) {
                lblError.setText("Todos los campos son obligatorios.");
                return;
            }

            Proveedor nuevo = new Proveedor(null, nombreEmpresa, email, telefono, rut);
            boolean ok = JsonManager.agregarProveedor(nuevo);
            if (ok) {
                cargarDatos();
                dialog.close();
            } else {
                lblError.setText("No se pudo agregar el proveedor.");
            }
        });

        btnCancel.setOnAction(ev -> dialog.close());

        HBox btns = new HBox(8, btnSave, btnCancel);
        btns.setAlignment(Pos.CENTER_RIGHT);

        VBox formBox = new VBox(8, new Label("Nombre Empresa:"), tfNombreEmpresa, new Label("Email:"), tfEmail, new Label("Teléfono:"), tfTelefono, new Label("RUT:"), tfRut, lblError);
        root.setCenter(formBox);
        root.setBottom(btns);

        Scene scene = new Scene(root, 400, 300);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showModificarProveedorDialog() {
        Proveedor sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona un proveedor para modificar.").showAndWait();
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Modificar Proveedor");

        javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
        root.setPadding(new Insets(12));

        TextField tfNombreEmpresa = new TextField(sel.getNombreEmpresa());
        TextField tfEmail = new TextField(sel.getEmail());
        TextField tfTelefono = new TextField(sel.getTelefono());
        TextField tfRut = new TextField(sel.getRut());

        Button btnSave = new Button("Guardar");
        Button btnCancel = new Button("Cancelar");

        Label lblError = new Label();
        lblError.getStyleClass().add("dialog-error");

        btnSave.setOnAction(e -> {
            String nombreEmpresa = tfNombreEmpresa.getText().trim();
            String email = tfEmail.getText().trim();
            String telefono = tfTelefono.getText().trim();
            String rut = tfRut.getText().trim();

            if (nombreEmpresa.isEmpty() || email.isEmpty() || telefono.isEmpty() || rut.isEmpty()) {
                lblError.setText("Todos los campos son obligatorios.");
                return;
            }

            Proveedor actualizado = new Proveedor(sel.getId(), nombreEmpresa, email, telefono, rut);
            boolean ok = JsonManager.actualizarProveedor(sel.getId(), actualizado);
            if (ok) {
                cargarDatos();
                dialog.close();
            } else {
                lblError.setText("No se pudo actualizar el proveedor.");
            }
        });

        btnCancel.setOnAction(ev -> dialog.close());

        HBox btns = new HBox(8, btnSave, btnCancel);
        btns.setAlignment(Pos.CENTER_RIGHT);

        VBox formBox = new VBox(8, new Label("Nombre Empresa:"), tfNombreEmpresa, new Label("Email:"), tfEmail, new Label("Teléfono:"), tfTelefono, new Label("RUT:"), tfRut, lblError);
        root.setCenter(formBox);
        root.setBottom(btns);

        Scene scene = new Scene(root, 400, 300);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showNuevaCompraDialog(Proveedor proveedor) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Nueva Compra - " + proveedor.getNombreEmpresa());

        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.TOP_LEFT);

        Label info = new Label("Crear compra para proveedor:");
        info.setStyle("-fx-font-weight: bold;");
        Label lblNombre = new Label("Proveedor: " + proveedor.getNombreEmpresa());

        // Aquí puedes agregar más campos para la nueva compra

        Button btnCrear = new Button("Crear Compra");
        Button btnCancelar = new Button("Cancelar");
        HBox btns = new HBox(8, btnCrear, btnCancelar);
        btns.setAlignment(Pos.CENTER_RIGHT);

        btnCrear.setOnAction(e -> {
            // Lógica para crear la nueva compra
            new Alert(Alert.AlertType.INFORMATION, "Nueva compra creada para " + proveedor.getNombreEmpresa()).showAndWait();
            dialog.close();
        });
        btnCancelar.setOnAction(ev -> dialog.close());

        root.getChildren().addAll(info, lblNombre, btns);
        Scene scene = new Scene(root, 400, 200);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
