# LicoreraFX

Aplicación de escritorio (JavaFX + Maven) para la gestión básica de una licorera: clientes, proveedores, inventario, compras y ventas.

## Resumen

- Lenguaje: Java
- UI: JavaFX
- Construcción: Maven
- Propósito: proyecto de práctica/educativo para gestionar operaciones típicas de una tienda/licorera.

## Características principales

- Gestión de clientes y proveedores
- Registro de compras y ventas
- Control de inventario y catálogo de productos
- Interfaz gráfica basada en FXML y controladores JavaFX
- Datos de ejemplo en formato JSON (carpeta `data/`)

## Requisitos

- JDK 11 o superior
- Maven 3.6+
- (Opcional) JavaFX SDK si su JDK no incluye JavaFX; para ejecutar desde IDE puede ser necesario configurar el SDK de JavaFX.

## Cómo compilar

Desde la raíz del proyecto:

```bash
mvn clean package
```

Esto genera el JAR en `target/`. El proyecto ya contiene un JAR empaquetado (`pryLicoreraFx-1.0.0-shaded.jar`) en la raíz del repositorio; puede eliminarse si prefiere reconstruir localmente.

## Cómo ejecutar

- Ejecutar desde el IDE (IntelliJ/NetBeans/Eclipse): abrir la clase `com.licoreraFx.MainApp` y ejecutar la configuración de aplicación JavaFX.

- Ejecutar desde JAR (si está empaquetado como ejecutable):

```bash
java -jar target/pryLicoreraFx-1.0.0-shaded.jar
# o si prefiere usar el JAR que está en la raíz
java -jar pryLicoreraFx-1.0.0-shaded.jar
```

Nota: si el JAR no contiene las librerías de JavaFX, necesitará añadir las dependencias de JavaFX al classpath o usar el plugin de JavaFX de Maven.

## Estructura del proyecto (resumen)

- `src/main/java` - código fuente Java (paquetes `com.licoreraFx`)
- `src/main/resources` - recursos (FXML, imágenes, CSS)
- `data/` - archivos JSON con datos de ejemplo (clientes, productos, ventas, etc.)
- `pom.xml` - configuración de Maven
- `target/` - artefactos de compilación (generado por Maven)

## Limpieza y mantenimiento

- Para limpiar artefactos de compilación:

```bash
mvn clean
# o eliminar manualmente la carpeta target/
```

- Archivos seguros para eliminar (si ya están en control de versiones y desea limpiarlos):
  - `target/` (carpeta de build)
  - `*.jar` generados en la raíz (ej. `pryLicoreraFx-1.0.0-shaded.jar`, `LicoreraFX-1.0.0.jar`)
  - `dependency-reduced-pom.xml` si fue creado por el plugin de empaquetado y no forma parte del control de fuente

## Datos y pruebas

Los datos de ejemplo están en la carpeta `data/` como archivos JSON. No se eliminan por defecto; reviselos antes de borrar.

## Contribuir

1. Hacer un fork/branch para sus cambios.
2. Crear commits claros y abrir un Pull Request describiendo los cambios.
3. Ejecutar `mvn package` y asegurarse de que la aplicación arranca localmente.

## Problemas comunes

- Errores al ejecutar por falta de JavaFX: instalar/configurar JavaFX en el IDE o usar un JDK que lo incluya.
- Recursos no encontrados en tiempo de ejecución: verificar rutas relativas en FXML y que los recursos estén en `src/main/resources`.

## Licencia

Sin licencia especificada. Añada un archivo `LICENSE` si desea aplicar una licencia concreta (por ejemplo MIT, Apache-2.0).

---

