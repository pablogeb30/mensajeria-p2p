package agpg.server;

// Importamos los paquetes y librerias necesarias
import java.rmi.Remote;
import java.util.HashMap;
import agpg.client.CallbackClientInterface;
import java.rmi.RemoteException;

// Interfaz del servidor
public interface CallbackServerInterface extends Remote {

    // Getter del mapa de clientes
    public HashMap<String, CallbackClientInterface> getClientMap() throws RemoteException;

    // Metodo que registra a un cliente para que reciba callbacks
    public void registerCallback(CallbackClientInterface cObject) throws RemoteException;

    // Metodo que cancela el registro de un cliente para que no reciba callbacks
    public void unregisterCallback(CallbackClientInterface cObject) throws RemoteException;

}
