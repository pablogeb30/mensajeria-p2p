package agpg.client;

// Importamos las librerias necesarias
import java.rmi.*;
import java.rmi.server.*;

// Implementacion de la interfaz del cliente
public class CallbackClientImpl extends UnicastRemoteObject implements CallbackClientInterface {

    // Constructor de la clase
    public CallbackClientImpl() throws RemoteException {
        super();
    }

    // Metodo ejecutado por un cliente para notificar al otro
    public String notifyMe(String message) {
        System.out.println(message);
        return message;
    }

}
