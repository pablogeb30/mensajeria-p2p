package agpg.client;

// Importamos las librerias necesarias
import java.rmi.*;

// Interfaz del cliente
public interface CallbackClientInterface extends Remote {

    // Metodo ejecutado por un cliente para notificar al otro
    public String notifyMe(String message) throws RemoteException;

}
