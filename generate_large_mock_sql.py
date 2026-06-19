import random
import datetime

# Constantes de listas para generar nombres, apellidos y correos falsos
NOMBRES = ['Carlos', 'Maria', 'Andres', 'Laura', 'Juan', 'Ana', 'Luis', 'Pedro', 'Sofia', 'Camila', 'Jorge', 'Diana', 'Victor', 'Elena', 'Diego', 'Patricia', 'Fernando', 'Valeria', 'Daniel', 'Marta']
APELLIDOS = ['Gomez', 'Rodriguez', 'Perez', 'Martinez', 'Lopez', 'Garcia', 'Sanchez', 'Ramirez', 'Torres', 'Suarez', 'Diaz', 'Vargas', 'Rios', 'Castro', 'Ortiz', 'Silva', 'Rojas', 'Mendoza', 'Reyes', 'Morales']
CALLES = ['Calle', 'Carrera', 'Avenida', 'Diagonal', 'Transversal']

# Constantes de artículos
ARTICULOS_DATA = [
    (1, 'Pan Frances', 'Unidad', 500, 'Pan tradicional frances crujiente'),
    (2, 'Pan de Bono', 'Unidad', 1500, 'Pan de bono horneado con queso'),
    (3, 'Croissant', 'Unidad', 2500, 'Croissant de mantequilla'),
    (4, 'Torta de Chocolate', 'Porcion', 4500, 'Porcion de torta de chocolate humeda'),
    (5, 'Galletas de Avena', 'Docena', 6000, 'Docena de galletas de avena con pasas'),
    (6, 'Pan Integral', 'Unidad', 800, 'Pan saludable 100% integral'),
    (7, 'Buñuelo', 'Unidad', 1200, 'Buñuelo tradicional frito'),
    (8, 'Pastel de Pollo', 'Unidad', 3500, 'Pastel hojaldrado relleno de pollo'),
    (9, 'Empanada', 'Unidad', 2000, 'Empanada horneada de carne'),
    (10, 'Torta Tres Leches', 'Porcion', 5000, 'Porcion de torta tres leches'),
    (11, 'Roscon de Arequipe', 'Unidad', 3000, 'Roscon tradicional relleno de arequipe'),
    (12, 'Pan de Queso', 'Unidad', 1800, 'Pan suave con queso en el centro'),
    (13, 'Brazo de Reina', 'Porcion', 4000, 'Pionono relleno de crema y fresas'),
    (14, 'Galleta de Chocolate', 'Unidad', 1500, 'Galleta con chispas de chocolate'),
    (15, 'Muffin de Arandanos', 'Unidad', 2500, 'Muffin esponjoso de arandanos')
]

ESTADOS_PEDIDO = ['Pendiente', 'En Preparacion', 'Cancelado', 'Entregado', 'En Camino']
METODOS_PAGO = ['Efectivo', 'Tarjeta', 'Transferencia', 'Nequi', 'Daviplata']

def random_date(start_date, end_date):
    time_between_dates = end_date - start_date
    days_between_dates = time_between_dates.days
    random_number_of_days = random.randrange(days_between_dates)
    return start_date + datetime.timedelta(days=random_number_of_days)

def random_time():
    hour = random.randint(7, 18)
    minute = random.choice([0, 15, 30, 45])
    return datetime.time(hour, minute, 0)

def main():
    sql = "-- Script de inserción de 100 pedidos y datos relacionados\n\n"

    # 1. Clientes (30)
    sql += "-- 1. Clientes (30)\nINSERT INTO Cliente (IdNit, Nombre, Apellido, Direccion, Correo, Celular) VALUES\n"
    clientes_nit = []
    clientes_sql = []
    for i in range(1, 31):
        nit = 1000 + i
        clientes_nit.append(nit)
        nombre = random.choice(NOMBRES)
        apellido = random.choice(APELLIDOS)
        direccion = f"{random.choice(CALLES)} {random.randint(1,100)} # {random.randint(1,100)}-{random.randint(1,100)}"
        correo = f"{nombre.lower()}.{apellido.lower()}{random.randint(1,99)}@email.com"
        celular = 300000000 + random.randint(1000000, 20000000)
        clientes_sql.append(f"({nit}, '{nombre}', '{apellido}', '{direccion}', '{correo}', {celular})")
    sql += ",\n".join(clientes_sql) + ";\n\n"

    # 2. Articulos (15)
    sql += "-- 2. Articulos (15)\nINSERT INTO Articulo (IdArticulo, Nombre, Unidad, PrecioUnitario, Descripcion) VALUES\n"
    articulos_id = []
    articulos_sql = []
    for art in ARTICULOS_DATA:
        articulos_id.append(art[0])
        articulos_sql.append(f"({art[0]}, '{art[1]}', '{art[2]}', {art[3]}, '{art[4]}')")
    sql += ",\n".join(articulos_sql) + ";\n\n"

    # 3. DiasHabiles (15 - Simulando 15 días laborables del mes)
    sql += "-- 3. DiasHabiles (15)\nINSERT INTO DiasHabiles (IdCalendarioPedido, DiasHabiles, HoraInicio, HoraFin) VALUES\n"
    dias_id = []
    dias_sql = []
    start = datetime.date(2026, 5, 1)
    for i in range(1, 16):
        cal_id = 300 + i
        dias_id.append(cal_id)
        current_date = start + datetime.timedelta(days=i-1)
        hora_ini = '08:00:00' if random.random() > 0.5 else '14:00:00'
        hora_fin = '12:00:00' if hora_ini == '08:00:00' else '18:00:00'
        dias_sql.append(f"({cal_id}, '{current_date.strftime('%Y-%m-%d')}', '{hora_ini}', '{hora_fin}')")
    sql += ",\n".join(dias_sql) + ";\n\n"

    # 4. Pedidos (100)
    sql += "-- 4. Pedidos (100)\nINSERT INTO Pedido (IdPedido, FechaPedido, HoraPedido, EstadoPedido, DiasHabiles_IdCalendarioPedido, DiasHabiles_IdCalendarioPedido1, Cliente_IdNit) VALUES\n"
    pedidos_sql = []
    pedidos_id = []
    for i in range(1, 101):
        p_id = 400 + i
        pedidos_id.append(p_id)
        f_pedido = random_date(datetime.date(2026, 5, 1), datetime.date(2026, 5, 15)).strftime('%Y-%m-%d')
        h_pedido = random_time().strftime('%H:%M:%S')
        estado = random.choice(ESTADOS_PEDIDO)
        dia_cal_1 = random.choice(dias_id)
        dia_cal_2 = dia_cal_1 # usualmente el mismo turno
        if random.random() > 0.8:
            dia_cal_2 = random.choice(dias_id) # a veces se entrega en otro turno
        cliente = random.choice(clientes_nit)
        pedidos_sql.append(f"({p_id}, '{f_pedido}', '{h_pedido}', '{estado}', {dia_cal_1}, {dia_cal_2}, {cliente})")
    sql += ",\n".join(pedidos_sql) + ";\n\n"

    # 5. ArticuloPedido (aprox 200)
    sql += "-- 5. ArticuloPedido (Detalle)\nINSERT INTO ArticuloPedido (ArticuloPedido, Cantidad, Pedido_IdPedido, Articulo_IdArticulo) VALUES\n"
    ap_sql = []
    ap_id_counter = 500
    for p_id in pedidos_id:
        num_items = random.randint(1, 4)
        items_in_order = random.sample(articulos_id, num_items)
        for item in items_in_order:
            ap_id_counter += 1
            cantidad = random.randint(1, 10)
            ap_sql.append(f"({ap_id_counter}, {cantidad}, {p_id}, {item})")
    sql += ",\n".join(ap_sql) + ";\n\n"

    # 6. Pago (100)
    sql += "-- 6. Pagos (100)\nINSERT INTO Pago (IdPago, FechaPago, HoraPago, MetodoPago, Pedido_IdPedido) VALUES\n"
    pagos_sql = []
    pago_id_counter = 600
    for i, p_id in enumerate(pedidos_id):
        if random.random() > 0.05: # 95% de los pedidos tienen pago
            pago_id_counter += 1
            f_pago = random_date(datetime.date(2026, 5, 1), datetime.date(2026, 5, 15)).strftime('%Y-%m-%d')
            h_pago = random_time().strftime('%H:%M:%S')
            metodo = random.choice(METODOS_PAGO)
            pagos_sql.append(f"({pago_id_counter}, '{f_pago}', '{h_pago}', '{metodo}', {p_id})")
    sql += ",\n".join(pagos_sql) + ";\n"

    with open('mock_data_100.sql', 'w', encoding='utf-8') as f:
        f.write(sql)
    print("Archivo mock_data_100.sql generado correctamente.")

if __name__ == "__main__":
    main()
