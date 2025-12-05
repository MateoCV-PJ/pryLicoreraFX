package com.licoreraFx.view;

import com.licoreraFx.model.Venta;
import com.licoreraFx.model.DetalleVenta;
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

public class VentasView {

    private TableView<Venta> table;
    private ObservableList<Venta> masterData;
    private FilteredList<Venta> filtered;

    public Node createView() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Gestión de Ventas");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Campo de búsqueda
        TextField tfSearch = new TextField();
        tfSearch.getStyleClass().add("search-field");
        tfSearch.setPromptText("Buscar por ID de venta o nombre de cliente...");
        tfSearch.setMaxWidth(Double.MAX_VALUE);
        HBox searchBox = new HBox(8, tfSearch);
        HBox.setHgrow(tfSearch, Priority.ALWAYS);

        List<Venta> ventas = JsonManager.listarVentas();
        masterData = FXCollections.observableArrayList(ventas);

        table = new TableView<>();
        table.getStyleClass().add("ventas-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(320);
        table.setMinHeight(200);
        table.setMaxHeight(Region.USE_PREF_SIZE);

        TableColumn<Venta, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));

        TableColumn<Venta, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombreCliente()));


        TableColumn<Venta, String> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(cell -> new SimpleStringProperty(String.format("$%.2f", cell.getValue().getTotal())));

        TableColumn<Venta, Void> colAccion = new TableColumn<>("Acción");
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
        table.getColumns().addAll(colId, colCliente, colTotal, colAccion);

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

        return root;
    }

    public void mostrar(VBox contentArea) {
        Node view = createView();
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }

    private void eliminarVenta(Venta venta) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar venta #" + venta.getId() + "?");
        confirmacion.setContentText("Cliente: " + venta.getNombreCliente() + "\nTotal: $" + String.format("%.2f", venta.getTotal()));

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean ok = JsonManager.eliminarVenta(venta.getId());
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

        Label lblCliente = new Label("Cliente: " + venta.getNombreCliente());
        lblCliente.setStyle("-fx-font-weight: bold;");


        Separator sep = new Separator();

        Label lblProductos = new Label("Productos:");
        lblProductos.setStyle("-fx-font-weight: bold;");

        TableView<DetalleVenta> tableDetalles = new TableView<>();
        ObservableList<DetalleVenta> detalles = FXCollections.observableArrayList(venta.getDetalles());
        tableDetalles.setItems(detalles);

        TableColumn<DetalleVenta, String> colProducto = new TableColumn<>("Producto");
        colProducto.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombreProducto()));
        colProducto.setPrefWidth(200);

        TableColumn<DetalleVenta, String> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getCantidad())));
        colCantidad.setPrefWidth(100);

        TableColumn<DetalleVenta, String> colPrecio = new TableColumn<>("Precio Unit.");
        colPrecio.setCellValueFactory(cell -> new SimpleStringProperty(String.format("$%.2f", cell.getValue().getPrecioUnitario())));
        colPrecio.setPrefWidth(100);

        TableColumn<DetalleVenta, String> colSubtotal = new TableColumn<>("Subtotal");
        colSubtotal.setCellValueFactory(cell -> new SimpleStringProperty(String.format("$%.2f", cell.getValue().getSubtotal())));
        colSubtotal.setPrefWidth(100);

        //noinspection unchecked
        tableDetalles.getColumns().addAll(colProducto, colCantidad, colPrecio, colSubtotal);

        Label lblTotal = new Label(String.format("TOTAL: $%.2f", venta.getTotal()));
        lblTotal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setOnAction(e -> dialog.close());
        HBox btnBox = new HBox(btnCerrar);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(lblCliente, sep, lblProductos, tableDetalles, lblTotal, btnBox);

        Scene scene = new Scene(root, 600, 400);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}

