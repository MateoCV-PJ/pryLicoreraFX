package com.licoreraFx.view;

import com.licoreraFx.model.Cliente;
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
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ClientesView {

    private TableView<Cliente> table;
    private ObservableList<Cliente> masterData;
    private FilteredList<Cliente> filtered;

    public Node createView() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Clientes");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField tfSearch = new TextField();
        tfSearch.getStyleClass().add("search-field");
        tfSearch.setPromptText("Buscar cliente por nombre, email, teléfono o documento...");
        tfSearch.setMaxWidth(Double.MAX_VALUE);
        HBox searchBox = new HBox(8, tfSearch);
        HBox.setHgrow(tfSearch, Priority.ALWAYS);

        List<Cliente> clientes = JsonManager.listarClientes();
        masterData = FXCollections.observableArrayList(clientes);

        table = new TableView<>();
        table.getStyleClass().add("clientes-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(320);
        table.setMinHeight(200);
        table.setMaxHeight(Region.USE_PREF_SIZE);

        TableColumn<Cliente, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));

        TableColumn<Cliente, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombre()));

        TableColumn<Cliente, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmail()));

        TableColumn<Cliente, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTelefono()));

        TableColumn<Cliente, String> colDocumento = new TableColumn<>("Documento");
        colDocumento.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDocumento()));

        //noinspection unchecked
        table.getColumns().addAll(colId, colNombre, colEmail, colTelefono, colDocumento);

        // Columna de acciones: botón 'Nueva Venta' por cliente
        TableColumn<Cliente, Void> colAccion = new TableColumn<>("Acción");
        colAccion.setPrefWidth(120);
        colAccion.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("Nueva Venta");
            {
                btn.getStyleClass().add("dialog-button");
                btn.setOnAction(e -> {
                    Cliente c = getTableView().getItems().get(getIndex());
                    if (c != null) showNuevaVentaDialog(c);
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
            filtered.setPredicate(c -> {
                if (q.isEmpty()) return true;
                return (c.getNombre() != null && c.getNombre().toLowerCase().contains(q)) ||
                        (c.getEmail() != null && c.getEmail().toLowerCase().contains(q)) ||
                        (c.getTelefono() != null && c.getTelefono().toLowerCase().contains(q)) ||
                        (c.getDocumento() != null && c.getDocumento().toLowerCase().contains(q)) ||
                        (c.getId() != null && c.getId().toLowerCase().contains(q));
            });
        });

        SortedList<Cliente> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        Button btnAnadir = new Button("Añadir");
        Button btnModificar = new Button("Modificar");
        Button btnEliminar = new Button("Eliminar");

        btnAnadir.setOnAction(e -> showAgregarClienteDialog());
        btnModificar.setOnAction(e -> showModificarClienteDialog());
        btnEliminar.setOnAction(e -> {
            Cliente sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                new Alert(Alert.AlertType.WARNING, "Selecciona un cliente para eliminar.", ButtonType.OK).showAndWait();
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar el cliente '" + sel.getNombre() + "'?", ButtonType.YES, ButtonType.NO);
            confirm.initModality(Modality.APPLICATION_MODAL);
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    boolean ok = JsonManager.eliminarCliente(sel.getId());
                    if (ok) {
                        cargarDatos();
                        new Alert(Alert.AlertType.INFORMATION, "Cliente eliminado.", ButtonType.OK).showAndWait();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "No se pudo eliminar el cliente.", ButtonType.OK).showAndWait();
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
        List<Cliente> clientes = JsonManager.listarClientes();
        masterData.setAll(clientes);
    }

    private void showAgregarClienteDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Agregar cliente");

        javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
        root.setPadding(new Insets(12));

        TextField tfNombre = new TextField();
        tfNombre.getStyleClass().add("dialog-field");
        tfNombre.setPromptText("Nombre completo");
        TextField tfEmail = new TextField();
        tfEmail.getStyleClass().add("dialog-field");
        tfEmail.setPromptText("Email");
        TextField tfTelefono = new TextField();
        tfTelefono.getStyleClass().add("dialog-field");
        tfTelefono.setPromptText("Teléfono");
        TextField tfDocumento = new TextField();
        tfDocumento.getStyleClass().add("dialog-field");
        tfDocumento.setPromptText("Documento de identidad");

        Button btnSave = new Button("Guardar");
        btnSave.getStyleClass().add("dialog-button");
        Button btnCancel = new Button("Cancelar");
        btnCancel.getStyleClass().addAll("dialog-button", "secondary");

        Label lblError = new Label();
        lblError.getStyleClass().add("dialog-error");

        btnSave.setOnAction(e -> {
            String nombre = tfNombre.getText() != null ? tfNombre.getText().trim() : "";
            String email = tfEmail.getText() != null ? tfEmail.getText().trim() : "";
            String telefono = tfTelefono.getText() != null ? tfTelefono.getText().trim() : "";
            String documento = tfDocumento.getText() != null ? tfDocumento.getText().trim() : "";

            if (nombre.isEmpty()) { lblError.setText("Nombre es obligatorio."); return; }
            if (email.isEmpty()) { lblError.setText("Email es obligatorio."); return; }
            if (isInvalidEmail(email)) { lblError.setText("Email inválido."); return; }
            if (telefono.isEmpty()) { lblError.setText("Teléfono es obligatorio."); return; }
            if (isInvalidTelefono(telefono)) { lblError.setText("Teléfono inválido. Use dígitos, espacios, guiones o +."); return; }
            if (documento.isEmpty()) { lblError.setText("Documento es obligatorio."); return; }

            Cliente nuevo = new Cliente(null, nombre, email, telefono, documento);
            boolean ok = JsonManager.agregarCliente(nuevo);
            if (ok) {
                cargarDatos();
                dialog.close();
                new Alert(Alert.AlertType.INFORMATION, "Cliente agregado.", ButtonType.OK).showAndWait();
            } else {
                lblError.setText("No se pudo agregar el cliente (id duplicado o error de escritura).");
            }
        });

        btnCancel.setOnAction(ev -> dialog.close());

        HBox btns = new HBox(8, btnSave, btnCancel);
        btns.setAlignment(Pos.CENTER_RIGHT);

        VBox formBox = new VBox(8);
        formBox.getChildren().addAll(
                new Label("Nombre:"), tfNombre,
                new Label("Email:"), tfEmail,
                new Label("Teléfono:"), tfTelefono,
                new Label("Documento:"), tfDocumento,
                lblError
        );

        ScrollPane sp = new ScrollPane(formBox);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setPrefViewportHeight(220);

        root.setCenter(sp);
        HBox bottomBox = new HBox();
        bottomBox.setPadding(new Insets(8, 0, 0, 0));
        bottomBox.getChildren().add(btns);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 480, 320);
        dialog.setScene(scene);
        dialog.setResizable(true);
        dialog.setMinWidth(420);
        dialog.setMinHeight(300);
        dialog.showAndWait();
    }

    private void showModificarClienteDialog() {
        Cliente sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { new Alert(Alert.AlertType.WARNING, "Selecciona un cliente para modificar.", ButtonType.OK).showAndWait(); return; }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Modificar cliente");

        javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
        root.setPadding(new Insets(12));

        TextField tfNombre = new TextField(sel.getNombre()); tfNombre.getStyleClass().add("dialog-field");
        TextField tfEmail = new TextField(sel.getEmail()); tfEmail.getStyleClass().add("dialog-field");
        TextField tfTelefono = new TextField(sel.getTelefono()); tfTelefono.getStyleClass().add("dialog-field");
        TextField tfDocumento = new TextField(sel.getDocumento()); tfDocumento.getStyleClass().add("dialog-field");

        Button btnSave = new Button("Guardar"); btnSave.getStyleClass().add("dialog-button");
        Button btnCancel = new Button("Cancelar"); btnCancel.getStyleClass().addAll("dialog-button", "secondary");

        Label lblError = new Label(); lblError.getStyleClass().add("dialog-error");

        btnSave.setOnAction(e -> {
            String nombre = tfNombre.getText() != null ? tfNombre.getText().trim() : "";
            String email = tfEmail.getText() != null ? tfEmail.getText().trim() : "";
            String telefono = tfTelefono.getText() != null ? tfTelefono.getText().trim() : "";
            String documento = tfDocumento.getText() != null ? tfDocumento.getText().trim() : "";

            if (nombre.isEmpty()) { lblError.setText("Nombre es obligatorio."); return; }
            if (email.isEmpty()) { lblError.setText("Email es obligatorio."); return; }
            if (isInvalidEmail(email)) { lblError.setText("Email inválido."); return; }
            if (telefono.isEmpty()) { lblError.setText("Teléfono es obligatorio."); return; }
            if (isInvalidTelefono(telefono)) { lblError.setText("Teléfono inválido. Use dígitos, espacios, guiones o +."); return; }
            if (documento.isEmpty()) { lblError.setText("Documento es obligatorio."); return; }

            Cliente actualizado = new Cliente(sel.getId(), nombre, email, telefono, documento);
            boolean ok = JsonManager.actualizarCliente(sel.getId(), actualizado);
            if (ok) {
                cargarDatos(); dialog.close(); new Alert(Alert.AlertType.INFORMATION, "Cliente actualizado.", ButtonType.OK).showAndWait();
            } else {
                lblError.setText("No se pudo actualizar el cliente (id puede existir o error de escritura).");
            }
        });

        btnCancel.setOnAction(ev -> dialog.close());

        HBox btns = new HBox(8, btnSave, btnCancel); btns.setAlignment(Pos.CENTER_RIGHT);

        VBox formBox = new VBox(8);
        formBox.getChildren().addAll(
                new Label("Nombre:"), tfNombre,
                new Label("Email:"), tfEmail,
                new Label("Teléfono:"), tfTelefono,
                new Label("Documento:"), tfDocumento,
                lblError
        );

        ScrollPane sp = new ScrollPane(formBox); sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setPrefViewportHeight(260);

        root.setCenter(sp);
        HBox bottomBox = new HBox(); bottomBox.setPadding(new Insets(8, 0, 0, 0)); bottomBox.getChildren().add(btns); bottomBox.setAlignment(Pos.CENTER_RIGHT);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 480, 360);
        dialog.setScene(scene); dialog.setResizable(true); dialog.setMinWidth(420); dialog.setMinHeight(300); dialog.showAndWait();
    }

    private void showNuevaVentaDialog(Cliente cliente) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Nueva Venta - " + (cliente.getNombre() != null ? cliente.getNombre() : cliente.getId()));

        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.TOP_LEFT);

        // Información del cliente
        Label info = new Label("Crear venta para cliente:");
        info.setStyle("-fx-font-weight: bold;");
        Label lblNombre = new Label("Nombre: " + (cliente.getNombre() != null ? cliente.getNombre() : "-"));
        Label lblDocumento = new Label("Documento: " + (cliente.getDocumento() != null ? cliente.getDocumento() : "-"));

        Separator sep1 = new Separator();

        // Selección de productos
        Label lblProductos = new Label("Seleccionar productos:");
        lblProductos.setStyle("-fx-font-weight: bold;");

        // Tabla para mostrar productos seleccionados
        TableView<ProductoVenta> tablaProductos = new TableView<>();
        tablaProductos.setPrefHeight(200);
        ObservableList<ProductoVenta> productosVenta = FXCollections.observableArrayList();
        tablaProductos.setItems(productosVenta);

        TableColumn<ProductoVenta, String> colNombreProd = new TableColumn<>("Producto");
        colNombreProd.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombre()));
        colNombreProd.setPrefWidth(200);

        TableColumn<ProductoVenta, String> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(cell -> new SimpleStringProperty("$" + cell.getValue().getPrecio()));
        colPrecio.setPrefWidth(100);

        TableColumn<ProductoVenta, String> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getCantidad())));
        colCantidad.setPrefWidth(100);

        TableColumn<ProductoVenta, String> colSubtotal = new TableColumn<>("Subtotal");
        colSubtotal.setCellValueFactory(cell -> new SimpleStringProperty("$" + (cell.getValue().getPrecio() * cell.getValue().getCantidad())));
        colSubtotal.setPrefWidth(100);

        TableColumn<ProductoVenta, Void> colEliminar = new TableColumn<>("Acción");
        colEliminar.setCellFactory(tc -> new TableCell<>() {
            private final Button btnEliminar = new Button("Eliminar");

            {
                btnEliminar.setOnAction(e -> {
                    ProductoVenta pv = getTableView().getItems().get(getIndex());
                    productosVenta.remove(pv);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnEliminar);
            }
        });
        colEliminar.setPrefWidth(100);

        //noinspection unchecked
        tablaProductos.getColumns().addAll(colNombreProd, colPrecio, colCantidad, colSubtotal, colEliminar);

        // Controles para agregar productos
        HBox addProductoBox = new HBox(10);
        addProductoBox.setAlignment(Pos.CENTER_LEFT);

        ComboBox<com.licoreraFx.model.Producto> cbProducto = new ComboBox<>();
        cbProducto.setPromptText("Seleccionar producto");
        cbProducto.setPrefWidth(250);
        List<com.licoreraFx.model.Producto> productos = JsonManager.listarProductos();
        cbProducto.getItems().addAll(productos);
        cbProducto.setConverter(new javafx.util.StringConverter<com.licoreraFx.model.Producto>() {
            @Override
            public String toString(com.licoreraFx.model.Producto p) {
                return p == null ? "" : p.getNombre() + " - Stock: " + p.getStock();
            }

            @Override
            public com.licoreraFx.model.Producto fromString(String string) {
                return null;
            }
        });

        Spinner<Integer> spCantidad = new Spinner<>(1, 999, 1);
        spCantidad.setPrefWidth(100);
        spCantidad.setEditable(true);

        Button btnAgregarProducto = new Button("Agregar");
        btnAgregarProducto.setOnAction(e -> {
            com.licoreraFx.model.Producto productoSeleccionado = cbProducto.getValue();
            int cantidad = spCantidad.getValue();

            if (productoSeleccionado == null) {
                new Alert(Alert.AlertType.WARNING, "Selecciona un producto.", ButtonType.OK).showAndWait();
                return;
            }

            if (cantidad > productoSeleccionado.getStock()) {
                new Alert(Alert.AlertType.WARNING, "La cantidad supera el stock disponible (" + productoSeleccionado.getStock() + ").", ButtonType.OK).showAndWait();
                return;
            }

            // Verificar si el producto ya está en la lista
            boolean existe = false;
            for (ProductoVenta pv : productosVenta) {
                if (pv.getId().equals(productoSeleccionado.getId())) {
                    pv.setCantidad(pv.getCantidad() + cantidad);
                    existe = true;
                    break;
                }
            }

            if (!existe) {
                ProductoVenta pv = new ProductoVenta(
                        productoSeleccionado.getId(),
                        productoSeleccionado.getNombre(),
                        productoSeleccionado.getPrecio(),
                        cantidad
                );
                productosVenta.add(pv);
            }

            tablaProductos.refresh();
            cbProducto.setValue(null);
            spCantidad.getValueFactory().setValue(1);
        });

        addProductoBox.getChildren().addAll(new Label("Producto:"), cbProducto, new Label("Cantidad:"), spCantidad, btnAgregarProducto);

        // Total
        Label lblTotal = new Label("Total: $0.00");
        lblTotal.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Actualizar total cuando cambie la tabla
        productosVenta.addListener((javafx.collections.ListChangeListener<ProductoVenta>) c -> {
            double total = productosVenta.stream().mapToDouble(pv -> pv.getPrecio() * pv.getCantidad()).sum();
            lblTotal.setText(String.format("Total: $%.2f", total));
        });

        Separator sep2 = new Separator();

        // Botones de acción
        Button btnCrear = new Button("Crear Venta");
        btnCrear.getStyleClass().add("dialog-button");
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.getStyleClass().addAll("dialog-button", "secondary");
        HBox btns = new HBox(8, btnCrear, btnCancelar);
        btns.setAlignment(Pos.CENTER_RIGHT);

        btnCrear.setOnAction(e -> {
            if (productosVenta.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Agrega al menos un producto a la venta.", ButtonType.OK).showAndWait();
                return;
            }

            // Crear la lista de detalles de venta
            List<com.licoreraFx.model.DetalleVenta> detalles = new ArrayList<>();
            for (ProductoVenta pv : productosVenta) {
                double subtotal = pv.getPrecio() * pv.getCantidad();
                com.licoreraFx.model.DetalleVenta detalle = new com.licoreraFx.model.DetalleVenta(
                        pv.getId(),
                        pv.getNombre(),
                        pv.getCantidad(),
                        pv.getPrecio(),
                        subtotal
                );
                detalles.add(detalle);
            }

            // Calcular el total
            double total = detalles.stream().mapToDouble(com.licoreraFx.model.DetalleVenta::getSubtotal).sum();

            // Crear la venta
            com.licoreraFx.model.Venta venta = new com.licoreraFx.model.Venta(
                    null,
                    cliente.getId(),
                    cliente.getNombre(),
                    detalles,
                    total
            );

            // Guardar la venta
            boolean ok = JsonManager.agregarVenta(venta);
            if (ok) {
                new Alert(Alert.AlertType.INFORMATION, "Venta creada exitosamente para: " + cliente.getNombre() + "\nTotal: $" + String.format("%.2f", total)).showAndWait();
                dialog.close();
            } else {
                new Alert(Alert.AlertType.ERROR, "Error al guardar la venta. Revisa la consola para más detalles.").showAndWait();
            }
        });

        btnCancelar.setOnAction(ev -> dialog.close());

        root.getChildren().addAll(info, lblNombre, lblDocumento, sep1, lblProductos, addProductoBox, tablaProductos, sep2, lblTotal, btns);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(scrollPane, 700, 600);
        dialog.setScene(scene);
        dialog.setResizable(true);
        dialog.showAndWait();
    }

    // Clase interna para representar un producto en la venta
    private static class ProductoVenta {
        private String id;
        private String nombre;
        private double precio;
        private int cantidad;

        public ProductoVenta(String id, String nombre, double precio, int cantidad) {
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
            this.cantidad = cantidad;
        }

        public String getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        public double getPrecio() {
            return precio;
        }

        public int getCantidad() {
            return cantidad;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
        }
    }

    // Validadores
    private boolean isInvalidEmail(String email) {
        if (email == null) return true;
        String v = email.trim();
        if (v.isEmpty()) return true;
        String pattern = "^[\\w.%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,6}$";
        return !Pattern.compile(pattern).matcher(v).matches();
    }

    private boolean isInvalidTelefono(String telefono) {
        if (telefono == null) return true;
        String v = telefono.trim();
        if (v.isEmpty()) return true;
        // Permite dígitos, espacios, guiones y el símbolo '+'
        String pattern = "^[\\d\\s\\-()+]+$";
        return !Pattern.compile(pattern).matcher(v).matches();
    }
}
