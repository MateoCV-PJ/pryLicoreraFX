package com.licoreraFx.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

/**
 * Controlador del menú del vendedor.
 * Permite mostrar secciones como clientes y ventas.
 */
public class MenuVendedorController {
    @FXML
    private VBox contentArea;

    /** Muestra la vista de clientes en el área de contenido. */
    public void mostrarClientes() {
        ClientesController controller = new ClientesController();
        controller.mostrar(contentArea);
    }

    /** Muestra la vista de ventas en el área de contenido. */
    public void mostrarVentas() {
        VentasController controller = new VentasController();
        controller.mostrar(contentArea);
    }
}
