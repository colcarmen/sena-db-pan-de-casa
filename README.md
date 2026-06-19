# 🍞 Sistema de Consulta y Gestión - PAN DE CASA

[![Java Version](https://img.shields.io/badge/Java-21%2B-orange.svg?style=flat-square&logo=openjdk)](https://adoptium.net/)
[![MySQL Version](https://img.shields.io/badge/MySQL-8.0%2B-blue.svg?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/Licencia-SENA-green.svg?style=flat-square)](https://www.sena.edu.co/)

Un sistema interactivo de gestión y consultas para el proyecto **PAN DE CASA** desarrollado en Java. El software incluye dos interfaces de usuario (gráfica mediante **Swing** y consola interactiva **CLI**) y conecta de forma robusta a un motor de base de datos **MySQL** aplicando las mejores prácticas de la industria con la API JDBC.

Este repositorio está estructurado para servir como un portafolio profesional o entregable académico de alta calidad (SENA).

---

## 🚀 Características Clave

* **Dual Interface**:
  * 🖥️ **Interfaz Gráfica Premium (GUI)**: Panel visual desarrollado en Swing con un diseño adaptativo, botones interactivos con estados hover/clic personalizados y renderizado de datos dinámico.
  * ⌨️ **Interfaz de Consola (CLI)**: Versión ligera e interactiva de comandos ideal para ejecución directa o administración básica.
* **Seguridad frente a Inyección SQL**: Implementación estricta de `PreparedStatement` en todos los flujos de lectura y escritura.
* **Carga Dinámica de Datos**: Uso avanzado de `ResultSetMetaData` para mapear de manera automática y genérica cualquier consulta SQL a filas y columnas en la tabla gráfica.
* **Desacoplamiento de Configuración**: Credenciales de la base de datos almacenadas en un archivo externo independiente (`db.properties`).
* **Gestión Segura de Recursos**: Mecanismo `try-with-resources` para evitar fugas de memoria y sockets abiertos en el servidor de base de datos.
* **Manejo de Errores e Integridad**: Captura especializada de excepciones SQL (ej. prevención de eliminación de registros con llaves foráneas activas - Error 1451).

---

## 🛠️ Arquitectura y Tecnologías

* **Lenguaje**: Java 21+ (Compatible desde Java 8).
* **Base de Datos**: MySQL 8.0 / 9.0.
* **Librerías**: Conector JDBC Oficial (`mysql-connector-j-9.0.0.jar`).
* **Modelado**: MySQL Workbench (`PAN_DE_CASA.mwb`).
* **Scripts de Carga**: Python para la generación de datos de prueba robustos (`generate_large_mock_sql.py`).

---

## 📋 Requisitos del Sistema

1. **Java JDK / JRE**: Versión 21 o superior (se requiere compatibilidad compilada con `--release 21`).
2. **Servidor MySQL**: Instancia activa local o remota en el puerto 3306.
3. **Controlador de Base de Datos**: Driver JDBC (incluido en el repositorio).

---

## ⚙️ Configuración e Instalación

### Paso 1: Configurar la Base de Datos en MySQL
1. Abre tu herramienta de administración de MySQL (MySQL Workbench, phpMyAdmin, DBeaver o consola).
2. Crea la base de datos ejecutando:
   ```sql
   CREATE DATABASE PAN_DE_CASA;
   ```
3. Importa el esquema y los datos iniciales cargando los scripts SQL incluidos en este repositorio:
   - [mock_data.sql](file:///c:/Users/Aria/OneDrive/Documentos/SENA/BD/mock_data.sql): Estructura inicial y datos básicos.
   - [mock_data_100.sql](file:///c:/Users/Aria/OneDrive/Documentos/SENA/BD/mock_data_100.sql): Datos de prueba ampliados (100 registros).
   - [pedidos_adicionales_20.sql](file:///c:/Users/Aria/OneDrive/Documentos/SENA/BD/pedidos_adicionales_20.sql): Datos transaccionales extra para reportes de ventas.

*(Opcional: Si deseas recrear o modificar el volumen de datos de prueba, puedes ejecutar el script de Python `generate_large_mock_sql.py`)*.

### Paso 2: Configurar las Credenciales de Conexión
Edita el archivo [db.properties](file:///c:/Users/Aria/OneDrive/Documentos/SENA/BD/db.properties) con las credenciales de tu servidor local:
```properties
db.url=jdbc:mysql://localhost:3306/PAN_DE_CASA?useSSL=false&allowPublicKeyRetrieval=true
db.user=tu_usuario_mysql
db.password=tu_contraseña_mysql
```

---

## 💻 Instrucciones de Uso

### A. Ejecución en Windows (Doble Clic)
El proyecto incluye scripts por lotes preconfigurados para ejecutarse de forma inmediata en Windows:
* **Modo Gráfico (Recomendado)**: Ejecuta [Ejecutar_Interfaz_Grafica.bat](file:///c:/Users/Aria/OneDrive/Documentos/SENA/BD/Ejecutar_Interfaz_Grafica.bat) para abrir la interfaz Swing silenciosamente en segundo plano.
* **Modo Consola**: Ejecuta [Ejecutar_Consola.bat](file:///c:/Users/Aria/OneDrive/Documentos/SENA/BD/Ejecutar_Consola.bat) para abrir el menú interactivo directamente en el terminal.

### B. Ejecución desde Terminal (PowerShell / CMD)
Si deseas ejecutar la aplicación de forma manual en consola, utiliza los siguientes comandos:

**Interfaz Gráfica:**
```powershell
java -cp ".;mysql-connector-j-9.0.0.jar" QueryGui
```

**Interfaz de Consola:**
```powershell
java -cp ".;mysql-connector-j-9.0.0.jar" QueryApp
```

---

## 🧠 Buenas Prácticas JDBC Implementadas

Para ver el análisis teórico y técnico detallado de cómo el código Java interactúa con MySQL y la explicación detallada del flujo algorítmico, consulta nuestra documentación de soporte:

👉 **[Explicación Detallada del Algoritmo JDBC](file:///c:/Users/Aria/OneDrive/Documentos/SENA/BD/EXPLICACION_JDBC.md)**

---

## 📂 Estructura del Repositorio

```bash
├── .gitignore                   # Archivos excluidos del control de versiones.
├── db.properties                # Archivo externo de configuración de credenciales.
├── mysql-connector-j-9.0.0.jar  # Driver/Conector JDBC oficial de MySQL.
├── Ejecutar_Interfaz_Grafica.bat# Lanzador por lotes para la interfaz gráfica.
├── Ejecutar_Consola.bat         # Lanzador por lotes para el menú de consola.
│
├── QueryGui.java                # Código fuente Java de la interfaz gráfica Swing.
├── QueryApp.java                # Código fuente Java del menú interactivo por consola.
│
├── PAN_DE_CASA.mwb              # Modelo relacional de base de datos (Workbench).
├── mock_data.sql                # SQL con el script de base de datos estructurado.
├── mock_data_100.sql            # Datos de prueba para simulación masiva.
├── pedidos_adicionales_20.sql   # Datos transaccionales transitorios.
│
├── generate_large_mock_sql.py   # Script de Python generador de datos mock SQL.
├── parse_mwb.py                 # Analizador auxiliar del archivo workbench (.mwb).
│
├── README.md                    # Manual general del proyecto (este archivo).
└── EXPLICACION_JDBC.md          # Documentación teórica y técnica del algoritmo de conexión.
```

---

## 👨‍🏫 Créditos y Autoría
Este proyecto ha sido desarrollado en el marco del programa de formación técnica en Bases de Datos y Programación de Software del **SENA (Servicio Nacional de Aprendizaje)**, enfocado en afianzar las competencias de persistencia relacional con el lenguaje Java.
