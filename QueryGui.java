import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.Vector;

public class QueryGui extends JFrame {
    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JTextField txtPedidoId;
    private JTextArea txtCustomSql;
    private JLabel lblStatus;
    private JLabel lblTitle;
    private String currentView = "CLIENTE";

    public QueryGui() {
        cargarConfiguracion();
        initUI();
        conectarYProbar();
    }

    private void cargarConfiguracion() {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream("db.properties")) {
            prop.load(fis);
            dbUrl = prop.getProperty("db.url");
            dbUser = prop.getProperty("db.user");
            dbPassword = prop.getProperty("db.password");
        } catch (IOException e) {
            dbUrl = "jdbc:mysql://localhost:3306/PAN_DE_CASA?useSSL=false&allowPublicKeyRetrieval=true";
            dbUser = "root";
            dbPassword = "";
        }
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    private void conectarYProbar() {
        try (Connection conn = conectar()) {
            lblStatus.setText("Estado: Conectado a la base de datos");
            lblStatus.setForeground(new Color(46, 125, 50)); // Verde
            // Cargar datos por defecto al iniciar
            ejecutarQuery("SELECT IdNit, Nombre, Apellido, Direccion, Correo, Celular FROM cliente");
        } catch (SQLException e) {
            lblStatus.setText("Estado: Error de conexión - " + e.getMessage());
            lblStatus.setForeground(new Color(198, 40, 40)); // Rojo
        }
    }

    private void initUI() {
        setTitle("Sistema de Consulta - PAN DE CASA");
        setSize(950, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Cambiar Look and Feel a sistema operativo para mejor apariencia
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ignorar
        }

        // PANEL PRINCIPAL
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 245, 247));
        setContentPane(mainPanel);

        // 1. ENCABEZADO (Espacio para el título personalizable)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(33, 150, 243)); // Azul brillante premium
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // TÍTULO CENTRALIZADO Y EDITABLE (Puedes cambiar este texto fácilmente)
        lblTitle = new JLabel("PAN DE CASA - PANEL DE CONSULTAS", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.CENTER);

        // Subtítulo descriptivo
        JLabel lblSubtitle = new JLabel("Visualizador y Gestor de Base de Datos SENA", JLabel.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(224, 242, 241));
        headerPanel.add(lblSubtitle, BorderLayout.SOUTH);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. SIDEBAR - PANEL IZQUIERDO DE ACCIONES RÁPIDAS
        JPanel sidebarPanel = new JPanel(new GridLayout(7, 1, 0, 10));
        sidebarPanel.setBackground(new Color(245, 245, 247));
        sidebarPanel.setPreferredSize(new Dimension(240, 0));

        JButton btnClientes = crearBotonEstilizado("Listar Clientes");
        JButton btnRegCliente = crearBotonEstilizado("Registrar Cliente");
        JButton btnArticulos = crearBotonEstilizado("Listar Artículos");
        JButton btnRegArticulo = crearBotonEstilizado("Registrar Artículo");
        JButton btnPedidos = crearBotonEstilizado("Listar Pedidos");
        JButton btnReporteVentas = crearBotonEstilizado("Ventas por Cliente");
        JButton btnReconectar = crearBotonEstilizado("Probar Conexión");

        // Eventos
        btnClientes.addActionListener(e -> {
            currentView = "CLIENTE";
            ejecutarQuery(
                    "SELECT IdNit AS 'NIT', Nombre, Apellido, Direccion AS 'Dirección', Correo, Celular FROM cliente");
        });
        btnRegCliente.addActionListener(e -> mostrarDialogoRegistroCliente());

        btnArticulos.addActionListener(e -> {
            currentView = "ARTICULO";
            ejecutarQuery(
                    "SELECT IdArticulo AS 'Código', Nombre, Unidad, PrecioUnitario AS 'Precio Unitario', Descripcion AS 'Descripción' FROM articulo");
        });
        btnRegArticulo.addActionListener(e -> mostrarDialogoRegistroArticulo());

        btnPedidos.addActionListener(e -> {
            currentView = "PEDIDO";
            ejecutarQuery(
                    "SELECT p.IdPedido AS 'ID Pedido', p.FechaPedido AS 'Fecha', p.HoraPedido AS 'Hora', p.EstadoPedido AS 'Estado', "
                            +
                            "CONCAT(c.Nombre, ' ', c.Apellido) AS 'Cliente' " +
                            "FROM pedido p JOIN cliente c ON p.Cliente_IdNit = c.IdNit ORDER BY p.FechaPedido DESC");
        });
        btnReporteVentas.addActionListener(e -> {
            currentView = "REPORTE";
            ejecutarQuery(
                    "SELECT c.IdNit AS 'NIT', CONCAT(c.Nombre, ' ', c.Apellido) AS 'Cliente', " +
                            "COUNT(DISTINCT p.IdPedido) AS 'Total Pedidos', " +
                            "COALESCE(SUM(ap.Cantidad * a.PrecioUnitario), 0) AS 'Total Comprado ($)' " +
                            "FROM cliente c " +
                            "LEFT JOIN pedido p ON c.IdNit = p.Cliente_IdNit " +
                            "LEFT JOIN articulopedido ap ON p.IdPedido = ap.Pedido_IdPedido " +
                            "LEFT JOIN articulo a ON ap.Articulo_IdArticulo = a.IdArticulo " +
                            "GROUP BY c.IdNit, c.Nombre, c.Apellido " +
                            "ORDER BY 4 DESC");
        });
        btnReconectar.addActionListener(e -> conectarYProbar());

        sidebarPanel.add(btnClientes);
        sidebarPanel.add(btnRegCliente);
        sidebarPanel.add(btnArticulos);
        sidebarPanel.add(btnRegArticulo);
        sidebarPanel.add(btnPedidos);
        sidebarPanel.add(btnReporteVentas);
        sidebarPanel.add(btnReconectar);

        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        // 3. CUERPO PRINCIPAL (Centro y Derecha)
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(new Color(245, 245, 247));

        // 3.1 Panel superior de búsquedas y filtros
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Fila 1: Buscar pedido por ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        filterPanel.add(new JLabel("Buscar Pedido por ID:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        txtPedidoId = new JTextField();
        filterPanel.add(txtPedidoId, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JButton btnBuscarPedido = new ModernButton(
                "Buscar",
                new Color(33, 150, 243),
                new Color(21, 101, 192),
                new Color(13, 71, 161),
                Color.WHITE,
                Color.WHITE,
                null);
        btnBuscarPedido.addActionListener(e -> buscarPedidoDetallado());
        filterPanel.add(btnBuscarPedido, gbc);

        // Fila 2: Consulta SQL Personalizada
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        filterPanel.add(new JLabel("Consulta SQL Libre:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        txtCustomSql = new JTextArea(2, 40);
        txtCustomSql.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtCustomSql.setBorder(BorderFactory.createLineBorder(new Color(189, 189, 189)));
        filterPanel.add(new JScrollPane(txtCustomSql), gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        JButton btnEjecutarSql = new ModernButton(
                "Ejecutar SQL",
                new Color(67, 160, 71),
                new Color(46, 125, 50),
                new Color(27, 94, 32),
                Color.WHITE,
                Color.WHITE,
                null);
        btnEjecutarSql.addActionListener(e -> ejecutarSqlPersonalizado());
        filterPanel.add(btnEjecutarSql, gbc);

        centerPanel.add(filterPanel, BorderLayout.NORTH);

        // 3.2 Tabla de Resultados
        tableModel = new DefaultTableModel();
        resultTable = new JTable(tableModel);
        resultTable.setRowHeight(24);
        resultTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        resultTable.setGridColor(new Color(224, 224, 224));

        JScrollPane scrollPane = new JScrollPane(resultTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224)));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel inferior para eliminar registros
        JPanel bottomTablePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomTablePanel.setBackground(new Color(245, 245, 247));
        JButton btnEliminar = new ModernButton(
                "Eliminar Fila Seleccionada",
                new Color(239, 83, 80),
                new Color(211, 47, 47),
                new Color(183, 28, 28),
                Color.WHITE,
                Color.WHITE,
                null);
        btnEliminar.addActionListener(e -> eliminarFilaSeleccionada());
        bottomTablePanel.add(btnEliminar);
        centerPanel.add(bottomTablePanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 4. BARRA DE ESTADO (SUR)
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        statusPanel.setBackground(new Color(238, 238, 238));

        lblStatus = new JLabel("Estado: Iniciando...");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusPanel.add(lblStatus, BorderLayout.WEST);

        JLabel lblFooter = new JLabel("Desarrollado en Java - SENA");
        lblFooter.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblFooter.setForeground(Color.GRAY);
        statusPanel.add(lblFooter, BorderLayout.EAST);

        mainPanel.add(statusPanel, BorderLayout.SOUTH);
    }

    private JButton crearBotonEstilizado(String texto) {
        return new ModernButton(
                texto,
                Color.WHITE, // normalBg
                new Color(224, 242, 241), // hoverBg
                new Color(178, 223, 219), // pressedBg
                new Color(33, 33, 33), // normalFg
                new Color(0, 77, 64), // hoverFg
                new Color(200, 200, 200) // borderCol
        );
    }

    private void ejecutarQuery(String sql) {
        try (Connection conn = conectar();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Cabeceras
            Vector<String> columnNames = new Vector<>();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnLabel(i));
            }

            // Datos
            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> vector = new Vector<>();
                for (int i = 1; i <= columnCount; i++) {
                    vector.add(rs.getObject(i));
                }
                data.add(vector);
            }

            tableModel.setDataVector(data, columnNames);
            lblStatus.setText("Consulta realizada con éxito. Filas cargadas: " + data.size());
            lblStatus.setForeground(new Color(46, 125, 50));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al consultar: " + e.getMessage(), "Error de Base de Datos",
                    JOptionPane.ERROR_MESSAGE);
            lblStatus.setText("Error en consulta: " + e.getMessage());
            lblStatus.setForeground(new Color(198, 40, 40));
        }
    }

    private void buscarPedidoDetallado() {
        String idStr = txtPedidoId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese un ID de Pedido.", "Campo vacío",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int idPedido = Integer.parseInt(idStr);
            String sql = "SELECT ap.Pedido_IdPedido AS 'Pedido #', " +
                    "CONCAT(c.Nombre, ' ', c.Apellido) AS 'Cliente', " +
                    "a.Nombre AS 'Articulo', ap.Cantidad AS 'Cantidad', " +
                    "a.PrecioUnitario AS 'Precio Unit ($)', " +
                    "(ap.Cantidad * a.PrecioUnitario) AS 'Subtotal ($)', " +
                    "p.FechaPedido AS 'Fecha Pedido', " +
                    "p.EstadoPedido AS 'Estado' " +
                    "FROM articulopedido ap " +
                    "JOIN articulo a ON ap.Articulo_IdArticulo = a.IdArticulo " +
                    "JOIN pedido p ON ap.Pedido_IdPedido = p.IdPedido " +
                    "JOIN cliente c ON p.Cliente_IdNit = c.IdNit " +
                    "WHERE p.IdPedido = " + idPedido;

            ejecutarQuery(sql);
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                        "No se encontró el pedido #" + idPedido + " o no tiene artículos asociados.", "Sin registros",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El ID del pedido debe ser un número entero válido.",
                    "Entrada inválida", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ejecutarSqlPersonalizado() {
        String sql = txtCustomSql.getText().trim();
        if (sql.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese una consulta SQL para ejecutar.", "Campo vacío",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validación simple de comandos destructivos por seguridad
        String lowerSql = sql.toLowerCase();
        if (lowerSql.contains("drop ") || lowerSql.contains("delete ") || lowerSql.contains("truncate ")
                || lowerSql.contains("alter ")) {
            int resp = JOptionPane.showConfirmDialog(this,
                    "La consulta contiene comandos que podrían alterar o borrar datos (DROP, DELETE, TRUNCATE, ALTER).\n¿Está seguro de que desea continuar?",
                    "Advertencia de Seguridad", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (resp != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try (Connection conn = conectar();
                Statement stmt = conn.createStatement()) {

            boolean isResultSet = stmt.execute(sql);
            if (isResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    Vector<String> columnNames = new Vector<>();
                    for (int i = 1; i <= columnCount; i++) {
                        columnNames.add(metaData.getColumnLabel(i));
                    }

                    Vector<Vector<Object>> data = new Vector<>();
                    while (rs.next()) {
                        Vector<Object> vector = new Vector<>();
                        for (int i = 1; i <= columnCount; i++) {
                            vector.add(rs.getObject(i));
                        }
                        data.add(vector);
                    }

                    tableModel.setDataVector(data, columnNames);
                    lblStatus.setText("Consulta ejecutada. Registros devueltos: " + data.size());
                    lblStatus.setForeground(new Color(46, 125, 50));
                }
            } else {
                int affectedRows = stmt.getUpdateCount();
                tableModel.setDataVector(new Vector<>(), new Vector<>());
                lblStatus.setText("Operación ejecutada con éxito. Filas afectadas: " + affectedRows);
                lblStatus.setForeground(new Color(33, 150, 243));
                JOptionPane.showMessageDialog(this, "Operación completada con éxito. Filas afectadas: " + affectedRows,
                        "Ejecución SQL", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error de sintaxis SQL:\n" + e.getMessage(), "Error al Ejecutar",
                    JOptionPane.ERROR_MESSAGE);
            lblStatus.setText("Error en SQL: " + e.getMessage());
            lblStatus.setForeground(new Color(198, 40, 40));
        }
    }

    private void mostrarDialogoRegistroCliente() {
        JDialog dialog = new JDialog(this, "Registrar Nuevo Cliente", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setResizable(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        JTextField txtNit = new JTextField(15);
        JTextField txtNombre = new JTextField(15);
        JTextField txtApellido = new JTextField(15);
        JTextField txtDireccion = new JTextField(15);
        JTextField txtCorreo = new JTextField(15);
        JTextField txtCelular = new JTextField(15);

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("NIT / ID (Numérico):"), gbc);
        gbc.gridx = 1;
        dialog.add(txtNit, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtNombre, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("Apellido:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtApellido, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(new JLabel("Dirección:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtDireccion, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        dialog.add(new JLabel("Correo Electrónico:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtCorreo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        dialog.add(new JLabel("Celular (Numérico):"), gbc);
        gbc.gridx = 1;
        dialog.add(txtCelular, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new ModernButton(
                "Guardar",
                new Color(33, 150, 243),
                new Color(21, 101, 192),
                new Color(13, 71, 161),
                Color.WHITE,
                Color.WHITE,
                null);
        JButton btnCancelar = new ModernButton(
                "Cancelar",
                Color.WHITE,
                new Color(245, 245, 245),
                new Color(224, 224, 224),
                new Color(33, 33, 33),
                new Color(33, 33, 33),
                new Color(200, 200, 200));

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnGuardar.addActionListener(e -> {
            try {
                long nit = Long.parseLong(txtNit.getText().trim());
                String nombre = txtNombre.getText().trim();
                String apellido = txtApellido.getText().trim();
                String direccion = txtDireccion.getText().trim();
                String correo = txtCorreo.getText().trim();
                long celular = Long.parseLong(txtCelular.getText().trim());

                if (nombre.isEmpty() || apellido.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Nombre y Apellido son campos obligatorios.", "Validación",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try (Connection conn = conectar()) {
                    String sqlCheck = "SELECT COUNT(*) FROM cliente WHERE IdNit = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
                        pstmt.setLong(1, nit);
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                JOptionPane.showMessageDialog(dialog, "Ya existe un cliente con este NIT/ID.",
                                        "Duplicado", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }

                    String sqlInsert = "INSERT INTO cliente (IdNit, Nombre, Apellido, Direccion, Correo, Celular) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                        pstmt.setLong(1, nit);
                        pstmt.setString(2, nombre);
                        pstmt.setString(3, apellido);
                        pstmt.setString(4, direccion);
                        pstmt.setString(5, correo);
                        pstmt.setLong(6, celular);
                        pstmt.executeUpdate();
                    }
                }

                JOptionPane.showMessageDialog(this, "Cliente registrado con éxito.", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();

                if (currentView.equals("CLIENTE")) {
                    ejecutarQuery(
                            "SELECT IdNit AS 'NIT', Nombre, Apellido, Direccion AS 'Dirección', Correo, Celular FROM cliente");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "NIT/ID y Celular deben ser valores numéricos enteros.",
                        "Error de formato", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error en base de datos:\n" + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCancelar);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void mostrarDialogoRegistroArticulo() {
        JDialog dialog = new JDialog(this, "Registrar Nuevo Artículo", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setResizable(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        JTextField txtCodigo = new JTextField(15);
        JTextField txtNombre = new JTextField(15);
        JTextField txtUnidad = new JTextField(15);
        JTextField txtPrecio = new JTextField(15);
        JTextField txtDescripcion = new JTextField(15);

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("Código (Numérico):"), gbc);
        gbc.gridx = 1;
        dialog.add(txtCodigo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtNombre, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("Unidad de Medida:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtUnidad, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(new JLabel("Precio Unitario:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtPrecio, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        dialog.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtDescripcion, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new ModernButton(
                "Guardar",
                new Color(33, 150, 243),
                new Color(21, 101, 192),
                new Color(13, 71, 161),
                Color.WHITE,
                Color.WHITE,
                null);
        JButton btnCancelar = new ModernButton(
                "Cancelar",
                Color.WHITE,
                new Color(245, 245, 245),
                new Color(224, 224, 224),
                new Color(33, 33, 33),
                new Color(33, 33, 33),
                new Color(200, 200, 200));

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnGuardar.addActionListener(e -> {
            try {
                int codigo = Integer.parseInt(txtCodigo.getText().trim());
                String nombre = txtNombre.getText().trim();
                String unidad = txtUnidad.getText().trim();
                double precio = Double.parseDouble(txtPrecio.getText().trim());
                String descripcion = txtDescripcion.getText().trim();

                if (nombre.isEmpty() || unidad.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Nombre y Unidad son campos obligatorios.", "Validación",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try (Connection conn = conectar()) {
                    String sqlCheck = "SELECT COUNT(*) FROM articulo WHERE IdArticulo = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
                        pstmt.setInt(1, codigo);
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                JOptionPane.showMessageDialog(dialog, "Ya existe un artículo con este código.",
                                        "Duplicado", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }

                    String sqlInsert = "INSERT INTO articulo (IdArticulo, Nombre, Unidad, PrecioUnitario, Descripcion) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                        pstmt.setInt(1, codigo);
                        pstmt.setString(2, nombre);
                        pstmt.setString(3, unidad);
                        pstmt.setDouble(4, precio);
                        pstmt.setString(5, descripcion);
                        pstmt.executeUpdate();
                    }
                }

                JOptionPane.showMessageDialog(this, "Artículo registrado con éxito.", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();

                if (currentView.equals("ARTICULO")) {
                    ejecutarQuery(
                            "SELECT IdArticulo AS 'Código', Nombre, Unidad, PrecioUnitario AS 'Precio Unitario', Descripcion AS 'Descripción' FROM articulo");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Código debe ser entero y Precio debe ser un valor decimal.",
                        "Error de formato", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error en base de datos:\n" + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCancelar);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void eliminarFilaSeleccionada() {
        int selectedRow = resultTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una fila de la tabla para eliminar.",
                    "Ninguna selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!currentView.equals("CLIENTE") && !currentView.equals("ARTICULO")) {
            JOptionPane.showMessageDialog(this,
                    "Solo se pueden eliminar registros directamente desde las vistas 'Listar Clientes' y 'Listar Artículos'.",
                    "Acción no permitida", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Object idVal = resultTable.getValueAt(selectedRow, 0);
        if (idVal == null)
            return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar el registro seleccionado con ID: " + idVal + "?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sqlDelete = "";
        if (currentView.equals("CLIENTE")) {
            sqlDelete = "DELETE FROM cliente WHERE IdNit = ?";
        } else if (currentView.equals("ARTICULO")) {
            sqlDelete = "DELETE FROM articulo WHERE IdArticulo = ?";
        }

        try (Connection conn = conectar();
                PreparedStatement pstmt = conn.prepareStatement(sqlDelete)) {

            try {
                if (currentView.equals("CLIENTE")) {
                    pstmt.setLong(1, Long.parseLong(idVal.toString()));
                } else {
                    pstmt.setInt(1, Integer.parseInt(idVal.toString()));
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this,
                        "El ID del registro seleccionado no tiene un formato numérico válido.", "Error de Formato",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                JOptionPane.showMessageDialog(this, "Registro eliminado correctamente.", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                if (currentView.equals("CLIENTE")) {
                    ejecutarQuery(
                            "SELECT IdNit AS 'NIT', Nombre, Apellido, Direccion AS 'Dirección', Correo, Celular FROM cliente");
                } else {
                    ejecutarQuery(
                            "SELECT IdArticulo AS 'Código', Nombre, Unidad, PrecioUnitario AS 'Precio Unitario', Descripcion AS 'Descripción' FROM articulo");
                }
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar el registro.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                JOptionPane.showMessageDialog(this,
                        "Error de Integridad: No se puede eliminar este registro porque está asociado a pedidos en el sistema.",
                        "Error al eliminar", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error de Base de Datos:\n" + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class ModernButton extends JButton {
        private Color normalBg;
        private Color hoverBg;
        private Color pressedBg;
        private Color normalFg;
        private Color hoverFg;
        private Color borderCol;

        public ModernButton(String text, Color normalBg, Color hoverBg, Color pressedBg, Color normalFg, Color hoverFg,
                Color borderCol) {
            super(text);
            this.normalBg = normalBg;
            this.hoverBg = hoverBg;
            this.pressedBg = pressedBg;
            this.normalFg = normalFg;
            this.hoverFg = hoverFg;
            this.borderCol = borderCol;

            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(normalFg);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            if (getModel().isPressed()) {
                g2.setColor(pressedBg);
                setForeground(hoverFg);
            } else if (getModel().isRollover()) {
                g2.setColor(hoverBg);
                setForeground(hoverFg);
            } else {
                g2.setColor(normalBg);
                setForeground(normalFg);
            }

            g2.fillRoundRect(0, 0, width, height, 8, 8);

            if (borderCol != null) {
                g2.setColor(borderCol);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, width - 1, height - 1, 8, 8);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            QueryGui gui = new QueryGui();
            gui.setVisible(true);
        });
    }
}
