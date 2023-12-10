package agpg.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;
import agpg.client.CallbackClientInterface;
import java.rmi.RemoteException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class CallbackServerImpl extends UnicastRemoteObject implements CallbackServerInterface {

    // Archivo de configuración
    private static final Properties properties = new Properties();

    // Gestor de conexiones a la base de datos
    private static HikariDataSource dataSource;

    // Estado del usuario usando un enumerado:
    private enum EstadoUsuario {
        online, offline
    }


    // Cargar la configuración del servidor desde el archivo y configurar la BD
    static {
        try {
            // Comprobar si el archivo de configuración existe
            if (!new java.io.File("server_config.properties").exists()) {
                System.err.println("No se ha encontrado el archivo de configuración del servidor.");
                System.exit(1);
            }

            properties.load(new FileInputStream("server_config.properties"));
            configureDatabase();

        } catch (IOException e) {
            System.err.println("Error al cargar la configuración: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void configureDatabase() {

        try {
            // Comprobar si se han especificado las propiedades de la base de datos
            if (!properties.containsKey("db.jdbcUrl") || !properties.containsKey("db.username")
                    || !properties.containsKey("db.password") || !properties.containsKey("db.maxPoolSize")) {
                System.err.println("No se han especificado las propiedades necesarias de la base de datos.");
                System.exit(1);
            }

            String jdbcUrl = properties.getProperty("db.jdbcUrl");
            String username = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");
            int maxPoolSize = Integer.parseInt(properties.getProperty("db.maxPoolSize"));

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(maxPoolSize);

            dataSource = new HikariDataSource(config);

        } catch (Exception e) {
            System.err.println("Error al configurar la base de datos: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public CallbackServerImpl() throws RemoteException {
        super();
    }


    // Método para registrar un usuario en el servidor
    public boolean iniciarSesion(String username, String password, CallbackClientInterface cObject)
            throws RemoteException {
        try (Connection conn = dataSource.getConnection()) {
            if (!usuarioYaExiste(username, conn)) {
                return false; // Usuario no existe
            }

            String sql = "SELECT Password FROM Usuarios WHERE Username = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String hashedPassword = rs.getString("Password");

                        if (BCrypt.checkpw(password, hashedPassword)) {

                            String sql2 = "UPDATE Usuarios SET Estado = ? WHERE Username = ?;";
                            try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                                pstmt2.setString(1, EstadoUsuario.online.toString());
                                pstmt2.setString(2, username);
                                pstmt2.executeUpdate();
                            }

                            return true;
                        }
                    }
                }
            }
            return false; // Contraseña incorrecta
        } catch (SQLException e) {
            System.err.println("Error al iniciar sesión: " + e.getMessage());
            throw new RemoteException("Error al iniciar sesión", e);
        }
    }

    public boolean registrarCliente(String username, String password, String correo, CallbackClientInterface cObject)
            throws RemoteException {
        try (Connection conn = dataSource.getConnection()) {
            if (usuarioYaExiste(username, conn)) {
                return false; // Usuario ya existe
            }

            String sql = "INSERT INTO Usuarios (Username, Password, Email, Estado) VALUES (?, ?, ?, ?);";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, hashPassword(password));
                pstmt.setString(3, correo);
                pstmt.setString(4, EstadoUsuario.online.toString());
                pstmt.executeUpdate();

                return true; // Usuario registrado con éxito
            }
        } catch (SQLException e) {
            System.err.println("Error al registrar el cliente: " + e.getMessage());
            throw new RemoteException("Error al registrar el cliente", e);
        }
    }

    private boolean usuarioYaExiste(String username, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Usuarios WHERE Username = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Método auxiliar para cifrar o hashear la contraseña
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Función para cambiar la contraseña de un usuario dada la contraseña antigua
    public boolean cambiarPassword(String username, String oldPassword, String newPassword) throws RemoteException {
        try (Connection conn = dataSource.getConnection()) {
            if (!usuarioYaExiste(username, conn)) {
                return false; // Usuario no existe
            }

            String sql = "SELECT Password FROM Usuarios WHERE Username = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String hashedPassword = rs.getString("Password");
                        if (BCrypt.checkpw(oldPassword, hashedPassword)) {
                            String sql2 = "UPDATE Usuarios SET Password = ? WHERE Username = ?;";
                            try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                                pstmt2.setString(1, hashPassword(newPassword));
                                pstmt2.setString(2, username);
                                pstmt2.executeUpdate();
                                return true;
                            }
                        }
                    }
                }
            }
            return false; // Contraseña incorrecta
        } catch (SQLException e) {
            System.err.println("Error al cambiar la contraseña: " + e.getMessage());
            throw new RemoteException("Error al cambiar la contraseña", e);
        }
    }

    // Método para cerrar sesión
    public void cerrarSesion(String username) throws RemoteException {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "UPDATE Usuarios SET Estado = ? WHERE Username = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, EstadoUsuario.offline.toString());
                pstmt.setString(2, username);
                pstmt.executeUpdate();

                // unregisterCallback(clientMap.get(username));
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar sesión: " + e.getMessage());
            throw new RemoteException("Error al cerrar sesión", e);
        }
    }

    // Método para enviar solicitud de amistad
    public void enviarSolicitudAmistad(String userName, String friendName) throws RemoteException {
        validarSolicitudAmistad(userName, friendName);
        int userID = obtenerUserID(userName);
        int friendID = obtenerUserID(friendName);
        insertarSolicitudAmistad(userID, friendID);
    }

    private void validarSolicitudAmistad(String userName, String friendName) throws RemoteException {
        if (userName.equals(friendName)) {
            throw new RemoteException("No puedes enviarte una solicitud de amistad a ti mismo.");
        }

        if (sonAmigos(userName, friendName) || solicitudYaEnviada(userName, friendName)) {
            throw new RemoteException("Solicitud de amistad no válida.");
        }
    }

    private boolean sonAmigos(String userName, String friendName) throws RemoteException {
        int userID = obtenerUserID(userName);
        int friendID = obtenerUserID(friendName);

        String sql = "SELECT COUNT(*) FROM Amigos WHERE (UserID1 = ? AND UserID2 = ?) OR (UserID1 = ? AND UserID2 = ?) AND EstadoAmistad = 'aceptada';";
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            pstmt.setInt(2, friendID);
            pstmt.setInt(3, friendID);
            pstmt.setInt(4, userID);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RemoteException("Error al comprobar si los usuarios son amigos", e);
        }
        return false;
    }

    private boolean solicitudYaEnviada(String userName, String friendName) throws RemoteException {
        int userID = obtenerUserID(userName);
        int friendID = obtenerUserID(friendName);

        String sql = "SELECT COUNT(*) FROM Amigos WHERE (UserID1 = ? AND UserID2 = ?) OR (UserID1 = ? AND UserID2 = ?) AND EstadoAmistad = 'pendiente';";
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            pstmt.setInt(2, friendID);
            pstmt.setInt(3, friendID);
            pstmt.setInt(4, userID);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RemoteException("Error al comprobar si la solicitud de amistad ya ha sido enviada", e);
        }
        return false;
    }

    private void insertarSolicitudAmistad(int userID, int friendID) throws RemoteException {
        String sql = "INSERT INTO Amigos (UserID1, UserID2, EstadoAmistad) VALUES (?, ?, 'pendiente');";
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            pstmt.setInt(2, friendID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RemoteException("Error al insertar la solicitud de amistad", e);
        }
    }

    // Método para aceptar solicitud de amistad
    public void aceptarSolicitudAmistad(String userName, String friendName) throws RemoteException {

        // Obtener el ID del usuario actual
        int userID = obtenerUserID(userName);

        // Obtener el ID del amigo
        int friendID = obtenerUserID(friendName);

        // Actualizar la solicitud de amistad en la base de datos
        String sql = "UPDATE Amigos SET EstadoAmistad = 'aceptada' WHERE UserID1 = ? AND UserID2 = ?;";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, friendID);
            pstmt.setInt(2, userID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al aceptar solicitud de amistad: " + e.getMessage());
            throw new RemoteException("Error al aceptar solicitud de amistad", e);
        }

    }

    // Método para rechazar solicitud de amistad
    public void rechazarSolicitudAmistad(String userName, String friendName) throws RemoteException {

        // Obtener el ID del usuario actual
        int userID = obtenerUserID(userName);

        // Obtener el ID del amigo
        int friendID = obtenerUserID(friendName);

        // Actualizar la solicitud de amistad en la base de datos
        String sql = "UPDATE Amigos SET EstadoAmistad = 'rechazada' WHERE UserID1 = ? AND UserID2 = ?;";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, friendID);
            pstmt.setInt(2, userID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al rechazar solicitud de amistad: " + e.getMessage());
            throw new RemoteException("Error al rechazar solicitud de amistad", e);
        }
    }

    // Método para obtener una lista de usuarios recomendados para enviar
    // solicitudes de amistad (no amigos, no solicitudes pendientes)
    public List<String> obtenerUsuariosRecomendados(String username) throws RemoteException {

        // Obtener el ID del usuario actual
        int userID = obtenerUserID(username);

        List<String> usuariosRecomendados = new ArrayList<>();
        String sql = "SELECT Username FROM Usuarios WHERE UserID NOT IN (SELECT UserID1 FROM Amigos WHERE UserID2 = ?) "
                +
                "AND UserID NOT IN (SELECT UserID2 FROM Amigos WHERE UserID1 = ?) " +
                "AND UserID NOT IN (SELECT UserID1 FROM Amigos WHERE UserID2 = ? AND EstadoAmistad = 'pendiente') " +
                "AND UserID NOT IN (SELECT UserID2 FROM Amigos WHERE UserID1 = ? AND EstadoAmistad = 'pendiente') " +
                "AND UserID != ?;";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            pstmt.setInt(2, userID);
            pstmt.setInt(3, userID);
            pstmt.setInt(4, userID);
            pstmt.setInt(5, userID);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String usernameRecomendado = rs.getString("Username");
                    usuariosRecomendados.add(usernameRecomendado);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuarios recomendados: " + e.getMessage());
            throw new RemoteException("Error al obtener usuarios recomendados", e);
        }

        return usuariosRecomendados;
    }

    // Metodo privado para obtener el ID de un usuario a partir de su nombre
    private int obtenerUserID(String username) throws RemoteException {
        String sql = "SELECT UserID FROM Usuarios WHERE Username = ?;";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("UserID");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener el ID del usuario: " + e.getMessage());
            throw new RemoteException("Error al obtener el ID del usuario", e);
        }
        return 0;
    }

    // Método para obtener una lista de solicitudes de amistad pendientes
    public List<String> obtenerSolicitudesAmistad(String username) throws RemoteException {

        // Obtener el ID del usuario actual
        int userID = obtenerUserID(username);

        List<String> solicitudesAmistad = new ArrayList<>();
        String sql = "SELECT Username FROM Usuarios WHERE UserID IN (SELECT UserID1 FROM Amigos WHERE UserID2 = ? AND EstadoAmistad = 'pendiente');";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userID);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String usernameAmigo = rs.getString("Username");
                    solicitudesAmistad.add(usernameAmigo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener solicitudes de amistad: " + e.getMessage());
            throw new RemoteException("Error al obtener solicitudes de amistad", e);
        }

        return solicitudesAmistad;
    }

    // Metodo que devuelve la lista de amigos de un usuario
    public List<String> obtenerAmigos(String username) throws RemoteException {

        // Obtener el ID del usuario actual
        int userID = obtenerUserID(username);

        List<String> amigos = new ArrayList<>();
        // Lista de amigos del usuario actual --> Solicitudes aceptadas =
        // bidireccionales
        String sql = "SELECT Username FROM Usuarios WHERE UserID IN (SELECT UserID1 FROM Amigos WHERE UserID2 = ? AND EstadoAmistad = 'aceptada') OR UserID IN (SELECT UserID2 FROM Amigos WHERE UserID1 = ? AND EstadoAmistad = 'aceptada');";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            pstmt.setInt(2, userID);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String usernameAmigo = rs.getString("Username");
                    amigos.add(usernameAmigo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener amigos: " + e.getMessage());
            throw new RemoteException("Error al obtener amigos", e);
        }

        return amigos;
    }
    

    // Metodo para comprobar si un usuario esta conectado
    public boolean estaConectado(String username) throws RemoteException {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT Estado FROM Usuarios WHERE Username = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String estado = rs.getString("Estado");
                        return estado.equals(EstadoUsuario.online.toString());
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error al comprobar si el usuario está conectado: " + e.getMessage());
            throw new RemoteException("Error al comprobar si el usuario está conectado", e);        }
    }

}
