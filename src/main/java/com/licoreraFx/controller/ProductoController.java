package com.licoreraFx.controller;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * Controlador para la gestión de productos.
 * Actualmente muestra una vista programática simple como marcador.
 */
public class ProductoController {
    /**
     * Inserta la vista de productos en el área de contenido dada.
     * @param contentArea Contenedor donde se mostrará la vista.
     */
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
