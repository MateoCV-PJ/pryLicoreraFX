package com.licoreraFx.view;

import com.licoreraFx.model.Cliente;
import com.licoreraFx.repository.ClienteRepository;
import com.licoreraFx.repository.ProductoRepository;
import com.licoreraFx.model.DetalleVenta;
import com.licoreraFx.model.Venta;
import com.licoreraFx.service.VentaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
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

public class ClientesView {

    private TableView<Cliente> table;
    private ObservableList<Cliente> masterData;
    private FilteredList<Cliente> filtered;

    // Cache de vistas: una para admin (isVendor=false) y otra para vendedor (isVendor=true)
    private Node cachedViewAdmin = null;
    private Node cachedViewVendor = null;

    // Nueva versión: crear vista parametrizada por rol (isVendor). Si isVendor==true, ocultar botón Eliminar.
    public Node createView(boolean isVendor) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Clientes");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField tfSearch = new TextField();
        tfSearch.getStyleClass().add("search-field");
        tfSearch.setPromptText("Buscar cliente por nombre, email, dirección o documento...");
        tfSearch.setMaxWidth(Double.MAX_VALUE);
        HBox searchBox = new HBox(8, tfSearch);
        HBox.setHgrow(tfSearch, Priority.ALWAYS);

        // Inicializar lista vacía y configurar tabla inmediatamente (sin I/O en UI thread)
        masterData = FXCollections.observableArrayList();

        table = new TableView<>();
        table.getStyleClass().add("clientes-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(320);
        table.setMinHeight(200);
        table.setMaxHeight(Region.USE_PREF_SIZE);

        TableColumn<Cliente, String> colId = new TableColumn<>();
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        Label lblId = new Label("ID"); lblId.setStyle("-fx-font-weight: bold;"); colId.setGraphic(lblId); colId.setStyle("-fx-alignment: CENTER;");

        TableColumn<Cliente, String> colNombre = new TableColumn<>();
        colNombre.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombre()));
        Label lblNombre = new Label("Nombre"); lblNombre.setStyle("-fx-font-weight: bold;"); colNombre.setGraphic(lblNombre); colNombre.setStyle("-fx-alignment: CENTER;");

        TableColumn<Cliente, String> colEmail = new TableColumn<>();
        colEmail.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmail()));
        Label lblEmail = new Label("Email"); lblEmail.setStyle("-fx-font-weight: bold;"); colEmail.setGraphic(lblEmail); colEmail.setStyle("-fx-alignment: CENTER;");

        TableColumn<Cliente, String> colDireccion = new TableColumn<>();
        colDireccion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDireccion()));
        Label lblDirec = new Label("Dirección"); lblDirec.setStyle("-fx-font-weight: bold;"); colDireccion.setGraphic(lblDirec); colDireccion.setStyle("-fx-alignment: CENTER;");

        TableColumn<Cliente, String> colDocumento = new TableColumn<>();
        colDocumento.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDocumento()));
        Label lblDoc = new Label("Documento"); lblDoc.setStyle("-fx-font-weight: bold;"); colDocumento.setGraphic(lblDoc); colDocumento.setStyle("-fx-alignment: CENTER;");

        final java.util.List<TableColumn<Cliente, ?>> _cols = java.util.Arrays.asList(colId, colNombre, colEmail, colDireccion, colDocumento);
        table.getColumns().addAll(_cols);

        // Columna de acciones: botón 'Nueva Venta' por cliente
        TableColumn<Cliente, Void> colAccion = new TableColumn<>();
        colAccion.setPrefWidth(120);
        Label lblAcc = new Label("Acción"); lblAcc.setStyle("-fx-font-weight: bold;"); colAccion.setGraphic(lblAcc); colAccion.setStyle("-fx-alignment: CENTER;");
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

        // Protección: eliminar columnas sin encabezado ni graphic que puedan aparecer vacías (p. ej. filler column)
        javafx.application.Platform.runLater(() -> {
            try {
                var cols = table.getColumns();
                for (int i = cols.size() - 1; i >= 0; i--) {
                    TableColumn<?,?> col = cols.get(i);
                    String header = col.getText();
                    boolean hasGraphic = col.getGraphic() != null;
                    if ((header == null || header.trim().isEmpty()) && !hasGraphic) {
                        cols.remove(i);
                    }
                }
            } catch (Exception ignored) {}
        });

        filtered = new FilteredList<>(masterData, p -> true);
        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal == null ? "" : newVal.trim().toLowerCase();
            filtered.setPredicate(c -> {
                if (q.isEmpty()) return true;
                return (c.getNombre() != null && c.getNombre().toLowerCase().contains(q)) ||
                        (c.getEmail() != null && c.getEmail().toLowerCase().contains(q)) ||
                        (c.getDireccion() != null && c.getDireccion().toLowerCase().contains(q)) ||
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
                new Alert(Alert.AlertType.WARNING, "Selecciona un cliente para eliminar.").showAndWait();
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar el cliente '" + sel.getNombre() + "'?", ButtonType.YES, ButtonType.NO);
            confirm.initModality(Modality.APPLICATION_MODAL);
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    boolean ok = ClienteRepository.eliminarCliente(sel.getId());
                    if (ok) {
                        cargarDatos();
                        new Alert(Alert.AlertType.INFORMATION, "Cliente eliminado.").showAndWait();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "No se pudo eliminar el cliente.").showAndWait();
                    }
                }
            });
        });

        HBox actions = new HBox(8, btnAnadir, btnModificar);
        actions.setAlignment(Pos.CENTER_RIGHT);
        // Añadir botón Eliminar sólo si no es vendedor
        if (!isVendor) actions.getChildren().add(btnEliminar);

        root.getChildren().addAll(titulo, searchBox, table, actions);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Iniciar carga asíncrona de datos
        loadDataAsync();

        return root;
    }

    // Compatibilidad: mostrar para admin (incluye eliminar)
    public void mostrar(VBox contentArea) {
        mostrar(contentArea, false);
    }

    // Nueva API: mostrar con bandera isVendor
    public void mostrar(VBox contentArea, boolean isVendor) {
        Node view = isVendor ? cachedViewVendor : cachedViewAdmin;
        if (view == null) {
            view = createView(isVendor);
            if (isVendor) cachedViewVendor = view; else cachedViewAdmin = view;
        }
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }

    private void loadDataAsync() {
        Task<List<Cliente>> task = new Task<>() {
            @Override protected List<Cliente> call() {
                return ClienteRepository.listarClientes();
            }
        };
        task.setOnSucceeded(e -> {
            List<Cliente> lista = task.getValue();
            if (lista != null) masterData.setAll(lista);
        });
        task.setOnFailed(e -> {
            // opcional: mostrar alerta o log
            Throwable ex = task.getException();
            if (ex != null) ex.printStackTrace(System.err);
        });
        new Thread(task).start();
    }

    private void cargarDatos() {
        // Refrescar datos usando la misma carga asíncrona
        loadDataAsync();
    }

    private void showAgregarClienteDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Agregar cliente");

        VBox root = new VBox(8);
        root.setPadding(new Insets(12));

        TextField tfNombre = new TextField(); tfNombre.setPromptText("Nombre completo");
        TextField tfEmail = new TextField(); tfEmail.setPromptText("Email");
        TextField tfDireccion = new TextField(); tfDireccion.setPromptText("Dirección");
        TextField tfDocumento = new TextField(); tfDocumento.setPromptText("Documento de identidad");

        Button btnSave = new Button("Guardar");
        Button btnCancel = new Button("Cancelar");
        Label lblError = new Label(); lblError.getStyleClass().add("dialog-error");

        btnSave.setOnAction(e -> {
            String nombre = tfNombre.getText() != null ? tfNombre.getText().trim() : "";
            String email = tfEmail.getText() != null ? tfEmail.getText().trim() : "";
            String direccion = tfDireccion.getText() != null ? tfDireccion.getText().trim() : "";
            String documento = tfDocumento.getText() != null ? tfDocumento.getText().trim() : "";

            if (nombre.isEmpty()) { lblError.setText("Nombre es obligatorio."); return; }
            if (email.isEmpty()) { lblError.setText("Email es obligatorio."); return; }
            if (direccion.isEmpty()) { lblError.setText("Dirección es obligatoria."); return; }
            if (documento.isEmpty()) { lblError.setText("Documento es obligatorio."); return; }

            // Pasamos la dirección al constructor (se conservará en ambos campos para compatibilidad)
            Cliente nuevo = new Cliente(null, nombre, email, direccion, documento);
            boolean ok = ClienteRepository.agregarCliente(nuevo);
            if (ok) {
                cargarDatos();
                dialog.close();
                new Alert(Alert.AlertType.INFORMATION, "Cliente agregado.").showAndWait();
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
                new Label("Dirección:"), tfDireccion,
                new Label("Documento:"), tfDocumento,
                lblError
        );

        root.getChildren().addAll(formBox, btns);
        Scene scene1 = new Scene(root, 480, 320);
        dialog.setScene(scene1);
        dialog.setResizable(true);
        dialog.setMinWidth(420);
        dialog.setMinHeight(300);
        dialog.showAndWait();
    }

    private void showModificarClienteDialog() {
        List<Cliente> clientes = ClienteRepository.listarClientes();
         if (clientes.isEmpty()) { new Alert(Alert.AlertType.WARNING, "Selecciona un cliente para modificar.").showAndWait(); return; }
         Stage dialog = new Stage();
         dialog.initModality(Modality.APPLICATION_MODAL);
         dialog.setTitle("Modificar cliente");

         VBox root = new VBox(8);
         root.setPadding(new Insets(12));

         ComboBox<Cliente> cb = new ComboBox<>(); cb.getItems().addAll(clientes);
         cb.setConverter(new javafx.util.StringConverter<>() {
             @Override public String toString(Cliente c) { return c == null ? "" : (c.getNombre() != null ? c.getNombre() : c.getId()); }
             @Override public Cliente fromString(String string) { return null; }
         });

         TextField tfNombre = new TextField(); TextField tfEmail = new TextField(); TextField tfDireccion = new TextField(); TextField tfDocumento = new TextField();
         Label lblError = new Label(); lblError.getStyleClass().add("dialog-error");

         cb.setOnAction(ev -> {
             Cliente sel = cb.getValue();
             if (sel != null) { tfNombre.setText(sel.getNombre()); tfEmail.setText(sel.getEmail()); tfDireccion.setText(sel.getDireccion()); tfDocumento.setText(sel.getDocumento()); }
         });

         Button btnSave = new Button("Guardar"); Button btnCancel = new Button("Cancelar");
         btnSave.setOnAction(e -> {
             Cliente sel = cb.getValue(); if (sel == null) { lblError.setText("Selecciona un cliente."); return; }
             String nombre = tfNombre.getText() != null ? tfNombre.getText().trim() : "";
             String email = tfEmail.getText() != null ? tfEmail.getText().trim() : "";
             String direccion = tfDireccion.getText() != null ? tfDireccion.getText().trim() : "";
             String documento = tfDocumento.getText() != null ? tfDocumento.getText().trim() : "";
             if (nombre.isEmpty()) { lblError.setText("Nombre es obligatorio."); return; }
             if (email.isEmpty()) { lblError.setText("Email es obligatorio."); return; }
             if (direccion.isEmpty()) { lblError.setText("Dirección es obligatoria."); return; }
             if (documento.isEmpty()) { lblError.setText("Documento es obligatorio."); return; }

             Cliente actualizado = new Cliente(sel.getId(), nombre, email, direccion, documento);
            boolean ok = ClienteRepository.actualizarCliente(sel.getId(), actualizado);
             if (ok) { cargarDatos(); dialog.close(); new Alert(Alert.AlertType.INFORMATION, "Cliente actualizado.").showAndWait(); }
             else { lblError.setText("No se pudo actualizar el cliente (id puede existir o error de escritura). "); }
         });
         btnCancel.setOnAction(ev -> dialog.close());

         HBox btns = new HBox(8, btnSave, btnCancel); btns.setAlignment(Pos.CENTER_RIGHT);
         VBox formBox = new VBox(8);
         formBox.getChildren().addAll(new Label("Selecciona cliente:"), cb, new Label("Nombre:"), tfNombre, new Label("Email:"), tfEmail, new Label("Dirección:"), tfDireccion, new Label("Documento:"), tfDocumento, lblError);

         root.getChildren().addAll(formBox, btns);
        Scene scene2 = new Scene(root, 480, 360);
         dialog.setScene(scene2);
         dialog.setResizable(true);
         dialog.setMinWidth(420);
         dialog.setMinHeight(300);
         dialog.showAndWait();
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
        // limpiar columnas previas y forzar política de redimensionado
        tablaProductos.getColumns().clear();
        tablaProductos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ProductoVenta, String> colNombreProd = new TableColumn<>("Producto");
        colNombreProd.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombre()));
        colNombreProd.setStyle("-fx-alignment: CENTER;"); colNombreProd.setPrefWidth(220);

        TableColumn<ProductoVenta, String> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(cell -> new SimpleStringProperty("$" + cell.getValue().getPrecio()));
        colPrecio.setStyle("-fx-alignment: CENTER;"); colPrecio.setPrefWidth(90);

        TableColumn<ProductoVenta, String> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getCantidad())));
        colCantidad.setStyle("-fx-alignment: CENTER;"); colCantidad.setPrefWidth(90);

        TableColumn<ProductoVenta, String> colSubtotal = new TableColumn<>("Subtotal");
        colSubtotal.setCellValueFactory(cell -> new SimpleStringProperty("$" + (cell.getValue().getPrecio() * cell.getValue().getCantidad())));
        colSubtotal.setStyle("-fx-alignment: CENTER;"); colSubtotal.setPrefWidth(110);

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
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(btnEliminar);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
        colEliminar.setPrefWidth(100);

        tablaProductos.getColumns().addAll(colNombreProd, colPrecio, colCantidad, colSubtotal, colEliminar);

        // Política personalizada: calcular anchos de columnas según ratios y asignarlos cada vez que la tabla redimensione
        double[] prefsTab = new double[] {220, 90, 90, 110, 100};
        final double sumPrefsTab = java.util.Arrays.stream(prefsTab).sum();
        tablaProductos.setColumnResizePolicy(param -> {
            double totalW = tablaProductos.getWidth() - 2; // margen
            if (totalW <= 0) return true;
            for (int i = 0; i < tablaProductos.getColumns().size() && i < prefsTab.length; i++) {
                TableColumn<?,?> col = tablaProductos.getColumns().get(i);
                double w = prefsTab[i] / sumPrefsTab * totalW;
                col.setPrefWidth(Math.max(40, w));
            }
            return true;
        });

        // Protección: eliminar cualquier columna que no tenga encabezado (texto) ni graphic tras el layout
        javafx.application.Platform.runLater(() -> {
            try {
                var cols = tablaProductos.getColumns();
                for (int i = cols.size() - 1; i >= 0; i--) {
                    TableColumn<?,?> col = cols.get(i);
                    String header = col.getText();
                    boolean hasGraphic = col.getGraphic() != null;
                    if ((header == null || header.trim().isEmpty()) && !hasGraphic) {
                        cols.remove(i);
                    }
                }
            } catch (Exception ignored) {}
        });

        // Controles para agregar productos
        HBox addProductoBox = new HBox(10);
        addProductoBox.setAlignment(Pos.CENTER_LEFT);

        ComboBox<com.licoreraFx.model.Producto> cbProducto = new ComboBox<>();
        cbProducto.setPromptText("Seleccionar producto");
        cbProducto.setPrefWidth(250);
        List<com.licoreraFx.model.Producto> productos = ProductoRepository.listarProductos();
        cbProducto.getItems().addAll(productos);
        cbProducto.setConverter(new javafx.util.StringConverter<>() {
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
        // Label total declarado antes del handler para poder actualizarlo al cambiar cantidades
        Label lblTotal = new Label("Total: $0.00");
        btnAgregarProducto.setOnAction(e -> {
            com.licoreraFx.model.Producto productoSeleccionado = cbProducto.getValue();
            int cantidad = spCantidad.getValue();

            if (productoSeleccionado == null) {
                new Alert(Alert.AlertType.WARNING, "Selecciona un producto.", ButtonType.OK).showAndWait();
                return;
            }

            int stock = productoSeleccionado.getStock();
            if (stock == 0) {
                new Alert(Alert.AlertType.WARNING, "Producto Agotado", ButtonType.OK).showAndWait();
                return;
            }
            if (cantidad > stock) {
                new Alert(Alert.AlertType.WARNING, "La cantidad supera el stock disponible (" + stock + ").", ButtonType.OK).showAndWait();
                return;
            }

            // Buscar si el producto ya está en la lista
            ProductoVenta existente = null;
            for (ProductoVenta pv : productosVenta) {
                if (pv.getId().equals(productoSeleccionado.getId())) { existente = pv; break; }
            }

            if (existente != null) {
                // Aumentar la cantidad existente
                existente.setCantidad(existente.getCantidad() + cantidad);
                // Refrescar la tabla para que muestre el nuevo subtotal
                tablaProductos.refresh();
            } else {
                productosVenta.add(new ProductoVenta(productoSeleccionado.getId(), productoSeleccionado.getNombre(), productoSeleccionado.getPrecio(), cantidad));
            }

            // Recalcular total y actualizar label inmediatamente (listener no se dispara al cambiar cantidad)
            double totalNow = 0;
            for (ProductoVenta pv2 : productosVenta) totalNow += pv2.getPrecio() * pv2.getCantidad();
            lblTotal.setText("Total: $" + String.format("%.2f", totalNow));
        });

        HBox addBox = new HBox(8, cbProducto, spCantidad, btnAgregarProducto);
        addBox.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(info, lblNombre, lblDocumento, sep1, lblProductos, addBox, tablaProductos);

        // Total y botones para crear la venta
        productosVenta.addListener((javafx.collections.ListChangeListener<ProductoVenta>) c -> {
            double t = 0;
            for (ProductoVenta pv : productosVenta) t += pv.getPrecio() * pv.getCantidad();
            lblTotal.setText("Total: $" + String.format("%.2f", t));
        });

        Button btnCrearVenta = new Button("Agregar Venta");
        Button btnCerrar = new Button("Cerrar");
        HBox btnsVenta = new HBox(8, btnCrearVenta, btnCerrar);
        btnsVenta.setAlignment(Pos.CENTER_RIGHT);

        btnCrearVenta.setOnAction(e -> {
            // Construir detalles de la venta a partir de productosVenta
            java.util.List<DetalleVenta> detalles = new java.util.ArrayList<>();
            double total = 0;
            for (ProductoVenta pv : productosVenta) {
                if (pv.getCantidad() > 0) {
                    double subtotal = pv.getPrecio() * pv.getCantidad();
                    detalles.add(new DetalleVenta(pv.getId(), pv.getNombre(), pv.getCantidad(), pv.getPrecio(), subtotal));
                    total += subtotal;
                }
            }
            if (detalles.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Selecciona al menos un producto con cantidad mayor a 0.", ButtonType.OK).showAndWait();
                return;
            }

            Venta venta = new Venta(null, cliente.getId(), cliente.getNombre(), detalles, total);
            // Asignar información del vendedor desde la sesión actual si está disponible
            try {
                com.licoreraFx.util.SesionActual sa = null; // solo para referenciar la clase
                com.licoreraFx.model.Usuario usuario = com.licoreraFx.util.SesionActual.getUsuario();
                if (usuario != null) {
                    venta.setVendedorId(usuario.getId());
                    venta.setVendedorNombre(usuario.getNombre() != null && !usuario.getNombre().isEmpty() ? usuario.getNombre() : usuario.getUsername());
                    venta.setVendedorRol(usuario.getRol());
                }
            } catch (Exception ignored) {}
            // Asignar fecha actual en formato ISO
            try {
                String fechaNow = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                venta.setFecha(fechaNow);
            } catch (Exception ignored) {}
             boolean ok = VentaService.crearVenta(venta);
            if (ok) {
                new Alert(Alert.AlertType.INFORMATION, "Venta registrada correctamente.\nTotal: $" + String.format("%.2f", total), ButtonType.OK).showAndWait();
                dialog.close();
            } else {
                new Alert(Alert.AlertType.ERROR, "No se pudo registrar la venta (stock insuficiente o error).", ButtonType.OK).showAndWait();
            }
        });

        btnCerrar.setOnAction(ev -> dialog.close());

        VBox bottomBox = new VBox(8, lblTotal, btnsVenta);
        bottomBox.setPadding(new Insets(8,0,0,0));
        root.getChildren().addAll(bottomBox);

        Scene scene = new Scene(root, 700, 520);
         dialog.setScene(scene);
         dialog.showAndWait();
    }

    // Clases auxiliares internas
    private static class ProductoVenta {
        private final String id; private final String nombre; private final double precio; private int cantidad;
        ProductoVenta(String id, String nombre, double precio, int cantidad) { this.id = id; this.nombre = nombre; this.precio = precio; this.cantidad = cantidad; }
        String getId() { return id; }
        String getNombre() { return nombre; }
        double getPrecio() { return precio; }
        int getCantidad() { return cantidad; }
        void setCantidad(int cantidad) { this.cantidad = cantidad; }
    }
}
