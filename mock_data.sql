-- Script de inserción de datos aleatorios para la base de datos PAN_DE_CASA
-- Este script respeta la integridad referencial (llaves foráneas)

-- 1. Insertar Clientes
INSERT INTO Cliente (IdNit, Nombre, Apellido, Direccion, Correo, Celular) VALUES
(1001, 'Carlos', 'Gomez', 'Calle 10 # 5-20', 'carlos.gomez@email.com', 300123456),
(1002, 'Maria', 'Rodriguez', 'Carrera 15 # 8-40', 'maria.rodriguez@email.com', 310987654),
(1003, 'Andres', 'Perez', 'Avenida 33 # 12-10', 'andres.perez@email.com', 315456789),
(1004, 'Laura', 'Martinez', 'Calle 50 # 40-11', 'laura.martinez@email.com', 320111222),
(1005, 'Juan', 'Lopez', 'Diagonal 74 # 10-15', 'juan.lopez@email.com', 301333444);

-- 2. Insertar Articulos (Catálogo de productos)
INSERT INTO Articulo (IdArticulo, Nombre, Unidad, PrecioUnitario, Descripcion) VALUES
(201, 'Pan Frances', 'Unidad', 500, 'Pan tradicional frances crujiente'),
(202, 'Pan de Bono', 'Unidad', 1500, 'Pan de bono horneado con queso'),
(203, 'Croissant', 'Unidad', 2500, 'Croissant de mantequilla'),
(204, 'Torta de Chocolate', 'Porcion', 4500, 'Porcion de torta de chocolate humeda'),
(205, 'Galletas de Avena', 'Docena', 6000, 'Docena de galletas de avena con pasas');

-- 3. Insertar DiasHabiles (Calendario de entregas / operaciones)
INSERT INTO DiasHabiles (IdCalendarioPedido, DiasHabiles, HoraInicio, HoraFin) VALUES
(301, '2026-05-10', '08:00:00', '12:00:00'),
(302, '2026-05-10', '14:00:00', '18:00:00'),
(303, '2026-05-11', '08:00:00', '12:00:00'),
(304, '2026-05-11', '14:00:00', '18:00:00'),
(305, '2026-05-12', '08:00:00', '12:00:00');

-- 4. Insertar Pedidos
-- Se asocian al cliente y a las franjas horarias de DiasHabiles
INSERT INTO Pedido (IdPedido, FechaPedido, HoraPedido, EstadoPedido, DiasHabiles_IdCalendarioPedido, DiasHabiles_IdCalendarioPedido1, Cliente_IdNit) VALUES
(401, '2026-05-02', '09:30:00', 'Pendiente', 301, 301, 1001),
(402, '2026-05-02', '10:15:00', 'En Preparacion', 302, 303, 1002),
(403, '2026-05-03', '11:00:00', 'Entregado', 301, 302, 1003),
(404, '2026-05-04', '14:20:00', 'Cancelado', 304, 305, 1004),
(405, '2026-05-05', '16:45:00', 'Pendiente', 305, 305, 1005);

-- 5. Insertar ArticuloPedido (Detalle de los productos en cada pedido)
INSERT INTO ArticuloPedido (ArticuloPedido, Cantidad, Pedido_IdPedido, Articulo_IdArticulo) VALUES
(501, 10, 401, 201),
(502, 5, 401, 202),
(503, 2, 402, 203),
(504, 1, 402, 204),
(505, 20, 403, 201),
(506, 1, 403, 205),
(507, 3, 404, 202),
(508, 4, 404, 203),
(509, 2, 405, 204),
(510, 2, 405, 205);

-- 6. Insertar Pagos (Asociados a los pedidos)
INSERT INTO Pago (IdPago, FechaPago, HoraPago, MetodoPago, Pedido_IdPedido) VALUES
(601, '2026-05-02', '09:35:00', 'Efectivo', 401),
(602, '2026-05-02', '10:20:00', 'Tarjeta', 402),
(603, '2026-05-03', '11:05:00', 'Transferencia', 403),
(604, '2026-05-05', '16:50:00', 'Nequi', 405);
