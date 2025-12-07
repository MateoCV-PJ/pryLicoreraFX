package com.licoreraFx.controller;

import com.licoreraFx.model.DetalleVenta;
import com.licoreraFx.model.Venta;
import com.licoreraFx.util.JsonManager;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class VentasController implements Initializable {

    @FXML private TextField tfSearch;
    @FXML private TableView<Venta> tablaVentas;

    private ObservableList<Venta> masterData;
    private FilteredList<Venta> filtered;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<Venta> ventas = JsonManager.listarVentas();
        masterData = FXCollections.observableArrayList(ventas);

        TableColumn<Venta, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        TableColumn<Venta, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(c -> new SimpleStringProperty(
                JsonManager.buscarClientePorId(c.getValue().getClienteId()).map(cl -> cl.getNombre()).orElse("-")
        ));
        TableColumn<Venta, Number> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTotal()));

        tablaVentas.getColumns().setAll(colId, colCliente, colTotal);

        TableColumn<Venta, Void> colAccion = new TableColumn<>("Acción");
        colAccion.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button("Ver factura");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox box = new HBox(6, btnVer, btnEliminar);
            {
                btnVer.setOnAction(ev -> {
                    Venta v = getTableView().getItems().get(getIndex());
                    mostrarFactura(v);
                });
                btnEliminar.setOnAction(ev -> {
                    Venta v = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar venta '" + v.getId() + "'?", ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(bt -> {
                        if (bt == ButtonType.YES) {
                            boolean ok = JsonManager.eliminarVenta(v.getId());
                            if (ok) tablaVentas.getItems().remove(v); else new Alert(Alert.AlertType.ERROR, "No se pudo eliminar").showAndWait();
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
        tablaVentas.getColumns().add(colAccion);

        filtered = new FilteredList<>(masterData, v -> true);
        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal == null ? "" : newVal.trim().toLowerCase();
            filtered.setPredicate(v -> {
                if (q.isEmpty()) return true;
                String cliente = JsonManager.buscarClientePorId(v.getClienteId()).map(cl -> cl.getNombre()).orElse("").toLowerCase();
                return (v.getId() != null && v.getId().toLowerCase().contains(q)) || cliente.contains(q);
            });
        });
        SortedList<Venta> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tablaVentas.comparatorProperty());
        tablaVentas.setItems(sorted);
    }

    public void mostrar(VBox contentArea) {
        // Mostrar siempre la vista programática para evitar el uso de FXML
        Node view = createViewProgramatic();
        contentArea.getChildren().setAll(view);
    }

    private Node createViewProgramatic() {
        VBox root = new VBox(8);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.TOP_CENTER);
        root.getChildren().add(new Label("Ventas"));
        return root;
    }

    private void mostrarFactura(Venta venta) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Factura " + venta.getId());
        VBox root = new VBox(8);
        root.setPadding(new Insets(12));

        // Obtener datos del cliente (si existen)
        var clienteOpt = JsonManager.buscarClientePorId(venta.getClienteId());
        String nombreCliente = clienteOpt.map(cl -> cl.getNombre()).orElse(venta.getClienteId());
        String documento = clienteOpt.map(cl -> cl.getDocumento()).orElse("-");
        String direccion = clienteOpt.map(cl -> cl.getDireccion()).orElse("-");
        String correo = clienteOpt.map(cl -> cl.getEmail()).orElse("-");
        String idCliente = venta.getClienteId();

        Label lblCliente = new Label("Cliente: " + nombreCliente);
        Label lblDocumento = new Label("Documento: " + documento);
        Label lblDireccion = new Label("Dirección: " + direccion);
        Label lblCorreo = new Label("Correo: " + correo);
        Label lblIdCliente = new Label("ID Cliente: " + idCliente);

        // Tabla de productos: Producto | Cantidad | Precio Unit. | Subtotal
        TableView<DetalleVenta> tv = new TableView<>();
        ObservableList<DetalleVenta> items = FXCollections.observableArrayList(venta.getDetalles());
        tv.setItems(items);
        TableColumn<DetalleVenta, String> cProd = new TableColumn<>("Producto");
        cProd.setCellValueFactory(d -> new SimpleStringProperty(
                JsonManager.buscarProductoPorId(d.getValue().getProductoId()).map(p -> p.getNombre()).orElse(d.getValue().getProductoId())
        ));
        TableColumn<DetalleVenta, Number> cCant = new TableColumn<>("Cantidad");
        cCant.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getCantidad()));
        TableColumn<DetalleVenta, Number> cPrecio = new TableColumn<>("Precio Unit.");
        cPrecio.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPrecioUnitario()));
        TableColumn<DetalleVenta, Number> cSub = new TableColumn<>("Subtotal");
        cSub.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getSubtotal()));
        tv.getColumns().addAll(cProd, cCant, cPrecio, cSub);

        // Calcular totales
        double subtotal = 0;
        for (DetalleVenta dv : venta.getDetalles()) subtotal += dv.getSubtotal();
        double iva = subtotal * 0.19; // 19%
        double totalPagar = subtotal + iva;

        Label lblSubtotal = new Label("Subtotal: $" + String.format("%.2f", subtotal));
        Label lblIva = new Label("IVA (19%): $" + String.format("%.2f", iva));
        Label lblTotal = new Label("Total a pagar: $" + String.format("%.2f", totalPagar));

        // organizar layout: cliente info y totales en líneas separadas (alineadas a la izquierda)
        VBox clienteBox = new VBox(2, lblCliente, lblDocumento, lblDireccion, lblCorreo, lblIdCliente);
        clienteBox.setAlignment(Pos.CENTER_LEFT);
        VBox totalesBox = new VBox(6);
        totalesBox.setAlignment(Pos.CENTER_LEFT);
        Label lblSubtotalLine = new Label(lblSubtotal.getText()); lblSubtotalLine.setStyle(lblSubtotal.getStyle());
        Label lblIvaLine = new Label(lblIva.getText()); lblIvaLine.setStyle(lblIva.getStyle());
        Label lblTotalLine = new Label(lblTotal.getText()); lblTotalLine.setStyle(lblTotal.getStyle());
        totalesBox.getChildren().addAll(lblSubtotalLine, lblIvaLine, lblTotalLine);

        root.getChildren().addAll(clienteBox, new Label("Detalle:"), tv, totalesBox);

        Scene scene = new Scene(root, 600, 400);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
