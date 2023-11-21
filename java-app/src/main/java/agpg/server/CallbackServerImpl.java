package agpg.server;

// Importamos los paquetes y librerias necesarias
import agpg.client.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;

// Implementacion de la interfaz del servidor
public class CallbackServerImpl extends UnicastRemoteObject implements CallbackServerInterface {

    // Lista de clientes registrados
    private ArrayList<CallbackClientInterface> clientList;

    // Constructor de la clase
    public CallbackServerImpl() throws RemoteException {
        super();
        clientList = new ArrayList<>();
    }

    // Este metodo registra a un cliente para que reciba callbacks
    public synchronized void registerForCallback(CallbackClientInterface clientObject) throws RemoteException {
        if (!(clientList.contains(clientObject))) {
            clientList.add(clientObject);
            System.out.println("Nuevo cliente registrado");
            doCallbacks();
        }
    }

    // Este metodo cancela el registro de un cliente para que no reciba callbacks
    public synchronized void unregisterForCallback(CallbackClientInterface clientObject) throws RemoteException {
        if (clientList.remove(clientObject)) {
            System.out.println("Cancelado registro de un cliente");
        } else {
            System.out.println("Cliente no registrado anteriormente");
        }
    }

    // Este metodo realiza los callbacks a los clientes registrados
    private synchronized void doCallbacks() throws RemoteException {
        for (int i = 0; i < clientList.size(); i++) {
            clientList.get(i).notifyMe("Hola");
        }
    }

    // Este metodo verifica si un cliente esta registrado
    public synchronized boolean isRegistered(CallbackClientInterface clientObject) throws RemoteException {
        return clientList.contains(clientObject);
    }

}
