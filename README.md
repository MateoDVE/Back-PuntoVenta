# Sistema de Punto de Venta - Backend

## Descripcion

API para administrar autenticacion, vendedores y catalogo de productos del sistema de punto de venta. El backend expone endpoints REST con NestJS, integra JWT para seguridad y usa Supabase para base de datos y almacenamiento de imagenes de productos.

## Tecnologias

**Backend:**
- Node.js
- NestJS 11
- TypeScript

**Librerias principales:**
- @nestjs/common
- @nestjs/core
- @nestjs/platform-express
- @nestjs/config
- @nestjs/jwt
- @nestjs/passport
- passport
- passport-jwt
- @supabase/supabase-js
- dotenv
- rxjs

**Base de datos:**
- Supabase (PostgreSQL)
- Supabase Storage (bucket de imagenes de productos)

## Equipo

- Maria Alejandra Loayza Claure
- Jorge Alejandro Rosales Gutierrez
- Mateo Daniel Vargas Estrada
- Andrews Jimmy Zelada Cespedes
- David Hassan Lopez Olivares

## Configuraciones

1. Instalar dependencias:

```bash
npm install
```

2. Crear archivo `.env` en la raiz del backend con:

```env
SUPABASE_URL=tu_url_supabase
SUPABASE_SERVICE_KEY=tu_service_key
JWT_SECRET=tu_jwt_secret
JWT_EXPIRES_IN=1d
PORT=3000
```

3. Levantar en desarrollo:

```bash
npm run start:dev
```

4. Scripts utiles:

```bash
npm run build
npm run start
npm run start:prod
npm run test
npm run test:e2e
npm run lint
```
