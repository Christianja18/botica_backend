# Importacion y Exportacion

Esta carpeta contiene archivos CSV de ejemplo para los endpoints de importacion y exportacion.

Los endpoints aceptan:

- `GET /api/{recurso}/exportar/csv`
- `GET /api/{recurso}/exportar/excel`
- `POST /api/{recurso}/importar/csv`
- `POST /api/{recurso}/importar/excel`

Recursos soportados:

- `clientes`
- `boletas`
- `productos`
- `inventario`
- `categorias`
- `proveedores`
- `detalles-pedido`
- `usuarios`

## Regla general

- Los archivos Excel usan las mismas columnas que los CSV de ejemplo.
- La primera fila siempre es la cabecera.
- Las plantillas exportadas priorizan columnas de negocio y descripciones en lugar de IDs tecnicos.
- Si una fila incluye opcionalmente el `id_*`, el sistema intenta actualizar ese registro.
- Si no trae `id_*`, el sistema intenta ubicar el registro por un valor unico del dominio o por la combinacion descriptiva correspondiente.
- Si no existe, inserta uno nuevo.

## Claves de upsert por recurso

- `clientes`: `id_cliente` o `dni`
- `categorias`: `id_categoria` o `nombre`
- `proveedores`: `id_proveedor` o `ruc`
- `productos`: `id_producto` o `codigo_barras`
- `inventario`: `id_inventario` o `id_producto` / `producto_codigo_barras`
- `boletas`: `id_boleta` o `numero_boleta`
- `detalles-pedido`: `id_detalle` o combinacion `pedido + producto`
- `usuarios`: `id_usuario` o `email`

## Resolucion de asociaciones

- `productos`:
  - categoria por `id_categoria` o `categoria_nombre`
  - proveedor por `id_proveedor`, `proveedor_ruc` o `proveedor_nombre`
- `inventario`:
  - producto por `id_producto`, `producto_codigo_barras` o `producto_nombre`
- `boletas`:
  - pedido por `id_pedido` o por `pedido_fecha` + `pedido_usuario_email`
  - `pedido_cliente_dni` ayuda a desambiguar cuando hay mas de un pedido posible
- `detalles-pedido`:
  - pedido por `id_pedido` o por `pedido_fecha` + `pedido_usuario_email`
  - `pedido_cliente_dni` ayuda a desambiguar cuando hay mas de un pedido posible
  - producto por `id_producto`, `producto_codigo_barras` o `producto_nombre`
- `usuarios`:
  - rol por `id_rol` o `rol_nombre`

## Orden recomendado de importacion

1. `categorias`
2. `proveedores`
3. `productos`
4. `clientes`
5. `usuarios`
6. `inventario`
7. `detalles-pedido`
8. `boletas`

## Notas de negocio

- `usuarios`: para actualizar, la columna `password` puede ir vacia. Para crear un usuario nuevo, `password` debe tener valor.
- `boletas`: el total real se recalcula desde el pedido. La importacion conserva `igv`, `datos_cliente`, `datos_empleado` e `impresa`.
- `detalles-pedido`: al importar, el backend recalcula el total del pedido afectado.
- `inventario`: para insertar un inventario nuevo, el producto ya debe existir.
- `boletas` y `detalles-pedido` dependen de que el `pedido` ya exista en base de datos.
