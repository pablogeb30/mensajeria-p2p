package agpg.client;

// Importamos los paquetes y librerias necesarias
import agpg.server.*;
import java.rmi.*;

// Clase principal del cliente
public class CallbackClient {

    // Metodo principal
    public static void main(String args[]) {
        try {
            if (args.length != 2) {
                System.out.println("Uso: java CallbackClient <hostRMI> <puertoRMI>");
                System.exit(1);
            }
            String hostName = args[0];
            int RMIPort = Integer.parseInt(args[1]);
            String registryURL = "rmi://" + hostName + ":" + RMIPort + "/callback";
            CallbackServerInterface h = (CallbackServerInterface) Naming.lookup(registryURL);
            CallbackClientInterface callbackObj = new CallbackClientImpl();
            h.registerForCallback(callbackObj);
            System.out.println("Registrado para callback");
            while (h.isRegistered(callbackObj)) {
                // Tareas del cliente
            }
            h.unregisterForCallback(callbackObj);
            System.out.println("Cancelado registro para callback");
        } catch (Exception e) {
            System.out.println("Excepcion en el main de CallbackClient: " + e);
        }
    }

}
