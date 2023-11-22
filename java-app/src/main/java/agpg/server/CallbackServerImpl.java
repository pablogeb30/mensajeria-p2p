package agpg.server;

// Importamos los paquetes y librerias necesarias
import agpg.client.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.HashMap;

// Implementacion de la interfaz del servidor
public class CallbackServerImpl extends UnicastRemoteObject implements CallbackServerInterface {

    // Mapa de clientes registrados
    private HashMap<CallbackClientInterface, String> clientMap;

    // Constructor de la clase
    public CallbackServerImpl() throws RemoteException {
        super();
        clientMap = new HashMap<>();
    }

    // Este metodo registra a un cliente para que reciba callbacks
    public synchronized void registerCallback(CallbackClientInterface cObject, String name) throws RemoteException {
        if (!(clientMap.containsKey(cObject))) {
            clientMap.put(cObject, name);
            updateClientsCallback(cObject, name);
            System.out.println("Nuevo cliente registrado");
        }
    }

    // Este metodo cancela el registro de un cliente para que no reciba callbacks
    public synchronized void unregisterCallback(CallbackClientInterface cObject, String name) throws RemoteException {
        if (clientMap.containsKey(cObject)) {
            clientMap.remove(cObject);
            updateClientsCallback(cObject, name);
            System.out.println("Cancelado registro de un cliente");
        } else {
            System.out.println("Cliente no registrado anteriormente");
        }
    }

    // Este metodo actualiza el mapa de clientes que tiene cada objeto cliente
    private void updateClientsCallback(CallbackClientInterface cObject, String name) {
        try {
            for (CallbackClientInterface client : clientMap.keySet()) {
                client.updateMyClients(cObject, name);
            }
        } catch (RemoteException e) {
            System.out.println("Excepcion en updateClientsCallback: " + e);
        }

    }

}
