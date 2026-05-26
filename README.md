# Backend PuntoVenta (Spring Boot)

Backend del sistema PuntoVenta construido con Spring Boot, Maven y Supabase, siguiendo una arquitectura hexagonal (Ports and Adapters).

## Contenido

- Descripción general
- Tecnologías
- Arquitectura del proyecto
- Requisitos
- Configuración de variables de entorno
- Ejecución local
- Endpoints de la API
- Manejo de errores
- Logging
- CORS
- Estructura de carpetas
- Notas de despliegue

## Descripción general

Este backend expone APIs REST para:

- Autenticación de usuarios
- Gestión de vendedores
- Gestión de productos con carga de imágenes en Supabase Storage
- Gestión de clientes registrados por cada vendedor con foto de fachada
- Control de inventario: asignación de stock del almacén central a transportes de vendedores
- Registro y sincronización de ventas (incluye modo offline con idempotencia por `id_transaccion_local`)
- Cierre de jornada: conciliación financiera e inventario por vendedor
- Reportes consolidados: ventas, ingresos, stock y discrepancias de inventario

La lógica de negocio vive en la capa de dominio y depende de interfaces (puertos), mientras que los adapters de infraestructura implementan esas interfaces usando Supabase.

## Tecnologías

- Java 25 (target de compilación)
- Spring Boot 4.0.4
- Spring Web MVC
- Spring RestClient
- Maven Wrapper
- Supabase (Auth, PostgREST, Storage)

Dependencias principales definidas en pom.xml:

- spring-boot-starter-webmvc
- spring-boot-starter-restclient
- spring-boot-starter-validation
- jackson-databind

## Arquitectura del proyecto

Arquitectura hexagonal:

- Dominio: entidades, puertos y casos de uso
- Application: controladores REST (adapters primarios)
- Infrastructure: adapters secundarios para Supabase
- Config: wiring de puertos y servicios

El wiring principal se realiza en:

- src/main/java/com/back/puntoventa/app/config/HexagonalArchitectureConfig.java

## Requisitos

- JDK 25 o superior instalado
- Maven Wrapper (incluido)
- Proyecto Supabase con tablas/bucket configurados

Importante sobre Java 26:

- Puedes ejecutar la app con JDK 26.
- El proyecto compila con release 25 para evitar incompatibilidades del ecosistema Java/Maven con release 26 en algunas herramientas.

## Configuración de variables de entorno

Archivo actual:

- src/main/resources/application.properties

Propiedades utilizadas:

- PORT (por defecto 3000)
- SUPABASE_URL
- SUPABASE_SERVICE_KEY
- SUPABASE_JWT_SECRET

Ejemplo en Windows PowerShell (sesión actual):

```powershell
$env:PORT="3000"
$env:SUPABASE_URL="https://TU-PROYECTO.supabase.co"
$env:SUPABASE_SERVICE_KEY="TU_SERVICE_ROLE_KEY"
$env:SUPABASE_JWT_SECRET="TU_JWT_SECRET"
```

Recomendación:

- No versionar secrets reales en repositorio.
- Usar variables de entorno por ambiente (local, staging, producción).

## Ejecución local

En Windows (PowerShell):

```powershell
cd Back-PuntoVenta
.\mvnw.cmd spring-boot:run
```

En macOS/Linux:

```bash
cd Back-PuntoVenta
./mvnw spring-boot:run
```

La API levanta en:

- http://localhost:3000 (puerto configurado en application.properties)

## Endpoints de la API

Base URL local:

- http://localhost:3000

### Auth

Base path:

- /auth

1) Iniciar sesión

- Método: POST
- Ruta: /auth/signin
- Body JSON:

```json
{
  "email": "admin@correo.com",
  "password": "123456"
}
```

- Respuesta: objeto con datos de sesión/tokens retornados por Supabase Auth.

2) Obtener usuario autenticado por token

- Método: GET
- Ruta: /auth/me
- Header requerido:
  - Authorization: Bearer TOKEN

- Respuesta JSON:

```json
{
  "id_usuario": "uuid",
  "nombre": "Nombre",
  "email": "correo@dominio.com",
  "rol": "ADMIN",
  "estado": "ACTIVO"
}
```

3) Obtener perfil por id

- Método: GET
- Ruta: /auth/profile?userId={id}

4) Cerrar sesión lógica

- Método: POST
- Ruta: /auth/signout?userId={id}
- Respuesta: 204 No Content

### Productos

Base path:

- /productos

1) Crear producto

- Método: POST
- Ruta: /productos
- Body JSON:

```json
{
  "sku": "SKU-001",
  "id_categoria": 1,
  "nombre": "Producto A",
  "descripcion": "Descripción",
  "url_imagen": "https://...",
  "precio_unidad": 10.5,
  "precio_caja": 100.0,
  "unidades_por_caja": 12,
  "stock_almacen_central": 30
}
```

También acepta alias camelCase para:

- idCategoria
- urlImagen
- precioUnidad
- precioCaja
- unidadesPorCaja
- stockAlmacenCentral

2) Listar productos

- Método: GET
- Ruta: /productos

3) Obtener producto por id

- Método: GET
- Ruta: /productos/{id}

4) Actualizar producto

- Método: PUT
- Ruta: /productos/{id}
- Body JSON: mismos campos que creación + estado

5) Eliminar producto

- Método: DELETE
- Ruta: /productos/{id}

6) Subir imagen temporal (sin id de producto)

- Método: POST
- Ruta: /productos/upload
- Content-Type: multipart/form-data
- Campo archivo: image
- Respuesta:

```json
{
  "imageUrl": "https://..."
}
```

7) Subir imagen para producto existente

- Método: POST
- Ruta: /productos/upload/{id}
- Content-Type: multipart/form-data
- Campo archivo: image

8) Eliminar imagen por URL

- Método: POST
- Ruta: /productos/delete-image
- Body JSON:

```json
{
  "imageUrl": "https://.../storage/v1/object/public/productos/{id}/{archivo}"
}
```

### Vendedores

Base path:

- /vendedores

1) Crear vendedor

- Método: POST
- Ruta: /vendedores
- Body JSON:

```json
{
  "nombre": "Vendedor",
  "email": "vendedor@correo.com",
  "password": "123456"
}
```

2) Listar vendedores

- Método: GET
- Ruta: /vendedores

3) Obtener vendedor por id

- Método: GET
- Ruta: /vendedores/{id}

4) Actualizar vendedor

- Método: PUT
- Ruta: /vendedores/{id}
- Body JSON:

```json
{
  "nombre": "Nuevo Nombre",
  "email": "nuevo@correo.com",
  "password": "nueva-clave",
  "estado": "ACTIVO"
}
```

5) Eliminar vendedor

- Método: DELETE
- Ruta: /vendedores/{id}

### Clientes

Base path:

- /clientes

1) Listar clientes

- Método: GET
- Ruta: /clientes
- Query param opcional: vendedorId={uuid} — filtra clientes por vendedor
- Respuesta: lista de clientes con ubicación GPS, foto de fachada y datos de contacto

2) Obtener cliente por id

- Método: GET
- Ruta: /clientes/{id}

3) Crear cliente

- Método: POST
- Ruta: /clientes
- Body JSON:

```json
{
  "nombre": "Cliente A",
  "telefono": "70000000",
  "direccion": "Calle 1 #100",
  "latitud": -17.783,
  "longitud": -63.182,
  "id_vendedor": "uuid",
  "url_foto": "https://..."
}
```

4) Subir foto de fachada

- Método: POST
- Ruta: /clientes/upload-photo
- Content-Type: multipart/form-data
- Campo archivo: photo
- Respuesta:

```json
{
  "photoUrl": "https://..."
}
```

5) Actualizar cliente

- Método: PUT
- Ruta: /clientes/{id}
- Body JSON: mismos campos que creación + estado (ACTIVO / INACTIVO)

6) Eliminar cliente

- Método: DELETE
- Ruta: /clientes/{id}

### Inventario

Base path:

- /inventario

1) Asignar stock a transporte de vendedor

- Método: POST
- Ruta: /inventario/asignar
- Body JSON:

```json
{
  "id_vendedor": "uuid",
  "productos": [
    { "id_producto": 1, "cantidad": 20 }
  ]
}
```

2) Registrar stock inicial (apertura de jornada)

- Método: POST
- Ruta: /inventario/stock-inicial
- Body JSON: misma estructura que asignar

3) Consultar inventario de vendedor

- Método: GET
- Ruta: /inventario/vendedor/{id}
- Respuesta: lista de ítems con producto, cantidad asignada, cantidad actual y diferencia

4) Validar inventario como admin

- Método: PATCH
- Ruta: /inventario/validar-admin/{id}
- Respuesta: 200 con el registro actualizado

5) Confirmar salida del vendedor

- Método: PATCH
- Ruta: /inventario/confirmar-salida/{id}
- Respuesta: 200 con el registro actualizado

### Ventas

Base path:

- /ventas

1) Registrar venta

- Método: POST
- Ruta: /ventas
- Body JSON:

```json
{
  "id_vendedor": "uuid",
  "id_cliente": "uuid",
  "id_transaccion_local": "uuid",
  "productos": [
    { "id_producto": 1, "cantidad": 2, "precio_unidad": 10.5, "descuento": 0 }
  ],
  "total_efectivo": 21.0,
  "fecha": "2026-05-26"
}
```

> `id_transaccion_local` es la clave de idempotencia generada en el cliente con `crypto.randomUUID()`. Si la misma venta se envía más de una vez, el backend ignora el duplicado.

2) Sincronizar ventas pendientes (modo offline)

- Método: POST
- Ruta: /ventas/sincronizar
- Body JSON: array de ventas con la misma estructura que la creación individual

3) Listar ventas

- Método: GET
- Ruta: /ventas

4) Obtener venta por id

- Método: GET
- Ruta: /ventas/{id}

5) Resumen diario de ventas

- Método: GET
- Ruta: /ventas/resumen-diario?fecha={yyyy-MM-dd}&vendedorId={uuid}
- Respuesta: lista de ventas del día con totales por vendedor

6) Datos de cierre de jornada por vendedor

- Método: GET
- Ruta: /ventas/cierre-jornada?vendedorId={uuid}&fecha={yyyy-MM-dd}
- Respuesta: resumen financiero y discrepancias de inventario para el cierre

7) Confirmar cierre de jornada

- Método: POST
- Ruta: /ventas/confirmar-cierre
- Body JSON:

```json
{
  "id_vendedor": "uuid",
  "fecha": "2026-05-26",
  "dinero_contado": 150.0
}
```

8) Reportes consolidados

- Método: GET
- Ruta: /ventas/reportes?fecha={yyyy-MM-dd}
- Respuesta:

```json
{
  "ventas": [...],
  "vendedores": [...],
  "productos": [...],
  "discrepancias": [
    {
      "nombre": "Vendedor A",
      "stockEsperado": 10,
      "stockActual": 8,
      "diferencia": -2,
      "correcto": false
    }
  ]
}
```

9) Devolver stock al almacén central

- Método: POST
- Ruta: /ventas/devolver-stock
- Body JSON:

```json
{
  "idVendedor": "uuid",
  "fecha": "2026-05-26"
}
```

### Cierres de jornada

Base path:

- /cierres

1) Registrar cierre

- Método: POST
- Ruta: /cierres
- Body JSON:

```json
{
  "id_vendedor": "uuid",
  "fecha": "2026-05-26",
  "dinero_esperado": 200.0,
  "dinero_contado": 198.0
}
```

2) Listar todos los cierres

- Método: GET
- Ruta: /cierres

3) Obtener cierre por id

- Método: GET
- Ruta: /cierres/{id}

4) Cierres por vendedor

- Método: GET
- Ruta: /cierres/vendedor/{id}

5) Cierre de vendedor por fecha

- Método: GET
- Ruta: /cierres/vendedor/{id}/fecha/{fecha}
- Parámetro fecha: yyyy-MM-dd

6) Actualizar cierre

- Método: PUT
- Ruta: /cierres/{id}
- Body JSON: mismos campos que la creación

7) Eliminar cierre

- Método: DELETE
- Ruta: /cierres/{id}

## Manejo de errores

El proyecto centraliza errores en:

- src/main/java/com/back/puntoventa/app/common/GlobalExceptionHandler.java

Formato estándar de error:

```json
{
  "status": 400,
  "message": "Detalle del error"
}
```

Se manejan:

- ApiException
- MethodArgumentNotValidException
- ConstraintViolationException
- Exception genérica

## Loggers

### Sistema de logging

El proyecto utiliza **SLF4J + Logback** (incluido por defecto en Spring Boot) para registrar eventos, errores y debugging.

**Archivo de configuración:**
- `src/main/resources/logback-spring.xml`

**Archivos de logs:**
- `logs/app.log` (rotación diaria, máximo 30 días)

### Niveles de logging

| Nivel | Uso | Ejemplo |
|-------|-----|---------|
| **DEBUG** | Información detallada para debugging | Entrada en métodos, valores intermedios, búsquedas |
| **INFO** | Eventos importantes de la aplicación | Creación exitosa, actualización, login |
| **WARN** | Situaciones inesperadas pero recuperables | Email duplicado, validación fallida, archivo vacío |
| **ERROR** | Errores que requieren atención | Excepciones inesperadas (incluir stack trace) |

### Guía: Agregar logging a un nuevo Controller

**Paso 1:** Importar las clases necesarias

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
```

**Paso 2:** Declarar el logger en la clase

```java
@RestController
@RequestMapping("/categorias")
public class CategoriasRestAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoriasRestAdapter.class);
    private final GestionCategoriasService gestionCategoriasService;
    
    // ... constructor y métodos
}
```

**Paso 3:** Agregar logs en cada endpoint

```java
@PostMapping
public ResponseEntity<Map<String, Object>> crear(@RequestBody CreateCategoriaDto request) {
    logger.info("POST /categorias - Crear nueva categoría: {}", request.getNombre());
    try {
        Categoria categoria = gestionCategoriasService.crearCategoria(request.getNombre());
        logger.info("Categoría creada exitosamente - ID: {}, Nombre: {}", 
            categoria.getId(), categoria.getNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapCategoriaToResponse(categoria));
    } catch (IllegalArgumentException ex) {
        logger.warn("Error al crear categoría: {}", ex.getMessage());
        throw new ApiException(HttpStatus.BAD_REQUEST, ex.getMessage());
    } catch (Exception ex) {
        logger.error("Error interno al crear categoría", ex);
        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}


```

### Guía: Agregar logging a un nuevo Service

**Paso 1:** Importar el logger

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
```

**Paso 2:** Declarar el logger

```java
public class GestionCategoriasService {
    
    private static final Logger logger = LoggerFactory.getLogger(GestionCategoriasService.class);
    private final CategoriaRepositoryPort CategoriaRepositoryPort;
    
    // ... constructor
}
```

**Paso 3:** Agregar logs en métodos clave

```java
public Categoria crearCategoria(String nombre) {
    logger.debug("Creando categoría - Nombre: {}", nombre);
    nombre = nombre.trim();
    
    if (nombre.isBlank()) {
        logger.warn("Intento de crear categoría con nombre vacío");
        throw new DomainException("Nombre no puede estar vacío");
    }
    
    if (categoriaRepositoryPort.existeNombre(nombre)) {
        logger.warn("Intento de crear categoría con nombre existente: {}", nombre);
        throw new DomainException("Ya existe una categoría con ese nombre");
    }
    
    Categoria categoria = new Categoria(null, nombre, LocalDateTime.now());
    categoria = categoriaRepositoryPort.crear(categoria);
    logger.info("Categoría creada exitosamente - ID: {}, Nombre: {}", 
        categoria.getId(), nombre);
    return categoria;
}

```

## CORS

Configurado para permitir peticiones desde:

- http://localhost:4200

Archivo:

- src/main/java/com/back/puntoventa/app/config/CorsConfig.java

Métodos permitidos:

- GET, POST, PUT, PATCH, DELETE, OPTIONS

## Estructura de carpetas

Organizado por módulos de negocio siguiendo arquitectura hexagonal:

```
src/main/java/com/back/puntoventa/app/
├── application/
│   ├── auth/
│   │   ├── controller/
│   │   │   └── AuthRestAdapter.java
│   │   └── dto/
│   │       └── SignInDto.java
│   ├── cierre/
│   │   ├── controller/
│   │   │   └── CierreJornadaRestAdapter.java
│   │   └── dto/
│   │       ├── CreateCierreDto.java
│   │       └── UpdateCierreDto.java
│   ├── clientes/
│   │   ├── controller/
│   │   │   └── ClientesRestAdapter.java
│   │   └── dto/
│   │       ├── CreateClienteDto.java
│   │       └── UpdateClienteDto.java
│   ├── inventario/
│   │   ├── controller/
│   │   │   └── InventarioRestAdapter.java
│   │   └── dto/
│   │       └── AsignarInventarioDto.java
│   ├── productos/
│   │   ├── controller/
│   │   │   └── ProductosRestAdapter.java
│   │   └── dto/
│   │       ├── CreateProductoDto.java
│   │       ├── UpdateProductoDto.java
│   │       └── DeleteImageDto.java
│   ├── ventas/
│   │   ├── controller/
│   │   │   └── VentaRestAdapter.java
│   │   └── dto/
│   │       ├── CreateVentaDto.java
│   │       └── ConfirmarCierreDto.java
│   └── vendedores/
│       ├── controller/
│       │   └── VendedoresRestAdapter.java
│       └── dto/
│           ├── CreateVendedorDto.java
│           └── UpdateVendedorDto.java
├── domain/
│   ├── auth/
│   │   ├── model/
│   │   ├── port/
│   │   └── service/
│   ├── cierre/
│   │   ├── model/
│   │   ├── port/
│   │   └── service/
│   ├── clientes/
│   │   ├── model/
│   │   ├── port/
│   │   └── service/
│   ├── common/
│   │   └── exception/
│   ├── inventario/
│   │   ├── model/
│   │   ├── port/
│   │   └── service/
│   ├── productos/
│   │   ├── model/
│   │   ├── port/
│   │   └── service/
│   ├── ventas/
│   │   ├── model/
│   │   ├── port/
│   │   └── service/
│   └── vendedores/
│       ├── model/
│       ├── port/
│       └── service/
├── infrastructure/
│   └── persistence/
│       └── supabase/
│           ├── adapter/
│           │   ├── SupabaseAuthAdapter.java
│           │   ├── SupabaseCierreJornadaAdapter.java
│           │   ├── SupabaseClientesAdapter.java
│           │   ├── SupabaseInventarioAdapter.java
│           │   ├── SupabaseProductosAdapter.java
│           │   ├── SupabaseVentasAdapter.java
│           │   └── SupabaseVendedoresAdapter.java
│           └── client/
│               └── SupabaseClient.java
├── common/
│   ├── ApiException.java
│   └── GlobalExceptionHandler.java
└── config/
    ├── CorsConfig.java
    ├── HexagonalArchitectureConfig.java
    ├── RestClientConfig.java
    └── SupabaseProperties.java
```

### Desglose por capa:

**Application** (adapters primarios - REST)
- Controllers: exponen APIs HTTP
- DTOs: objetos de transferencia de datos para solicitudes/respuestas

**Domain** (lógica de negocio)
- Models: entidades de dominio
- Services: casos de uso
- Ports: interfaces para contratos con infraestructura

**Infrastructure** (adapters secundarios - Supabase)
- Adapters: implementan puertos para acceso a datos (PostgREST + Storage)
- Client: cliente HTTP configurado para Supabase

**Common**: excepciones y utilidades transversales
**Config**: configuración y wiring de Spring

## Notas de despliegue

- Definir variables de entorno en el proveedor de hosting.
- No usar credenciales hardcodeadas en producción.
- Verificar que los buckets `productos` y `clientes` existan en Supabase Storage.
- Confirmar políticas/RLS y permisos para operaciones de Auth, tablas y Storage.
- El endpoint `/ventas/sincronizar` es idempotente: duplicados se identifican por `id_transaccion_local` y se ignoran silenciosamente.

## Comandos útiles

Compilar:

```powershell
.\mvnw.cmd clean compile
```

Ejecutar tests:

```powershell
.\mvnw.cmd test
```

Empaquetar:

```powershell
.\mvnw.cmd clean package
```
