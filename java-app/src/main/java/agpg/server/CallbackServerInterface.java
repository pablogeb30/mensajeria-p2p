package agpg.server;

// Importamos los paquetes y librerias necesarias
import java.rmi.Remote;
import agpg.client.CallbackClientInterface;
import java.rmi.RemoteException;

// Interfaz del servidor
public interface CallbackServerInterface extends Remote {

    // Getter del mapa de clientes
    //public HashMap<String, CallbackClientInterface> getClientMap() throws RemoteException;

    // Metodo que registra a un cliente para que reciba callbacks
    public void registerCallback(CallbackClientInterface cObject) throws RemoteException;

    // Metodo que cancela el registro de un cliente para que no reciba callbacks
    public void unregisterCallback(CallbackClientInterface cObject) throws RemoteException;

    // Método para iniciar sesión
    public boolean iniciarSesion(String username, String password, CallbackClientInterface cObject) throws RemoteException;

    // Método para registrar un nuevo cliente
    public boolean registrarCliente(String username, String password) throws RemoteException;

    // Métodos para gestionar las solicitudes de amistad
    public void enviarSolicitudAmistad(int userID, int friendID) throws RemoteException;

    public void aceptarSolicitudAmistad(int userID, int friendID) throws RemoteException;

    public void rechazarSolicitudAmistad(int userID, int friendID) throws RemoteException;

    



}
