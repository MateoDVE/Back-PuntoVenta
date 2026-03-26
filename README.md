# Backend PuntoVenta (Spring Boot)

Backend del sistema PuntoVenta construido con Spring Boot, Maven y Supabase, siguiendo una arquitectura hexagonal (Ports and Adapters).

> ✨ **Estructura recientemente reorganizada** - El backend ahora mantiene una separación clara por capas y módulos de negocio, con DTOs organizados en `application/<modulo>/dto/` para máxima claridad y mantenibilidad.

## Contenido

- Descripción general
- Tecnologías
- Arquitectura del proyecto
- Requisitos
- Configuración de variables de entorno
- Ejecución local
- Endpoints de la API
- Manejo de errores
- CORS
- Estructura de carpetas
- Notas de despliegue

## Descripción general

Este backend expone APIs REST para:

- Autenticación de usuarios
- Gestión de vendedores
- Gestión de productos
- Carga y eliminación de imágenes de productos en Supabase Storage

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

Desde la carpeta app:

```powershell
.\mvnw.cmd clean compile
.\mvnw.cmd spring-boot:run
```

En macOS/Linux:

```bash
./mvnw clean compile
./mvnw spring-boot:run
```

La API levanta en:

- http://localhost:3000

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

## CORS

Configurado para permitir peticiones desde:

- http://localhost:4200

Archivo:

- src/main/java/com/back/puntoventa/app/config/CorsConfig.java

Métodos permitidos:

- GET, POST, PUT, PATCH, DELETE, OPTIONS

## Estructura de carpetas

Organizado por módulos de negocio (auth, productos, vendedores) siguiendo arquitectura hexagonal:

```
src/main/java/com/back/puntoventa/app/
├── application/
│   ├── auth/
│   │   ├── controller/
│   │   │   └── AuthRestAdapter.java
│   │   └── dto/
│   │       └── SignInDto.java
│   ├── productos/
│   │   ├── controller/
│   │   │   └── ProductosRestAdapter.java
│   │   └── dto/
│   │       ├── CreateProductoDto.java
│   │       ├── UpdateProductoDto.java
│   │       └── DeleteImageDto.java
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
│   ├── common/
│   │   └── exception/
│   ├── productos/
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
│           └── client/
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
- Adapters: implementan puertos para acceso a datos
- Client: cliente HTTP configurado para Supabase

**Common**: excepciones y utilidades transversales
**Config**: configuración y wiring de Spring

## Notas de despliegue

- Definir variables de entorno en el proveedor de hosting.
- No usar credenciales hardcodeadas en producción.
- Verificar que el bucket productos exista en Supabase Storage.
- Confirmar políticas/RLS y permisos para operaciones de Auth, tablas y Storage.

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
