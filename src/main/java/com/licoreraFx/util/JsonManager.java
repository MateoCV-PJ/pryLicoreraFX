package com.licoreraFx.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.licoreraFx.model.Usuario;
import com.licoreraFx.model.Cliente;
import com.licoreraFx.model.Proveedor;
import com.licoreraFx.model.Producto;
import com.licoreraFx.model.Venta;
import com.licoreraFx.model.Compra;

import java.io.FileReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utilitario para leer y escribir datos JSON en la carpeta `data`.
 * Provee métodos simples para entidades: usuarios, clientes, proveedores,
 * productos, ventas y compras.
 */
public class JsonManager {

    private static final String USUARIOS_PATH = "data/usuarios.json";
    private static final String CLIENTES_PATH = "data/clientes.json";
    private static final String PROVEEDORES_PATH = "data/proveedores.json";
    private static final String PRODUCTOS_PATH = "data/productos.json";
    private static final String VENTAS_PATH = "data/ventas.json";
    private static final String COMPRAS_PATH = "data/compras.json";

    // Crear Gson con formato pretty printing
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * Lista todos los usuarios desde el archivo JSON.
     *
     * @return Lista de usuarios.
     */
    public static List<Usuario> listarUsuarios() {
        try {
            Path path = Path.of(USUARIOS_PATH);
            if (!Files.exists(path)) {
                return new ArrayList<>();
            }
            try (Reader reader = new FileReader(path.toFile())) {
                Type listType = new TypeToken<List<Usuario>>() {}.getType();
                List<Usuario> usuarios = gson.fromJson(reader, listType);
                return usuarios != null ? usuarios : new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error leyendo usuarios JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Lista todos los usuarios con rol de vendedor.
     *
     * @return Lista de usuarios vendedores.
     */
    public static List<Usuario> listarVendedores() {
        try {
            List<Usuario> usuarios = listarUsuarios();
            return usuarios.stream()
                    .filter(u -> u != null && u.getRol() != null && "VENDEDOR".equalsIgnoreCase(u.getRol()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error filtrando vendedores: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Guarda la lista de usuarios en el archivo JSON.
     *
     * @param usuarios Lista de usuarios a guardar.
     * @return Verdadero si la operación fue exitosa, falso en caso contrario.
     */
    public static boolean guardarUsuarios(List<Usuario> usuarios) {
        try {
            Path path = Path.of(USUARIOS_PATH);
            // Asegurar carpeta
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(path)) {
                gson.toJson(usuarios, writer);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error guardando usuarios JSON: " + e.getMessage());
            return false;
        }
    }

    /**
     * Agrega un nuevo usuario.
     *
     * @param usuario Usuario a agregar.
     * @return Verdadero si el usuario fue agregado, falso si ya existe.
     */
    public static boolean agregarUsuario(Usuario usuario) {
        try {
            List<Usuario> usuarios = listarUsuarios();
            // generar id autoincremental
            if (usuario.getId() == null || usuario.getId().trim().isEmpty()) {
                long maxId = usuarios.stream()
                        .map(Usuario::getId)
                        .filter(id -> id != null && id.matches("\\d+"))
                        .mapToLong(Long::parseLong)
                        .max()
                        .orElse(0);
                usuario.setId(String.valueOf(maxId + 1));
            }
            // comprobar unicidad de username
            boolean exists = usuarios.stream().anyMatch(u -> u != null && u.getUsername() != null && u.getUsername().equals(usuario.getUsername()));
            if (exists) {
                System.err.println("El usuario ya existe: " + usuario.getUsername());
                return false;
            }
            usuarios.add(usuario);
            return guardarUsuarios(usuarios);
        } catch (Exception e) {
            System.err.println("Error agregando usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param usernameOriginal Nombre de usuario original.
     * @param usuarioActualizado Objeto Usuario con los datos actualizados.
     * @return Verdadero si la actualización fue exitosa, falso en caso contrario.
     */
    public static boolean actualizarUsuario(String usernameOriginal, Usuario usuarioActualizado) {
        try {
            List<Usuario> usuarios = listarUsuarios();
            boolean encontrado = false;
            // si cambia el username, comprobar que no choque con otro existente
            if (usuarioActualizado.getUsername() != null && !usuarioActualizado.getUsername().equals(usernameOriginal)) {
                boolean exists = usuarios.stream().anyMatch(u -> u != null && usuarioActualizado.getUsername().equals(u.getUsername()));
                if (exists) {
                    System.err.println("El username actualizado ya existe: " + usuarioActualizado.getUsername());
                    return false;
                }
            }
            for (int i = 0; i < usuarios.size(); i++) {
                Usuario u = usuarios.get(i);
                if (u != null && usernameOriginal.equals(u.getUsername())) {
                    usuarios.set(i, usuarioActualizado);
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) return false;
            return guardarUsuarios(usuarios);
        } catch (Exception e) {
            System.err.println("Error actualizando usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un usuario por su nombre de usuario.
     *
     * @param username Nombre de usuario a eliminar.
     * @return Verdadero si el usuario fue eliminado, falso en caso contrario.
     */
    public static boolean eliminarUsuario(String username) {
        try {
            List<Usuario> usuarios = listarUsuarios();
            boolean removed = usuarios.removeIf(u -> u != null && username.equals(u.getUsername()));
            if (!removed) return false;
            return guardarUsuarios(usuarios);
        } catch (Exception e) {
            System.err.println("Error eliminando usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param username Nombre de usuario a buscar.
     * @return Un objeto Optional con el usuario encontrado, o vacío si no se encontró.
     */
    public static Optional<Usuario> buscarUsuarioPorNombre(String username) {
        if (username == null) return Optional.empty();
        List<Usuario> usuarios = listarUsuarios();
        return usuarios.stream()
                .filter(u -> username.equals(u.getUsername()))
                .findFirst();
    }

    // ----------------- Métodos para clientes -----------------
    /**
     * Lista todos los clientes desde el archivo JSON.
     *
     * @return Lista de clientes.
     */
    public static List<Cliente> listarClientes() {
        try {
            Path path = Path.of(CLIENTES_PATH);
            if (!Files.exists(path)) {
                return new ArrayList<>();
            }
            try (Reader reader = new FileReader(path.toFile())) {
                Type listType = new TypeToken<List<Cliente>>() {}.getType();
                List<Cliente> clientes = gson.fromJson(reader, listType);
                return clientes != null ? clientes : new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error leyendo clientes JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Guarda la lista de clientes en el archivo JSON.
     *
     * @param clientes Lista de clientes a guardar.
     * @return Verdadero si la operación fue exitosa, falso en caso contrario.
     */
    public static boolean guardarClientes(List<Cliente> clientes) {
        try {
            Path path = Path.of(CLIENTES_PATH);
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(path)) {
                gson.toJson(clientes, writer);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error guardando clientes JSON: " + e.getMessage());
            return false;
        }
    }

    /**
     * Agrega un nuevo cliente.
     *
     * @param cliente Cliente a agregar.
     * @return Verdadero si el cliente fue agregado, falso si ya existe.
     */
    public static boolean agregarCliente(Cliente cliente) {
        try {
            List<Cliente> clientes = listarClientes();
            // generar id autoincremental
            if (cliente.getId() == null || cliente.getId().trim().isEmpty()) {
                long maxId = clientes.stream()
                        .map(Cliente::getId)
                        .filter(id -> id != null && id.matches("\\d+"))
                        .mapToLong(Long::parseLong)
                        .max()
                        .orElse(0);
                cliente.setId(String.valueOf(maxId + 1));
            }
            boolean exists = clientes.stream().anyMatch(c -> c != null && c.getId() != null && c.getId().equals(cliente.getId()));
            if (exists) {
                System.err.println("El cliente ya existe id: " + cliente.getId());
                return false;
            }
            clientes.add(cliente);
            return guardarClientes(clientes);
        } catch (Exception e) {
            System.err.println("Error agregando cliente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza los datos de un cliente existente.
     *
     * @param idOriginal ID original del cliente.
     * @param clienteActualizado Objeto Cliente con los datos actualizados.
     * @return Verdadero si la actualización fue exitosa, falso en caso contrario.
     */
    public static boolean actualizarCliente(String idOriginal, Cliente clienteActualizado) {
        try {
            List<Cliente> clientes = listarClientes();
            boolean encontrado = false;
            // si cambia el id, comprobar que no choque con otro existente
            if (clienteActualizado.getId() != null && !clienteActualizado.getId().equals(idOriginal)) {
                boolean exists = clientes.stream().anyMatch(c -> c != null && clienteActualizado.getId().equals(c.getId()));
                if (exists) {
                    System.err.println("El id actualizado ya existe: " + clienteActualizado.getId());
                    return false;
                }
            }
            for (int i = 0; i < clientes.size(); i++) {
                Cliente c = clientes.get(i);
                if (c != null && idOriginal.equals(c.getId())) {
                    clientes.set(i, clienteActualizado);
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) return false;
            return guardarClientes(clientes);
        } catch (Exception e) {
            System.err.println("Error actualizando cliente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un cliente por su ID.
     *
     * @param id ID del cliente a eliminar.
     * @return Verdadero si el cliente fue eliminado, falso en caso contrario.
     */
    public static boolean eliminarCliente(String id) {
        try {
            List<Cliente> clientes = listarClientes();
            boolean removed = clientes.removeIf(c -> c != null && id.equals(c.getId()));
            if (!removed) return false;
            return guardarClientes(clientes);
        } catch (Exception e) {
            System.err.println("Error eliminando cliente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca un cliente por su ID.
     *
     * @param id ID del cliente a buscar.
     * @return Un objeto Optional con el cliente encontrado, o vacío si no se encontró.
     */
    public static Optional<Cliente> buscarClientePorId(String id) {
        if (id == null) return Optional.empty();
        List<Cliente> clientes = listarClientes();
        return clientes.stream().filter(c -> id.equals(c.getId())).findFirst();
    }

    // ----------------- Métodos para proveedores -----------------
    /**
     * Lista todos los proveedores desde el archivo JSON.
     *
     * @return Lista de proveedores.
     */
    public static List<Proveedor> listarProveedores() {
        try {
            Path path = Path.of(PROVEEDORES_PATH);
            if (!Files.exists(path)) {
                return new ArrayList<>();
            }
            try (Reader reader = new FileReader(path.toFile())) {
                Type listType = new TypeToken<List<Proveedor>>() {}.getType();
                List<Proveedor> proveedores = gson.fromJson(reader, listType);
                return proveedores != null ? proveedores : new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error leyendo proveedores JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Guarda la lista de proveedores en el archivo JSON.
     *
     * @param proveedores Lista de proveedores a guardar.
     * @return Verdadero si la operación fue exitosa, falso en caso contrario.
     */
    public static boolean guardarProveedores(List<Proveedor> proveedores) {
        try {
            Path path = Path.of(PROVEEDORES_PATH);
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(path)) {
                gson.toJson(proveedores, writer);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error guardando proveedores JSON: " + e.getMessage());
            return false;
        }
    }

    /**
     * Agrega un nuevo proveedor.
     *
     * @param proveedor Proveedor a agregar.
     * @return Verdadero si el proveedor fue agregado, falso si ya existe.
     */
    public static boolean agregarProveedor(Proveedor proveedor) {
        try {
            List<Proveedor> proveedores = listarProveedores();
            if (proveedor.getId() == null || proveedor.getId().trim().isEmpty()) {
                long maxId = proveedores.stream()
                        .map(Proveedor::getId)
                        .filter(id -> id != null && id.matches("\\d+"))
                        .mapToLong(Long::parseLong)
                        .max()
                        .orElse(0);
                proveedor.setId(String.valueOf(maxId + 1));
            }
            boolean exists = proveedores.stream().anyMatch(p -> p != null && p.getId() != null && p.getId().equals(proveedor.getId()));
            if (exists) {
                System.err.println("El proveedor ya existe id: " + proveedor.getId());
                return false;
            }
            proveedores.add(proveedor);
            return guardarProveedores(proveedores);
        } catch (Exception e) {
            System.err.println("Error agregando proveedor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza los datos de un proveedor existente.
     *
     * @param idOriginal ID original del proveedor.
     * @param proveedorActualizado Objeto Proveedor con los datos actualizados.
     * @return Verdadero si la actualización fue exitosa, falso en caso contrario.
     */
    public static boolean actualizarProveedor(String idOriginal, Proveedor proveedorActualizado) {
        try {
            List<Proveedor> proveedores = listarProveedores();
            boolean encontrado = false;
            if (proveedorActualizado.getId() != null && !proveedorActualizado.getId().equals(idOriginal)) {
                boolean exists = proveedores.stream().anyMatch(p -> p != null && proveedorActualizado.getId().equals(p.getId()));
                if (exists) {
                    System.err.println("El id actualizado ya existe: " + proveedorActualizado.getId());
                    return false;
                }
            }
            for (int i = 0; i < proveedores.size(); i++) {
                Proveedor p = proveedores.get(i);
                if (p != null && idOriginal.equals(p.getId())) {
                    proveedores.set(i, proveedorActualizado);
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) return false;
            return guardarProveedores(proveedores);
        } catch (Exception e) {
            System.err.println("Error actualizando proveedor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un proveedor por su ID.
     *
     * @param id ID del proveedor a eliminar.
     * @return Verdadero si el proveedor fue eliminado, falso en caso contrario.
     */
    public static boolean eliminarProveedor(String id) {
        try {
            List<Proveedor> proveedores = listarProveedores();
            boolean removed = proveedores.removeIf(p -> p != null && id.equals(p.getId()));
            if (!removed) return false;
            return guardarProveedores(proveedores);
        } catch (Exception e) {
            System.err.println("Error eliminando proveedor: " + e.getMessage());
            return false;
        }
    }

    // ----------------- Métodos para productos -----------------
    /**
     * Lista todos los productos desde el archivo JSON.
     *
     * @return Lista de productos.
     */
    public static List<Producto> listarProductos() {
        try {
            Path path = Path.of(PRODUCTOS_PATH);
            if (!Files.exists(path)) {
                return new ArrayList<>();
            }
            try (Reader reader = new FileReader(path.toFile())) {
                Type listType = new TypeToken<List<Producto>>() {}.getType();
                List<Producto> productos = gson.fromJson(reader, listType);
                return productos != null ? productos : new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error leyendo productos JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Guarda la lista de productos en el archivo JSON.
     *
     * @param productos Lista de productos a guardar.
     * @return Verdadero si la operación fue exitosa, falso en caso contrario.
     */
    public static boolean guardarProductos(List<Producto> productos) {
        try {
            Path path = Path.of(PRODUCTOS_PATH);
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(path)) {
                gson.toJson(productos, writer);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error guardando productos JSON: " + e.getMessage());
            return false;
        }
    }

    /**
     * Agrega un nuevo producto.
     *
     * @param producto Producto a agregar.
     * @return Verdadero si el producto fue agregado, falso si ya existe.
     */
    public static boolean agregarProducto(Producto producto) {
        try {
            List<Producto> productos = listarProductos();
            if (producto.getId() == null || producto.getId().trim().isEmpty()) {
                long maxId = productos.stream()
                        .map(Producto::getId)
                        .filter(id -> id != null && id.matches("\\d+"))
                        .mapToLong(Long::parseLong)
                        .max()
                        .orElse(0);
                producto.setId(String.valueOf(maxId + 1));
            }
            boolean exists = productos.stream().anyMatch(p -> p != null && p.getId() != null && p.getId().equals(producto.getId()));
            if (exists) {
                System.err.println("El producto ya existe id: " + producto.getId());
                return false;
            }
            productos.add(producto);
            return guardarProductos(productos);
        } catch (Exception e) {
            System.err.println("Error agregando producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza los datos de un producto existente.
     *
     * @param idOriginal ID original del producto.
     * @param productoActualizado Objeto Producto con los datos actualizados.
     * @return Verdadero si la actualización fue exitosa, falso en caso contrario.
     */
    public static boolean actualizarProducto(String idOriginal, Producto productoActualizado) {
        try {
            List<Producto> productos = listarProductos();
            boolean encontrado = false;
            if (productoActualizado.getId() != null && !productoActualizado.getId().equals(idOriginal)) {
                boolean exists = productos.stream().anyMatch(p -> p != null && productoActualizado.getId().equals(p.getId()));
                if (exists) {
                    System.err.println("El id actualizado ya existe: " + productoActualizado.getId());
                    return false;
                }
            }
            for (int i = 0; i < productos.size(); i++) {
                Producto p = productos.get(i);
                if (p != null && idOriginal.equals(p.getId())) {
                    productos.set(i, productoActualizado);
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) return false;
            return guardarProductos(productos);
        } catch (Exception e) {
            System.err.println("Error actualizando producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un producto por su ID.
     *
     * @param id ID del producto a eliminar.
     * @return Verdadero si el producto fue eliminado, falso en caso contrario.
     */
    public static boolean eliminarProducto(String id) {
        try {
            List<Producto> productos = listarProductos();
            boolean removed = productos.removeIf(p -> p != null && id.equals(p.getId()));
            if (!removed) return false;
            return guardarProductos(productos);
        } catch (Exception e) {
            System.err.println("Error eliminando producto: " + e.getMessage());
            return false;
        }
    }

    // ----------------- Métodos para ventas -----------------
    /**
     * Lista todas las ventas desde el archivo JSON.
     *
     * @return Lista de ventas.
     */
    public static List<Venta> listarVentas() {
        try {
            Path path = Path.of(VENTAS_PATH);
            if (!Files.exists(path)) {
                return new ArrayList<>();
            }
            try (Reader reader = new FileReader(path.toFile())) {
                Type listType = new TypeToken<List<Venta>>() {}.getType();
                List<Venta> ventas = gson.fromJson(reader, listType);
                return ventas != null ? ventas : new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error leyendo ventas JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Guarda la lista de ventas en el archivo JSON.
     *
     * @param ventas Lista de ventas a guardar.
     * @return Verdadero si la operación fue exitosa, falso en caso contrario.
     */
    public static boolean guardarVentas(List<Venta> ventas) {
        try {
            Path path = Path.of(VENTAS_PATH);
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(path)) {
                gson.toJson(ventas, writer);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error guardando ventas JSON: " + e.getMessage());
            return false;
        }
    }

    /**
     * Agrega una nueva venta.
     *
     * @param venta Venta a agregar.
     * @return Verdadero si la venta fue agregada correctamente.
     */
    public static boolean agregarVenta(Venta venta) {
        try {
            List<Venta> ventas = listarVentas();
            if (venta.getId() == null || venta.getId().trim().isEmpty()) {
                long maxId = ventas.stream()
                        .map(Venta::getId)
                        .filter(id -> id != null && id.matches("\\d+"))
                        .mapToLong(Long::parseLong)
                        .max()
                        .orElse(0);
                venta.setId(String.valueOf(maxId + 1));
            }
            ventas.add(venta);

            // Nota: la actualización del stock se realiza en VentaService.crearVenta
            // para evitar decrementar el stock dos veces no actualizamos productos aquí.

            return guardarVentas(ventas);
        } catch (Exception e) {
            System.err.println("Error agregando venta: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina una venta por su ID.
     *
     * @param id ID de la venta a eliminar.
     * @return Verdadero si la venta fue eliminada, falso en caso contrario.
     */
    public static boolean eliminarVenta(String id) {
        try {
            List<Venta> ventas = listarVentas();
            boolean removed = ventas.removeIf(v -> v != null && id.equals(v.getId()));
            if (!removed) return false;
            return guardarVentas(ventas);
        } catch (Exception e) {
            System.err.println("Error eliminando venta: " + e.getMessage());
            return false;
        }
    }

    // ----------------- Métodos para compras -----------------
    /**
     * Lista todas las compras desde el archivo JSON.
     *
     * @return Lista de compras.
     */
    public static List<Compra> listarCompras() {
        try {
            Path path = Path.of(COMPRAS_PATH);
            if (!Files.exists(path)) {
                return new ArrayList<>();
            }
            try (Reader reader = new FileReader(path.toFile())) {
                Type listType = new TypeToken<List<Compra>>() {}.getType();
                List<Compra> compras = gson.fromJson(reader, listType);
                return compras != null ? compras : new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error leyendo compras JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Guarda la lista de compras en el archivo JSON.
     *
     * @param compras Lista de compras a guardar.
     * @return Verdadero si la operación fue exitosa, falso en caso contrario.
     */
    public static boolean guardarCompras(List<Compra> compras) {
        try {
            Path path = Path.of(COMPRAS_PATH);
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(path)) {
                gson.toJson(compras, writer);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error guardando compras JSON: " + e.getMessage());
            return false;
        }
    }

    /**
     * Genera un nuevo ID para una compra, basado en el máximo ID existente.
     *
     * @return Nuevo ID de compra.
     */
    public static String generarIdCompra() {
        try {
            List<Compra> compras = listarCompras();
            long maxId = compras.stream()
                    .map(Compra::getId)
                    .filter(id -> id != null && id.matches("\\d+"))
                    .mapToLong(Long::parseLong)
                    .max()
                    .orElse(0);
            return String.valueOf(maxId + 1);
        } catch (Exception e) {
            return "1";
        }
    }

    /**
     * Agrega una nueva compra.
     *
     * @param compra Compra a agregar.
     * @return Verdadero si la compra fue agregada correctamente.
     */
    public static boolean agregarCompra(Compra compra) {
        try {
            List<Compra> compras = listarCompras();
            if (compra.getId() == null || compra.getId().trim().isEmpty()) {
                compra.setId(generarIdCompra());
            }
            compras.add(compra);
            // Actualizar el stock de los productos involucrados en la compra (sumar cantidades)
            try {
                List<Producto> productos = listarProductos();
                for (Compra.Item item : compra.getItems()) {
                    for (Producto p : productos) {
                        if (p.getId().equals(item.getProductoId())) {
                            // sumar la cantidad comprada
                            int newStock = p.getStock() + item.getCantidad();
                            p.setStock(Math.max(0, newStock));
                            break;
                        }
                    }
                }
                guardarProductos(productos);
            } catch (Exception ex) {
                System.err.println("Error actualizando stock tras compra: " + ex.getMessage());
            }
            return guardarCompras(compras);
        } catch (Exception e) {
            System.err.println("Error agregando compra: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina una compra por su ID.
     *
     * @param id ID de la compra a eliminar.
     * @return Verdadero si la compra fue eliminada, falso en caso contrario.
     */
    public static boolean eliminarCompra(String id) {
        try {
            List<Compra> compras = listarCompras();
            boolean removed = compras.removeIf(c -> c != null && id.equals(c.getId()));
            if (!removed) return false;
            return guardarCompras(compras);
        } catch (Exception e) {
            System.err.println("Error eliminando compra: " + e.getMessage());
            return false;
        }
    }

    public static Optional<Producto> buscarProductoPorId(String id) {
        if (id == null) return Optional.empty();
        List<Producto> productos = listarProductos();
        return productos.stream().filter(p -> id.equals(p.getId())).findFirst();
    }

    public static String generarIdVenta() {
        try {
            List<Venta> ventas = listarVentas();
            long maxId = ventas.stream()
                    .map(Venta::getId)
                    .filter(id -> id != null && id.matches("\\d+"))
                    .mapToLong(Long::parseLong)
                    .max()
                    .orElse(0);
            return String.valueOf(maxId + 1);
        } catch (Exception e) {
            return "1";
        }
    }
}
