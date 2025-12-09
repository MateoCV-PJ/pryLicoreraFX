package com.licoreraFx.view;

import com.licoreraFx.model.Venta;
import com.licoreraFx.model.DetalleVenta;
import com.licoreraFx.repository.VentaRepository;
import javafx.concurrent.Task;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
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

import java.util.List;
import com.licoreraFx.model.Cliente;
import com.licoreraFx.repository.ClienteRepository;

/**
 * Vista para listar y gestionar ventas.
 * Permite ver detalle y eliminar ventas.
 */
public class VentasView {

    private ObservableList<Venta> masterData;
    private FilteredList<Venta> filtered;

    private Node cachedView = null;

    public Node createView() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Gestión de Ventas");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Campo de búsqueda
        TextField tfSearch = new TextField();
        tfSearch.setPromptText("Buscar por ID de venta o nombre de cliente...");
        tfSearch.setMaxWidth(Double.MAX_VALUE);
        HBox searchBox = new HBox(8, tfSearch);
        HBox.setHgrow(tfSearch, Priority.ALWAYS);

        masterData = FXCollections.observableArrayList();

        TableView<Venta> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(320);
        table.setMinHeight(200);
        table.setMaxHeight(Region.USE_PREF_SIZE);

        TableColumn<Venta, String> colId = new TableColumn<>();
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        Label lblId = new Label("Factura"); lblId.setStyle("-fx-font-weight: bold;"); colId.setGraphic(lblId); colId.setStyle("-fx-alignment: CENTER;");

        TableColumn<Venta, String> colCliente = new TableColumn<>();
        colCliente.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombreCliente()));
        Label lblCliente = new Label("Cliente"); lblCliente.setStyle("-fx-font-weight: bold;"); colCliente.setGraphic(lblCliente); colCliente.setStyle("-fx-alignment: CENTER;");

        // Columna Fecha (mostrar solo dd/MM/yyyy)
        TableColumn<Venta, String> colFecha = new TableColumn<>();
        colFecha.setCellValueFactory(cell -> {
            String raw = cell.getValue() != null ? cell.getValue().getFecha() : null;
            String out = "-";
            if (raw != null && !raw.trim().isEmpty()) {
                try {
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(raw);
                    out = ldt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } catch (Exception ex1) {
                    try {
                        java.time.LocalDate ld = java.time.LocalDate.parse(raw);
                        out = ld.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (Exception ex2) {
                        // Fallback: si contiene 'T' tomar la parte antes de T y tratar de parsear
                        try {
                            if (raw.contains("T")) {
                                String datePart = raw.split("T")[0];
                                java.time.LocalDate ld2 = java.time.LocalDate.parse(datePart);
                                out = ld2.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                            } else {
                                out = raw;
                            }
                        } catch (Exception ex3) { out = raw; }
                    }
                }
            }
            return new SimpleStringProperty(out);
        });
        Label lblFecha = new Label("Fecha"); lblFecha.setStyle("-fx-font-weight: bold;"); colFecha.setGraphic(lblFecha); colFecha.setStyle("-fx-alignment: CENTER;");

        // Columna para mostrar el vendedor (nombre) o indicar si fue Administrador
        TableColumn<Venta, String> colVendedor = new TableColumn<>();
        colVendedor.setCellValueFactory(cell -> {
            Venta v = cell.getValue();
            String nombre = v == null ? null : v.getVendedorNombre();
            String rol = v == null ? null : v.getVendedorRol();
            String texto;
            if (nombre != null && !nombre.trim().isEmpty()) {
                texto = nombre;
                if (rol != null) {
                    String r = rol.trim().toUpperCase();
                    if (r.equals("ADMIN") || r.equals("ADMINISTRADOR")) texto += " (Administrador)";
                }
            } else if (rol != null) {
                String r = rol.trim().toUpperCase();
                if (r.equals("ADMIN") || r.equals("ADMINISTRADOR")) texto = "Administrador";
                else texto = "(no registrado)";
            } else {
                texto = "(no registrado)";
            }
            return new SimpleStringProperty(texto);
        });
        Label lblVendedor = new Label("Vendedor"); lblVendedor.setStyle("-fx-font-weight: bold;"); colVendedor.setGraphic(lblVendedor); colVendedor.setStyle("-fx-alignment: CENTER;");

        TableColumn<Venta, String> colTotal = new TableColumn<>();
        colTotal.setCellValueFactory(cell -> new SimpleStringProperty(String.format("$%.2f", cell.getValue().getTotal())));
        Label lblTotal = new Label("Total"); lblTotal.setStyle("-fx-font-weight: bold;"); colTotal.setGraphic(lblTotal); colTotal.setStyle("-fx-alignment: CENTER;");

        TableColumn<Venta, Void> colAccion = new TableColumn<>();
        Label lblAccion = new Label("Acción"); lblAccion.setStyle("-fx-font-weight: bold;"); colAccion.setGraphic(lblAccion); colAccion.setStyle("-fx-alignment: CENTER;");
        colAccion.setCellFactory(param -> new TableCell<>() {
            private final Button btnVerFactura = new Button("Ver Factura");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox hbox = new HBox(5, btnVerFactura, btnEliminar);

            {
                btnVerFactura.setOnAction(event -> {
                    Venta v = getTableView().getItems().get(getIndex());
                    showDetalleVenta(v);
                });

                btnEliminar.setOnAction(event -> {
                    Venta v = getTableView().getItems().get(getIndex());
                    eliminarVenta(v);
                });

                hbox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

        //noinspection unchecked
        table.getColumns().addAll(colId, colFecha, colCliente, colVendedor, colTotal, colAccion);

        // Configurar filtrado
        filtered = new FilteredList<>(masterData, p -> true);
        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal == null ? "" : newVal.trim().toLowerCase();
            filtered.setPredicate(v -> {
                if (q.isEmpty()) return true;
                return (v.getId() != null && v.getId().toLowerCase().contains(q)) ||
                        (v.getNombreCliente() != null && v.getNombreCliente().toLowerCase().contains(q));
            });
        });

        SortedList<Venta> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        root.getChildren().addAll(titulo, searchBox, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Cargar datos en background
        loadDataAsync();

        // Registrar listener para refrescar la tabla cuando cambien las ventas
        com.licoreraFx.repository.VentaRepository.addChangeListener(() -> {
            // recargar datos en background
            loadDataAsync();
        });

        return root;
    }

    public void mostrar(VBox contentArea) {
        Node view = cachedView;
        if (view == null) {
            view = createView();
            cachedView = view;
        }
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }

    private void loadDataAsync() {
        Task<List<Venta>> task = new Task<>() {
            @Override protected List<Venta> call() {
                return VentaRepository.listarVentas();
            }
        };
        task.setOnSucceeded(e -> {
            List<Venta> lista = task.getValue();
            if (lista != null) masterData.setAll(lista);
        });
        task.setOnFailed(e -> { Throwable ex = task.getException(); if (ex != null) ex.printStackTrace(System.err); });
        new Thread(task).start();
    }

    private void eliminarVenta(Venta venta) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar venta #" + venta.getId() + "?");
        confirmacion.setContentText("Cliente: " + venta.getNombreCliente() + "\nTotal: $" + String.format("%.2f", venta.getTotal()));

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean ok = VentaRepository.eliminarVenta(venta.getId());
                if (ok) {
                    masterData.remove(venta);
                    new Alert(Alert.AlertType.INFORMATION, "Venta eliminada exitosamente.", ButtonType.OK).showAndWait();
                } else {
                    new Alert(Alert.AlertType.ERROR, "No se pudo eliminar la venta.", ButtonType.OK).showAndWait();
                }
            }
        });
    }

    private void showDetalleVenta(Venta venta) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Detalle de Venta #" + venta.getId());

        VBox root = new VBox(10);
        root.setPadding(new Insets(12));

        // Obtener datos del cliente (si existen) para mostrar detalle completo
        String documento = "-"; String direccion = "-"; String correo = "-"; String idCliente = venta.getClienteId() != null ? venta.getClienteId() : "-";
        try {
            java.util.Optional<Cliente> clOpt = ClienteRepository.buscarClientePorId(venta.getClienteId());
            if (clOpt.isPresent()) {
                Cliente c = clOpt.get();
                documento = c.getDocumento() != null ? c.getDocumento() : "-";
                direccion = c.getDireccion() != null ? c.getDireccion() : "-";
                correo = c.getEmail() != null ? c.getEmail() : "-";
                idCliente = c.getId() != null ? c.getId() : idCliente;
            }
        } catch (Exception ignored) {}

        Label lblCliente = new Label("Cliente: " + (venta.getNombreCliente() != null ? venta.getNombreCliente() : "-"));
        // Formatear fecha detalle (dd/MM/yyyy)
        String fechaRaw = venta.getFecha();
        String fechaFmt = "-";
        if (fechaRaw != null && !fechaRaw.trim().isEmpty()) {
            try { fechaFmt = java.time.LocalDateTime.parse(fechaRaw).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")); }
            catch (Exception ex1) {
                try { fechaFmt = java.time.LocalDate.parse(fechaRaw).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")); }
                catch (Exception ex2) {
                    try { if (fechaRaw.contains("T")) fechaFmt = java.time.LocalDate.parse(fechaRaw.split("T")[0]).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")); else fechaFmt = fechaRaw; } catch (Exception ex3) { fechaFmt = fechaRaw; }
                }
            }
        }
        Label lblFechaVenta = new Label("Fecha: " + fechaFmt);
        Label lblDocumento = new Label("Documento: " + documento);
        Label lblDireccion = new Label("Dirección: " + direccion);
        Label lblCorreo = new Label("Correo: " + correo);
        Label lblIdCliente = new Label("Factura: " + idCliente);

        // Mostrar información del vendedor (si se registró al crear la venta)
        String vendedorNombre = venta.getVendedorNombre() != null ? venta.getVendedorNombre() : null;
        String vendedorRol = venta.getVendedorRol() != null ? venta.getVendedorRol() : null;
        String vendedorTexto = "(no registrado)";
        if (vendedorNombre != null && !vendedorNombre.trim().isEmpty()) {
            vendedorTexto = vendedorNombre;
            if (vendedorRol != null) {
                String r = vendedorRol.trim().toUpperCase();
                if (r.equals("ADMIN") || r.equals("ADMINISTRADOR")) vendedorTexto += " (Administrador)";
            }
        } else if (vendedorRol != null) {
            String r = vendedorRol.trim().toUpperCase();
            if (r.equals("ADMIN") || r.equals("ADMINISTRADOR")) vendedorTexto = "Administrador";
        }
        Label lblVendedor = new Label(vendedorTexto);

        TableView<DetalleVenta> tableDetalles = new TableView<>();
        tableDetalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ObservableList<DetalleVenta> detalles = FXCollections.observableArrayList(venta.getDetalles());
        tableDetalles.setItems(detalles);

        TableColumn<DetalleVenta, String> colProducto = new TableColumn<>();
        colProducto.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombreProducto()));
        Label lblProd = new Label("Producto"); lblProd.setStyle("-fx-font-weight: bold;"); colProducto.setGraphic(lblProd); colProducto.setStyle("-fx-alignment: CENTER-LEFT;");
        colProducto.setPrefWidth(300);

        TableColumn<DetalleVenta, Number> colCantidad = new TableColumn<>();
        colCantidad.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getCantidad()));
        Label lblCant = new Label("Cantidad"); lblCant.setStyle("-fx-font-weight: bold;"); colCantidad.setGraphic(lblCant); colCantidad.setStyle("-fx-alignment: CENTER;");
        colCantidad.setPrefWidth(120);
        colCantidad.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("%d", item.intValue()));
                setStyle("-fx-alignment: CENTER_RIGHT;");
            }
        });

        TableColumn<DetalleVenta, Number> colPrecio = new TableColumn<>();
        colPrecio.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getPrecioUnitario()));
        Label lblPrecioUnit = new Label("Precio Unit."); lblPrecioUnit.setStyle("-fx-font-weight: bold;"); colPrecio.setGraphic(lblPrecioUnit); colPrecio.setStyle("-fx-alignment: CENTER;");
        colPrecio.setPrefWidth(120);
        colPrecio.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("$%.2f", item.doubleValue()));
                setStyle("-fx-alignment: CENTER_RIGHT;");
            }
        });

        TableColumn<DetalleVenta, Number> colSubtotal = new TableColumn<>();
        colSubtotal.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getSubtotal()));
        Label lblSubtotal = new Label("Subtotal"); lblSubtotal.setStyle("-fx-font-weight: bold;"); colSubtotal.setGraphic(lblSubtotal); colSubtotal.setStyle("-fx-alignment: CENTER;");
        colSubtotal.setPrefWidth(120);
        colSubtotal.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("$%.2f", item.doubleValue()));
                setStyle("-fx-alignment: CENTER_RIGHT;");
            }
        });

        tableDetalles.getColumns().addAll(colProducto, colCantidad, colPrecio, colSubtotal);
        // Quitar columnas vacías (sin texto y sin graphic) que pudieran aparecer como columnas 'filler'
        Platform.runLater(() -> {
            try {
                var cols = tableDetalles.getColumns();
                for (int i = cols.size() - 1; i >= 0; i--) {
                    TableColumn<?, ?> col = cols.get(i);
                    String header = col.getText();
                    boolean hasGraphic = col.getGraphic() != null;
                    if ((header == null || header.trim().isEmpty()) && !hasGraphic) {
                        cols.remove(i);
                    }
                }
            } catch (Exception ignored) {}
        });

        // Calcular totales
        double subtotalVal = 0;
        for (DetalleVenta dv : detalles) subtotalVal += dv.getSubtotal();
        double ivaVal = subtotalVal * 0.19; // 19%
        // El total a pagar en esta vista es solo la suma de los subtotales (sin añadir IVA)
        double totalPagarVal = subtotalVal;

        Label lblSubtotalVal = new Label("Subtotal: $" + String.format("%.2f", subtotalVal));
        Label lblIvaVal = new Label("IVA (19%): $" + String.format("%.2f", ivaVal));
        Label lblTotalVal = new Label("Total a pagar: $" + String.format("%.2f", totalPagarVal));

        // Mostrar totales en líneas separadas, alineados a la derecha
        VBox totales = new VBox(6);
        totales.setAlignment(Pos.CENTER_RIGHT);
        Label lSubtotalLine = new Label(lblSubtotalVal.getText()); lSubtotalLine.setStyle(lblSubtotalVal.getStyle());
        Label lIvaLine = new Label(lblIvaVal.getText()); lIvaLine.setStyle(lblIvaVal.getStyle());
        Label lTotalLine = new Label(lblTotalVal.getText()); lTotalLine.setStyle(lblTotalVal.getStyle());
        // Forzar que las etiquetas ocupen ancho completo para respetar alineación derecha
        lSubtotalLine.setMaxWidth(Double.MAX_VALUE); lSubtotalLine.setAlignment(Pos.CENTER_RIGHT);
        lIvaLine.setMaxWidth(Double.MAX_VALUE); lIvaLine.setAlignment(Pos.CENTER_RIGHT);
        lTotalLine.setMaxWidth(Double.MAX_VALUE); lTotalLine.setAlignment(Pos.CENTER_RIGHT);
        totales.getChildren().addAll(lSubtotalLine, lIvaLine, lTotalLine);

        // Botón Cerrar alineado a la derecha
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setOnAction(ev -> dialog.close());
        HBox btnBox = new HBox(btnCerrar);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(new VBox(2, lblCliente, lblFechaVenta, lblDocumento, lblDireccion, lblCorreo, lblIdCliente), new Label("Productos:"), tableDetalles, totales, btnBox);

        Scene scene = new Scene(root, 600, 400);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
