# 🍷 Sistema Licorera FX

Sistema de punto de venta presencial para licorera desarrollado en JavaFX con arquitectura MVC y almacenamiento en JSON.

## 📋 Características

- **Arquitectura MVC** (Modelo-Vista-Controlador)
- **Base de datos JSON** para persistencia de datos
- **Sistema de roles**: Administrador y Vendedor
- **Gestión completa**: Clientes, Proveedores, Productos, Ventas y Compras
- **Facturación**: Registro de facturas de venta y compra

## 🛠️ Requisitos Previos

- **Java JDK 17** o superior
- **Apache Maven 3.6** o superior
- **JavaFX 21** (incluido en las dependencias)

## 📦 Instalación

### 1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd pryLicoreraFx
```

### 2. Compilar el proyecto

```bash
mvn clean install
```

### 3. Ejecutar la aplicación

```bash
mvn javafx:run
```

## 📁 Estructura del Proyecto

```
pryLicoreraFx/
├── src/main/java/com/licorerafx/
│   ├── model/          # Clases de datos
│   ├── view/           # Interfaces JavaFX
│   ├── controller/     # Lógica de negocio
│   └── util/           # Utilidades
├── src/main/resources/
│   ├── fxml/           # Archivos FXML
│   ├── css/            # Estilos
│   └── images/         # Imágenes
├── data/               # Base de datos JSON
└── pom.xml             # Configuración Maven
```

## 👥 Usuarios del Sistema

### Administrador
- **Usuario**: admin
- **Contraseña**: admin123
- **Permisos**: Acceso completo a todas las funcionalidades

### Vendedor
- **Usuario**: vendedor
- **Contraseña**: vend123
- **Permisos**: Ventas y consulta de clientes

## 🔧 Funcionalidades

### Administrador
- ✅ CRUD de Vendedores
- ✅ CRUD de Clientes
- ✅ CRUD de Proveedores
- ✅ CRUD de Productos
- ✅ Realizar Ventas
- ✅ Ingresar Compras de Proveedores
- ✅ Consultar Facturas de Venta
- ✅ Consultar Facturas de Compra

### Vendedor
- ✅ Consultar Clientes
- ✅ Registrar Nuevos Clientes
- ✅ Realizar Ventas
- ✅ Consultar Facturas de Venta

## 🗂️ Archivos JSON

Los datos se almacenan en el directorio `data/`:

- `usuarios.json` - Usuarios del sistema
- `vendedores.json` - Vendedores registrados
- `clientes.json` - Clientes
- `proveedores.json` - Proveedores
- `productos.json` - Inventario
- `ventas.json` - Historial de ventas
- `compras.json` - Historial de compras
- `facturasVenta.json` - Facturas de ventas
- `facturasCompra.json` - Facturas de compras

## 🚀 Comandos Maven Útiles

```bash
# Limpiar y compilar
mvn clean compile

# Ejecutar pruebas
mvn test

# Crear JAR ejecutable
mvn clean package

# Ejecutar la aplicación
mvn javafx:run

# Limpiar archivos compilados
mvn clean
```

## 📝 Desarrollo

### Agregar nuevas dependencias

Edita el archivo `pom.xml` y agrega la dependencia en la sección `<dependencies>`:

```xml
<dependency>
    <groupId>grupo</groupId>
    <artifactId>artefacto</artifactId>
    <version>version</version>
</dependency>
```

Luego ejecuta:
```bash
mvn clean install
```

## 🐛 Solución de Problemas

### Error: "JavaFX runtime components are missing"

Asegúrate de ejecutar con Maven:
```bash
mvn javafx:run
```

### Error: "No se encuentra el directorio data"

El directorio se crea automáticamente al iniciar. Si hay problemas, créalo manualmente:
```bash
mkdir data
```

### Error de compilación

Limpia y recompila:
```bash
mvn clean install
```

## 📄 Licencia

Este proyecto es de código abierto y está disponible bajo la Licencia MIT.

## 👨‍💻 Autor

Desarrollado como proyecto de Sistema de Punto de Venta para Licorera.

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/NuevaCaracteristica`)
3. Commit tus cambios (`git commit -m 'Agregar nueva característica'`)
4. Push a la rama (`git push origin feature/NuevaCaracteristica`)
5. Abre un Pull Request

---

**Versión**: 1.0.0  
**Última actualización**: Diciembre 2025