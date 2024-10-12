# Monitoreo de Estado de Servicios de Transporte Público y de Establecimientos

## Materia: Diseño de Sistemas

## Descripción

Este proyecto tiene como objetivo proporcionar una herramienta que permita monitorear el estado de los servicios de transporte público y otros establecimientos para personas con movilidad reducida. El sistema está diseñado para apoyar a las comunidades, permitiéndoles informar sobre incidentes en los servicios, acceder a reportes en tiempo real y colaborar con otros usuarios.

El sistema permitirá a los usuarios:
- Reportar incidentes en ascensores, escaleras mecánicas, baños y otros servicios de transporte.
- Consultar el estado de los servicios en estaciones de trenes y subtes.
- Recibir notificaciones sobre incidentes en servicios de interés a través de Email o WhatsApp.
- Visualizar informes y rankings sobre el estado de los servicios, ayudando a las entidades reguladoras y prestadoras de servicios a mejorar su calidad.

## Características
- Gestión de servicios públicos: El sistema permite la administración de servicios como ascensores, escaleras mecánicas y baños en estaciones de transporte.
- Gestión de incidentes: Los usuarios pueden reportar incidentes en los servicios y recibir notificaciones de incidentes abiertos y cerrados.
- Comunidad de usuarios: Los usuarios pueden formar parte de comunidades y compartir información sobre el estado de los servicios.
- Rankings de incidentes: Generación de rankings semanales sobre el desempeño de los servicios y el impacto en las comunidades.
- Integración con GeoRef API: Utiliza el servicio de normalización de datos geográficos del gobierno de Argentina para obtener la ubicación de las entidades y servicios.

## Tecnologías Utilizadas
- ### Backend:
  - Lenguaje: Java 17
  - Framework: Javalin
  - Testing: JUnit 5, Mockito
  - API REST: Integración de servicios externos usando Retrofit.

- ### Frontend:
  - Arquitectura Web: Cliente Liviano utilizando Javalin.
  - Motor de plantillas: Handlebars
  - Interfaces: HTML, CSS y JavaScript.

- ### Base de Datos:
  - Relacional: MySQL
  - ORM: Hibernate

- **Seguridad**: Implementación de políticas de contraseñas siguiendo las recomendaciones de OWASP.

- **Notificaciones**: A través de Email o WhatsApp, configurables por el usuario.