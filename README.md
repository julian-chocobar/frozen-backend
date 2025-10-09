# Frozen Backend

Aplicación Spring Boot con PostgreSQL para sistema de Gestión de Producción para una PyME Alimenticia “Frozen”.

## Requisitos Previos

- Java 17 o superior
- Maven 3.6.3 o superior
- PostgreSQL 13 o superior
- IDE de tu preferencia (VS Code, IntelliJ IDEA, Eclipse)

## Configuración del Entorno

### Variables de Entorno Requeridas

La aplicación necesita las siguientes variables de entorno configuradas:

| Variable     | Descripción                                    | Valor por defecto                     |
|--------------|------------------------------------------------|---------------------------------------|
| DB_URL      | URL de conexión a la base de datos PostgreSQL  | jdbc:postgresql://localhost:5432/frozen |
| DB_USERNAME | Usuario de la base de datos                    | postgres                              |
| DB_PASSWORD | Contraseña del usuario de la base de datos     | - (requerido)                         |
| SP_PASSWORD | Contraseña para el usuario de Spring Security  | - (requerido)                         |

### Configuración en Diferentes IDEs

#### 1. Visual Studio Code / Cursor / Windsurf

1. Crea o edita el archivo `.vscode/launch.json` en la raíz del proyecto:
   ```json
   {
       "version": "0.2.0",
       "configurations": [
           {
               "type": "java",
               "name": "Launch FrozenBackendApplication",
               "request": "launch",
               "mainClass": "com.enigcode.frozen_backend.FrozenBackendApplication",
               "projectName": "frozen-backend",
               "env": {
                   "DB_URL": "jdbc:postgresql://localhost:5432/frozen",
                   "DB_USERNAME": "postgres",
                   "DB_PASSWORD": "tu_contraseña",
                   "SP_PASSWORD": "tu_contraseña_segura"
               },
               "vmArgs": "-Dspring.profiles.active=dev"
           }
       ]
   }
   ```

#### 2. IntelliJ IDEA

1. Ve a `Run` > `Edit Configurations...`
2. Haz clic en `+` y selecciona `Application`
3. Configura los siguientes campos:
   - Name: `FrozenBackendApplication`
   - Main class: `com.enigcode.frozen_backend.FrozenBackendApplication`
   - Environment variables:
     ```
     DB_URL=jdbc:postgresql://localhost:5432/frozen
     DB_USERNAME=postgres
     DB_PASSWORD=tu_contraseña
     SP_PASSWORD=tu_contraseña_segura
     ```
4. En `VM options`: `-Dspring.profiles.active=dev`
5. Aplica y guarda la configuración

#### 3. Eclipse

1. Haz clic derecho en el proyecto > `Run As` > `Run Configurations...`
2. Haz doble clic en `Java Application` para crear una nueva configuración
3. En la pestaña `Main`:
   - Project: `frozen-backend`
   - Main class: `com.enigcode.frozen_backend.FrozenBackendApplication`
4. En la pestaña `Environment`:
   - Añade las variables de entorno con sus respectivos valores
5. En `VM arguments`: `-Dspring.profiles.active=dev`
6. Aplica y ejecuta

### Configuración con archivo .env (opcional)

1. Instala la extensión `DotENV` en tu IDE
2. Crea un archivo `.env` en la raíz del proyecto:
   ```env
   DB_URL=jdbc:postgresql://localhost:5432/frozen
   DB_USERNAME=postgres
   DB_PASSWORD=tu_contraseña
   SP_PASSWORD=tu_contraseña_segura
   ```
3. Asegúrate de que el archivo `.env` esté en `.gitignore`

## Ejecución


### Con Maven
```bash
mvn spring-boot:run
```


### Construir el proyecto
```bash
mvn clean package
```

### Con Maven Wrapper
```bash
./mvnw spring-boot:run
```

### Construir el proyecto
```bash
./mvnw clean package
```

## Perfiles de Ejecución

- **dev**: Perfil de desarrollo (activo por defecto)
- **prod**: Perfil de producción

Para especificar un perfil, usa el parámetro: `-Dspring.profiles.active=prod`

## Base de Datos

La aplicación está configurada para usar PostgreSQL. Asegúrate de tener:
- Una base de datos llamada `frozen` creada
- Un usuario con los permisos necesarios
- Las credenciales configuradas en las variables de entorno

## Solución de Problemas

Si encuentras problemas con las variables de entorno:
1. Verifica que los nombres de las variables coincidan exactamente
2. Asegúrate de que la base de datos esté en ejecución
3. Verifica los logs de la aplicación para mensajes de error
4. Confirma que el puerto 8080 esté disponible o configura `server.port` en `application.properties`
