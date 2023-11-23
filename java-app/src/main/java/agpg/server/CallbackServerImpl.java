package agpg.server;

// Importamos los paquetes y librerias necesarias
import agpg.client.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.HashMap;

// Implementacion de la interfaz del servidor
public class CallbackServerImpl extends UnicastRemoteObject implements CallbackServerInterface {

    // Mapa de clientes registrados
    private HashMap<String, CallbackClientInterface> clientMap;

    // Constructor de la clase
    public CallbackServerImpl() throws RemoteException {
        super();
        clientMap = new HashMap<>();
    }

    // Este metodo registra a un cliente para que reciba callbacks
    public synchronized boolean registerCallback(CallbackClientInterface cObject) throws RemoteException {
        if (!(clientMap.containsKey(cObject.getName()))) {
            cObject.setFriends(clientMap);
            clientMap.put(cObject.getName(), cObject);
            updateClientsCallback(cObject);
            System.out.println("Nuevo usuario conectado: " + cObject.getName());
            return true;
        } else {
            System.out.println("Usuario ya conectado: " + cObject.getName());
            return false;
        }
    }

    // Este metodo cancela el registro de un cliente para que no reciba callbacks
    public synchronized void unregisterCallback(CallbackClientInterface cObject) throws RemoteException {
        clientMap.remove(cObject.getName());
        updateClientsCallback(cObject);
        System.out.println("Usuario desconectado: " + cObject.getName());
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
