package agpg.client;

// Importamos las librerias necesarias
import java.rmi.Remote;
import java.rmi.RemoteException;

// Interfaz del cliente
public interface CallbackClientInterface extends Remote {

    // Getter del nombre del cliente
    public String getUsername() throws RemoteException;

    // Metodo ejecutado por un cliente para enviar un mensaje a otro cliente
    public void sendMessage(String message) throws RemoteException;

    // Metodo ejecutado por un cliente para notificar a otro
    public void notifyMe(String username, String message) throws RemoteException;

    public void notifyEvent(String message) throws RemoteException;

}
