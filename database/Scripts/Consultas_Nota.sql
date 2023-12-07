/*

-- Registrar un Nuevo Usuario
INSERT INTO Usuarios (Username, Password, Email) VALUES ('nombreUsuario', 'contraseñaCifrada', 'email@example.com');

-- Enviar una Solicitud de Amistad
INSERT INTO Amigos (UserID1, UserID2, EstadoAmistad) VALUES (IDUsuario1, IDUsuario2, 'pendiente');

-- Aceptar una Solicitud de Amistad

-- Rechazar solicitud de amistad
UPDATE Amigos SET EstadoAmistad = 'rechazada' WHERE UserID1 = IDUsuario1 AND UserID2 = IDUsuario2;

-- Consultar la Lista de Amigos
SELECT * FROM Amigos WHERE UserID1 = IDUsuarioActual AND EstadoAmistad = 'aceptada';

*/