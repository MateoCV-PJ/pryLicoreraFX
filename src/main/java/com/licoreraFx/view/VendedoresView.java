package com.licoreraFx.view;

import com.licoreraFx.model.Usuario;
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
import java.util.regex.Pattern;

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
        tfSearch.setPromptText("Buscar vendedor por usuario, nombre, email o teléfono...");
        tfSearch.setMaxWidth(Double.MAX_VALUE);

        HBox searchBox = new HBox(8, tfSearch);
        HBox.setHgrow(tfSearch, Priority.ALWAYS);

        // Obtener datos
        List<Usuario> vendedores = JsonManager.listarVendedores();
        masterData = FXCollections.observableArrayList(vendedores);

        // TableView
        table = new TableView<>();
        table.getStyleClass().add("vendedores-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        // Forzar altura preferida para que la tabla muestre barras de scroll cuando haya muchas filas
        table.setPrefHeight(320);
        table.setMinHeight(200);
        table.setMaxHeight(Region.USE_PREF_SIZE);

        TableColumn<Usuario, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));

        TableColumn<Usuario, String> colUsuario = new TableColumn<>("Usuario");
        colUsuario.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUsername()));

        TableColumn<Usuario, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombre()));

        TableColumn<Usuario, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmail()));

        TableColumn<Usuario, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTelefono()));

        TableColumn<Usuario, String> colDocumento = new TableColumn<>("Documento");
        colDocumento.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDocumento()));

        //noinspection unchecked
        table.getColumns().addAll(colId, colUsuario, colNombre, colEmail, colTelefono, colDocumento);

        // Filtrado y orden
        filtered = new FilteredList<>(masterData, p -> true);
        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal == null ? "" : newVal.trim().toLowerCase();
            filtered.setPredicate(u -> {
                if (q.isEmpty()) return true;
                if (u.getId() != null && u.getId().toLowerCase().contains(q)) return true;
                if (u.getUsername() != null && u.getUsername().toLowerCase().contains(q)) return true;
                if (u.getNombre() != null && u.getNombre().toLowerCase().contains(q)) return true;
                if (u.getEmail() != null && u.getEmail().toLowerCase().contains(q)) return true;
                if (u.getTelefono() != null && u.getTelefono().toLowerCase().contains(q)) return true;
                return false;
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
                    boolean ok = JsonManager.eliminarUsuario(sel.getUsername());
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

        // Layout
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
        List<Usuario> vendedores = JsonManager.listarVendedores();
        masterData.setAll(vendedores);
    }

    private void showAgregarVendedorDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Agregar vendedor");

        javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
        root.setPadding(new Insets(12));

        TextField tfUsuario = new TextField();
        tfUsuario.getStyleClass().add("dialog-field");
        tfUsuario.setPromptText("Usuario");
        TextField tfPassword = new TextField();
        tfPassword.getStyleClass().add("dialog-field");
        tfPassword.setPromptText("Contraseña");
        TextField tfNombre = new TextField();
        tfNombre.getStyleClass().add("dialog-field");
        tfNombre.setPromptText("Nombre completo");
        TextField tfEmail = new TextField();
        tfEmail.getStyleClass().add("dialog-field");
        tfEmail.setPromptText("Email");
        TextField tfTelefono = new TextField();
        tfTelefono.getStyleClass().add("dialog-field");
        tfTelefono.setPromptText("Teléfono");
        TextField tfDocumento = new TextField();
        tfDocumento.getStyleClass().add("dialog-field");
        tfDocumento.setPromptText("Documento de identidad");

        Button btnSave = new Button("Guardar");
        btnSave.getStyleClass().add("dialog-button");
        Button btnCancel = new Button("Cancelar");
        btnCancel.getStyleClass().addAll("dialog-button", "secondary");

        Label lblError = new Label();
        lblError.getStyleClass().add("dialog-error");

        btnSave.setOnAction(e -> {
            String u = tfUsuario.getText() != null ? tfUsuario.getText().trim() : "";
            String p = tfPassword.getText() != null ? tfPassword.getText().trim() : "";
            String nombre = tfNombre.getText() != null ? tfNombre.getText().trim() : "";
            String email = tfEmail.getText() != null ? tfEmail.getText().trim() : "";
            String telefono = tfTelefono.getText() != null ? tfTelefono.getText().trim() : "";
            String documento = tfDocumento.getText() != null ? tfDocumento.getText().trim() : "";
            // Validaciones básicas
            if (u.isEmpty() || p.isEmpty()) {
                lblError.setText("Usuario y contraseña son obligatorios.");
                return;
            }
            if (nombre.isEmpty()) {
                lblError.setText("Nombre es obligatorio.");
                return;
            }
            if (email.isEmpty()) {
                lblError.setText("Email es obligatorio.");
                return;
            }
            if (!isValidEmail(email)) {
                lblError.setText("Email inválido.");
                return;
            }
            if (telefono.isEmpty()) {
                lblError.setText("Teléfono es obligatorio.");
                return;
            }
            if (!isValidTelefono(telefono)) {
                lblError.setText("Teléfono inválido. Use dígitos, espacios, guiones o +.");
                return;
            }
            if (documento.isEmpty()) {
                lblError.setText("Documento es obligatorio.");
                return;
            }
            Usuario nuevo = new Usuario(u, p, "VENDEDOR", nombre, email, telefono);
            nuevo.setDocumento(documento);
            boolean ok = JsonManager.agregarUsuario(nuevo);
            if (ok) {
                cargarDatos();
                dialog.close();
                Alert info = new Alert(Alert.AlertType.INFORMATION, "Vendedor agregado.", ButtonType.OK);
                info.showAndWait();
            } else {
                lblError.setText("No se pudo agregar el vendedor (username ya existe o error de escritura).");
            }
        });

        btnCancel.setOnAction(e -> dialog.close());

        HBox btns = new HBox(8, btnSave, btnCancel);
        btns.setAlignment(Pos.CENTER_RIGHT);

        // Poner el formulario dentro de un ScrollPane para que los botones queden siempre visibles
        VBox formBox = new VBox(8);
        formBox.getChildren().addAll(
                new Label("Usuario:"), tfUsuario,
                new Label("Contraseña:"), tfPassword,
                new Label("Nombre:"), tfNombre,
                new Label("Email:"), tfEmail,
                new Label("Teléfono:"), tfTelefono,
                new Label("Documento:"), tfDocumento,
                lblError
        );
        ScrollPane sp = new ScrollPane(formBox);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setPrefViewportHeight(220);

        // center = formulario desplazable, bottom = botones (siempre visibles)
        root.setCenter(sp);
        HBox bottomBox = new HBox();
        bottomBox.setPadding(new Insets(8, 0, 0, 0));
        bottomBox.getChildren().add(btns);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 480, 360);
        dialog.setScene(scene);
        dialog.setResizable(true);
        dialog.setMinWidth(420);
        dialog.setMinHeight(300);
        dialog.showAndWait();
    }

    private void showModificarVendedorDialog() {
        Usuario sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Selecciona un vendedor para modificar.", ButtonType.OK);
            a.showAndWait();
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Modificar vendedor");

        javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
        root.setPadding(new Insets(12));

        TextField tfUsuario = new TextField(sel.getUsername());
        tfUsuario.getStyleClass().add("dialog-field");
        TextField tfPassword = new TextField(sel.getPassword());
        tfPassword.getStyleClass().add("dialog-field");
        TextField tfNombre = new TextField(sel.getNombre());
        tfNombre.getStyleClass().add("dialog-field");
        TextField tfEmail = new TextField(sel.getEmail());
        tfEmail.getStyleClass().add("dialog-field");
        TextField tfTelefono = new TextField(sel.getTelefono());
        tfTelefono.getStyleClass().add("dialog-field");
        TextField tfDocumento = new TextField(sel.getDocumento());
        tfDocumento.getStyleClass().add("dialog-field");

        Button btnSave = new Button("Guardar");
        btnSave.getStyleClass().add("dialog-button");
        Button btnCancel = new Button("Cancelar");
        btnCancel.getStyleClass().addAll("dialog-button", "secondary");

        Label lblError = new Label();
        lblError.getStyleClass().add("dialog-error");

        btnSave.setOnAction(e -> {
            String u = tfUsuario.getText() != null ? tfUsuario.getText().trim() : "";
            String p = tfPassword.getText() != null ? tfPassword.getText().trim() : "";
            String nombre = tfNombre.getText() != null ? tfNombre.getText().trim() : "";
            String email = tfEmail.getText() != null ? tfEmail.getText().trim() : "";
            String telefono = tfTelefono.getText() != null ? tfTelefono.getText().trim() : "";
            String documento = tfDocumento.getText() != null ? tfDocumento.getText().trim() : "";
            if (u.isEmpty() || p.isEmpty()) {
                lblError.setText("Usuario y contraseña son obligatorios.");
                return;
            }
            if (nombre.isEmpty()) {
                lblError.setText("Nombre es obligatorio.");
                return;
            }
            if (email.isEmpty()) {
                lblError.setText("Email es obligatorio.");
                return;
            }
            if (!isValidEmail(email)) {
                lblError.setText("Email inválido.");
                return;
            }
            if (telefono.isEmpty()) {
                lblError.setText("Teléfono es obligatorio.");
                return;
            }
            if (!isValidTelefono(telefono)) {
                lblError.setText("Teléfono inválido. Use dígitos, espacios, guiones o +.");
                return;
            }
            if (documento.isEmpty()) {
                lblError.setText("Documento es obligatorio.");
                return;
            }
            Usuario actualizado = new Usuario(u, p, "VENDEDOR", nombre, email, telefono);
            actualizado.setDocumento(documento);
            boolean ok = JsonManager.actualizarUsuario(sel.getUsername(), actualizado);
            if (ok) {
                cargarDatos();
                dialog.close();
                Alert info = new Alert(Alert.AlertType.INFORMATION, "Vendedor actualizado.", ButtonType.OK);
                info.showAndWait();
            } else {
                lblError.setText("No se pudo actualizar el vendedor (username puede existir o error de escritura).");
            }
        });

        btnCancel.setOnAction(e -> dialog.close());

        HBox btns = new HBox(8, btnSave, btnCancel);
        btns.setAlignment(Pos.CENTER_RIGHT);

        VBox formBox = new VBox(8);
        formBox.getChildren().addAll(
                new Label("Usuario:"), tfUsuario,
                new Label("Contraseña:"), tfPassword,
                new Label("Nombre:"), tfNombre,
                new Label("Email:"), tfEmail,
                new Label("Teléfono:"), tfTelefono,
                new Label("Documento:"), tfDocumento,
                lblError
        );

        ScrollPane sp = new ScrollPane(formBox);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setPrefViewportHeight(260);

        // center = formulario desplazable, bottom = botones (siempre visibles)
        root.setCenter(sp);
        HBox bottomBox2 = new HBox();
        bottomBox2.setPadding(new Insets(8, 0, 0, 0));
        bottomBox2.getChildren().add(btns);
        bottomBox2.setAlignment(Pos.CENTER_RIGHT);
        root.setBottom(bottomBox2);

        Scene scene = new Scene(root, 520, 420);
        dialog.setScene(scene);
        dialog.setResizable(true);
        dialog.setMinWidth(460);
        dialog.setMinHeight(360);
        dialog.showAndWait();
    }

    // Validación simple de email (ahora obligatorio)
    private boolean isValidEmail(String email) {
        if (email == null) return false; // obligatorio
        String v = email.trim();
        if (v.isEmpty()) return false;
        // Regex más permisiva para la parte local: letras, dígitos, _, ., %, +, -
        String pattern = "^[\\w.%+\\-]+@[A-ZaZ0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(v).matches();
    }

    // Validación simple de teléfono (ahora obligatorio)
    private boolean isValidTelefono(String t) {
        if (t == null) return false; // obligatorio
        String v = t.trim();
        if (v.isEmpty()) return false;
        String pattern = "^[0-9+\\-()\\s]{4,20}$";
        return Pattern.compile(pattern).matcher(v).matches();
    }

}
