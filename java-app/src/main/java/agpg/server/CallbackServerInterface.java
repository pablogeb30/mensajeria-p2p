package agpg.server;

// Importamos los paquetes y librerias necesarias
import java.rmi.Remote;
import agpg.client.CallbackClientInterface;
import java.rmi.RemoteException;
import java.util.List;

// Interfaz del servidor
public interface CallbackServerInterface extends Remote {
    
    // Metodo para iniciar sesion
    public boolean iniciarSesion(String username, String password, CallbackClientInterface cObject) throws RemoteException;

    // Metodo para registrar un nuevo cliente
    public boolean registrarCliente(String username, String password, String correo, CallbackClientInterface cObject) throws RemoteException;

    // Metodo para cambiar la contraseña
    public boolean cambiarPassword(String username, String password, String newPassword) throws RemoteException;
    
    // Metodo para cerrar sesion
    public void cerrarSesion(String username) throws RemoteException;

    // Metodo para obtener una lista de usuarios recomendados
    public List<String> obtenerUsuariosRecomendados(String username) throws RemoteException;

    // Metodo para enviar una solicitud de amistad
    public void enviarSolicitudAmistad(String userName, String friendName) throws RemoteException;

    // Metodo para aceptar una solicitud de amistad
    public void aceptarSolicitudAmistad(String userName, String friendName) throws RemoteException;

    // Metodo para rechazar una solicitud de amistad
    public void rechazarSolicitudAmistad(String userName, String friendName) throws RemoteException;

    // Metodo para obtener una lista de las solicitudes de amistad pendientes
    public List<String> obtenerSolicitudesAmistad(String userName) throws RemoteException;

    // Metodo para obtener una lista de los amigos
    public List<String> obtenerAmigos(String userName) throws RemoteException;

    // Metodo para comprobar si un usuario esta conectado
    public boolean estaConectado(String userName) throws RemoteException;

}
