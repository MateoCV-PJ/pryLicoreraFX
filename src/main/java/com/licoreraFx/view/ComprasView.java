package com.licoreraFx.view;

import com.licoreraFx.model.Compra;
import com.licoreraFx.model.Proveedor;
import com.licoreraFx.util.JsonManager;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Vista de Compras a Proveedores, similar a la gestión de Ventas.
 * Permite listar compras y ver facturas.
 */
public class ComprasView {

    private ObservableList<Compra> masterData;
    private FilteredList<Compra> filtered;
    private Map<String, Proveedor> proveedoresById;

    /**
     * Crea la vista programática de compras.
     */
    public Node createView() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Compras a Proveedores");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField tfSearch = new TextField();
        tfSearch.setPromptText("Buscar por proveedor, factura o método de pago...");
        tfSearch.setMaxWidth(Double.MAX_VALUE);
        HBox searchBox = new HBox(8, tfSearch);
        HBox.setHgrow(tfSearch, Priority.ALWAYS);

        // Datos de proveedores para mostrar nombres
        List<Proveedor> proveedores = JsonManager.listarProveedores();
        proveedoresById = proveedores.stream().collect(Collectors.toMap(Proveedor::getId, p -> p));

        // Cargar compras
        List<Compra> compras = JsonManager.listarCompras();
        masterData = FXCollections.observableArrayList(compras);

        // table como variable local (no es necesario campo de instancia)
        TableView<Compra> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(360);
        table.setMinHeight(240);
        table.setMaxHeight(Region.USE_PREF_SIZE);

        TableColumn<Compra, String> colId = new TableColumn<>();
        colId.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().getId()));
        Label lblColId = new Label("ID"); lblColId.setStyle("-fx-font-weight: bold;"); colId.setGraphic(lblColId); colId.setStyle("-fx-alignment: CENTER;");
        colId.setPrefWidth(80);

        TableColumn<Compra, String> colProveedor = new TableColumn<>();
        colProveedor.setCellValueFactory(cdf -> {
            Proveedor p = proveedoresById.get(cdf.getValue().getProveedorId());
            return new SimpleStringProperty(p != null ? p.getNombreEmpresa() : "-");
        });
        Label lblColProv = new Label("Proveedor"); lblColProv.setStyle("-fx-font-weight: bold;"); colProveedor.setGraphic(lblColProv); colProveedor.setStyle("-fx-alignment: CENTER;");
        colProveedor.setPrefWidth(220);

        TableColumn<Compra, String> colFactura = new TableColumn<>();
        colFactura.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().getId()));
        Label lblColFact = new Label("Factura"); lblColFact.setStyle("-fx-font-weight: bold;"); colFactura.setGraphic(lblColFact); colFactura.setStyle("-fx-alignment: CENTER;");
        colFactura.setPrefWidth(120);

        // Columna Fecha (mostrar solo dd/MM/yyyy)
        TableColumn<Compra, String> colFecha = new TableColumn<>();
        colFecha.setCellValueFactory(cdf -> {
            String raw = cdf.getValue() != null ? cdf.getValue().getFecha() : null;
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
        Label lblColFecha = new Label("Fecha"); lblColFecha.setStyle("-fx-font-weight: bold;"); colFecha.setGraphic(lblColFecha); colFecha.setStyle("-fx-alignment: CENTER;");
        colFecha.setPrefWidth(160);

        TableColumn<Compra, Number> colTotal = new TableColumn<>();
        colTotal.setCellValueFactory(cdf -> new SimpleDoubleProperty(cdf.getValue().getTotal()));
        Label lblColTotal = new Label("Total"); lblColTotal.setStyle("-fx-font-weight: bold;"); colTotal.setGraphic(lblColTotal); colTotal.setStyle("-fx-alignment: CENTER;");
        // Formatear como moneda
        colTotal.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("$%.2f", item.doubleValue()));
            }
        });
        colTotal.setPrefWidth(100);

        TableColumn<Compra, String> colPago = new TableColumn<>();
        colPago.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().getMetodoPago()));
        Label lblColPago = new Label("Método Pago"); lblColPago.setStyle("-fx-font-weight: bold;"); colPago.setGraphic(lblColPago); colPago.setStyle("-fx-alignment: CENTER;");
        colPago.setPrefWidth(140);

        TableColumn<Compra, Void> colAccion = new TableColumn<>();
        colAccion.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button("Ver factura");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox box = new HBox(6, btnVer, btnEliminar);
            {
                btnVer.setOnAction(evt -> {
                    Compra compra = getTableView().getItems().get(getIndex());
                    mostrarFactura(compra);
                });
                btnEliminar.setOnAction(evt -> {
                    Compra compra = getTableView().getItems().get(getIndex());
                    String facturaOrId = compra.getId();
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar compra '" + facturaOrId + "'?", ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(bt -> {
                        if (bt == ButtonType.YES) {
                            boolean ok = JsonManager.eliminarCompra(compra.getId());
                            if (ok) {
                                masterData.remove(compra);
                            } else {
                                new Alert(Alert.AlertType.ERROR, "No se pudo eliminar.").showAndWait();
                            }
                        }
                    });
                 });
             }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
        Label lblColAccion = new Label("Acción"); lblColAccion.setStyle("-fx-font-weight: bold;"); colAccion.setGraphic(lblColAccion); colAccion.setStyle("-fx-alignment: CENTER;");
        colAccion.setPrefWidth(160);

        // No incluimos la columna 'ID' en la tabla pública para no mostrarla en la vista
        final java.util.List<TableColumn<Compra, ?>> _cols = java.util.Arrays.asList(colProveedor, colFactura, colFecha, colTotal, colPago, colAccion);
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

        filtered = new FilteredList<>(masterData, c -> true);
        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal == null ? "" : newVal.trim().toLowerCase();
            filtered.setPredicate(compra -> {
                if (q.isEmpty()) return true;
                Proveedor p = proveedoresById.get(compra.getProveedorId());
                String provName = p != null ? p.getNombreEmpresa() : "";
                if (provName != null && provName.toLowerCase().contains(q)) return true;
                if (compra.getId() != null && compra.getId().toLowerCase().contains(q)) return true;
                return compra.getMetodoPago() != null && compra.getMetodoPago().toLowerCase().contains(q);
            });
        });

        SortedList<Compra> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        root.getChildren().addAll(titulo, searchBox, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return root;
    }

    public void mostrar(VBox contentArea) {
        Node view = createView();
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }

    private void mostrarFactura(Compra compra) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        // Mostrar diálogo sin campo de factura en la cabecera. El detalle no mostrará el campo factura.
        dialog.setTitle("Factura de Compra");

        VBox root = new VBox(10);
        root.setPadding(new Insets(12));

        Proveedor p = proveedoresById.get(compra.getProveedorId());
        Label lblProv = new Label("Proveedor: " + (p != null ? p.getNombreEmpresa() : compra.getProveedorId()));
        // Formatear fecha detalle (dd/MM/yyyy)
        String fechaRaw = compra.getFecha();
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
        Label lblFecha = new Label("Fecha: " + fechaFmt);
        Label lblRut = new Label("RUT: " + (p != null && p.getRut() != null ? p.getRut() : "-"));
        Label lblDir = new Label("Dirección: " + (p != null && p.getDireccion() != null ? p.getDireccion() : "-"));
        Label lblCorreo = new Label("Correo: " + (p != null && p.getEmail() != null ? p.getEmail() : "-"));
        Label lblFac = new Label("Factura: " + compra.getId());

        // Tabla de items: Producto | Cantidad | Precio unitario
        TableView<Compra.Item> tablaItems = new TableView<>();
        tablaItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ObservableList<Compra.Item> items = FXCollections.observableArrayList(compra.getItems());
        tablaItems.setItems(items);

        TableColumn<Compra.Item, String> cProd = new TableColumn<>();
        cProd.setCellValueFactory(ci -> new SimpleStringProperty(ci.getValue().getNombreProducto()));
        Label lblCProd = new Label("Producto"); lblCProd.setStyle("-fx-font-weight: bold;"); cProd.setGraphic(lblCProd); cProd.setStyle("-fx-alignment: CENTER-LEFT;");
        cProd.setPrefWidth(300);

        TableColumn<Compra.Item, Number> cCant = new TableColumn<>();
        cCant.setCellValueFactory(ci -> new SimpleDoubleProperty(ci.getValue().getCantidad()));
        Label lblCCant = new Label("Cantidad"); lblCCant.setStyle("-fx-font-weight: bold;"); cCant.setGraphic(lblCCant); cCant.setStyle("-fx-alignment: CENTER;");
        cCant.setPrefWidth(120);

        TableColumn<Compra.Item, Number> cPrecio = new TableColumn<>();
        cPrecio.setCellValueFactory(ci -> new SimpleDoubleProperty(ci.getValue().getPrecio()));
        Label lblCPrecio = new Label("Precio Unit."); lblCPrecio.setStyle("-fx-font-weight: bold;"); cPrecio.setGraphic(lblCPrecio); cPrecio.setStyle("-fx-alignment: CENTER;");
        cPrecio.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("$%.2f", item.doubleValue()));
            }
        });
        cPrecio.setPrefWidth(120);

        TableColumn<Compra.Item, Number> cSub = new TableColumn<>();
        cSub.setCellValueFactory(ci -> new SimpleDoubleProperty(ci.getValue().getPrecio() * ci.getValue().getCantidad()));
        Label lblCSub = new Label("Subtotal"); lblCSub.setStyle("-fx-font-weight: bold;"); cSub.setGraphic(lblCSub); cSub.setStyle("-fx-alignment: CENTER;");
        cSub.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("$%.2f", item.doubleValue()));
            }
        });
        cSub.setPrefWidth(120);

        final java.util.List<TableColumn<Compra.Item, ?>> _colsItems = java.util.Arrays.asList(cProd, cCant, cPrecio, cSub);
        tablaItems.getColumns().addAll(_colsItems);
        Platform.runLater(() -> {
            try {
                var cols = tablaItems.getColumns();
                for (int i = cols.size() - 1; i >= 0; i--) {
                    TableColumn<?,?> col = cols.get(i);
                    String header = col.getText(); boolean hasGraphic = col.getGraphic() != null;
                    if ((header == null || header.trim().isEmpty()) && !hasGraphic) cols.remove(i);
                }
            } catch (Exception ignored) {}
        });

        // Calcular subtotal e IVA (19%) y total
        double subtotalVal = 0;
        for (Compra.Item it : items) subtotalVal += it.getCantidad() * it.getPrecio();
        double ivaVal = subtotalVal * 0.19; // 19%
        double totalVal = subtotalVal + ivaVal;

        Label lblSubtotalVal = new Label("Subtotal: $" + String.format("%.2f", subtotalVal));
        Label lblIvaVal = new Label("IVA (19%): $" + String.format("%.2f", ivaVal));
        Label lblTotalVal = new Label("Total: $" + String.format("%.2f", totalVal));

        // Agrupar información del proveedor en una columna (incluye etiqueta fecha y factura)
        VBox proveedorBox = new VBox(4, lblProv, lblFecha, lblRut, lblDir, lblCorreo, lblFac);
        proveedorBox.setPadding(new Insets(4,0,6,0));

        VBox totalesBox = new VBox(6, lblSubtotalVal, lblIvaVal, lblTotalVal);
        totalesBox.setAlignment(Pos.CENTER_RIGHT);
        totalesBox.setPadding(new Insets(8,0,0,0));

        // Botón Cerrar alineado a la derecha
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setOnAction(ev -> dialog.close());
        HBox btnBox = new HBox(btnCerrar);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(proveedorBox, new Label("Detalle:"), tablaItems, totalesBox, btnBox);
        Scene scene = new Scene(root, 700, 500);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
