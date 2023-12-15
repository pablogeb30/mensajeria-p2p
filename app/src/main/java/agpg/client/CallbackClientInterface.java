package agpg.client;

// Importamos las librerias necesarias (RMI y HashMap)
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.concurrent.ConcurrentHashMap;


// Interfaz del cliente
public interface CallbackClientInterface extends Remote {

    // Getter del nombre del cliente
    public String getUsername() throws RemoteException;

    // Metodo ejecutado por el servidor para inicializar el mapa de clientes
    public void setClients(ConcurrentHashMap<String, CallbackClientInterface> clients, ConcurrentHashMap<String, PublicKey> keys) throws RemoteException;

    // Metodo ejecutado por el servidor para actualizar el mapa de clientes
    public void addClient(CallbackClientInterface client, PublicKey key) throws RemoteException;

    // Metodo ejecutado por un cliente para mandar un mensaje a otro cliente
    public void sendMessage(String username, String message) throws RemoteException;

    // Metodo ejecutado por el cliente al que mandan el mensaje
    public void notifyMe(String username, String message) throws RemoteException;

    //Clave
    public PublicKey registroConServidor() throws Exception;


}
