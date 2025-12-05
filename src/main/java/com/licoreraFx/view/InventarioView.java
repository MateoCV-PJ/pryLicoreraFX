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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class InventarioView {

    private ListView<Producto> listView;
    private ObservableList<Producto> masterData;
    private FilteredList<Producto> filteredData;

    public Node createView() {
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

        listView = new ListView<>(filteredData);
        listView.setCellFactory(param -> new ListCell<>() {
            private ImageView imageView = new ImageView();
            private Label lblNombre = new Label();
            private Label lblDescripcion = new Label();
            private Label lblPrecio = new Label();
            private Label lblStock = new Label();
            private HBox hbox = new HBox(10);

            {
                imageView.setFitHeight(80);
                imageView.setFitWidth(80);
                VBox vbox = new VBox(5, lblNombre, lblDescripcion, lblPrecio, lblStock);
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
                    String imagePath = "/img/productos/" + item.getImagen();
                    try {
                        Image image = new Image(getClass().getResourceAsStream(imagePath));
                        imageView.setImage(image);
                    } catch (Exception e) {
                        // Placeholder image if not found
                        imageView.setImage(new Image(getClass().getResourceAsStream("/img/logo.png")));
                    }
                    setGraphic(hbox);
                }
            }
        });

        Button btnAnadir = new Button("Añadir");
        Button btnModificar = new Button("Modificar");
        Button btnEliminar = new Button("Eliminar");

        btnAnadir.setOnAction(e -> showProductoDialog(null));
        btnModificar.setOnAction(e -> {
            Producto selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showProductoDialog(selected);
            }
        });
        btnEliminar.setOnAction(e -> {
            Producto selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                JsonManager.eliminarProducto(selected.getId());
                cargarDatos();
            }
        });

        HBox actions = new HBox(8, btnAnadir, btnModificar, btnEliminar);
        actions.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(titulo, searchBox, listView, actions);
        return root;
    }

    public void mostrar(VBox contentArea) {
        Node view = createView();
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }

    private void cargarDatos() {
        List<Producto> productos = JsonManager.listarProductos();
        masterData.setAll(productos);
    }

    private void showProductoDialog(Producto producto) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(producto == null ? "Añadir Producto" : "Modificar Producto");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField tfNombre = new TextField();
        TextField tfDescripcion = new TextField();
        TextField tfPrecio = new TextField();
        TextField tfStock = new TextField();
        ComboBox<String> cbImagen = new ComboBox<>();

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
        btnSave.setOnAction(e -> {
            String nombre = tfNombre.getText();
            String descripcion = tfDescripcion.getText();
            double precio = Double.parseDouble(tfPrecio.getText());
            int stock = Integer.parseInt(tfStock.getText());
            String imagen = cbImagen.getValue();

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

        root.setCenter(grid);
        root.setBottom(btnSave);
        BorderPane.setAlignment(btnSave, Pos.CENTER_RIGHT);

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}

