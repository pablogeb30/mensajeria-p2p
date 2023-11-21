package agpg.server;

// Importamos los paquetes y librerias necesarias
import agpg.client.*;
import java.rmi.*;

// Interfaz del servidor
public interface CallbackServerInterface extends Remote {

    // Metodo que registra a un cliente para que reciba callbacks
    public void registerForCallback(CallbackClientInterface clientObject) throws RemoteException;

    // Metodo que cancela el registro de un cliente para que no reciba callbacks
    public void unregisterForCallback(CallbackClientInterface clientObject) throws RemoteException;

    // Metodo que verifica si un cliente esta registrado
    public boolean isRegistered(CallbackClientInterface clientObject) throws RemoteException;

}
