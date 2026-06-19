import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class QueryApp {
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;

    public static void main(String[] args) {
        cargarConfiguracion();
        
        System.out.println("====================================================");
        System.out.println("          PAN DE CASA - SISTEMA DE CONSULTA         ");
        System.out.println("====================================================");

        try (Connection conn = conectar()) {
            if (conn == null) {
                System.out.println("No se pudo establecer la conexión a la base de datos.");
                return;
            }
            System.out.println("Conexión establecida con éxito.");
            
            Scanner scanner = new Scanner(System.in);
            boolean salir = false;

            while (!salir) {
                mostrarMenu();
                System.out.print("\nSeleccione una opción (1-11): ");
                String opcion = scanner.nextLine().trim();

                switch (opcion) {
                    case "1":
                        listarClientes(conn);
                        break;
                    case "2":
                        listarArticulos(conn);
                        break;
                    case "3":
                        listarPedidos(conn);
                        break;
                    case "4":
                        buscarPedidoDetalle(conn, scanner);
                        break;
                    case "5":
                        reporteVentasClientes(conn);
                        break;
                    case "6":
                        registrarCliente(conn, scanner);
                        break;
                    case "7":
                        eliminarCliente(conn, scanner);
                        break;
                    case "8":
                        registrarArticulo(conn, scanner);
                        break;
                    case "9":
                        eliminarArticulo(conn, scanner);
                        break;
                    case "10":
                        ejecutarConsultaPersonalizada(conn, scanner);
                        break;
                    case "11":
                        salir = true;
                        System.out.println("Saliendo del sistema...");
                        break;
                    default:
                        System.out.println("Opción no válida. Intente nuevamente.");
                }
                if (!salir) {
                    System.out.println("\nPresione ENTER para continuar...");
                    scanner.nextLine();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en la aplicación: " + e.getMessage());
        }
    }

    private static void cargarConfiguracion() {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream("db.properties")) {
            prop.load(fis);
            dbUrl = prop.getProperty("db.url");
            dbUser = prop.getProperty("db.user");
            dbPassword = prop.getProperty("db.password");
        } catch (IOException e) {
            System.out.println("[Advertencia] No se pudo leer db.properties, usando valores por defecto.");
            dbUrl = "jdbc:mysql://localhost:3306/PAN_DE_CASA?useSSL=false&allowPublicKeyRetrieval=true";
            dbUser = "root";
            dbPassword = "";
        }
    }

    private static Connection conectar() {
        try {
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            return null;
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n--- MENÚ DE CONSULTAS Y GESTIÓN ---");
        System.out.println("1. Listar todos los Clientes");
        System.out.println("2. Listar Catálogo de Artículos");
        System.out.println("3. Listar Pedidos");
        System.out.println("4. Ver Detalle de un Pedido (Factura/Resumen)");
        System.out.println("5. Reporte de Ventas por Cliente");
        System.out.println("6. Registrar nuevo Cliente");
        System.out.println("7. Eliminar Cliente");
        System.out.println("8. Registrar nuevo Artículo");
        System.out.println("9. Eliminar Artículo");
        System.out.println("10. Ejecutar Consulta SQL Personalizada");
        System.out.println("11. Salir");
    }

    private static void listarClientes(Connection conn) {
        String sql = "SELECT IdNit, Nombre, Apellido, Direccion, Correo, Celular FROM cliente";
        System.out.println("\n--- LISTADO DE CLIENTES ---");
        ejecutarYMostrar(conn, sql);
    }

    private static void listarArticulos(Connection conn) {
        String sql = "SELECT IdArticulo, Nombre, Unidad, PrecioUnitario, Descripcion FROM articulo";
        System.out.println("\n--- CATÁLOGO DE ARTÍCULOS ---");
        ejecutarYMostrar(conn, sql);
    }

    private static void listarPedidos(Connection conn) {
        String sql = "SELECT p.IdPedido, p.FechaPedido, p.HoraPedido, p.EstadoPedido, " +
                     "c.Nombre AS NombreCliente, c.Apellido AS ApellidoCliente " +
                     "FROM pedido p " +
                     "JOIN cliente c ON p.Cliente_IdNit = c.IdNit " +
                     "ORDER BY p.FechaPedido DESC, p.HoraPedido DESC";
        System.out.println("\n--- LISTADO DE PEDIDOS ---");
        ejecutarYMostrar(conn, sql);
    }

    private static void buscarPedidoDetalle(Connection conn, Scanner scanner) {
        System.out.print("Ingrese el ID del Pedido a buscar: ");
        String idPedidoStr = scanner.nextLine().trim();
        
        try {
            int idPedido = Integer.parseInt(idPedidoStr);
            
            // 1. Obtener datos del pedido y cliente
            String sqlPedido = "SELECT p.IdPedido, p.FechaPedido, p.HoraPedido, p.EstadoPedido, " +
                               "c.IdNit, c.Nombre, c.Apellido, c.Direccion, c.Correo, c.Celular " +
                               "FROM pedido p " +
                               "JOIN cliente c ON p.Cliente_IdNit = c.IdNit " +
                               "WHERE p.IdPedido = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sqlPedido)) {
                pstmt.setInt(1, idPedido);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("No se encontró ningún pedido con el ID: " + idPedido);
                        return;
                    }
                    
                    System.out.println("\n==================================================");
                    System.out.println("             DETALLE DEL PEDIDO #" + rs.getInt("IdPedido"));
                    System.out.println("==================================================");
                    System.out.printf("Fecha: %s | Hora: %s | Estado: %s\n", 
                                      rs.getString("FechaPedido"), rs.getString("HoraPedido"), rs.getString("EstadoPedido"));
                    System.out.println("--------------------------------------------------");
                    System.out.println("CLIENTE:");
                    System.out.printf("  NIT/ID: %d\n", rs.getLong("IdNit"));
                    System.out.printf("  Nombre: %s %s\n", rs.getString("Nombre"), rs.getString("Apellido"));
                    System.out.printf("  Dirección: %s\n", rs.getString("Direccion"));
                    System.out.printf("  Contacto: %s | %s\n", rs.getString("Celular"), rs.getString("Correo"));
                    System.out.println("--------------------------------------------------");
                }
            }

            // 2. Obtener artículos en el pedido
            String sqlArticulos = "SELECT a.IdArticulo, a.Nombre, ap.Cantidad, a.PrecioUnitario, " +
                                  "(ap.Cantidad * a.PrecioUnitario) AS Subtotal " +
                                  "FROM articulopedido ap " +
                                  "JOIN articulo a ON ap.Articulo_IdArticulo = a.IdArticulo " +
                                  "WHERE ap.Pedido_IdPedido = ?";
            
            double totalPedido = 0;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlArticulos)) {
                pstmt.setInt(1, idPedido);
                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.println("ARTÍCULOS:");
                    System.out.println("+------------+----------------------+----------+---------------+---------------+");
                    System.out.println("| ID Art.    | Descripción          | Cant.    | Precio Unit.  | Subtotal      |");
                    System.out.println("+------------+----------------------+----------+---------------+---------------+");
                    
                    int itemsCount = 0;
                    while (rs.next()) {
                        itemsCount++;
                        double subtotal = rs.getDouble("Subtotal");
                        totalPedido += subtotal;
                        System.out.printf("| %-10d | %-20s | %-8d | $%-12.2f | $%-12.2f |\n",
                                          rs.getInt("IdArticulo"),
                                          rs.getString("Nombre"),
                                          rs.getInt("Cantidad"),
                                          rs.getDouble("PrecioUnitario"),
                                          subtotal);
                    }
                    System.out.println("+------------+----------------------+----------+---------------+---------------+");
                    if (itemsCount == 0) {
                        System.out.println("|             No hay artículos registrados para este pedido.           |");
                        System.out.println("+------------+----------------------+----------+---------------+---------------+");
                    }
                }
            }

            // 3. Obtener pagos asociados
            String sqlPagos = "SELECT IdPago, FechaPago, HoraPago, MetodoPago FROM pago WHERE Pedido_IdPedido = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlPagos)) {
                pstmt.setInt(1, idPedido);
                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.println("\nINFORMACIÓN DE PAGO:");
                    if (rs.next()) {
                        System.out.printf("  ID Pago: %d\n", rs.getInt("IdPago"));
                        System.out.printf("  Método de Pago: %s\n", rs.getString("MetodoPago"));
                        System.out.printf("  Fecha/Hora de Pago: %s %s\n", rs.getString("FechaPago"), rs.getString("HoraPago"));
                    } else {
                        System.out.println("  Sin pagos registrados.");
                    }
                }
            }
            
            System.out.println("--------------------------------------------------");
            System.out.printf("TOTAL DEL PEDIDO: $%.2f\n", totalPedido);
            System.out.println("==================================================");

        } catch (NumberFormatException e) {
            System.out.println("ID de pedido inválido. Debe ser un número entero.");
        } catch (SQLException e) {
            System.err.println("Error al consultar detalles: " + e.getMessage());
        }
    }

    private static void reporteVentasClientes(Connection conn) {
        String sql = "SELECT c.IdNit, c.Nombre, c.Apellido, " +
                     "COUNT(distinct p.IdPedido) AS TotalPedidos, " +
                     "SUM(ap.Cantidad * a.PrecioUnitario) AS TotalComprado " +
                     "FROM cliente c " +
                     "LEFT JOIN pedido p ON c.IdNit = p.Cliente_IdNit " +
                     "LEFT JOIN articulopedido ap ON p.IdPedido = ap.Pedido_IdPedido " +
                     "LEFT JOIN articulo a ON ap.Articulo_IdArticulo = a.IdArticulo " +
                     "GROUP BY c.IdNit, c.Nombre, c.Apellido " +
                     "ORDER BY TotalComprado DESC";
        System.out.println("\n--- REPORTE DE VENTAS POR CLIENTE ---");
        ejecutarYMostrar(conn, sql);
    }

    private static void ejecutarConsultaPersonalizada(Connection conn, Scanner scanner) {
        System.out.println("\n--- EJECUTAR CONSULTA SQL PERSONALIZADA ---");
        System.out.println("Escriba su consulta SQL (ej. SELECT * FROM cliente WHERE Celular LIKE '300%'):");
        System.out.print("> ");
        String sql = scanner.nextLine().trim();

        if (sql.isEmpty()) {
            System.out.println("La consulta no puede estar vacía.");
            return;
        }

        // Restricción de seguridad: Solo consultas de selección (SELECT)
        if (!sql.toLowerCase().startsWith("select") && !sql.toLowerCase().startsWith("show") && !sql.toLowerCase().startsWith("describe")) {
            System.out.println("[Advertencia] Por seguridad, esta herramienta está diseñada principalmente para consultas de lectura (SELECT/SHOW/DESCRIBE).");
            System.out.print("¿Desea ejecutar esta consulta de todos modos? (s/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("s")) {
                System.out.println("Ejecución cancelada.");
                return;
            }
        }

        try (Statement stmt = conn.createStatement()) {
            boolean isResultSet = stmt.execute(sql);
            if (isResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    mostrarTablaResultSet(rs);
                }
            } else {
                int updateCount = stmt.getUpdateCount();
                System.out.println("Consulta ejecutada con éxito. Filas afectadas: " + updateCount);
            }
        } catch (SQLException e) {
            System.err.println("Error al ejecutar la consulta: " + e.getMessage());
        }
    }

    private static void ejecutarYMostrar(Connection conn, String sql) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            mostrarTablaResultSet(rs);
        } catch (SQLException e) {
            System.err.println("Error al ejecutar consulta: " + e.getMessage());
        }
    }

    private static void mostrarTablaResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        String[] headers = new String[columnCount];
        int[] colWidths = new int[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            headers[i - 1] = metaData.getColumnLabel(i);
            colWidths[i - 1] = Math.max(headers[i - 1].length(), 8);
        }
        
        List<String[]> rows = new ArrayList<>();
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                Object val = rs.getObject(i);
                row[i - 1] = val == null ? "NULL" : val.toString();
                colWidths[i - 1] = Math.max(colWidths[i - 1], row[i - 1].length());
            }
            rows.add(row);
        }
        
        // Dibujar borde superior
        StringBuilder separator = new StringBuilder("+");
        for (int width : colWidths) {
            separator.append("-".repeat(width + 2)).append("+");
        }
        
        System.out.println(separator);
        
        // Dibujar cabeceras
        System.out.print("|");
        for (int i = 0; i < columnCount; i++) {
            System.out.printf(" %-" + colWidths[i] + "s |", headers[i]);
        }
        System.out.println();
        System.out.println(separator);
        
        // Dibujar filas
        for (String[] row : rows) {
            System.out.print("|");
            for (int i = 0; i < columnCount; i++) {
                System.out.printf(" %-" + colWidths[i] + "s |", row[i]);
            }
            System.out.println();
        }
        System.out.println(separator);
        System.out.println("Total registros: " + rows.size());
    }

    private static void registrarCliente(Connection conn, Scanner scanner) {
        System.out.println("\n--- REGISTRAR NUEVO CLIENTE ---");
        try {
            System.out.print("Ingrese NIT / ID del Cliente (numérico): ");
            long idNit = Long.parseLong(scanner.nextLine().trim());

            // Verificar si ya existe
            String sqlCheck = "SELECT COUNT(*) FROM cliente WHERE IdNit = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
                pstmt.setLong(1, idNit);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Error: Ya existe un cliente con el NIT/ID " + idNit);
                        return;
                    }
                }
            }

            System.out.print("Ingrese Nombre: ");
            String nombre = scanner.nextLine().trim();
            System.out.print("Ingrese Apellido: ");
            String apellido = scanner.nextLine().trim();
            System.out.print("Ingrese Dirección: ");
            String direccion = scanner.nextLine().trim();
            System.out.print("Ingrese Correo electrónico: ");
            String correo = scanner.nextLine().trim();
            System.out.print("Ingrese Celular / Teléfono (numérico): ");
            long celular = Long.parseLong(scanner.nextLine().trim());

            String sqlInsert = "INSERT INTO cliente (IdNit, Nombre, Apellido, Direccion, Correo, Celular) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                pstmt.setLong(1, idNit);
                pstmt.setString(2, nombre);
                pstmt.setString(3, apellido);
                pstmt.setString(4, direccion);
                pstmt.setString(5, correo);
                pstmt.setLong(6, celular);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Cliente registrado con éxito!");
                } else {
                    System.out.println("No se pudo registrar el cliente.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: El NIT y el Celular deben ser valores numéricos enteros.");
        } catch (SQLException e) {
            System.out.println("Error de base de datos al registrar: " + e.getMessage());
        }
    }

    private static void eliminarCliente(Connection conn, Scanner scanner) {
        System.out.println("\n--- ELIMINAR CLIENTE ---");
        try {
            System.out.print("Ingrese el NIT / ID del Cliente a eliminar: ");
            long idNit = Long.parseLong(scanner.nextLine().trim());

            // Verificar si existe
            String sqlCheck = "SELECT Nombre, Apellido FROM cliente WHERE IdNit = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
                pstmt.setLong(1, idNit);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("No se encontró ningún cliente con el NIT/ID: " + idNit);
                        return;
                    }
                    System.out.println("¿Está seguro de que desea eliminar al cliente: " + rs.getString("Nombre") + " " + rs.getString("Apellido") + "? (s/n)");
                    String confirm = scanner.nextLine().trim().toLowerCase();
                    if (!confirm.equals("s")) {
                        System.out.println("Eliminación cancelada.");
                        return;
                    }
                }
            }

            String sqlDelete = "DELETE FROM cliente WHERE IdNit = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDelete)) {
                pstmt.setLong(1, idNit);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Cliente eliminado con éxito!");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: El NIT debe ser numérico.");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                System.out.println("Error: No se puede eliminar el cliente porque tiene pedidos asociados en el sistema.");
            } else {
                System.out.println("Error al eliminar el cliente: " + e.getMessage());
            }
        }
    }

    private static void registrarArticulo(Connection conn, Scanner scanner) {
        System.out.println("\n--- REGISTRAR NUEVO ARTÍCULO ---");
        try {
            System.out.print("Ingrese Código del Artículo (numérico): ");
            int idArticulo = Integer.parseInt(scanner.nextLine().trim());

            // Verificar si ya existe
            String sqlCheck = "SELECT COUNT(*) FROM articulo WHERE IdArticulo = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
                pstmt.setInt(1, idArticulo);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Error: Ya existe un artículo con el código " + idArticulo);
                        return;
                    }
                }
            }

            System.out.print("Ingrese Nombre del artículo: ");
            String nombre = scanner.nextLine().trim();
            System.out.print("Ingrese Unidad de medida (ej. Unidad, Docena, Porcion): ");
            String unidad = scanner.nextLine().trim();
            System.out.print("Ingrese Precio Unitario (numérico): ");
            double precio = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Ingrese Descripción del artículo: ");
            String descripcion = scanner.nextLine().trim();

            String sqlInsert = "INSERT INTO articulo (IdArticulo, Nombre, Unidad, PrecioUnitario, Descripcion) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                pstmt.setInt(1, idArticulo);
                pstmt.setString(2, nombre);
                pstmt.setString(3, unidad);
                pstmt.setDouble(4, precio);
                pstmt.setString(5, descripcion);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Artículo registrado con éxito!");
                } else {
                    System.out.println("No se pudo registrar el artículo.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: El Código debe ser entero y el Precio debe ser numérico.");
        } catch (SQLException e) {
            System.out.println("Error de base de datos al registrar artículo: " + e.getMessage());
        }
    }

    private static void eliminarArticulo(Connection conn, Scanner scanner) {
        System.out.println("\n--- ELIMINAR ARTÍCULO ---");
        try {
            System.out.print("Ingrese el Código del Artículo a eliminar: ");
            int idArticulo = Integer.parseInt(scanner.nextLine().trim());

            // Verificar si existe
            String sqlCheck = "SELECT Nombre FROM articulo WHERE IdArticulo = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
                pstmt.setInt(1, idArticulo);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("No se encontró ningún artículo con el código: " + idArticulo);
                        return;
                    }
                    System.out.println("¿Está seguro de que desea eliminar el artículo: " + rs.getString("Nombre") + "? (s/n)");
                    String confirm = scanner.nextLine().trim().toLowerCase();
                    if (!confirm.equals("s")) {
                        System.out.println("Eliminación cancelada.");
                        return;
                    }
                }
            }

            String sqlDelete = "DELETE FROM articulo WHERE IdArticulo = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDelete)) {
                pstmt.setInt(1, idArticulo);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Artículo eliminado con éxito!");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: El código debe ser numérico.");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                System.out.println("Error: No se puede eliminar el artículo porque está registrado en pedidos existentes.");
            } else {
                System.out.println("Error al eliminar el artículo: " + e.getMessage());
            }
        }
    }
}
