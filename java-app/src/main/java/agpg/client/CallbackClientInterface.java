package agpg.client;

// Importamos las librerias necesarias (RMI y HashMap)
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

// Interfaz del cliente
public interface CallbackClientInterface extends Remote {

    // Getter del nombre del cliente
    public String getUsername() throws RemoteException;

    // Getter del mapa de clientes
    public HashMap<String, CallbackClientInterface> getClientMap() throws RemoteException;

    // Metodo ejecutado por el servidor para inicializar el mapa de clientes
    public void setFriends(HashMap<String, CallbackClientInterface> clientMap) throws RemoteException;

    // Metodo ejecutado por el servidor para actualizar el mapa de clientes
    public void updateFriends(CallbackClientInterface cObject) throws RemoteException;

    // Metodo ejecutado por un cliente para mandar un mensaje a otro cliente
    public void sendMessage(String username, String message) throws RemoteException;

    // Metodo ejecutado por el cliente al que mandan el mensaje
    public void notifyMe(String username, String message) throws RemoteException;

}
