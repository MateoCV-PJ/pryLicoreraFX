package com.licoreraFx.view;

import com.licoreraFx.model.Producto;
import com.licoreraFx.util.JsonManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

/**
 * Vista de inventario que muestra productos y detalle.
 * Permite añadir/modificar/eliminar productos (según rol).
 */
public class InventarioView {

    private ListView<Producto> listView;
    private ObservableList<Producto> masterData;
    private FilteredList<Producto> filteredData;

    public Node createView() {
        return createView(false);
    }

    /**
     * Crea la vista de inventario.
     * @param isVendor Si true oculta acciones de gestión.
     */
    public Node createView(boolean isVendor) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Inventario de Productos");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Campo de búsqueda
        TextField tfSearch = new TextField();
        tfSearch.getStyleClass().add("search-field");
        tfSearch.setPromptText("Buscar por nombre, descripción o ID...");
        tfSearch.setMaxWidth(Double.MAX_VALUE);
        HBox searchBox = new HBox(8, tfSearch);
        HBox.setHgrow(tfSearch, Priority.ALWAYS);

        List<Producto> productos = JsonManager.listarProductos();
        masterData = FXCollections.observableArrayList(productos);

        // Configurar filtrado
        filteredData = new FilteredList<>(masterData, p -> true);
        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal == null ? "" : newVal.trim().toLowerCase();
            filteredData.setPredicate(producto -> {
                if (q.isEmpty()) return true;
                return (producto.getNombre() != null && producto.getNombre().toLowerCase().contains(q)) ||
                        (producto.getDescripcion() != null && producto.getDescripcion().toLowerCase().contains(q)) ||
                        (producto.getId() != null && producto.getId().toLowerCase().contains(q));
            });
        });

        // ListView con celdas compactas (izquierda)
        listView = new ListView<>(filteredData);
        listView.setCellFactory(param -> new ListCell<>() {
            private ImageView imageView = new ImageView();
            private Label lblNombre = new Label();
            private Label lblDescripcion = new Label();
            private Label lblPrecio = new Label();
            private Label lblStock = new Label();
            private Label lblAgotado = new Label("Producto Agotado");
            private HBox hbox = new HBox(10);

            {
                imageView.setFitHeight(80);
                imageView.setFitWidth(80);
                lblAgotado.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                lblAgotado.setVisible(false);
                HBox stockBox = new HBox(6, lblStock, lblAgotado);
                VBox vbox = new VBox(5, lblNombre, lblDescripcion, lblPrecio, stockBox);
                hbox.getChildren().addAll(imageView, vbox);
                hbox.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    lblNombre.setText(item.getNombre());
                    lblDescripcion.setText(item.getDescripcion());
                    lblPrecio.setText("Precio: $" + item.getPrecio());
                    lblStock.setText("Stock: " + item.getStock());
                    // Mostrar indicador cuando stock == 0
                    lblAgotado.setVisible(item.getStock() == 0);
                    String imagePath = "/img/productos/" + item.getImagen();
                    try {
                        Image image = new Image(getClass().getResourceAsStream(imagePath));
                        imageView.setImage(image);
                    } catch (Exception e) {
                        imageView.setImage(new Image(getClass().getResourceAsStream("/img/logo.png")));
                    }
                    setGraphic(hbox);
                }
            }
        });

        // Panel derecho: detalle del producto seleccionado
        VBox detailBox = new VBox(10);
        detailBox.setPadding(new Insets(8));
        detailBox.setAlignment(Pos.TOP_CENTER);
        Label detailTitle = new Label("Selecciona un producto");
        detailTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        ImageView imageLarge = new ImageView();
        // Imagen de detalle más grande (mejor visibilidad)
        imageLarge.setFitWidth(360);
        imageLarge.setFitHeight(270);
        imageLarge.setPreserveRatio(true);

        Label lblName = new Label();
        Label lblDesc = new Label(); lblDesc.setWrapText(true);
        Label lblPrice = new Label();
        Label lblStock = new Label();
        Label lblAgotadoDetail = new Label("Producto Agotado");
        lblAgotadoDetail.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        lblAgotadoDetail.setVisible(false);

        HBox stockDetailBox = new HBox(6, lblStock, lblAgotadoDetail);

        detailBox.getChildren().addAll(detailTitle, imageLarge, lblName, lblDesc, lblPrice, stockDetailBox);

        // Botones de acción (footer fijo en la columna derecha)
        Button btnAnadir = new Button("Añadir");
        Button btnModificar = new Button("Modificar");
        Button btnEliminar = new Button("Eliminar");

        // Establecer que los botones puedan crecer y compartan el mismo ancho
        btnAnadir.setMaxWidth(Double.MAX_VALUE); btnModificar.setMaxWidth(Double.MAX_VALUE); btnEliminar.setMaxWidth(Double.MAX_VALUE);

        HBox footer = new HBox(8, btnAnadir, btnModificar, btnEliminar);
        footer.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(btnAnadir, Priority.ALWAYS); HBox.setHgrow(btnModificar, Priority.ALWAYS); HBox.setHgrow(btnEliminar, Priority.ALWAYS);

        // Añadir un spacer para empujar el footer al fondo del panel
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        if (!isVendor) {
            detailBox.getChildren().addAll(spacer, footer);
        } else {
            detailBox.getChildren().add(spacer);
        }

        // Acciones
        btnAnadir.setOnAction(e -> showProductoDialog(null));
        btnModificar.setOnAction(e -> {
            Producto selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) showProductoDialog(selected);
        });
        btnEliminar.setOnAction(e -> {
            Producto selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) { JsonManager.eliminarProducto(selected.getId()); cargarDatos(); }
        });

        // Cuando cambia la selección, actualizar panel derecho
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel == null) {
                detailTitle.setText("Selecciona un producto");
                imageLarge.setImage(null);
                lblName.setText(""); lblDesc.setText(""); lblPrice.setText(""); lblStock.setText(""); lblAgotadoDetail.setVisible(false);
            } else {
                detailTitle.setText(newSel.getNombre());
                lblName.setText("Nombre: " + newSel.getNombre());
                lblDesc.setText(newSel.getDescripcion());
                lblPrice.setText("Precio: $" + newSel.getPrecio());
                lblStock.setText("Stock: " + newSel.getStock());
                lblAgotadoDetail.setVisible(newSel.getStock() == 0);
                String imagePath = "/img/productos/" + newSel.getImagen();
                try { imageLarge.setImage(new Image(getClass().getResourceAsStream(imagePath))); }
                catch (Exception ex) { imageLarge.setImage(new Image(getClass().getResourceAsStream("/img/logo.png"))); }
            }
        });

        // Layout: dos columnas
        VBox leftCol = new VBox(8, searchBox, listView);
        leftCol.setAlignment(Pos.TOP_CENTER);
        leftCol.setPrefWidth(520);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        // Ajustar ancho del panel derecho directamente
        detailBox.setPrefWidth(360);

        HBox twoCols = new HBox(12, leftCol, detailBox);
        twoCols.setAlignment(Pos.TOP_CENTER);

        // Auto-seleccionar el primer producto si existe
        if (!filteredData.isEmpty()) { listView.getSelectionModel().selectFirst(); listView.scrollTo(0); }

        root.getChildren().addAll(titulo, twoCols);
        return root;
    }

    public void mostrar(VBox contentArea) {
        mostrar(contentArea, false);
    }

    // Nueva API: mostrar con bandera isVendor
    public void mostrar(VBox contentArea, boolean isVendor) {
        Node view = createView(isVendor);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }

    private void cargarDatos() {
        // Intentar preservar la selección actual (por id) al recargar los productos
        String selectedId = null;
        if (listView != null && listView.getSelectionModel().getSelectedItem() != null) {
            selectedId = listView.getSelectionModel().getSelectedItem().getId();
        }
        List<Producto> productos = JsonManager.listarProductos();
        masterData.setAll(productos);
        // Restaurar selección por id si existe, o seleccionar el primero
        if (listView != null && !filteredData.isEmpty()) {
            if (selectedId != null) {
                for (int i = 0; i < masterData.size(); i++) {
                    if (selectedId.equals(masterData.get(i).getId())) {
                        listView.getSelectionModel().select(i);
                        listView.scrollTo(i);
                        return;
                    }
                }
            }
            listView.getSelectionModel().selectFirst();
            listView.scrollTo(0);
        }
    }

    private void showProductoDialog(Producto producto) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(producto == null ? "Añadir Producto" : "Modificar Producto");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(8));
        // Column constraints: primera columna fija (etiquetas), segunda columna expandible (inputs)
        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        col1.setPrefWidth(140);
        col1.setHgrow(Priority.NEVER);
        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);
        grid.setPrefWidth(660);

        TextField tfNombre = new TextField();
        TextField tfDescripcion = new TextField();
        TextField tfPrecio = new TextField();
        TextField tfStock = new TextField();
        ComboBox<String> cbImagen = new ComboBox<>();

        // Permitir que los campos crezcan para aprovechar el nuevo ancho
        GridPane.setHgrow(tfNombre, Priority.ALWAYS);
        GridPane.setHgrow(tfDescripcion, Priority.ALWAYS);
        GridPane.setHgrow(tfPrecio, Priority.ALWAYS);
        GridPane.setHgrow(tfStock, Priority.ALWAYS);
        GridPane.setHgrow(cbImagen, Priority.ALWAYS);

        // Llenar el ComboBox con los nombres de archivo de la carpeta de imágenes
        File folder = new File("src/main/resources/img/productos");
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    cbImagen.getItems().add(file.getName());
                }
            }
        }


        if (producto != null) {
            tfNombre.setText(producto.getNombre());
            tfDescripcion.setText(producto.getDescripcion());
            tfPrecio.setText(String.valueOf(producto.getPrecio()));
            tfStock.setText(String.valueOf(producto.getStock()));
            cbImagen.setValue(producto.getImagen());
        }

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(tfNombre, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(tfDescripcion, 1, 1);
        grid.add(new Label("Precio:"), 0, 2);
        grid.add(tfPrecio, 1, 2);
        grid.add(new Label("Stock:"), 0, 3);
        grid.add(tfStock, 1, 3);
        grid.add(new Label("Imagen:"), 0, 4);
        grid.add(cbImagen, 1, 4);

        Button btnSave = new Button("Guardar");
        btnSave.setDefaultButton(true);
        btnSave.setMinWidth(100);

        // Etiqueta para mostrar errores de validación dentro del diálogo
        Label lblError = new Label();
        lblError.getStyleClass().add("dialog-error");

        Button btnCancel = new Button("Cancelar");
        btnCancel.setMinWidth(100);

        btnSave.setOnAction(e -> {
            String nombre = tfNombre.getText().trim();
            String descripcion = tfDescripcion.getText().trim();
            String precioStr = tfPrecio.getText().trim();
            String stockStr = tfStock.getText().trim();
            String imagen = cbImagen.getValue();

            if (nombre.isEmpty() || descripcion.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty() || imagen == null || imagen.isEmpty()) {
                lblError.setText("Todos los campos son obligatorios.");
                return;
            }

            double precio;
            int stock;
            try {
                precio = Double.parseDouble(precioStr);
            } catch (NumberFormatException ex) {
                lblError.setText("Precio inválido. Usa un número válido (ej: 12.50).");
                return;
            }
            try {
                stock = Integer.parseInt(stockStr);
            } catch (NumberFormatException ex) {
                lblError.setText("Stock inválido. Usa un número entero.");
                return;
            }

            if (precio < 0) { lblError.setText("El precio debe ser mayor o igual a 0."); return; }
            if (stock < 0) { lblError.setText("El stock debe ser mayor o igual a 0."); return; }

            if (producto == null) {
                Producto nuevo = new Producto(null, nombre, descripcion, precio, stock, imagen);
                JsonManager.agregarProducto(nuevo);
            } else {
                Producto actualizado = new Producto(producto.getId(), nombre, descripcion, precio, stock, imagen);
                JsonManager.actualizarProducto(producto.getId(), actualizado);
            }
            cargarDatos();
            dialog.close();
        });

        btnCancel.setOnAction(ev -> dialog.close());

        root.setCenter(grid);
        HBox bottom = new HBox(8, lblError, btnCancel, btnSave);
        bottom.setPadding(new Insets(10));
        bottom.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(lblError, Priority.ALWAYS);
        root.setBottom(bottom);
        BorderPane.setAlignment(bottom, Pos.CENTER_RIGHT);

        // Aumentar tamaño del diálogo y permitir redimensionar
        Scene scene = new Scene(root, 700, 520);
        dialog.setScene(scene);
        dialog.setResizable(true);
        dialog.setMinWidth(660);
        dialog.setMinHeight(480);
        dialog.showAndWait();
    }
}
