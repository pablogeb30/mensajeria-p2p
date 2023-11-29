package agpg.client;

// Importamos las librerias necesarias
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

// Interfaz del cliente
public interface CallbackClientInterface extends Remote {

    // Getter del nombre del cliente
    public String getUsername() throws RemoteException;

    // Metodo ejecutado por el servidor para inicializar el mapa de clientes
    public void setFriends(HashMap<String, CallbackClientInterface> clientMap) throws RemoteException;

    // Metodo ejecutado por el servidor para actualizar el mapa de clientes
    public void updateFriends(CallbackClientInterface cObject) throws RemoteException;

    // Metodo ejecutado por un cliente para enviar un mensaje a otro cliente
    public void sendMessage(String message) throws RemoteException;

    // Metodo ejecutado por un cliente para notificar a otro
    public void notifyMe(String username, String message) throws RemoteException;

}
