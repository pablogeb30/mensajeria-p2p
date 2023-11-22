package agpg.client;

// Importamos los paquetes y librerias necesarias
import agpg.server.*;
import java.rmi.*;
import java.util.Scanner;

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
            String name = scanner.nextLine();
            CallbackClientInterface callbackObj = new CallbackClientImpl(name);
            h.registerCallback(name, callbackObj);
            System.out.println("CHAT INICIADO --- 'EXIT' PARA SALIR");
            while (true) {
                System.out.println("Mensaje a enviar:");
                String input = scanner.nextLine();
                if (input.equals("EXIT")) {
                    break;
                }
                System.out.println("Nombre de usuario receptor:");
                String receiver = scanner.nextLine();
                callbackObj.sendMessage(callbackObj.getClientMap().get(receiver), input);
            }
            h.unregisterCallback(name, callbackObj);
            scanner.close();
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Excepcion en el main de CallbackClient: " + e);
        }
    }

}
