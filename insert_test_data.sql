USE tienda_disfraces;

-- Insert test rentals
INSERT INTO alquileres (id_usuario, id_producto, fecha_inicio, fecha_fin, dias, cantidad, total, estado)
VALUES
((SELECT id FROM usuarios WHERE correo = 'usuario1@tienda.com'), (SELECT id FROM productos WHERE nombre = 'Capibara'), CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY), 3, 1, 75.00, 'activo'),
((SELECT id FROM usuarios WHERE correo = 'usuario2@tienda.com'), (SELECT id FROM productos WHERE nombre = 'Guepardo'), DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), 3, 1, 60.00, 'atrasado');

-- Insert test sales
INSERT INTO compras (id_usuario, id_producto, cantidad, total)
VALUES
((SELECT id FROM usuarios WHERE correo = 'usuario1@tienda.com'), (SELECT id FROM productos WHERE nombre = 'Corona Real'), 1, 40.00),
((SELECT id FROM usuarios WHERE correo = 'usuario2@tienda.com'), (SELECT id FROM productos WHERE nombre = 'Moños Abrazadores'), 2, 30.00);
