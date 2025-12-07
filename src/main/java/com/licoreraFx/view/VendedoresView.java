package com.licoreraFx.view;

import com.licoreraFx.model.Usuario;
import com.licoreraFx.repository.UsuarioRepository;
import javafx.application.Platform;
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

public class VendedoresView {

    private TableView<Usuario> table;
    private ObservableList<Usuario> masterData;
    private FilteredList<Usuario> filtered;

    public Node createView() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Vendedores");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Campo de búsqueda
        TextField tfSearch = new TextField();
        tfSearch.getStyleClass().add("search-field");
        tfSearch.setPromptText("Buscar vendedor por usuario, nombre o email...");
        tfSearch.setMaxWidth(Double.MAX_VALUE);

        HBox searchBox = new HBox(8, tfSearch);
        HBox.setHgrow(tfSearch, Priority.ALWAYS);

        // Obtener datos desde el repositorio
        List<Usuario> vendedores = UsuarioRepository.listarVendedores();
        masterData = FXCollections.observableArrayList(vendedores);

        // TableView
        table = new TableView<>();
        table.getStyleClass().add("vendedores-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(320);
        table.setMinHeight(200);
        table.setMaxHeight(Region.USE_PREF_SIZE);

        TableColumn<Usuario, String> colId = new TableColumn<>();
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        Label lblId = new Label("ID"); lblId.setStyle("-fx-font-weight: bold;"); colId.setGraphic(lblId); colId.setStyle("-fx-alignment: CENTER;");

        TableColumn<Usuario, String> colUsuario = new TableColumn<>();
        colUsuario.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUsername()));
        Label lblUsuario = new Label("Usuario"); lblUsuario.setStyle("-fx-font-weight: bold;"); colUsuario.setGraphic(lblUsuario); colUsuario.setStyle("-fx-alignment: CENTER;");

        TableColumn<Usuario, String> colNombre = new TableColumn<>();
        colNombre.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombre()));
        Label lblNombreCol = new Label("Nombre"); lblNombreCol.setStyle("-fx-font-weight: bold;"); colNombre.setGraphic(lblNombreCol); colNombre.setStyle("-fx-alignment: CENTER;");

        TableColumn<Usuario, String> colEmail = new TableColumn<>();
        colEmail.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmail()));
        Label lblEmailCol = new Label("Email"); lblEmailCol.setStyle("-fx-font-weight: bold;"); colEmail.setGraphic(lblEmailCol); colEmail.setStyle("-fx-alignment: CENTER;");

        TableColumn<Usuario, String> colDocumento = new TableColumn<>();
        colDocumento.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDocumento()));
        Label lblDoc = new Label("Documento"); lblDoc.setStyle("-fx-font-weight: bold;"); colDocumento.setGraphic(lblDoc); colDocumento.setStyle("-fx-alignment: CENTER;");

        //noinspection unchecked
        table.getColumns().addAll(colId, colUsuario, colNombre, colEmail, colDocumento);
        // Quitar columnas vacías (filler) si aparecen
        Platform.runLater(() -> {
            try {
                var cols = table.getColumns();
                for (int i = cols.size() - 1; i >= 0; i--) {
                    TableColumn<?,?> col = cols.get(i);
                    String header = col.getText();
                    boolean hasGraphic = col.getGraphic() != null;
                    if ((header == null || header.trim().isEmpty()) && !hasGraphic) cols.remove(i);
                }
            } catch (Exception ignored) {}
        });

        // Filtrado y orden
        filtered = new FilteredList<>(masterData, p -> true);
        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal == null ? "" : newVal.trim().toLowerCase();
            filtered.setPredicate(u -> {
                if (q.isEmpty()) return true;
                return (u.getId() != null && u.getId().toLowerCase().contains(q)) ||
                        (u.getUsername() != null && u.getUsername().toLowerCase().contains(q)) ||
                        (u.getNombre() != null && u.getNombre().toLowerCase().contains(q)) ||
                        (u.getEmail() != null && u.getEmail().toLowerCase().contains(q));
            });
        });

        SortedList<Usuario> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        // Botones de acción: Añadir / Modificar / Eliminar
        Button btnAnadir = new Button("Añadir");
        Button btnModificar = new Button("Modificar");
        Button btnEliminar = new Button("Eliminar");

        btnAnadir.setOnAction(e -> showAgregarVendedorDialog());
        btnModificar.setOnAction(e -> showModificarVendedorDialog());
        btnEliminar.setOnAction(e -> {
            Usuario sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Selecciona un vendedor para eliminar.", ButtonType.OK);
                a.showAndWait();
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar el vendedor '" + sel.getUsername() + "'?", ButtonType.YES, ButtonType.NO);
            confirm.initModality(Modality.APPLICATION_MODAL);
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    boolean ok = UsuarioRepository.eliminarUsuario(sel.getUsername());
                    if (ok) {
                        cargarDatos();
                        Alert info = new Alert(Alert.AlertType.INFORMATION, "Vendedor eliminado.", ButtonType.OK);
                        info.showAndWait();
                    } else {
                        Alert err = new Alert(Alert.AlertType.ERROR, "No se pudo eliminar el vendedor.", ButtonType.OK);
                        err.showAndWait();
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
        List<Usuario> vendedores = UsuarioRepository.listarVendedores();
        masterData.setAll(vendedores);
    }

    private void showAgregarVendedorDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Agregar vendedor");

        javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
        root.setPadding(new Insets(12));

        TextField tfUsuario = new TextField(); tfUsuario.setPromptText("Usuario");
        TextField tfPassword = new TextField(); tfPassword.setPromptText("Contraseña");
        TextField tfNombre = new TextField(); tfNombre.setPromptText("Nombre completo");
        TextField tfEmail = new TextField(); tfEmail.setPromptText("Email");
        TextField tfDocumento = new TextField(); tfDocumento.setPromptText("Documento de identidad");

        Button btnSave = new Button("Guardar");
        Button btnCancel = new Button("Cancelar");
        Label lblError = new Label(); lblError.getStyleClass().add("dialog-error");

        btnSave.setOnAction(e -> {
            String u = tfUsuario.getText() != null ? tfUsuario.getText().trim() : "";
            String p = tfPassword.getText() != null ? tfPassword.getText().trim() : "";
            String nombre = tfNombre.getText() != null ? tfNombre.getText().trim() : "";
            String email = tfEmail.getText() != null ? tfEmail.getText().trim() : "";
            String documento = tfDocumento.getText() != null ? tfDocumento.getText().trim() : "";

            if (u.isEmpty() || p.isEmpty() || nombre.isEmpty() || email.isEmpty() || documento.isEmpty()) {
                lblError.setText("Todos los campos son obligatorios.");
                return;
            }
            Usuario nuevo = new Usuario(u, p, "VENDEDOR", nombre, email);
            nuevo.setDocumento(documento);
            boolean ok = UsuarioRepository.agregarUsuario(nuevo);
            if (ok) {
                cargarDatos();
                dialog.close();
                new Alert(Alert.AlertType.INFORMATION, "Vendedor agregado.").showAndWait();
            } else {
                lblError.setText("No se pudo agregar el vendedor (username ya existe o error de escritura).");
            }
        });

        btnCancel.setOnAction(ev -> dialog.close());

        HBox btns = new HBox(8, btnSave, btnCancel); btns.setAlignment(Pos.CENTER_RIGHT);
        // Añadir padding al contenedor de botones para que no queden pegados al borde
        btns.setPadding(new Insets(8, 0, 8, 0));
        VBox form = new VBox(8,
                new Label("Usuario:"), tfUsuario,
                new Label("Contraseña:"), tfPassword,
                new Label("Nombre:"), tfNombre,
                new Label("Email:"), tfEmail,
                new Label("Documento:"), tfDocumento,
                lblError
        );

        root.setCenter(form);
        root.setBottom(btns);
        // Aumentar el tamaño del diálogo y permitir redimensionar
        Scene scene = new Scene(root, 540, 420);
        dialog.setScene(scene);
        dialog.setResizable(true);
        dialog.setMinWidth(520);
        dialog.setMinHeight(380);
        dialog.showAndWait();
    }

    private void showModificarVendedorDialog() {
        Usuario sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { new Alert(Alert.AlertType.WARNING, "Selecciona un vendedor para modificar.").showAndWait(); return; }

        Stage dialog = new Stage(); dialog.initModality(Modality.APPLICATION_MODAL); dialog.setTitle("Modificar vendedor");
        javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane(); root.setPadding(new Insets(12));

        TextField tfUsuario = new TextField(sel.getUsername());
        TextField tfPassword = new TextField(sel.getPassword());
        TextField tfNombre = new TextField(sel.getNombre());
        TextField tfEmail = new TextField(sel.getEmail());
        TextField tfDocumento = new TextField(sel.getDocumento());

        Button btnSave = new Button("Guardar"); Button btnCancel = new Button("Cancelar");
        Label lblError = new Label(); lblError.getStyleClass().add("dialog-error");

        btnSave.setOnAction(e -> {
            String u = tfUsuario.getText() != null ? tfUsuario.getText().trim() : "";
            String p = tfPassword.getText() != null ? tfPassword.getText().trim() : "";
            String nombre = tfNombre.getText() != null ? tfNombre.getText().trim() : "";
            String email = tfEmail.getText() != null ? tfEmail.getText().trim() : "";
            String documento = tfDocumento.getText() != null ? tfDocumento.getText().trim() : "";
            if (u.isEmpty() || p.isEmpty() || nombre.isEmpty() || email.isEmpty() || documento.isEmpty()) { lblError.setText("Todos los campos son obligatorios."); return; }
            Usuario actualizado = new Usuario(u, p, "VENDEDOR", nombre, email); actualizado.setDocumento(documento);
            boolean ok = UsuarioRepository.actualizarUsuario(sel.getUsername(), actualizado);
            if (ok) { cargarDatos(); dialog.close(); new Alert(Alert.AlertType.INFORMATION, "Vendedor actualizado.").showAndWait(); }
            else { lblError.setText("No se pudo actualizar (username puede existir o error de escritura)."); }
        });
        btnCancel.setOnAction(ev -> dialog.close());

        HBox btns = new HBox(8, btnSave, btnCancel); btns.setAlignment(Pos.CENTER_RIGHT);
        // padding para separación
        btns.setPadding(new Insets(8, 0, 8, 0));
        VBox form = new VBox(8,
                new Label("Usuario:"), tfUsuario,
                new Label("Contraseña:"), tfPassword,
                new Label("Nombre:"), tfNombre,
                new Label("Email:"), tfEmail,
                new Label("Documento:"), tfDocumento,
                lblError
        );

        root.setCenter(form); root.setBottom(btns);
        Scene scene = new Scene(root, 560, 440);
        dialog.setScene(scene);
        dialog.setResizable(true);
        dialog.setMinWidth(540);
        dialog.setMinHeight(400);
        dialog.showAndWait();
    }

}
