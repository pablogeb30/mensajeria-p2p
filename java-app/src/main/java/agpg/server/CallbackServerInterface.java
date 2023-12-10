package agpg.server;

// Importamos las librerias necesarias (RMI y CallbackClientInterface)
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import agpg.client.CallbackClientInterface;

// Interfaz del servidor
public interface CallbackServerInterface extends Remote {

        // Metodo para iniciar sesion
        public boolean iniciarSesion(String username, String password)
                        throws RemoteException;

        // Metodo para registrar un nuevo cliente
        public boolean registrarCliente(String username, String password, String correo)
                        throws RemoteException;

        // Metodo para cambiar la contraseña
        public boolean cambiarPassword(String username, String password, String newPassword) throws RemoteException;

        // Metodo para cerrar sesion
        public void cerrarSesion(String username) throws RemoteException;

        // Metodo que registra a un cliente para que reciba callbacks
        public void registerCallback(CallbackClientInterface cObject) throws RemoteException;

        // Metodo que cancela el registro de un cliente para que no reciba callbacks
        public void unregisterCallback(CallbackClientInterface cObject) throws RemoteException;

        // Metodo para obtener una lista de usuarios recomendados
        public List<String> obtenerUsuariosRecomendados(String username) throws RemoteException;

        // Metodo para enviar una solicitud de amistad
        public void enviarSolicitudAmistad(String userName, String friendName) throws RemoteException;

        // Metodo para aceptar una solicitud de amistad
        public void aceptarSolicitudAmistad(String userName, String friendName) throws RemoteException;
        // Metodo para registrar un nuevo cliente

        // Metodo para rechazar una solicitud de amistad
        public void rechazarSolicitudAmistad(String userName, String friendName) throws RemoteException;

        // Metodo para obtener una lista de las solicitudes de amistad pendientes
        public List<String> obtenerSolicitudesAmistad(String userName) throws RemoteException;

        // Metodo para obtener una lista de los amigos
        public List<String> obtenerAmigos(String userName) throws RemoteException;

        // Metodo para comprobar si un usuario esta conectado
        public boolean estaConectado(String userName) throws RemoteException;

}
