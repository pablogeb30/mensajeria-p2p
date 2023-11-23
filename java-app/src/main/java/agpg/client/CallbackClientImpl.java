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

    // Metodo ejecutado por el servidor para inicializar el mapa de clientes
    public void setFriends(HashMap<String, CallbackClientInterface> clientMap) throws RemoteException {
        System.out.println("Usuarios conectados (" + clientMap.size() + "):");
        System.out.println("--------------------------------------------------");
        for (CallbackClientInterface client : clientMap.values()) {
            this.clientMap.put(client.getName(), client);
            System.out.println(client.getName());
        }
        System.out.println("--------------------------------------------------");
    }

    // Metodo ejecutado por el servidor para actualizar el mapa de clientes
    public void updateFriends(CallbackClientInterface cObject) throws RemoteException {
        if (!(clientMap.containsKey(cObject.getName())) && !(cObject.getName().equals(this.getName()))) {
            clientMap.put(cObject.getName(), cObject);
            System.out.println("Nuevo usuario conectado: " + cObject.getName());
        } else {
            if (!cObject.getName().equals(this.getName())) {
                clientMap.remove(cObject.getName());
                System.out.println("Usuario desconectado: " + cObject.getName());
            }
        }
    }

    // Metodo ejecutado por un cliente para enviar un mensaje a otro cliente
    public void sendMessage(String name, String message) throws RemoteException {
        if (!clientMap.containsKey(name)) {
            System.out.println("No existe el usuario: " + name);
        } else {
            clientMap.get(name).notifyMe(message);
        }
    }

    // Getter del nombre del cliente
    public String getName() throws RemoteException {
        return name;
    }

}
