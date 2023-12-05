package agpg.server;

import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import agpg.client.CallbackClientInterface;
import java.rmi.RemoteException;
import org.mindrot.jbcrypt.BCrypt;


public class CallbackServerImpl extends UnicastRemoteObject implements CallbackServerInterface {

    private HashMap<String, CallbackClientInterface> clientMap;
    private String dbURL = "jdbc:postgresql://localhost:5432/usuariosChat";
    private String dbUsername = "postgres";
    private String dbPassword = "myPassword";

    public CallbackServerImpl() throws RemoteException {
        super();
        clientMap = new HashMap<>();
        // Inicializar la conexión a la base de datos, si es necesario
    }

    private Connection conectarBD() throws SQLException {
        return DriverManager.getConnection(dbURL, dbUsername, dbPassword);
    }

    public synchronized void registerCallback(CallbackClientInterface cObject) throws RemoteException {
        if (!(clientMap.containsKey(cObject.getUsername()))) {
            // Aquí, en lugar de solo añadir el cliente al mapa, también podrías verificar
            // en la base de datos
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

    // Método para iniciar sesión
    public boolean iniciarSesion(String username, String password, CallbackClientInterface cObject) throws RemoteException {
        // Conectar a la base de datos
        try (Connection conn = conectarBD()) {
            // Verificar si el usuario existe
            if (!usuarioYaExiste(username, conn)) {
                return false; // Usuario no existe
            }

            // Verificar si la contraseña es correcta
            String sql = "SELECT Password FROM Usuarios WHERE Username = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String hashedPassword = rs.getString(1);
                        if (BCrypt.checkpw(password, hashedPassword)) {
                            // Contraseña correcta
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

    // Método para registrar un nuevo cliente
    public boolean registrarCliente(String username, String password) throws RemoteException {
        // Conectar a la base de datos
        try (Connection conn = conectarBD()) {
            // Verificar si el usuario ya existe
            if (usuarioYaExiste(username, conn)) {
                return false; // Usuario ya existe
            }

            // Insertar el nuevo usuario en la base de datos
            String sql = "INSERT INTO Usuarios (Username, Password) VALUES (?, ?);";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, hashPassword(password)); // Asegúrate de cifrar o hashear la contraseña
                pstmt.executeUpdate();
                return true; // Usuario registrado con éxito
            }
        } catch (SQLException e) {
            System.err.println("Error al registrar el cliente: " + e.getMessage());
            throw new RemoteException("Error al registrar el cliente", e);
        }
    }

    // Método auxiliar para verificar si un usuario ya existe
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

    // Método para enviar solicitud de amistad
    public void enviarSolicitudAmistad(int userID, int friendID) throws RemoteException {
        String sql = "INSERT INTO Amigos (UserID1, UserID2, EstadoAmistad) VALUES (?, ?, 'pendiente');";
        try (Connection conn = conectarBD();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            pstmt.setInt(2, friendID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al enviar solicitud de amistad: " + e.getMessage());
            throw new RemoteException("Error al enviar solicitud de amistad", e);
        }
    }

    // Método para aceptar solicitud de amistad
    public void aceptarSolicitudAmistad(int userID, int friendID) throws RemoteException {
        String sql = "UPDATE Amigos SET EstadoAmistad = 'aceptada' WHERE UserID1 = ? AND UserID2 = ?;";
        try (Connection conn = conectarBD();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, friendID); // Invertir el orden ya que el amigo fue el solicitante
            pstmt.setInt(2, userID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al aceptar solicitud de amistad: " + e.getMessage());
            throw new RemoteException("Error al aceptar solicitud de amistad", e);
        }
    }

    // Método para rechazar solicitud de amistad
    public void rechazarSolicitudAmistad(int userID, int friendID) throws RemoteException {
        String sql = "UPDATE Amigos SET EstadoAmistad = 'rechazada' WHERE UserID1 = ? AND UserID2 = ?;";
        try (Connection conn = conectarBD();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, friendID); // Invertir el orden ya que el amigo fue el solicitante
            pstmt.setInt(2, userID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al rechazar solicitud de amistad: " + e.getMessage());
            throw new RemoteException("Error al rechazar solicitud de amistad", e);
        }
    }

    /*
     * private void updateClientsCallback(CallbackClientInterface cObject) {
     * try {
     * for (CallbackClientInterface client : clientMap.values()) {
     * client.updateFriends(cObject);
     * }
     * } catch (RemoteException e) {
     * System.out.println("Excepcion en updateClientsCallback: " + e);
     * }
     * 
     * }
     */

}
