package agpg.client;

// Importamos los paquetes y librerias necesarias
import agpg.server.CallbackServerInterface;
import java.rmi.Naming;
import java.util.Scanner;
import java.rmi.RemoteException;

// Clase principal del cliente
public class CallbackClient {

    // Metodo principal
    public static void main(String args[]) {
        try {
            if (args.length != 1) {
                System.out.println("Uso: java CallbackClient <serverHost>");
                System.exit(1);
            }
            String serverHost = args[0];
            String registryURL = "rmi://" + serverHost + ":1099/callback";
            CallbackServerInterface h = (CallbackServerInterface) Naming.lookup(registryURL);
            Scanner scanner = new Scanner(System.in);
            System.out.println("Introduce tu nombre de usuario:");
            String username = scanner.nextLine();
            if (h.getClientMap().keySet().contains(username)) {
                System.out.println("Usuario ya conectado: " + username);
                System.exit(1);
            }
            CallbackClientInterface callbackObj = new CallbackClientImpl(username);
            h.registerCallback(callbackObj);
            // Agregar un shutdown hook (es una opcion, lo podemos cambiar)
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    h.unregisterCallback(callbackObj);
                } catch (RemoteException e) {
                    System.out.println("Excepcion en el shutdown hook: " + e);
                }
            }));
            // System.out.println("----- CHAT INICIADO -----");
            while (true) {
                System.out.println("Cliente listo (EXIT para salir)");
                String input = scanner.nextLine();
                if (input.equals("EXIT")) {
                    break;
                }
            }
            h.unregisterCallback(callbackObj);
            scanner.close();
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Excepcion en el main de CallbackClient: " + e);
        }
    }

}
