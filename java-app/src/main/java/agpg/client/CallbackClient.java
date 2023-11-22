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
            CallbackClientInterface callbackObj = new CallbackClientImpl();
            h.registerCallback(callbackObj, name);
            System.out.println("'EXIT' PARA SALIR O 'CHAT' PARA CHATEAR");
            while (true) {
                String input = scanner.nextLine();
                if (input.equals("EXIT")) {
                    break;
                } else if (input.equals("CHAT")) {
                    System.out.println("Nombre de usuario receptor:");
                    String receiver = scanner.nextLine();
                    System.out.println("Mensaje a enviar:");
                    String message = scanner.nextLine();
                    callbackObj.sendMessage(receiver, message);
                } else {
                    System.out.println("Opcion no valida");
                }
            }
            h.unregisterCallback(callbackObj, name);
            scanner.close();
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Excepcion en el main de CallbackClient: " + e);
        }
    }

}
