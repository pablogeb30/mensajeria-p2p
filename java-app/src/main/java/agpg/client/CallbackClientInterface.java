package agpg.client;

// Importamos las librerias necesarias
import java.rmi.*;
import java.util.HashMap;

// Interfaz del cliente
public interface CallbackClientInterface extends Remote {

    // Metodo ejecutado por un cliente para notificar a otro
    public void notifyMe(String message) throws RemoteException;

    // Metodo ejecutado por el servidor para actualizar el mapa de clientes
    public void updateMyClients(String name, CallbackClientInterface cObject) throws RemoteException;

    // Metodo ejecutado por un cliente para enviar un mensaje a otro cliente
    public void sendMessage(CallbackClientInterface cObject, String message) throws RemoteException;

    // Getter del nombre del cliente
    public String getName() throws RemoteException;

    // Getter del mapa de clientes
    public HashMap<String, CallbackClientInterface> getClientMap() throws RemoteException;

}
