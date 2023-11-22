package agpg.client;

// Importamos las librerias necesarias
import java.rmi.*;

// Interfaz del cliente
public interface CallbackClientInterface extends Remote {

    // Metodo ejecutado por un cliente para notificar a otro
    public void notifyMe(String message) throws RemoteException;

    // Metodo ejecutado por el servidor para actualizar el mapa de clientes
    public void updateMyClients(CallbackClientInterface cObject, String name) throws RemoteException;

    // Metodo ejecutado por un cliente para enviar un mensaje a otro cliente
    public void sendMessage(String name, String message) throws RemoteException;

}
