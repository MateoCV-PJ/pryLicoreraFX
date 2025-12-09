package com.licoreraFx.controller;

import com.licoreraFx.model.Compra;
import com.licoreraFx.model.Producto;
import com.licoreraFx.model.Proveedor;
import com.licoreraFx.util.JsonManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para gestionar proveedores y crear compras.
 * Permite listar, buscar, añadir, modificar y eliminar proveedores.
 */
public class ProveedoresController {

    private TableView<Proveedor> table;
    private ObservableList<Proveedor> masterData;
    private FilteredList<Proveedor> filtered;

    /**
     * Crea la vista programática de proveedores.
     * @return Nodo con la interfaz de proveedores.
     */
    public Node createView() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Proveedores");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField tfSearch = new TextField();
        tfSearch.setPromptText("Buscar proveedor por nombre de empresa, email, dirección o RUT...");
        tfSearch.setMaxWidth(Double.MAX_VALUE);
        HBox searchBox = new HBox(8, tfSearch);
        HBox.setHgrow(tfSearch, Priority.ALWAYS);

        List<Proveedor> proveedores = JsonManager.listarProveedores();
        masterData = FXCollections.observableArrayList(proveedores);

        table = new TableView<>();
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
        TableColumn<Proveedor, String> colDireccion = new TableColumn<>("Dirección");
        colDireccion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDireccion()));
        TableColumn<Proveedor, String> colRut = new TableColumn<>("RUT");
        colRut.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRut()));
        final java.util.List<TableColumn<Proveedor, ?>> _cols = java.util.Arrays.asList(colId, colNombreEmpresa, colEmail, colDireccion, colRut);
        table.getColumns().addAll(_cols);
        Platform.runLater(() -> {
            try {
                var cols = table.getColumns();
                for (int i = cols.size() - 1; i >= 0; i--) {
                    TableColumn<?,?> col = cols.get(i);
                    String header = col.getText(); boolean hasGraphic = col.getGraphic() != null;
                    if ((header == null || header.trim().isEmpty()) && !hasGraphic) cols.remove(i);
                }
            } catch (Exception ignored) {}
        });

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
                setGraphic(empty ? null : btn);
            }
        });
        table.getColumns().add(colAccion);

        filtered = new FilteredList<>(masterData, p -> true);
        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal == null ? "" : newVal.trim().toLowerCase();
            filtered.setPredicate(p -> {
                if (q.isEmpty()) return true;
                return (p.getNombreEmpresa() != null && p.getNombreEmpresa().toLowerCase().contains(q)) ||
                        (p.getEmail() != null && p.getEmail().toLowerCase().contains(q)) ||
                        (p.getDireccion() != null && p.getDireccion().toLowerCase().contains(q)) ||
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
        btnEliminar.setOnAction(e -> eliminarProveedorSeleccionado());

        HBox actions = new HBox(8, btnAnadir, btnModificar, btnEliminar);
        actions.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(titulo, searchBox, table, actions);
        VBox.setVgrow(table, Priority.ALWAYS);
        return root;
    }

    /**
     * Inserta la vista de proveedores en el área de contenido dada.
     * @param contentArea Contenedor donde se mostrará la vista.
     */
    public void mostrar(VBox contentArea) {
        // Mostrar siempre la vista programática para evitar el uso de FXML
        Node view = createView();
        contentArea.getChildren().setAll(view);
    }

    private void eliminarProveedorSeleccionado() {
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
    }

    private void cargarDatos() {
        List<Proveedor> proveedores = JsonManager.listarProveedores();
        masterData.setAll(proveedores);
    }

    private void showAgregarProveedorDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Agregar Proveedor");
        VBox root = new VBox(8);
        root.setPadding(new Insets(12));

        TextField tfNombreEmpresa = new TextField();
        TextField tfEmail = new TextField();
        TextField tfDireccion = new TextField();
        TextField tfRut = new TextField();
        tfNombreEmpresa.setPromptText("Nombre de la empresa");
        tfEmail.setPromptText("Email");
        tfDireccion.setPromptText("Dirección");
        tfRut.setPromptText("RUT");

        Label lblError = new Label();
        Button btnSave = new Button("Guardar");
        Button btnCancel = new Button("Cancelar");
        HBox btns = new HBox(8, btnSave, btnCancel);
        btns.setAlignment(Pos.CENTER_RIGHT);

        btnSave.setOnAction(e -> {
            String nombreEmpresa = tfNombreEmpresa.getText().trim();
            String email = tfEmail.getText().trim();
            String direccion = tfDireccion.getText().trim();
            String rut = tfRut.getText().trim();
            if (nombreEmpresa.isEmpty() || email.isEmpty() || direccion.isEmpty() || rut.isEmpty()) {
                lblError.setText("Todos los campos son obligatorios.");
                return;
            }
            Proveedor nuevo = new Proveedor(null, nombreEmpresa, email, direccion, rut);
            boolean ok = JsonManager.agregarProveedor(nuevo);
            if (ok) { cargarDatos(); dialog.close(); } else { lblError.setText("No se pudo agregar el proveedor."); }
        });
        btnCancel.setOnAction(ev -> dialog.close());

        root.getChildren().addAll(new Label("Nombre Empresa:"), tfNombreEmpresa, new Label("Email:"), tfEmail, new Label("Dirección:"), tfDireccion, new Label("RUT:"), tfRut, lblError, btns);
        Scene scene = new Scene(root, 400, 300);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showModificarProveedorDialog() {
        List<Proveedor> proveedores = JsonManager.listarProveedores();
        if (proveedores.isEmpty()) { new Alert(Alert.AlertType.WARNING, "No hay proveedores para modificar.", ButtonType.OK).showAndWait(); return; }
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Modificar Proveedor");
        VBox root = new VBox(8);
        root.setPadding(new Insets(12));

        ComboBox<Proveedor> cbProveedores = new ComboBox<>(FXCollections.observableArrayList(proveedores));
        cbProveedores.setPromptText("Seleccionar proveedor");
        cbProveedores.setMaxWidth(Double.MAX_VALUE);
        cbProveedores.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Proveedor p) { return p == null ? "" : (p.getNombreEmpresa() == null ? p.getId() : p.getNombreEmpresa()); }
            @Override public Proveedor fromString(String string) { return null; }
        });

        TextField tfNombreEmpresa = new TextField();
        TextField tfEmail = new TextField();
        TextField tfDireccion = new TextField();
        TextField tfRut = new TextField();

        Label lblError = new Label();
        Button btnSave = new Button("Guardar");
        Button btnCancel = new Button("Cancelar");
        HBox btns = new HBox(8, btnSave, btnCancel);
        btns.setAlignment(Pos.CENTER_RIGHT);

        cbProveedores.setOnAction(e -> {
            Proveedor p = cbProveedores.getValue();
            if (p != null) {
                tfNombreEmpresa.setText(p.getNombreEmpresa() == null ? "" : p.getNombreEmpresa());
                tfEmail.setText(p.getEmail() == null ? "" : p.getEmail());
                tfDireccion.setText(p.getDireccion() == null ? "" : p.getDireccion());
                tfRut.setText(p.getRut() == null ? "" : p.getRut());
            } else {
                tfNombreEmpresa.clear(); tfEmail.clear(); tfDireccion.clear(); tfRut.clear();
            }
        });

        btnSave.setOnAction(e -> {
            Proveedor sel = cbProveedores.getValue();
            if (sel == null) { lblError.setText("Selecciona un proveedor."); return; }
            String nombreEmpresa = tfNombreEmpresa.getText().trim();
            String email = tfEmail.getText().trim();
            String direccion = tfDireccion.getText().trim();
            String rut = tfRut.getText().trim();
            if (nombreEmpresa.isEmpty() || email.isEmpty() || direccion.isEmpty() || rut.isEmpty()) { lblError.setText("Todos los campos son obligatorios."); return; }
            Proveedor actualizado = new Proveedor(sel.getId(), nombreEmpresa, email, direccion, rut);
            boolean ok = JsonManager.actualizarProveedor(sel.getId(), actualizado);
            if (ok) { cargarDatos(); dialog.close(); } else { lblError.setText("No se pudo actualizar el proveedor."); }
        });

        btnCancel.setOnAction(ev -> dialog.close());

        root.getChildren().addAll(new Label("Proveedor:"), cbProveedores,
                new Label("Nombre Empresa:"), tfNombreEmpresa,
                new Label("Email:"), tfEmail,
                new Label("Dirección:"), tfDireccion,
                new Label("RUT:"), tfRut,
                lblError,
                btns);

        Scene scene = new Scene(root, 420, 360);
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

        ComboBox<String> cbMetodoPago = new ComboBox<>(FXCollections.observableArrayList("Efectivo", "Transferencia", "Tarjeta", "Crédito"));
        cbMetodoPago.setPromptText("Método de pago");
        TextArea taNotas = new TextArea(); taNotas.setPromptText("Notas u observaciones"); taNotas.setPrefRowCount(3);

        TableView<ProductoCantidad> tablaProductos = new TableView<>();
        tablaProductos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        ObservableList<ProductoCantidad> productosData = FXCollections.observableArrayList();
        for (Producto p : JsonManager.listarProductos()) { productosData.add(new ProductoCantidad(p)); }
        tablaProductos.setItems(productosData);
        TableColumn<ProductoCantidad, String> colNombre = new TableColumn<>("Producto");
        colNombre.setCellValueFactory(pc -> new SimpleStringProperty(pc.getValue().producto.getNombre()));
        TableColumn<ProductoCantidad, Number> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(pc -> new SimpleDoubleProperty(pc.getValue().producto.getPrecio()));
        TableColumn<ProductoCantidad, Number> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(pc -> new SimpleIntegerProperty(pc.getValue().producto.getStock()));
        TableColumn<ProductoCantidad, Void> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellFactory(tc -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>(0, Integer.MAX_VALUE, 0);
            { spinner.setEditable(true); spinner.valueProperty().addListener((obs, ov, nv) -> { ProductoCantidad pc = getTableView().getItems().get(getIndex()); pc.setCantidad(nv); }); }
            @Override protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : spinner); }
        });
        TableColumn<ProductoCantidad, Number> colSubtotal = new TableColumn<>("Subtotal");
        colSubtotal.setCellValueFactory(pc -> new SimpleDoubleProperty(pc.getValue().getSubtotal()));
        final java.util.List<TableColumn<ProductoCantidad, ?>> _colsProductos = java.util.Arrays.asList(colNombre, colPrecio, colStock, colCantidad, colSubtotal);
        tablaProductos.getColumns().addAll(_colsProductos);
        Platform.runLater(() -> {
            try {
                var cols = tablaProductos.getColumns();
                for (int i = cols.size() - 1; i >= 0; i--) {
                    TableColumn<?,?> col = cols.get(i);
                    String header = col.getText(); boolean hasGraphic = col.getGraphic() != null;
                    if ((header == null || header.trim().isEmpty()) && !hasGraphic) cols.remove(i);
                }
            } catch (Exception ignored) {}
        });

        Label lblTotal = new Label("Total: $0.00");
        productosData.addListener((javafx.collections.ListChangeListener<ProductoCantidad>) c -> updateTotal(lblTotal, productosData));
        Label lblError = new Label(); lblError.getStyleClass().add("dialog-error");

        Button btnCrear = new Button("Crear Compra"); Button btnCancelar = new Button("Cancelar");
        HBox btns = new HBox(8, btnCrear, btnCancelar); btns.setAlignment(Pos.CENTER_RIGHT);

        btnCrear.setOnAction(e -> {
            String metodoPago = cbMetodoPago.getValue();
            String notas = taNotas.getText().trim();
            if (metodoPago == null) { lblError.setText("El método de pago es obligatorio."); return; }
            List<Compra.Item> items = new ArrayList<>(); double total = 0;
            for (ProductoCantidad pc : productosData) { if (pc.getCantidad() > 0) { total += pc.getSubtotal(); items.add(new Compra.Item(pc.producto.getId(), pc.producto.getNombre(), pc.getCantidad(), pc.producto.getPrecio())); } }
            if (items.isEmpty()) { lblError.setText("Selecciona cantidades para al menos un producto."); return; }
            // No actualizamos el stock aquí: JsonManager.agregarCompra se encarga de actualizar el stock de los productos.
            Compra compra = new Compra();
            compra.setId(JsonManager.generarIdCompra()); compra.setProveedorId(proveedor.getId());
            compra.setMetodoPago(metodoPago); compra.setTotal(total); compra.setItems(items); compra.setNotas(notas);
            boolean guardada = JsonManager.agregarCompra(compra);
            if (!guardada) { lblError.setText("Error guardando la compra."); return; }
            updateTotal(lblTotal, productosData);
            new Alert(Alert.AlertType.INFORMATION, "Compra creada para '" + proveedor.getNombreEmpresa() + "'\n" + "Factura: " + compra.getId() + "\n" + "Pago: " + metodoPago + "\n" + "Total: $" + String.format("%.2f", total)).showAndWait();
            dialog.close();
        });
        btnCancelar.setOnAction(ev -> dialog.close());

        VBox form = new VBox(8, new Label("Método de pago:"), cbMetodoPago, new Label("Notas:"), taNotas, new Label("Productos y cantidades:"), tablaProductos, lblTotal, lblError);
        root.getChildren().addAll(info, lblNombre, form, btns);
        Scene scene3 = new Scene(root, 700, 500);
        dialog.setScene(scene3);
        dialog.showAndWait();
    }

    private void updateTotal(Label lblTotal, ObservableList<ProductoCantidad> productosData) {
        double total = 0; for (ProductoCantidad pc : productosData) { total += pc.getSubtotal(); }
        lblTotal.setText("Total: $" + String.format("%.2f", total));
    }

    private static class ProductoCantidad {
        private final Producto producto; private int cantidad;
        ProductoCantidad(Producto producto) { this.producto = producto; this.cantidad = 0; }
        int getCantidad() { return cantidad; }
        void setCantidad(int cantidad) { this.cantidad = cantidad == -1 ? 0 : cantidad; }
        double getSubtotal() { return producto.getPrecio() * cantidad; }
    }
}
