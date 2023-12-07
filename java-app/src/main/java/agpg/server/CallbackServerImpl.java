package agpg.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;
import agpg.client.CallbackClientInterface;
import java.rmi.RemoteException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class CallbackServerImpl extends UnicastRemoteObject implements CallbackServerInterface {

    private static final Properties properties = new Properties();
    private static HikariDataSource dataSource; // Declarado como variable de clase estática

    static {
        try {
            properties.load(new FileInputStream("server_config.properties"));
            configureDatabase();
        } catch (IOException e) {
            System.err.println("Error al cargar la configuración: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void configureDatabase() {
        String jdbcUrl = properties.getProperty("db.jdbcUrl");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");
        int maxPoolSize = Integer.parseInt(properties.getProperty("db.maxPoolSize", "5"));

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maxPoolSize);
        dataSource = new HikariDataSource(config);
    }

    private HashMap<String, CallbackClientInterface> clientMap;

    public CallbackServerImpl() throws RemoteException {
        super();
        clientMap = new HashMap<>();
    }

    public synchronized void registerCallback(CallbackClientInterface cObject) throws RemoteException {
        if (!clientMap.containsKey(cObject.getUsername())) {
            clientMap.put(cObject.getUsername(), cObject);
            System.out.println("Nuevo usuario conectado: " + cObject.getUsername());
        } else {
            System.out.println("Usuario ya conectado: " + cObject.getUsername());
        }
    }

    public synchronized void unregisterCallback(CallbackClientInterface cObject) throws RemoteException {
        if (clientMap.containsKey(cObject.getUsername())) {
            clientMap.remove(cObject.getUsername());
            System.out.println("Usuario desconectado: " + cObject.getUsername());
        }
    }

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
                            registerCallback(cObject);
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

    public boolean registrarCliente(String username, String password) throws RemoteException {
        try (Connection conn = dataSource.getConnection()) {
            if (usuarioYaExiste(username, conn)) {
                return false; // Usuario ya existe
            }

            String sql = "INSERT INTO Usuarios (Username, Password) VALUES (?, ?);";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, hashPassword(password));
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


    // Método para enviar solicitud de amistad
    public void enviarSolicitudAmistad(String userName, String friendName) throws RemoteException {

        // Obtener el ID del usuario actual
        int userID = obtenerUserID(userName);

        // Obtener el ID del amigo
        int friendID = obtenerUserID(friendName);

        // Insertar la solicitud de amistad en la base de datos
        String sql = "INSERT INTO Amigos (UserID1, UserID2, EstadoAmistad) VALUES (?, ?, 'pendiente');";
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            pstmt.setInt(2, friendID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al enviar solicitud de amistad: " + e.getMessage());
            throw new RemoteException("Error al enviar solicitud de amistad", e);
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
        String sql = "SELECT Username FROM Usuarios WHERE UserID NOT IN (SELECT UserID1 FROM Amigos WHERE UserID2 = ?) AND UserID NOT IN (SELECT UserID2 FROM Amigos WHERE UserID1 = ?) AND UserID NOT IN (SELECT UserID1 FROM Amigos WHERE UserID2 = ? AND EstadoAmistad = 'pendiente') AND UserID NOT IN (SELECT UserID2 FROM Amigos WHERE UserID1 = ? AND EstadoAmistad = 'pendiente') AND UserID != ?;";

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

}
