package com.licoreraFx.view;

import com.licoreraFx.model.Proveedor;
import com.licoreraFx.util.JsonManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class ComprasView {

    private TableView<Proveedor> table;
    private ObservableList<Proveedor> masterData;

    public Node createView() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Gestión de Compras a Proveedores");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label info = new Label("Selecciona un proveedor para realizar una nueva compra:");
        info.setWrapText(true);

        List<Proveedor> proveedores = JsonManager.listarProveedores();
        masterData = FXCollections.observableArrayList(proveedores);

        table = new TableView<>();
        table.getStyleClass().add("compras-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(320);
        table.setMinHeight(200);
        table.setMaxHeight(Region.USE_PREF_SIZE);

        TableColumn<Proveedor, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));

        TableColumn<Proveedor, String> colNombreEmpresa = new TableColumn<>("Nombre Empresa");
        colNombreEmpresa.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombreEmpresa()));

        TableColumn<Proveedor, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmail()));

        TableColumn<Proveedor, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTelefono()));

        TableColumn<Proveedor, String> colRut = new TableColumn<>("RUT");
        colRut.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRut()));

        //noinspection unchecked
        table.getColumns().addAll(colId, colNombreEmpresa, colEmail, colTelefono, colRut);
        table.setItems(masterData);

        Label lblInstruccion = new Label("Nota: La funcionalidad de registro de compras está disponible desde la gestión de Proveedores (botón 'Nueva Compra').");
        lblInstruccion.setWrapText(true);
        lblInstruccion.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");

        root.getChildren().addAll(titulo, info, table, lblInstruccion);
        VBox.setVgrow(table, Priority.ALWAYS);

        return root;
    }

    public void mostrar(VBox contentArea) {
        Node view = createView();
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }
}

