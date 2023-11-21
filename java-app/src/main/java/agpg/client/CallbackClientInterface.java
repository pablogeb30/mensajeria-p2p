package agpg.client;

// Importamos las librerias necesarias
import java.rmi.*;

// Interfaz del cliente
public interface CallbackClientInterface extends Remote {

    // Metodo ejecutado por el servidor para notificar al cliente
    public String notifyMe(String message) throws RemoteException;

}
