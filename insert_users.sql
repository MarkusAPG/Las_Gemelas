USE tienda_disfraces;

INSERT INTO usuarios (nombre, apellido, correo, correo_recuperacion, contrasena, telefono, direccion, rol)
VALUES
('Tecnico', 'Uno', 'tecnico1@tienda.com', NULL, '12345', '999000111', 'Oficina Central', 'tecnico'),
('Tecnico', 'Dos', 'tecnico2@tienda.com', NULL, '12345', '999000222', 'Almacén', 'tecnico'),
('Usuario', 'Prueba1', 'usuario1@tienda.com', NULL, '12345', '999111222', 'Casa 1', 'cliente'),
('Usuario', 'Prueba2', 'usuario2@tienda.com', NULL, '12345', '999333444', 'Casa 2', 'cliente');
