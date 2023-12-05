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

            // Comprobamos que el numero de argumentos sea correcto
            if (args.length != 1) {
                System.out.println("Uso: java CallbackClient <serverHost>");
                System.exit(1);
            }
            // Obtenemos la referencia al objeto servidor
            String registryURL = "rmi://" + args[0] + ":1099/callback";
            CallbackServerInterface h = (CallbackServerInterface) Naming.lookup(registryURL);

            // Pedimos el nombre de usuario
            Scanner scanner = new Scanner(System.in);
            System.out.println("Introduce tu nombre de usuario:");
            String username = scanner.nextLine();
            scanner.close();

            // Comprobamos que el nombre de usuario no este ya en uso
            if (h.getClientMap().keySet().contains(username)) {
                System.out.println("Usuario ya conectado: " + username);
                System.exit(1);
            }

            // Creamos el objeto cliente y lo registramos en el servidor
            CallbackClientInterface callbackObj = new CallbackClientImpl(username);
            h.registerCallback(callbackObj);
            System.out.println("Cliente listo (CTRL-C para salir)");

            // Agregamos un shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    h.unregisterCallback(callbackObj);
                } catch (RemoteException e) {
                    System.out.println("Excepcion en el shutdown hook: " + e.getMessage());
                }
            }));

        } catch (Exception e) {
            System.out.println("Excepcion en el main de CallbackClient: " + e.getMessage());
        }

    }

}
