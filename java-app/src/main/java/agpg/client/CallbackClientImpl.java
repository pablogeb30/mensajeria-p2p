package agpg.client;

// Importamos las librerias necesarias (GUI, RMI y HashMap)
import agpg.GUI.CallbackClientGUI;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.HashMap;

// Implementacion de la interfaz del cliente
public class CallbackClientImpl extends UnicastRemoteObject implements CallbackClientInterface {

    // Nombre del cliente
    private String username;

    // Mapa de clientes registrados
    private HashMap<String, CallbackClientInterface> clientMap = new HashMap<>();

    // GUI del cliente
    private CallbackClientGUI gui;

    // Constructor de la clase
    public CallbackClientImpl(String username) throws RemoteException {
        super();
        this.username = username;
        clientMap = new HashMap<>();
        // Creamos la interfaz grafica pasandole la referencia al objeto cliente
        gui = new CallbackClientGUI(this);
    }

    // Getter del nombre del cliente
    public String getUsername() throws RemoteException {
        return username;
    }

    // Getter del mapa de clientes
    public HashMap<String, CallbackClientInterface> getClientMap() throws RemoteException {
        return clientMap;
    }

    // Metodo ejecutado por el servidor para inicializar el mapa de clientes
    public void setFriends(HashMap<String, CallbackClientInterface> clientMap) throws RemoteException {
        for (CallbackClientInterface client : clientMap.values()) {
            this.clientMap.put(client.getUsername(), client);
            // Actualizamos la interfaz grafica anhadiendo los clientes
            gui.addClient(client.getUsername());
        }
    }

    // Metodo ejecutado por el servidor para actualizar el mapa de clientes
    public void updateFriends(CallbackClientInterface cObject) throws RemoteException {
        if (!(clientMap.containsKey(cObject.getUsername())) && !(cObject.getUsername().equals(this.getUsername()))) {
            clientMap.put(cObject.getUsername(), cObject);
            System.out.println("Nuevo usuario conectado: " + cObject.getUsername());
            // Actualizamos la interfaz grafica anhadiendo los clientes
            gui.addClient(cObject.getUsername());
        } else {
            if (!cObject.getUsername().equals(this.getUsername())) {
                clientMap.remove(cObject.getUsername());
                System.out.println("Usuario desconectado: " + cObject.getUsername());
                // Actualizamos la interfaz grafica eliminando los clientes
                gui.removeClient(cObject.getUsername());
            }
        }
    }

    // Metodo ejecutado por un cliente para enviar un mensaje a otro cliente
    public void sendMessage(String message) throws RemoteException {
        // Obtenemos el cliente seleccionado
        String selectedUser = gui.selectClient();
        // Comprobamos que se haya seleccionado un cliente
        if (selectedUser == null) {
            System.out.println("No se ha seleccionado ningun usuario");
            return;
        }
        // Llamamos al metodo notifyMe del cliente seleccionado
        clientMap.get(selectedUser).notifyMe(this.getUsername(), message);
    }

    // Metodo ejecutado por un cliente para notificar al otro
    public void notifyMe(String username, String message) throws RemoteException {
        // Actualizamos la interfaz grafica mostrando el mensaje
        gui.updateChat(username, message);
    }

}
