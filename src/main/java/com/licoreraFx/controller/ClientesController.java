package com.licoreraFx.controller;

import com.licoreraFx.model.Cliente;
import com.licoreraFx.model.DetalleVenta;
import com.licoreraFx.model.Producto;
import com.licoreraFx.model.Venta;
import com.licoreraFx.repository.ClienteRepository;
import com.licoreraFx.repository.ProductoRepository;
import com.licoreraFx.service.VentaService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la gestión de clientes.
 * Lista clientes y permite crear una nueva venta para un cliente.
 */
public class ClientesController implements Initializable {

    @FXML private TextField tfSearch;
    @FXML private TableView<Cliente> tablaClientes;

    private ObservableList<Cliente> masterData;
    private FilteredList<Cliente> filtered;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<Cliente> clientes = ClienteRepository.listarClientes();
        masterData = FXCollections.observableArrayList(clientes);

        TableColumn<Cliente, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        TableColumn<Cliente, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        TableColumn<Cliente, String> colDocumento = new TableColumn<>("Documento");
        colDocumento.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDocumento()));
        tablaClientes.getColumns().setAll(colId, colNombre, colDocumento);

        TableColumn<Cliente, Void> colAccion = new TableColumn<>("Acción");
        colAccion.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Nueva Venta");
            {
                btn.setOnAction(ev -> {
                    Cliente c = getTableView().getItems().get(getIndex());
                    showNuevaVentaDialog(c);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        tablaClientes.getColumns().add(colAccion);

        filtered = new FilteredList<>(masterData, p -> true);
        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal == null ? "" : newVal.trim().toLowerCase();
            filtered.setPredicate(c -> {
                if (q.isEmpty()) return true;
                return (c.getNombre() != null && c.getNombre().toLowerCase().contains(q)) ||
                        (c.getDocumento() != null && c.getDocumento().toLowerCase().contains(q));
            });
        });
        SortedList<Cliente> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tablaClientes.comparatorProperty());
        tablaClientes.setItems(sorted);
    }

    /**
     * Inserta la vista de clientes en el área de contenido dada.
     * @param contentArea Contenedor donde se mostrará la vista.
     */
    public void mostrar(VBox contentArea) {
        // Mostrar la vista programática para evitar el uso de FXML
        Node view = createViewProgramatic();
        contentArea.getChildren().setAll(view);
    }

    private Node createViewProgramatic() {
        VBox root = new VBox(8);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.TOP_CENTER);
        root.getChildren().add(new Label("Clientes"));
        return root;
    }

    public void showNuevaVentaDialog(Cliente cliente) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Nueva Venta - " + cliente.getNombre());

        VBox root = new VBox(8);
        root.setPadding(new Insets(12));

        TableView<ProductoCantidad> tv = new TableView<>();
        ObservableList<ProductoCantidad> productosData = FXCollections.observableArrayList();
        for (Producto p : ProductoRepository.listarProductos()) productosData.add(new ProductoCantidad(p));
        tv.setItems(productosData);
        // Evitar columnas residuales y usar la política estándar para que las columnas ocupen el ancho correctamente
        tv.getColumns().clear();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Columnas: Producto | Cantidad (Spinner) | Precio Unit. | Subtotal
        TableColumn<ProductoCantidad, String> cProducto = new TableColumn<>("Producto");
        cProducto.setCellValueFactory(pc -> new SimpleStringProperty(pc.getValue().producto.getNombre()));
        cProducto.setStyle("-fx-alignment: CENTER;"); cProducto.setPrefWidth(260);

        TableColumn<ProductoCantidad, Void> cCantidad = new TableColumn<>("Cantidad");
        cCantidad.setCellFactory(tc -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>();
            private boolean updating = false;
            {
                spinner.setEditable(true);
                spinner.setValueFactory(new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));
                spinner.valueProperty().addListener((obs, ov, nv) -> {
                    if (updating) return;
                    int newVal = nv == null ? 0 : nv;
                    // usar el ValueFactory como referencia del max permitido
                    javafx.scene.control.SpinnerValueFactory<Integer> vf = spinner.getValueFactory();
                    if (!(vf instanceof javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory)) return;
                    javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory ivf = (javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory) vf;
                    int maxAllowed = ivf.getMax();
                    // si no tenemos userData (producto asociado) salir
                    Object uid = spinner.getUserData();
                    if (!(uid instanceof String)) return;
                    String pid = (String) uid;
                    // Si el spinner no permite más que 0 -> Producto Agotado
                    if (maxAllowed == 0 && newVal > 0) {
                        updating = true; ivf.setValue(0); updating = false;
                        // actualizar modelo
                        for (ProductoCantidad item : productosData) { if (item.producto != null && pid.equals(item.producto.getId())) { item.setCantidad(0); break; } }
                        recalcTotals(productosData, root);
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Producto Agotado");
                        if (tv.getScene() != null && tv.getScene().getWindow() != null) alert.initOwner(tv.getScene().getWindow());
                        alert.showAndWait();
                        return;
                    }
                    // Si excede el máximo visible -> limitar y advertir
                    if (maxAllowed > 0 && newVal > maxAllowed) {
                        updating = true; ivf.setValue(maxAllowed); updating = false;
                        for (ProductoCantidad item : productosData) { if (item.producto != null && pid.equals(item.producto.getId())) { item.setCantidad(maxAllowed); break; } }
                        recalcTotals(productosData, root);
                        Alert alert = new Alert(Alert.AlertType.WARNING, "La cantidad supera el stock disponible (" + maxAllowed + ")");
                        if (tv.getScene() != null && tv.getScene().getWindow() != null) alert.initOwner(tv.getScene().getWindow());
                        alert.showAndWait();
                        return;
                    }
                    // Valor válido -> actualizar modelo
                    for (ProductoCantidad item : productosData) { if (item.producto != null && pid.equals(item.producto.getId())) { item.setCantidad(newVal); break; } }
                    recalcTotals(productosData, root);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); }
                else {
                    int idx = getIndex();
                    ProductoCantidad pc = null;
                    if (tv != null && idx >= 0 && idx < tv.getItems().size()) pc = tv.getItems().get(idx);
                    int stock = (pc == null) ? 0 : pc.producto.getStock();
                    int current = (pc == null) ? 0 : pc.getCantidad();
                    updating = true;
                    spinner.setValueFactory(new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(0, Math.max(0, stock), Math.max(0, Math.min(current, Math.max(0, stock)))));
                    updating = false;
                    boolean agotado = stock <= 0;
                    spinner.setDisable(agotado);
                    if (spinner.getEditor() != null) spinner.getEditor().setDisable(agotado);
                    spinner.setTooltip(agotado ? new Tooltip("Agotado") : null);
                    if (pc != null && pc.producto != null) spinner.setUserData(pc.producto.getId()); else spinner.setUserData(null);
                    HBox box = new HBox(spinner); box.setAlignment(Pos.CENTER); setGraphic(box);
                }
            }
        });
        cCantidad.setStyle("-fx-alignment: CENTER;"); cCantidad.setPrefWidth(100);

        TableColumn<ProductoCantidad, Number> cPrecioUnit = new TableColumn<>("Precio Unit.");
        cPrecioUnit.setCellValueFactory(pc -> new SimpleDoubleProperty(pc.getValue().producto.getPrecio()));
        cPrecioUnit.setStyle("-fx-alignment: CENTER;"); cPrecioUnit.setPrefWidth(90);

        TableColumn<ProductoCantidad, Number> cSubtotal = new TableColumn<>("Subtotal");
        cSubtotal.setCellValueFactory(pc -> new SimpleDoubleProperty(pc.getValue().getSubtotal()));
        cSubtotal.setStyle("-fx-alignment: CENTER;"); cSubtotal.setPrefWidth(120);

        tv.getColumns().addAll(cProducto, cCantidad, cPrecioUnit, cSubtotal);
        // Eliminar columns vacías si aparecen (filler)
        javafx.application.Platform.runLater(() -> {
            try {
                var cols = tv.getColumns();
                for (int i = cols.size() - 1; i >= 0; i--) {
                    TableColumn<?,?> col = cols.get(i);
                    String header = col.getText(); boolean hasGraphic = col.getGraphic() != null;
                    if ((header == null || header.trim().isEmpty()) && !hasGraphic) cols.remove(i);
                }
            } catch (Exception ignored) {}
        });

        // Totales e IVA
        Label lblSubtotal = new Label("Subtotal: $0.00");
        Label lblIva = new Label("IVA: $0.00");
        Label lblTotalPagar = new Label("Total a pagar: $0.00");

        // Inicializar totales (se llamará de nuevo tras añadir los labels al layout)
        recalcTotals(productosData, root);

        Button btnCrear = new Button("Crear Venta");
        Button btnCancelar = new Button("Cancelar");
        btnCrear.setOnAction(ev -> {
             List<DetalleVenta> detalles = new ArrayList<>();
             double total = 0;
             for (ProductoCantidad pc : productosData) {
                 if (pc.getCantidad() > 0) {
                     double subtotal = pc.getSubtotal();
                     detalles.add(new DetalleVenta(pc.producto.getId(), pc.producto.getNombre(), pc.getCantidad(), pc.producto.getPrecio(), subtotal));
                     total += subtotal;
                 }
             }
             if (detalles.isEmpty()) { new Alert(Alert.AlertType.WARNING, "Selecciona al menos un producto").showAndWait(); return; }
            String id = com.licoreraFx.repository.VentaRepository.generarIdVenta();
            Venta venta = new Venta(id, cliente.getId(), cliente.getNombre(), detalles, total);
            // Asignar información del vendedor desde la sesión actual si existe
            try {
                com.licoreraFx.model.Usuario usuario = com.licoreraFx.util.SesionActual.getUsuario();
                if (usuario != null) {
                    venta.setVendedorId(usuario.getId());
                    venta.setVendedorNombre(usuario.getNombre() != null && !usuario.getNombre().isEmpty() ? usuario.getNombre() : usuario.getUsername());
                    venta.setVendedorRol(usuario.getRol());
                }
            } catch (Exception ignored) {}
            // Asignar fecha actual ISO
            try { venta.setFecha(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)); } catch (Exception ignored) {}
             boolean ok = VentaService.crearVenta(venta);
             if (!ok) { new Alert(Alert.AlertType.ERROR, "Error guardando venta").showAndWait(); return; }
             new Alert(Alert.AlertType.INFORMATION, "Venta creada. Total: $" + String.format("%.2f", total)).showAndWait();
             dialog.close();
         });
        btnCancelar.setOnAction(ev -> dialog.close());

        // Detalles del cliente en el orden solicitado
        Label lCliente = new Label("Cliente: " + (cliente.getNombre() != null ? cliente.getNombre() : "-"));
        Label lDocumento = new Label("Documento: " + (cliente.getDocumento() != null ? cliente.getDocumento() : "-"));
        String direccion = "-"; String correo = "-"; String idCliente = "-";
        try { direccion = cliente.getDireccion() != null ? cliente.getDireccion() : "-"; } catch (Exception ignored) {}
        try { correo = cliente.getEmail() != null ? cliente.getEmail() : "-"; } catch (Exception ignored) {}
        try { idCliente = cliente.getId() != null ? cliente.getId() : "-"; } catch (Exception ignored) {}
        Label lDireccion = new Label("Dirección: " + direccion);
        Label lCorreo = new Label("Correo: " + correo);
        Label lIdCliente = new Label("ID Cliente: " + idCliente);

        HBox totalesBox = new HBox(16, lblIva, lblTotalPagar);
        totalesBox.setAlignment(Pos.CENTER_RIGHT);
        VBox clienteBox = new VBox(4, lCliente, lDocumento, lDireccion, lCorreo, lIdCliente);
        clienteBox.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().addAll(clienteBox, tv, lblSubtotal, totalesBox, new HBox(8, btnCrear, btnCancelar));
        // Asegurar que los labels de totales se inicializan ahora que están en la escena
        recalcTotals(productosData, root);
         Scene scene = new Scene(root, 700, 500);
         dialog.setScene(scene);
         dialog.showAndWait();
    }

    // helper to recalc totals (placed near top-level of file)
    private void recalcTotals(ObservableList<ProductoCantidad> productosData, VBox root) {
        double total = 0;
        for (ProductoCantidad pc : productosData) total += pc.getSubtotal();
        double iva = total * 0.19; // IVA 19%
        double totalPagar = total + iva;
        // find labels in root
        Label lblSubtotal = null; Label lblIva = null; Label lblTotalPagar = null;
        for (Node n : root.getChildren()) {
            if (n instanceof Label) {
                Label L = (Label) n;
                String t = L.getText();
                if (t != null && t.startsWith("Subtotal:")) lblSubtotal = L;
            }
        }
        // if not found, nothing to update; otherwise update labels by searching deeper
        if (lblSubtotal != null) lblSubtotal.setText("Subtotal: $" + String.format("%.2f", total));
        // attempt to find lblIva and lblTotalPagar among children (we added totalesBox)
        for (Node n : root.getChildren()) {
            if (n instanceof HBox) {
                for (Node m : ((HBox) n).getChildren()) {
                    if (m instanceof Label) {
                        Label L = (Label) m;
                        String tt = L.getText();
                        if (tt != null && tt.startsWith("IVA:")) lblIva = L;
                        if (tt != null && tt.startsWith("Total a pagar:")) lblTotalPagar = L;
                    }
                }
            }
        }
        if (lblIva != null) lblIva.setText("IVA: $" + String.format("%.2f", iva));
        if (lblTotalPagar != null) lblTotalPagar.setText("Total a pagar: $" + String.format("%.2f", totalPagar));
    }

    private static class ProductoCantidad {
        private final Producto producto; private int cantidad;
        ProductoCantidad(Producto producto) { this.producto = producto; this.cantidad = 0; }
        int getCantidad() { return cantidad; }
        void setCantidad(int cantidad) { this.cantidad = cantidad; }
        double getSubtotal() { return producto.getPrecio() * cantidad; }
    }
}
