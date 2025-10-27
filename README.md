# 💻 WikiBackend: Plataforma de Capacitación y Documentación

Este documento contiene la ficha técnica detallada y los procedimientos de instalación y ejecución para el subsistema Backend de la plataforma, desarrollado con **Spring Boot 3.5.5** y **Java 21**.

---

## 1. ⚙️ Stack Tecnológico Detallado

| Componente        | Tecnología/Framework        | Versión              | Propósito Arquitectónico                                              |
| ----------------- | --------------------------- |----------------------| --------------------------------------------------------------------- |
| Lenguaje          | Java                        | 21 (LTS)             | Alto rendimiento, estabilidad y soporte para características modernas |
| Framework         | Spring Boot                 | 3.5.5                | Creación de aplicaciones robustas con configuración mínima            |
| Dependencias      | Maven                       | 3.14.0               | Gestión de dependencias y automatización de compilación               |
| Seguridad         | Spring Security             | 6.5.3                | Autenticación y autorización basada en roles (RBAC) y JWT             |
| Persistencia      | JPA / Hibernate             | 3.5.5                | ORM para interacción con la base de datos                             |
| API               | RESTful                     | -                    | JSON para comunicación Cliente-Servidor                               |
| Manejo de Errores | ControllerAdvice            | -                    | Formato estandarizado de errores (Problem Details RFC 7807)           |
| Documentación     | SpringDoc (Swagger/OpenAPI) | 2.8.13               | Documentación interactiva de la API REST                              |
| Testing           | JUnit                       | 3.5.5 (= Framework)  | Unit e Integration Testing                                            |
| IDE               | IntelliJ IDEA               | 21.0.7 (Recomendado) | Desarrollo optimizado para Java/Spring                                |
| Comunicaciones    | CORS                        | 6.2.10               | Permitir comunicación entre Frontend (Vue) y Backend                  |

---

## 2. 🏗️ Arquitectura y Estructura del Proyecto

El proyecto sigue arquitectura **Layered (en Capas)** para garantizar:

* Separación de responsabilidades
* Mantenibilidad
* Escalabilidad

### 📁 Estructura de Carpetas

```
src/main/java/com/teclab/practicas/WikiBackend
├── config/              # Configuración global: Security, CORS, JWT, Swagger
├── controller/          # Endpoints REST
├── converter/           # Convertidores DTOs <-> Entities
├── dto/                 # Data Transfer Objects
├── entity/              # Entidades JPA (Dominio)
├── exception/           # Excepciones personalizadas
├── repository/          # Persistencia (JPA)
├── service/             # Lógica de negocio
└── WikiBackendApplication.java
```

### ✅ Principios Clave

* **@Transactional** únicamente en capa `service`
* **DTOs para entrada/salida** (no exponer entidades JPA)
* Manejo **centralizado** de errores con `@RestControllerAdvice + ProblemDetail`

---

## 3. 🛠️ Requisitos de Instalación

### ✅ Java Development Kit (JDK)

* Requerido: **Java 21 (LTS)**
* Verificación:

```bash
java --version
```

### ✅ Maven

* Requerido: **3.14.0**
* Verificación:

```bash
mvn -v
```

### ✅ Base de Datos

* **Producción:** PostgreSQL
* **Desarrollo:** H2 en memoria incluida en configuración inicial

### ✅ IDE

* IntelliJ IDEA COMMUNITY (Gratuito)
* IntelliJ IDEA ULTIMATE (Pago)

---

## 4. 🚀 Configuración y Ejecución

### 1. Clonación del Repositorio (Sourcetree)

| Propósito       | Cliente gráfico para Git                         |
| --------------- | ------------------------------------------------ |
| Características | Ramas, merges, rebases, resolución de conflictos |

Pasos:

1. Descargar desde Atlassian
2. Instalar y configurar con GitHub
3. Clonar el repositorio

```bash
git clone https://github.com/PracticaProfesionalizante/WikiBackend.git
```

### 2. Configuración de Base de Datos

Archivo:

```
src/main/resources/application.properties
```

| Entorno    | Ejemplo       |
| ---------- |---------------|
| Desarrollo | H2 o DB local |
| Producción | PostgreSQL    |

### 3. CORS

* Desarrollo: permitir `http://localhost:5173`
* Producción: permitir `http://practicas.teclab.edu.ar`

### 4. Configuración JWT

```properties
# DEBE ser una cadena Larga y Compleja
jwt.secret-key=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
# 1000 ms * 60 segundos * 30 minutos = 1,800,000 ms
jwt.jwtExpirationAccessTokenMs=1800000
# 1000 ms * 60 segundos * 60 minutos * 24 horas * 7 dias = 604,800,000 ms
jwt.jwtExpirationRefreshTokenMs=604800000
```

✔ En producción se debe usar una variable de entorno


**B. IntelliJ IDEA**
Ejecutar `WikiBackendApplication.java`

---

## 5. 🔬 Testing y Calidad

Ejecutar pruebas (JUnit 5):

```bash
mvn test
```

🎯 Objetivo: cobertura alta en servicios y controladores

---

## 6. 📖 Documentación de la API (Swagger)

Disponible en:
### Local (Tener el proyecto ejecutandose y la VPN conectada)
```
http://localhost:8080/swagger-ui.html
```
### Online (Tener la VPN conectada)
```
http://practicas.teclab.edu.ar:8080/swagger-ui/index.html
```

Permite testear:

* Autenticación (Login)
* CRUD de Usuario
* CRUD de Menuitem
* CRUD de Documentos
* Acceso basado en Roles

---

## 7. 🛠️ Herramientas de Soporte (Workflow)

Estas herramientas facilitan un ciclo eficiente Desarrollo → Prueba → Depuración.


### ▪️ Postman

| Propósito | Testing de APIs, gestión de JWT |
| --------- | ------------------------------- |

Flujo de ejemplo:

1. Crear request `POST /api/auth/login`
2. Guardar JWT como variable de entorno
3. Probar endpoints protegidos con Bearer Token

---

### ▪️ DBeaver

| Propósito | Gestión de Bases de Datos (PostgreSQL y H2) |
| --------- |---------------------------------------------|

Uso:

* Navegar tablas
* Probar queries
* Verificar esquema y auditoría

---

📌 Nota del Lead Developer

El uso disciplinado de estas herramientas agiliza el desarrollo y garantiza la calidad del backend.

**Requisito clave**: integrarlas al workflow diario.
