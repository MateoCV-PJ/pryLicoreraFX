package com.licoreraFx.controller;

import com.licoreraFx.model.Compra;
import com.licoreraFx.model.Proveedor;
import com.licoreraFx.util.JsonManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
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
import java.util.Map;
import java.util.stream.Collectors;

public class ComprasController {

    private TableView<Compra> table;
    private ObservableList<Compra> masterData;
    private FilteredList<Compra> filtered;
    private Map<String, Proveedor> proveedoresById;

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

        List<Proveedor> proveedores = JsonManager.listarProveedores();
        proveedoresById = proveedores.stream().collect(Collectors.toMap(Proveedor::getId, p -> p));

        List<Compra> compras = JsonManager.listarCompras();
        masterData = FXCollections.observableArrayList(compras);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(360);
        table.setMinHeight(240);
        table.setMaxHeight(Region.USE_PREF_SIZE);

        TableColumn<Compra, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().getId()));
        TableColumn<Compra, String> colProveedor = new TableColumn<>("Proveedor");
        colProveedor.setCellValueFactory(cdf -> {
            Proveedor p = proveedoresById.get(cdf.getValue().getProveedorId());
            return new SimpleStringProperty(p != null ? p.getNombreEmpresa() : "-");
        });
        TableColumn<Compra, String> colFactura = new TableColumn<>("Factura");
        colFactura.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().getNumeroFactura()));
        TableColumn<Compra, Number> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(cdf -> new SimpleDoubleProperty(cdf.getValue().getTotal()));
        TableColumn<Compra, String> colPago = new TableColumn<>("Método Pago");
        colPago.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().getMetodoPago()));

        TableColumn<Compra, Void> colAccion = new TableColumn<>("Acción");
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
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar compra '" + compra.getNumeroFactura() + "'?", ButtonType.YES, ButtonType.NO);
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
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(colId, colProveedor, colFactura, colTotal, colPago, colAccion);
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
                return provName.toLowerCase().contains(q) ||
                        (compra.getNumeroFactura() != null && compra.getNumeroFactura().toLowerCase().contains(q)) ||
                        (compra.getMetodoPago() != null && compra.getMetodoPago().toLowerCase().contains(q));
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
        // Mostrar la vista programática para evitar el uso de FXML
        Node view = createView();
        contentArea.getChildren().setAll(view);
    }

    private void mostrarFactura(Compra compra) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Factura de Compra " + compra.getNumeroFactura());

        VBox root = new VBox(10);
        root.setPadding(new Insets(12));

        Proveedor p = proveedoresById.get(compra.getProveedorId());
        Label lblProv = new Label("Proveedor: " + (p != null ? p.getNombreEmpresa() : compra.getProveedorId()));
        Label lblFac = new Label("Factura: " + compra.getNumeroFactura());
        Label lblPago = new Label("Método de Pago: " + compra.getMetodoPago());
        Label lblTotal = new Label("Total: $" + String.format("%.2f", compra.getTotal()));

        TableView<Compra.Item> tablaItems = new TableView<>();
        ObservableList<Compra.Item> items = FXCollections.observableArrayList(compra.getItems());
        tablaItems.setItems(items);
        TableColumn<Compra.Item, String> cProd = new TableColumn<>("Producto");
        cProd.setCellValueFactory(ci -> new SimpleStringProperty(ci.getValue().getNombreProducto()));
        TableColumn<Compra.Item, Number> cCant = new TableColumn<>("Cantidad");
        cCant.setCellValueFactory(ci -> new SimpleDoubleProperty(ci.getValue().getCantidad()));
        TableColumn<Compra.Item, Number> cPrecio = new TableColumn<>("Precio");
        cPrecio.setCellValueFactory(ci -> new SimpleDoubleProperty(ci.getValue().getPrecio()));
        TableColumn<Compra.Item, Number> cSub = new TableColumn<>("Subtotal");
        cSub.setCellValueFactory(ci -> new SimpleDoubleProperty(ci.getValue().getPrecio() * ci.getValue().getCantidad()));
        tablaItems.getColumns().addAll(cProd, cCant, cPrecio, cSub);
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

        root.getChildren().addAll(lblProv, lblFac, lblPago, lblTotal, new Label("Detalle:"), tablaItems);
        Scene scene = new Scene(root, 600, 400);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
