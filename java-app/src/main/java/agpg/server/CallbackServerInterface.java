package agpg.server;

// Importamos los paquetes y librerias necesarias
import java.rmi.Remote;
import agpg.client.CallbackClientInterface;
import java.rmi.RemoteException;
import java.util.List;

// Interfaz del servidor
public interface CallbackServerInterface extends Remote {

    // Metodo que registra a un cliente para que reciba callbacks
    public void registerCallback(CallbackClientInterface cObject) throws RemoteException;

    // Metodo que cancela el registro de un cliente para que no reciba callbacks
    public void unregisterCallback(CallbackClientInterface cObject) throws RemoteException;

    public boolean iniciarSesion(String username, String password, CallbackClientInterface cObject) throws RemoteException;

    public boolean registrarCliente(String username, String password) throws RemoteException;

    public List<String> obtenerUsuariosRecomendados(String username) throws RemoteException;

    public void enviarSolicitudAmistad(String userName, String friendName) throws RemoteException;

    public void aceptarSolicitudAmistad(String userName, String friendName) throws RemoteException;

    public void rechazarSolicitudAmistad(String userName, String friendName) throws RemoteException;

    public List<String> obtenerSolicitudesAmistad(String userName) throws RemoteException;

    public List<String> obtenerAmigos(String userName) throws RemoteException;

    public boolean cambiarPassword(String username, String password, String newPassword) throws RemoteException;

    



}
