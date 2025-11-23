USE tienda_disfraces;

-- ==============================
-- TABLA USUARIOS
-- ==============================

CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    correo_recuperacion VARCHAR(150) NULL,
    contrasena VARCHAR(255) NOT NULL,
    telefono VARCHAR(30),
    direccion VARCHAR(255),
    rol ENUM('cliente', 'admin', 'tecnico') DEFAULT 'cliente',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==============================
-- TABLA TECNICOS
-- ==============================

CREATE TABLE IF NOT EXISTS tecnicos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(30),
    area_asignada VARCHAR(100),
    fecha_ingreso TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==============================
-- TABLA PRODUCTOS
-- ==============================

CREATE TABLE IF NOT EXISTS productos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    categoria VARCHAR(50),
    tipo ENUM('venta', 'alquiler', 'ambos') DEFAULT 'ambos',
    precio_venta DECIMAL(10,2),
    precio_alquiler DECIMAL(10,2),
    stock INT DEFAULT 0,
    imagen VARCHAR(255),
    estado ENUM('disponible', 'no disponible') DEFAULT 'disponible'
);

-- ==============================
-- TABLA COMPRAS (VENTA)
-- ==============================

CREATE TABLE IF NOT EXISTS compras (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT,
    id_producto INT,
    id_tecnico INT NULL,
    fecha DATETIME DEFAULT NOW(),
    cantidad INT DEFAULT 1,
    total DECIMAL(10,2),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES productos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_tecnico) REFERENCES tecnicos(id) ON DELETE SET NULL
);

-- ==============================
-- TABLA ALQUILERES
-- ==============================

CREATE TABLE IF NOT EXISTS alquileres (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT,
    id_producto INT,
    id_tecnico INT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dias INT,
    cantidad INT DEFAULT 1,
    total DECIMAL(10,2),
    estado ENUM('activo', 'devuelto', 'atrasado') DEFAULT 'activo',
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES productos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_tecnico) REFERENCES tecnicos(id) ON DELETE SET NULL
);

-- ==============================
-- INSERTAR PRODUCTOS
-- ==============================

INSERT INTO productos (nombre, descripcion, categoria, tipo, precio_venta, precio_alquiler, stock, imagen, estado)
VALUES
('Capibara', 'Disfraz divertido de capibara', 'Animal', 'ambos', 80.00, 25.00, 10, 'capibara.jpg', 'disponible'),
('Corona Real', 'Corona elegante / real (solo venta)', 'Accesorios', 'venta', 40.00, NULL, 5, 'corona-cata.JPG', 'disponible'),
('Guepardo', 'Disfraz de guepardo veloz', 'Animal', 'ambos', 45.00, 20.00, 7, 'guepardo-cata.JPG', 'disponible'),
('León', 'Disfraz de león con melena', 'Animal', 'ambos', 85.00, 27.00, 9, 'Leon-Traje.jpg', 'disponible'),
('Mario Bros', 'Disfraz del personaje clásico de videojuegos', 'Videojuego', 'ambos', 55.00, 24.00, 12, 'mario-cata.JPG', 'disponible'),
('Michael Jackson', 'Disfraz inspirado en Michael Jackson', 'Famoso', 'ambos', 72.00, 18.00, 6, 'michael-cata.JPG', 'disponible'),
('Monos', 'Disfraz divertido de monos', 'Animal', 'ambos', 75.00, 25.00, 10, 'monos-cata.JPG', 'disponible'),
('Nio', 'Traje ninja oscuro', 'Cultura', 'ambos', 85.00, 28.00, 5, 'Nio-Traje1.jpg', 'disponible'),
('Princesa', 'Disfraz de princesa de cuento', 'Realeza', 'ambos', 35.00, 30.00, 10, 'princesa-cata.JPG', 'disponible'),
('Roma', 'Disfraz de soldado romano', 'Histórico', 'ambos', 85.00, 45.00, 6, 'roma-cata.JPG', 'disponible'),
('Stitch', 'Disfraz del personaje Stitch', 'Dibujo animado', 'ambos', 55.00, 30.00, 9, 'stich-cata.JPG', 'disponible'),
('Moños Abrazadores', 'Accesorio colorido para disfraces', 'Accesorios', 'venta', 15.00, NULL, 15, 'monos-cata.JPG', 'disponible');

-- ==============================
-- INSERTAR USUARIOS (INCLUYENDO NUEVOS)
-- ==============================

INSERT INTO usuarios (nombre, apellido, correo, correo_recuperacion, contrasena, telefono, direccion, rol)
VALUES
('María', 'Pérez', 'maria@gmail.com', 'maria_recup@gmail.com', '12345', '987654321', 'Av. Los Olivos 123', 'cliente'),
('Juan', 'Ramírez', 'juan@gmail.com', 'juan_backup@gmail.com', '12345', '986543210', 'Calle Las Flores 456', 'cliente'),
('Tecnico', 'Uno', 'tecnico1@tienda.com', NULL, '12345', '999000111', 'Oficina Central', 'tecnico'),
('Tecnico', 'Dos', 'tecnico2@tienda.com', NULL, '12345', '999000222', 'Almacén', 'tecnico'),
('Usuario', 'Prueba1', 'usuario1@tienda.com', NULL, '12345', '999111222', 'Casa 1', 'cliente'),
('Usuario', 'Prueba2', 'usuario2@tienda.com', NULL, '12345', '999333444', 'Casa 2', 'cliente');

-- ==============================
-- INSERTAR TÉCNICOS
-- ==============================

INSERT INTO tecnicos (nombre, correo, telefono, area_asignada)
VALUES
('Carlos Torres', 'carlos@tienda.com', '999888777', 'Soporte general'),
('Lucía Méndez', 'lucia@tienda.com', '988777666', 'Gestión de alquileres');
