package com.licoreraFx.controller;

import com.licoreraFx.controller.ComprasController;
import com.licoreraFx.controller.ProveedoresController;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

/**
 * Controlador del menú del administrador.
 * Permite mostrar diferentes secciones en el área de contenido.
 */
public class MenuAdministradorController {

    @FXML
    private VBox contentArea;

    /** Muestra la vista de clientes dentro del área de contenido. */
    public void mostrarClientes() {
        ClientesController controller = new ClientesController();
        controller.mostrar(contentArea);
    }

    /** Muestra la vista de proveedores dentro del área de contenido. */
    public void mostrarProveedores() {
        ProveedoresController controller = new ProveedoresController();
        controller.mostrar(contentArea);
    }

    /** Muestra la vista de compras dentro del área de contenido. */
    public void mostrarCompras() {
        ComprasController controller = new ComprasController();
        controller.mostrar(contentArea);
    }

    /** Muestra la vista de inventario dentro del área de contenido. */
    public void mostrarInventario() {
        ProductoController controller = new ProductoController();
        controller.mostrar(contentArea);
    }

    // Agrega aquí otras vistas cuando existan, por ejemplo Inventario
}
