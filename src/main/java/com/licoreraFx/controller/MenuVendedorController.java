package com.licoreraFx.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class MenuVendedorController {
    @FXML
    private VBox contentArea;

    public void mostrarClientes() {
        ClientesController controller = new ClientesController();
        controller.mostrar(contentArea);
    }

    public void mostrarVentas() {
        VentasController controller = new VentasController();
        controller.mostrar(contentArea);
    }
}
