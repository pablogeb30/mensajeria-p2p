package agpg.server;

// Importamos las librerias necesarias (RMI, SQL, HashMap, CallbackClientInterface y BCrypt)
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.HashMap;
import agpg.client.CallbackClientInterface;
import org.mindrot.jbcrypt.BCrypt;

// Implementacion de la interfaz del servidor
public class CallbackServerImpl extends UnicastRemoteObject implements CallbackServerInterface {

    // Mapa de clientes registrados y credenciales de la base de datos
    private HashMap<String, CallbackClientInterface> clientMap;
    private String dbURL = "jdbc:postgresql://localhost:5432/usuariosChat";
    private String dbUsername = "postgres";
    private String dbPassword = "myPassword";

    // Constructor de la clase
    public CallbackServerImpl() throws RemoteException {
        super();
        clientMap = new HashMap<>();
    }

    // Metodo de conexion a la base de datos
    private Connection conectarBD() throws SQLException {
        return DriverManager.getConnection(dbURL, dbUsername, dbPassword);
    }

    // Metodo que registra a un cliente para que reciba callbacks
    public synchronized void registerCallback(CallbackClientInterface cObject) throws RemoteException {
        if (!(clientMap.containsKey(cObject.getUsername()))) {
            // En vez de anhadir al mapa, revisar base de datos (consulta SQL)
            cObject.setFriends(clientMap);
            clientMap.put(cObject.getUsername(), cObject);
            updateClientsCallback(cObject);
            System.out.println("Nuevo usuario conectado: " + cObject.getUsername());
        } else {
            System.out.println("Usuario ya conectado: " + cObject.getUsername());
        }
    }

    // Metodo que cancela el registro de un cliente para que no reciba callbacks
    public synchronized void unregisterCallback(CallbackClientInterface cObject) throws RemoteException {
        if (clientMap.containsKey(cObject.getUsername())) {
            clientMap.remove(cObject.getUsername());
            updateClientsCallback(cObject);
            System.out.println("Usuario desconectado: " + cObject.getUsername());
        }
    }

    // Metodo para iniciar sesion
    public boolean iniciarSesion(String username, String password)
            throws RemoteException {
        // Conectar a la base de datos
        try (Connection conn = conectarBD()) {
            // Verificar si el usuario existe
            if (!usuarioYaExiste(username, conn)) {
                return false; // Usuario no existe
            }

            // Verificar si la contrasenha es correcta
            String sql = "SELECT Password FROM Usuarios WHERE Username = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String hashedPassword = rs.getString(1);
                        if (BCrypt.checkpw(password, hashedPassword)) {
                            // Contrasenha correcta
                            return true;
                        }
                    }
                }
            }
            return false; // Contrasenha incorrecta
        } catch (SQLException e) {
            System.err.println("Error al iniciar sesión: " + e.getMessage());
            throw new RemoteException("Error al iniciar sesión", e);
        }
    }

    // Metodo para registrar un nuevo cliente
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
                pstmt.setString(2, hashPassword(password)); // Asegurate de cifrar o hashear la contrasenha
                pstmt.executeUpdate();
                return true; // Usuario registrado con exito
            }
        } catch (SQLException e) {
            System.err.println("Error al registrar el cliente: " + e.getMessage());
            throw new RemoteException("Error al registrar el cliente", e);
        }
    }

    // Metodo auxiliar para verificar si un usuario ya existe
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

    // Metodo auxiliar para cifrar o hashear la contrasenha
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Metodo para enviar solicitud de amistad
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

    // Metodo para aceptar solicitud de amistad
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

    // Metodo para rechazar solicitudes de amistad
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

    // Metodo que llama al metodo remoto del cliente para actualizar los amigos
    private void updateClientsCallback(CallbackClientInterface cObject) {
        try {
            for (CallbackClientInterface client : clientMap.values()) {
                client.updateFriends(cObject);
            }
        } catch (RemoteException e) {
            System.out.println("Excepcion en updateClientsCallback: " + e);
        }
    }

}
