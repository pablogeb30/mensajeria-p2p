CREATE TABLE Usuarios (
    UserID SMALLSERIAL PRIMARY KEY,
    Username VARCHAR(50) UNIQUE NOT NULL,
    Password VARCHAR(255) NOT NULL,
    Email VARCHAR(100) UNIQUE,
    FechaDeCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Amigos (
    UserID1 INT,
    UserID2 INT,
    EstadoAmistad TEXT NOT NULL,
    FechaAmistad TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (UserID1, UserID2),
    FOREIGN KEY (UserID1) REFERENCES Usuarios(UserID),
    FOREIGN KEY (UserID2) REFERENCES Usuarios(UserID),
    constraint EstadoAmistad_Tipo check (EstadoAmistad in ('aceptada', 'rechazada', 'pendiente'))
);

