package com.licoreraFx.view;

import com.licoreraFx.model.Compra;
import com.licoreraFx.model.Producto;
import com.licoreraFx.model.Proveedor;
import com.licoreraFx.repository.CompraRepository;
import com.licoreraFx.repository.ProductoRepository;
import com.licoreraFx.repository.ProveedorRepository;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.application.Platform;
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
 * Vista para gestionar proveedores y crear compras.
 */
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
        tfSearch.setPromptText("Buscar proveedor por nombre de empresa, email, dirección o RUT...");
        tfSearch.setMaxWidth(Double.MAX_VALUE);
        HBox searchBox = new HBox(8, tfSearch);
        HBox.setHgrow(tfSearch, Priority.ALWAYS);

        List<Proveedor> proveedores = ProveedorRepository.listarProveedores();
        masterData = FXCollections.observableArrayList(proveedores);

        table = new TableView<>();
        table.getStyleClass().add("proveedores-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(320);
        table.setMinHeight(200);
        table.setMaxHeight(Region.USE_PREF_SIZE);

        TableColumn<Proveedor, String> colId = new TableColumn<>();
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        Label lblId = new Label("ID"); lblId.setStyle("-fx-font-weight: bold;"); colId.setGraphic(lblId); colId.setStyle("-fx-alignment: CENTER;");

        TableColumn<Proveedor, String> colNombreEmpresa = new TableColumn<>();
        colNombreEmpresa.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombreEmpresa()));
        Label lblNEmp = new Label("Nombre Empresa"); lblNEmp.setStyle("-fx-font-weight: bold;"); colNombreEmpresa.setGraphic(lblNEmp); colNombreEmpresa.setStyle("-fx-alignment: CENTER;");

        TableColumn<Proveedor, String> colEmail = new TableColumn<>();
        colEmail.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmail()));
        Label lblEmail = new Label("Email"); lblEmail.setStyle("-fx-font-weight: bold;"); colEmail.setGraphic(lblEmail); colEmail.setStyle("-fx-alignment: CENTER;");

        TableColumn<Proveedor, String> colDireccion = new TableColumn<>();
        colDireccion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDireccion()));
        Label lblDir = new Label("Dirección"); lblDir.setStyle("-fx-font-weight: bold;"); colDireccion.setGraphic(lblDir); colDireccion.setStyle("-fx-alignment: CENTER;");

        TableColumn<Proveedor, String> colRut = new TableColumn<>();
        colRut.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRut()));
        Label lblRut = new Label("RUT"); lblRut.setStyle("-fx-font-weight: bold;"); colRut.setGraphic(lblRut); colRut.setStyle("-fx-alignment: CENTER;");

        final java.util.List<TableColumn<Proveedor, ?>> _colsP = java.util.Arrays.asList(colId, colNombreEmpresa, colEmail, colDireccion, colRut);
        table.getColumns().addAll(_colsP);
        // Quitar columnas vacías que puedan aparecer
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
        // Centro el contenido de la columna
        colAccion.setStyle("-fx-alignment: CENTER;");
        colAccion.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Nueva Compra");
            private final HBox box = new HBox(btn);

            {
                // Centrar el botón dentro del HBox
                box.setAlignment(Pos.CENTER);
                // Evitar que el HBox colapse si la celda es ancha
                box.setFillHeight(true);

                btn.setOnAction(event -> {
                    Proveedor p = getTableView().getItems().get(getIndex());
                    showNuevaCompraDialog(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
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
                    boolean ok = ProveedorRepository.eliminarProveedor(sel.getId());
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
        List<Proveedor> proveedores = ProveedorRepository.listarProveedores();
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
        TextField tfDireccion = new TextField();
        tfDireccion.setPromptText("Dirección");
        TextField tfRut = new TextField();
        tfRut.setPromptText("RUT");

        Button btnSave = new Button("Guardar");
        Button btnCancel = new Button("Cancelar");

        Label lblError = new Label();
        lblError.getStyleClass().add("dialog-error");

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
            boolean ok = ProveedorRepository.agregarProveedor(nuevo);
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

        VBox formBox = new VBox(8, new Label("Nombre Empresa:"), tfNombreEmpresa, new Label("Email:"), tfEmail, new Label("Dirección:"), tfDireccion, new Label("RUT:"), tfRut, lblError);
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
        TextField tfDireccion = new TextField(sel.getDireccion());
        TextField tfRut = new TextField(sel.getRut());

        Button btnSave = new Button("Guardar");
        Button btnCancel = new Button("Cancelar");

        Label lblError = new Label();
        lblError.getStyleClass().add("dialog-error");

        btnSave.setOnAction(e -> {
            String nombreEmpresa = tfNombreEmpresa.getText().trim();
            String email = tfEmail.getText().trim();
            String direccion = tfDireccion.getText().trim();
            String rut = tfRut.getText().trim();

            if (nombreEmpresa.isEmpty() || email.isEmpty() || direccion.isEmpty() || rut.isEmpty()) {
                lblError.setText("Todos los campos son obligatorios.");
                return;
            }

            Proveedor actualizado = new Proveedor(sel.getId(), nombreEmpresa, email, direccion, rut);
            boolean ok = ProveedorRepository.actualizarProveedor(sel.getId(), actualizado);
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

        VBox formBox = new VBox(8, new Label("Nombre Empresa:"), tfNombreEmpresa, new Label("Email:"), tfEmail, new Label("Dirección:"), tfDireccion, new Label("RUT:"), tfRut, lblError);
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
        Label lblNombre = new Label("Proveedor: " + proveedor.getNombreEmpresa());

        // Datos de compra (sin fecha)
        TextField tfNumeroFactura = new TextField();
        tfNumeroFactura.setPromptText("Número de factura");
        ComboBox<String> cbMetodoPago = new ComboBox<>(FXCollections.observableArrayList("Efectivo", "Transferencia", "Tarjeta", "Crédito"));
        cbMetodoPago.setPromptText("Método de pago");
        TextArea taNotas = new TextArea();
        taNotas.setPromptText("Notas u observaciones");
        taNotas.setPrefRowCount(3);

        // Selector de productos y cantidades (estilo nueva venta)
        ObservableList<Compra.Item> itemsData = FXCollections.observableArrayList();

        ComboBox<Producto> cbProducto = new ComboBox<>();
        cbProducto.setPromptText("Seleccionar producto");
        cbProducto.setPrefWidth(300);
        cbProducto.getItems().addAll(ProductoRepository.listarProductos());
        cbProducto.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Producto p) { return p == null ? "" : p.getNombre(); }
            @Override
            public Producto fromString(String string) { return null; }
        });

        Spinner<Integer> spCantidad = new Spinner<>(1, 999, 1);
        spCantidad.setEditable(true);
        spCantidad.setPrefWidth(100);

        // Spinner para precio unitario en la compra (puede diferir del precio en inventario)
        Spinner<Double> spPrecio = new Spinner<>();
        SpinnerValueFactory.DoubleSpinnerValueFactory priceFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1_000_000.0, 0.0, 0.01);
        spPrecio.setValueFactory(priceFactory);
        spPrecio.setEditable(true);
        spPrecio.setPrefWidth(120);

        // Etiqueta para el spinner de precio
        Label lblPrecioUnit = new Label("Precio Unitario:");
        lblPrecioUnit.setMinWidth(Region.USE_PREF_SIZE);

        // Al seleccionar producto, prellenar el precio con el precio del inventario
        cbProducto.valueProperty().addListener((obs, oldP, newP) -> {
            if (newP != null) {
                priceFactory.setValue(newP.getPrecio());
            }
        });

        Button btnAgregarProducto = new Button("Agregar");

        TableView<Compra.Item> tablaItems = new TableView<>();
        tablaItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tablaItems.setPrefHeight(220);
        tablaItems.setItems(itemsData);

        TableColumn<Compra.Item, String> colNombre = new TableColumn<>();
        colNombre.setCellValueFactory(ci -> new SimpleStringProperty(ci.getValue().getNombreProducto()));
        Label lblP = new Label("Producto"); lblP.setStyle("-fx-font-weight: bold;"); colNombre.setGraphic(lblP); colNombre.setStyle("-fx-alignment: CENTER;");

        TableColumn<Compra.Item, Number> colPrecio = new TableColumn<>();
        colPrecio.setCellValueFactory(ci -> new SimpleDoubleProperty(ci.getValue().getPrecio()));
        Label lblPrc = new Label("Precio"); lblPrc.setStyle("-fx-font-weight: bold;"); colPrecio.setGraphic(lblPrc); colPrecio.setStyle("-fx-alignment: CENTER;");
        colPrecio.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("$%.2f", item.doubleValue()));
            }
        });

        TableColumn<Compra.Item, Number> colCantidad = new TableColumn<>();
        colCantidad.setCellValueFactory(ci -> new SimpleDoubleProperty(ci.getValue().getCantidad()));
        Label lblCant = new Label("Cantidad"); lblCant.setStyle("-fx-font-weight: bold;"); colCantidad.setGraphic(lblCant); colCantidad.setStyle("-fx-alignment: CENTER;");

        TableColumn<Compra.Item, Number> colSubtotal = new TableColumn<>();
        colSubtotal.setCellValueFactory(ci -> new SimpleDoubleProperty(ci.getValue().getCantidad() * ci.getValue().getPrecio()));
        Label lblSub = new Label("Subtotal"); lblSub.setStyle("-fx-font-weight: bold;"); colSubtotal.setGraphic(lblSub); colSubtotal.setStyle("-fx-alignment: CENTER;");
        colSubtotal.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("$%.2f", item.doubleValue()));
            }
        });

        TableColumn<Compra.Item, Void> colEliminar = new TableColumn<>();
        colEliminar.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Eliminar");
            { btn.setOnAction(evt -> { Compra.Item it = getTableView().getItems().get(getIndex()); itemsData.remove(it); }); }
            @Override protected void updateItem(Void v, boolean empty) { super.updateItem(v, empty); setGraphic(empty ? null : btn); }
        });

        final java.util.List<TableColumn<Compra.Item, ?>> _colsItems = java.util.Arrays.asList(colNombre, colPrecio, colCantidad, colSubtotal, colEliminar);
        tablaItems.getColumns().addAll(_colsItems);
        Platform.runLater(() -> {
            try {
                var cols = tablaItems.getColumns();
                for (int i = cols.size() - 1; i >= 0; i--) {
                    TableColumn<?,?> col = cols.get(i); String header = col.getText(); boolean hasGraphic = col.getGraphic() != null;
                    if ((header == null || header.trim().isEmpty()) && !hasGraphic) cols.remove(i);
                }
            } catch (Exception ignored) {}
        });

        Label lblTotal = new Label("Total: $0.00");
        itemsData.addListener((javafx.collections.ListChangeListener<Compra.Item>) c -> updateTotal(lblTotal, itemsData));

        Label lblError = new Label();
        lblError.getStyleClass().add("dialog-error");

        btnAgregarProducto.setOnAction(e -> {
            Producto productoSeleccionado = cbProducto.getValue();
            int cantidad = spCantidad.getValue();
            double precioUnitario = spPrecio.getValue();
            if (productoSeleccionado == null) {
                new Alert(Alert.AlertType.WARNING, "Selecciona un producto.", ButtonType.OK).showAndWait();
                return;
            }
            if (cantidad <= 0) {
                new Alert(Alert.AlertType.WARNING, "Cantidad debe ser mayor a 0.", ButtonType.OK).showAndWait();
                return;
            }
            // Si ya existe el item, aumentar cantidad
            Compra.Item existente = null;
            for (Compra.Item it : itemsData) { if (it.getProductoId().equals(productoSeleccionado.getId())) { existente = it; break; } }
            if (existente != null) {
                existente.setCantidad(existente.getCantidad() + cantidad);
                // actualizar precio unitario al indicado en el formulario
                existente.setPrecio(precioUnitario);
                tablaItems.refresh();
            } else {
                itemsData.add(new Compra.Item(productoSeleccionado.getId(), productoSeleccionado.getNombre(), cantidad, precioUnitario));
            }
            updateTotal(lblTotal, itemsData);
        });


        Button btnCrear = new Button("Crear Compra");
        Button btnCancelar = new Button("Cancelar");
        HBox btns = new HBox(8, btnCrear, btnCancelar);
        btns.setAlignment(Pos.CENTER_RIGHT);

        btnCrear.setOnAction(e -> {
            String numeroFactura = tfNumeroFactura.getText().trim();
            String metodoPago = cbMetodoPago.getValue();
            String notas = taNotas.getText().trim();

            if (numeroFactura.isEmpty() || metodoPago == null) {
                lblError.setText("Número de factura y método de pago son obligatorios.");
                return;
            }

            // Construir items y total
            List<Compra.Item> items = new ArrayList<>();
            double total = 0;
            for (Compra.Item it : itemsData) {
                if (it.getCantidad() > 0) {
                    total += it.getCantidad() * it.getPrecio();
                    items.add(new Compra.Item(it.getProductoId(), it.getNombreProducto(), it.getCantidad(), it.getPrecio()));
                }
            }
            if (items.isEmpty()) {
                lblError.setText("Selecciona cantidades para al menos un producto.");
                return;
            }

            // Crear y guardar la compra
            Compra compra = new Compra();
            compra.setId(CompraRepository.generarIdCompra());
            compra.setProveedorId(proveedor.getId());
            compra.setNumeroFactura(numeroFactura);
            compra.setMetodoPago(metodoPago);
            compra.setTotal(total);
            compra.setItems(items);
            compra.setNotas(notas);

            boolean guardada = CompraRepository.agregarCompra(compra);
            if (!guardada) {
                lblError.setText("Error guardando la compra.");
                return;
            }

            updateTotal(lblTotal, itemsData);
            new Alert(Alert.AlertType.INFORMATION,
                    "Compra creada para '" + proveedor.getNombreEmpresa() + "'\n" +
                            "Factura: " + numeroFactura + "\n" +
                            "Pago: " + metodoPago + "\n" +
                            "Total: $" + String.format("%.2f", total)).showAndWait();
            dialog.close();
        });

        btnCancelar.setOnAction(ev -> dialog.close());

        // Agrupar selección y precio en una fila, y colocar el botón "Agregar" en la fila inferior
        HBox priceBox = new HBox(6, lblPrecioUnit, spPrecio);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        HBox selectionBox = new HBox(8, cbProducto, spCantidad, priceBox);
        selectionBox.setAlignment(Pos.CENTER_LEFT);

        HBox addBtnBox = new HBox(btnAgregarProducto);
        addBtnBox.setAlignment(Pos.CENTER_LEFT);
        addBtnBox.setPadding(new Insets(4, 0, 0, 0));

        // Usar selectionBox y addBtnBox en el formulario
        VBox form = new VBox(8,
                new Label("Número de factura:"), tfNumeroFactura,
                new Label("Método de pago:"), cbMetodoPago,
                new Label("Notas:"), taNotas,
                new Label("Productos y cantidades:"), selectionBox, addBtnBox, tablaItems,
                lblTotal,
                lblError
        );

        root.getChildren().addAll(info, lblNombre, form, btns);
        Scene scene = new Scene(root, 700, 500);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void updateTotal(Label lblTotal, ObservableList<Compra.Item> itemsData) {
        double total = 0;
        for (Compra.Item it : itemsData) {
            total += it.getCantidad() * it.getPrecio();
        }
        lblTotal.setText("Total: $" + String.format("%.2f", total));
    }
}
