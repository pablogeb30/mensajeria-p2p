package agpg.server;

// Importamos los paquetes y librerias necesarias
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import agpg.client.CallbackClientInterface;
import java.rmi.RemoteException;

// Implementacion de la interfaz del servidor
public class CallbackServerImpl extends UnicastRemoteObject implements CallbackServerInterface {

    // Mapa de clientes registrados
    private HashMap<String, CallbackClientInterface> clientMap;

    // Constructor de la clase
    public CallbackServerImpl() throws RemoteException {
        super();
        clientMap = new HashMap<>();
    }

    // Getter del mapa de clientes
    public HashMap<String, CallbackClientInterface> getClientMap() throws RemoteException {
        return clientMap;
    }

    // Este metodo registra a un cliente para que reciba callbacks
    public synchronized void registerCallback(CallbackClientInterface cObject) throws RemoteException {
        if (!(clientMap.containsKey(cObject.getUsername()))) {
            cObject.setFriends(clientMap);
            clientMap.put(cObject.getUsername(), cObject);
            updateClientsCallback(cObject);
            System.out.println("Nuevo usuario conectado: " + cObject.getUsername());
        } else {
            System.out.println("Usuario ya conectado: " + cObject.getUsername());
        }
    }

    // Este metodo cancela el registro de un cliente para que no reciba callbacks
    public synchronized void unregisterCallback(CallbackClientInterface cObject) throws RemoteException {
        clientMap.remove(cObject.getUsername());
        updateClientsCallback(cObject);
        System.out.println("Usuario desconectado: " + cObject.getUsername());
    }

    // Este metodo actualiza el mapa de clientes que tiene cada objeto cliente
    private void updateClientsCallback(CallbackClientInterface cObject) {
        try {
            for (CallbackClientInterface client : clientMap.values()) {
                client.updateFriends(cObject);
            }
        } catch (RemoteException e) {
            System.out.println("Excepcion en updateClientsCallback: " + e);
        }

    }

}
