# Proyecto Base Implementando Clean Architecture

## Antes de Iniciar

Empezaremos por explicar los diferentes componentes del proyectos y partiremos de los componentes externos, continuando con los componentes core de negocio (dominio) y por último el inicio y configuración de la aplicación.

Lee el artículo [Clean Architecture — Aislando los detalles](https://medium.com/bancolombia-tech/clean-architecture-aislando-los-detalles-4f9530f35d7a)

# Arquitectura

![Clean Architecture](https://miro.medium.com/max/1400/1*ZdlHz8B0-qu9Y-QO3AXR_w.png)

## Domain

Es el módulo más interno de la arquitectura, pertenece a la capa del dominio y encapsula la lógica y reglas del negocio mediante modelos y entidades del dominio.

## Usecases

Este módulo gradle perteneciente a la capa del dominio, implementa los casos de uso del sistema, define lógica de aplicación y reacciona a las invocaciones desde el módulo de entry points, orquestando los flujos hacia el módulo de entities.

## Infrastructure

### Helpers

En el apartado de helpers tendremos utilidades generales para los Driven Adapters y Entry Points.

Estas utilidades no están arraigadas a objetos concretos, se realiza el uso de generics para modelar comportamientos
genéricos de los diferentes objetos de persistencia que puedan existir, este tipo de implementaciones se realizan
basadas en el patrón de diseño [Unit of Work y Repository](https://medium.com/@krzychukosobudzki/repository-design-pattern-bc490b256006)

Estas clases no puede existir solas y debe heredarse su compartimiento en los **Driven Adapters**

### Driven Adapters

Los driven adapter representan implementaciones externas a nuestro sistema, como lo son conexiones a servicios rest,
soap, bases de datos, lectura de archivos planos, y en concreto cualquier origen y fuente de datos con la que debamos
interactuar.

### Entry Points

Los entry points representan los puntos de entrada de la aplicación o el inicio de los flujos de negocio.

## Application

Este módulo es el más externo de la arquitectura, es el encargado de ensamblar los distintos módulos, resolver las dependencias y crear los beans de los casos de use (UseCases) de forma automática, inyectando en éstos instancias concretas de las dependencias declaradas. Además inicia la aplicación (es el único módulo del proyecto donde encontraremos la función “public static void main(String[] args)”.

**Los beans de los casos de uso se disponibilizan automaticamente gracias a un '@ComponentScan' ubicado en esta capa.**



Paginação / Paginación de Solicitudes
- Endpoint: GET /api/v1/solicitud
- Parámetros de consulta soportados:
  - page: entero (0 por defecto). Página a obtener (base 0).
  - size: entero (50 por defecto). Tamaño de página.
  - sort: ASC o DESC (ASC por defecto). Dirección de ordenamiento.
  - columnSort: nombre de la columna por la cual ordenar (opcional; en la implementación actual el ordenamiento no se aplica en DB, se puede ignorar o extender más adelante).
  - query: texto para filtrar (opcional; la implementación actual no aplica este filtro a nivel de DB; utilícelo como placeholder si luego se extiende el repositorio).
  - status: puede repetirse (?status=A&status=B) para filtrar por estados (opcional; placeholder para futuras extensiones).
- Ejemplos:
  - GET /api/v1/solicitud?page=0&size=10
  - GET /api/v1/solicitud?page=2&size=20&sort=DESC&columnSort=id
- Respuesta: objeto SimplePage con las propiedades:
  - data: lista de Solicitud
  - totalRows: total de registros
  - pageSize: tamaño de página
  - pageNum: número de página devuelta

Notas
- Ahora la implementación incluye paginación por cursor (keyset) en base de datos para el endpoint de solicitudes.
  - Parámetros opcionales: lastId (Long) y direction (ASC|DESC, por defecto ASC).
  - Ejemplos:
    - Primera página (ASC): GET /api/v1/solicitud?size=10
    - Siguiente página: usar el nextCursor devuelto: GET /api/v1/solicitud?size=10&lastId={nextCursor}
    - Orden descendente (navegar hacia ids menores): GET /api/v1/solicitud?direction=DESC&size=10
      y luego continuar con lastId={nextCursor}.
  - La respuesta SimplePage incluye hasNext y nextCursor para continuar.
- Cuando lastId no se envía, el backend usa page/size y aplica ordenamiento por columnSort (whitelist: id_solicitud) y filtro básico por query (LIKE sobre email o documento_identidad) directamente en la base de datos.
- Cuando lastId se envía, se usa paginación por cursor (keyset) sobre id_solicitud.
- Funciona con motores relacionales en AWS (por ejemplo, Amazon RDS/Aurora para MySQL o PostgreSQL) siempre que exista un índice por la columna de orden (id_solicitud).

Dónde están las instrucciones
- Endpoint público: GET /api/v1/instrucciones devuelve un JSON con el resumen de uso.
- Colección Postman: docs/Postman_CrediYa_Solicitudes_Collection.json (incluye chaining automático de nextCursor).
