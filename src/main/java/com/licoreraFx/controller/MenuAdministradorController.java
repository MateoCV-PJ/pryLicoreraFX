package com.licoreraFx.controller;

import com.licoreraFx.controller.ComprasController;
import com.licoreraFx.controller.ProveedoresController;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class MenuAdministradorController {

    @FXML
    private VBox contentArea;

    public void mostrarClientes() {
        ClientesController controller = new ClientesController();
        controller.mostrar(contentArea);
    }

    public void mostrarProveedores() {
        ProveedoresController controller = new ProveedoresController();
        controller.mostrar(contentArea);
    }

    public void mostrarCompras() {
        ComprasController controller = new ComprasController();
        controller.mostrar(contentArea);
    }

    public void mostrarInventario() {
        ProductoController controller = new ProductoController();
        controller.mostrar(contentArea);
    }

    // Agrega aquí otras vistas cuando existan, por ejemplo Inventario
}
