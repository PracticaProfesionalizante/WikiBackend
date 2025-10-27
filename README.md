# 💻 WikiBackend: Plataforma de Capacitación y Documentación

Este documento contiene la ficha técnica detallada y los procedimientos de instalación y ejecución para el subsistema Backend de la plataforma, desarrollado con **Spring Boot 3.x** y **Java 21**.

---

## 1. ⚙️ Stack Tecnológico Detallado

| Componente        | Tecnología/Framework        | Versión       | Propósito Arquitectónico                                              |
| ----------------- | --------------------------- |---------------| --------------------------------------------------------------------- |
| Lenguaje          | Java                        | 21 (LTS)      | Alto rendimiento, estabilidad y soporte para características modernas |
| Framework         | Spring Boot                 | 3.5.5         | Creación de aplicaciones robustas con configuración mínima            |
| Dependencias      | Maven                       | 3.x           | Gestión de dependencias y automatización de compilación               |
| Seguridad         | Spring Security             | 6.x           | Autenticación y autorización basada en roles (RBAC) y JWT             |
| Persistencia      | JPA / Hibernate             | 6.x           | ORM para interacción con la base de datos                             |
| API               | RESTful                     | -             | JSON para comunicación Cliente-Servidor                               |
| Manejo de Errores | ControllerAdvice            | -             | Formato estandarizado de errores (Problem Details RFC 7807)           |
| Documentación     | SpringDoc (Swagger/OpenAPI) | 2.x           | Documentación interactiva de la API REST                              |
| Testing           | JUnit                       | 5.x           | Unit e Integration Testing                                            |
| IDE               | IntelliJ IDEA               | (Recomendado) | Desarrollo optimizado para Java/Spring                                |
| Comunicaciones    | CORS                        | -             | Permitir comunicación entre Frontend (Vue) y Backend                  |

---

## 2. 🏗️ Arquitectura y Estructura del Proyecto

El proyecto sigue arquitectura **Layered (en Capas)** para garantizar:

* Separación de responsabilidades
* Mantenibilidad
* Escalabilidad

### 📁 Estructura de Carpetas Sugerida

```
src/main/java/com/wiki/teclab/plataforma
├── config/              # Configuración global: Security, CORS, JWT, Swagger
├── controller/          # Endpoints REST
├── service/             # Lógica de negocio
├── repository/          # Persistencia (JPA)
├── model/               # Entidades JPA (Dominio)
├── dto/                 # Data Transfer Objects
├── security/            # Seguridad: JWT, UserDetails
├── exception/           # Excepciones personalizadas
├── util/                # Utilidades (ej: JwtUtil)
└── PlataformaApplication.java
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

* Requerido: **3.6.x+**
* Verificación:

```bash
mvn -v
```

### ✅ Base de Datos

* **Producción:** PostgreSQL o MySQL
* **Desarrollo:** H2 en memoria incluida en configuración inicial

### ✅ IDE

* IntelliJ IDEA (Ultimate o Community)

---

## 4. 🚀 Configuración y Ejecución

### 1. Clonación del Repositorio

```bash
git clone git@github.com:TuOrganizacion/plataforma-teclab-backend.git
cd plataforma-teclab-backend
```

### 2. Configuración de Base de Datos

Archivo:

```
src/main/resources/application.properties
```

| Entorno    | Ejemplo                | Seguridad                            |
| ---------- | ---------------------- | ------------------------------------ |
| Desarrollo | H2 o DB local          | No usar credenciales reales          |
| Producción | PostgreSQL recomendado | Secrets externos (Vault/K8s Secrets) |

### 3. CORS

* Desarrollo: permitir `http://localhost:8080` o `http://localhost:5173`
* Producción: dominio oficial del frontend

### 4. Configuración JWT

```properties
# DEBE ser una cadena Larga y Compleja
jwt.secret-key=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
jwt.expiration-ms=3600000
jwt.refresh-expiration-ms=604800000
```

✔ En producción se debe usar una variable de entorno

### 5. Compilación

```bash
mvn clean install
```

### 6. Ejecución

**A. Maven CLI**

```bash
mvn spring-boot:run
```

**B. IntelliJ IDEA**
Ejecutar `PlataformaApplication.java`

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

```
http://localhost:8080/swagger-ui.html
```

Permite testear:

* Autenticación (Login)
* Acceso basado en Roles

---

## 7. 🛠️ Herramientas de Soporte (Workflow)

Estas herramientas facilitan un ciclo eficiente Desarrollo → Prueba → Depuración.

### ▪️ Sourcetree

| Propósito       | Cliente gráfico para Git                         |
| --------------- | ------------------------------------------------ |
| Características | Ramas, merges, rebases, resolución de conflictos |

Pasos:

1. Descargar desde Atlassian
2. Instalar y configurar con GitHub
3. Clonar el repositorio

---

### ▪️ Postman

| Propósito | Testing de APIs, gestión de JWT |
| --------- | ------------------------------- |

Flujo recomendado:

1. Crear request `POST /api/auth/login`
2. Guardar JWT como variable de entorno
3. Probar endpoints protegidos con Bearer Token

---

### ▪️ DBeaver

| Propósito | Gestión de Bases de Datos (PostgreSQL, MySQL, H2, etc.) |
| --------- | ------------------------------------------------------- |

Uso:

* Navegar tablas
* Probar queries
* Verificar esquema y auditoría

---

📌 Nota del Lead Developer
El uso disciplinado de estas herramientas agiliza el desarrollo y garantiza la calidad del backend.
**Requisito clave**: integrarlas al workflow diario.

---

¿Querés que también arme una **versión en inglés**, o un README del **frontend** a juego?
Puedo sumarte además:
✔ Badges
✔ Diagrama de arquitectura
✔ Flujos de autenticación y permisos
✔ Enlaces a issues, CI/CD, releases

Solo pedírmelo. Con gusto te ayudo a dejarlo a nivel de vitrina tecnológica. 🚀
