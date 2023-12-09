package agpg.client;

// Importamos los paquetes y librerias necesarias
import agpg.GUI.CallbackClientGUI;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.rmi.RemoteException;

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
