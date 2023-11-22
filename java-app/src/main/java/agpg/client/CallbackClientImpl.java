package agpg.client;

// Importamos las librerias necesarias
import java.rmi.*;
import java.rmi.server.*;
import java.util.HashMap;

// Implementacion de la interfaz del cliente
public class CallbackClientImpl extends UnicastRemoteObject implements CallbackClientInterface {

    // Nombre del cliente
    private String name;

    // Mapa de clientes registrados
    private HashMap<String, CallbackClientInterface> clientMap = new HashMap<>();

    // Constructor de la clase
    public CallbackClientImpl(String name) throws RemoteException {
        super();
        clientMap = new HashMap<>();
        this.name = name;
    }

    // Metodo ejecutado por un cliente para notificar al otro
    public void notifyMe(String message) throws RemoteException {
        System.out.println(message);
    }

    // Metodo ejecutado por el servidor para actualizar el mapa de clientes
    public void updateMyClients(String name, CallbackClientInterface cObject) throws RemoteException {
        if (!(clientMap.containsKey(name))) {
            clientMap.put(name, cObject);
        } else {
            clientMap.remove(name);
        }
    }

    // Metodo ejecutado por un cliente para enviar un mensaje a otro cliente
    public void sendMessage(CallbackClientInterface cObject, String message) throws RemoteException {
        if (!clientMap.containsValue(cObject)) {
            System.out.println("No existe el usuario " + cObject.getName());
        } else {
            clientMap.get(cObject.getName()).notifyMe(message);
        }
    }

    // Getter del nombre del cliente
    public String getName() throws RemoteException {
        return name;
    }

    // Getter del mapa de clientes
    public HashMap<String, CallbackClientInterface> getClientMap() throws RemoteException {
        return clientMap;
    }

}
