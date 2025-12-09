package com.licoreraFx.service;

import com.licoreraFx.model.DetalleVenta;
import com.licoreraFx.model.Producto;
import com.licoreraFx.model.Venta;
import com.licoreraFx.repository.ProductoRepository;
import com.licoreraFx.repository.VentaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Servicio que encapsula la lógica de negocio para crear ventas.
 * Valida stock, actualiza inventario y persiste la venta.
 */
public class VentaService {
    private static final Object LOCK = new Object();

    /**
     * Crea una venta: valida stock, decrementa stock y persiste la venta.
     * Retorna true si todo fue exitoso, false si hubo error.
     */
    public static boolean crearVenta(Venta venta) {
        synchronized (LOCK) {
            try {
                // obtener productos actuales
                List<Producto> productos = ProductoRepository.listarProductos();

                // validar stock
                for (DetalleVenta dv : venta.getDetalles()) {
                    Optional<Producto> opt = productos.stream().filter(p -> p.getId().equals(dv.getProductoId())).findFirst();
                    if (opt.isEmpty()) return false; // producto no encontrado
                    Producto p = opt.get();
                    if (p.getStock() < dv.getCantidad()) return false; // stock insuficiente
                }

                // decrementar stock
                for (DetalleVenta dv : venta.getDetalles()) {
                    for (Producto p : productos) {
                        if (p.getId().equals(dv.getProductoId())) {
                            p.setStock(p.getStock() - dv.getCantidad());
                            break;
                        }
                    }
                }

                // persistir productos actualizados
                boolean okProd = ProductoRepository.guardarProductos(productos);
                if (!okProd) return false;

                // persistir venta
                boolean okVenta = VentaRepository.agregarVenta(venta);
                return okVenta;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
