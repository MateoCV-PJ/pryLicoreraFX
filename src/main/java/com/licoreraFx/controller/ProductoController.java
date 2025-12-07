package com.licoreraFx.controller;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class ProductoController {
    public void mostrar(VBox contentArea) {
        // Mostrar siempre la vista programática para evitar FXML
        Node view = createView();
        contentArea.getChildren().setAll(view);
    }

    private Node createView() {
        // Implementar la creación de la vista alternativa aquí
        return new VBox(); // Ejemplo: retornar un VBox vacío como vista por defecto
    }
}
