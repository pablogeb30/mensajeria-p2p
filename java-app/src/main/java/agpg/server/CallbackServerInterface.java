package agpg.server;

// Importamos las librerias necesarias (RMI y CallbackClientInterface)
import java.rmi.Remote;
import java.rmi.RemoteException;
import agpg.client.CallbackClientInterface;

// Interfaz del servidor
public interface CallbackServerInterface extends Remote {

    // Metodo que registra a un cliente para que reciba callbacks
    public void registerCallback(CallbackClientInterface cObject) throws RemoteException;

    // Metodo que cancela el registro de un cliente para que no reciba callbacks
    public void unregisterCallback(CallbackClientInterface cObject) throws RemoteException;

    // Metodo para iniciar sesion
    public boolean iniciarSesion(String username, String password) throws RemoteException;

    // Metodo para registrar un nuevo cliente
    public boolean registrarCliente(String username, String password) throws RemoteException;

    // Metodo para enviar solicitudes de amistad
    public void enviarSolicitudAmistad(int userID, int friendID) throws RemoteException;

    // Metodo para aceptar solicitudes de amistad
    public void aceptarSolicitudAmistad(int userID, int friendID) throws RemoteException;

    // Metodo para rechazar solicitudes de amistad
    public void rechazarSolicitudAmistad(int userID, int friendID) throws RemoteException;

}
