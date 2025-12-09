package com.licoreraFx.util;

import com.licoreraFx.model.Compra;
import com.licoreraFx.model.Producto;

import java.util.ArrayList;
import java.util.List;

/**
 * Pequeño runner para probar la creación de una compra y la actualización de stock.
 * Se ejecuta desde la línea de comandos para pruebas rápidas en local.
 */
public class TestCompraRunner {
    public static void main(String[] args) {
        try {
            System.out.println("--- TestCompraRunner START ---");
            List<Producto> prods = JsonManager.listarProductos();
            System.out.println("Productos antes:");
            for (Producto p : prods) System.out.println(p.getId() + " -> " + p.getNombre() + " stock=" + p.getStock());

            // Elegir un producto existente (id 2) o el primero
            String targetId = "2";
            Producto target = prods.stream().filter(p -> p.getId().equals(targetId)).findFirst().orElse(null);
            if (target == null && !prods.isEmpty()) target = prods.get(0);
            if (target == null) { System.err.println("No hay productos en data/productos.json"); return; }

            int qty = 2;
            System.out.println("Creando compra para producto id=" + target.getId() + " cantidad=" + qty);

            Compra.Item item = new Compra.Item(target.getId(), target.getNombre(), qty, target.getPrecio());
            List<Compra.Item> items = new ArrayList<>();
            items.add(item);

            Compra compra = new Compra();
            compra.setId(JsonManager.generarIdCompra());
            compra.setProveedorId("TEST_PROV");
            compra.setNumeroFactura("TEST-" + System.currentTimeMillis());
            compra.setMetodoPago("Efectivo");
            compra.setTotal(item.getCantidad() * item.getPrecio());
            compra.setItems(items);
            compra.setNotas("Compra de prueba desde TestCompraRunner");

            boolean ok = JsonManager.agregarCompra(compra);
            System.out.println("agregarCompra returned: " + ok);

            List<Producto> prodsAfter = JsonManager.listarProductos();
            System.out.println("Productos despues:");
            for (Producto p : prodsAfter) System.out.println(p.getId() + " -> " + p.getNombre() + " stock=" + p.getStock());
            System.out.println("--- TestCompraRunner END ---");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
