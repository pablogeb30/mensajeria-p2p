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
    public synchronized void registerCallback(String name, CallbackClientInterface cObject) throws RemoteException {
        if (!(clientMap.containsKey(name))) {
            clientMap.put(name, cObject);
            updateClientsCallback(name, cObject);
            System.out.println("Nuevo cliente registrado");
        } else {
            System.out.println("Ya existe un cliente con ese nombre");
        }
    }

    // Este metodo cancela el registro de un cliente para que no reciba callbacks
    public synchronized void unregisterCallback(String name, CallbackClientInterface cObject) throws RemoteException {
        if (clientMap.containsValue(cObject)) {
            clientMap.remove(name);
            updateClientsCallback(name, cObject);
            System.out.println("Cancelado registro de un cliente");
        } else {
            System.out.println("Cliente no registrado anteriormente");
        }
    }

    // Este metodo actualiza el mapa de clientes que tiene cada objeto cliente
    private void updateClientsCallback(String name, CallbackClientInterface cObject) {
        try {
            for (CallbackClientInterface client : clientMap.values()) {
                client.updateMyClients(name, cObject);
            }
        } catch (RemoteException e) {
            System.out.println("Excepcion en updateClientsCallback: " + e);
        }

    }

}
