package agpg.client;

// Importamos las librerias necesarias
import java.rmi.*;
import java.rmi.server.*;
import java.util.HashMap;

// Implementacion de la interfaz del cliente
public class CallbackClientImpl extends UnicastRemoteObject implements CallbackClientInterface {

    // Mapa de clientes registrados
    private HashMap<CallbackClientInterface, String> clientMap = new HashMap<>();

    // Constructor de la clase
    public CallbackClientImpl() throws RemoteException {
        super();
        clientMap = new HashMap<>();
    }

    // Metodo ejecutado por un cliente para notificar al otro
    public void notifyMe(String message) throws RemoteException {
        System.out.println(message);
    }

    // Metodo ejecutado por el servidor para actualizar el mapa de clientes
    public void updateMyClients(CallbackClientInterface cObject, String name) throws RemoteException {
        if (!(clientMap.containsKey(cObject))) {
            clientMap.put(cObject, name);
        } else {
            clientMap.remove(cObject);
        }
    }

    // Metodo ejecutado por un cliente para enviar un mensaje a otro cliente
    public void sendMessage(String name, String message) throws RemoteException {
        for (CallbackClientInterface client : clientMap.keySet()) {
            if (clientMap.get(client).equals(name)) {
                client.notifyMe(message);
            }
        }
    }

}
