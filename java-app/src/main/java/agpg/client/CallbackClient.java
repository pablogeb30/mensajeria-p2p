package agpg.client;

import agpg.server.CallbackServerInterface;
import java.rmi.Naming;
import java.util.Scanner;
import java.rmi.RemoteException;

public class CallbackClient {

    public static void main(String args[]) {

        try {

            // Comprobamos que el numero de argumentos sea correcto
            if (args.length != 1) {
                System.out.println("Uso: java CallbackClient <serverHost>");
                System.exit(1);
            }
            String serverHost = args[0];
            String registryURL = "rmi://" + serverHost + ":1099/callback";
            CallbackServerInterface server = (CallbackServerInterface) Naming.lookup(registryURL);
            Scanner scanner = new Scanner(System.in);

            System.out.println("¿Eres un usuario nuevo? (sí/no)");
            String respuesta = scanner.nextLine().trim().toLowerCase();
            String username;
            boolean registrado = false;

            if ("sí".equals(respuesta) || "si".equals(respuesta)) {
                // Registro de nuevo usuario
                System.out.println("Registrar nuevo usuario.");
                System.out.println("Introduce tu nombre de usuario:");
                username = scanner.nextLine();
                System.out.println("Introduce tu contraseña:");
                String password = scanner.nextLine();
                
                registrado = server.registrarCliente(username, password);
            } else {
                // Inicio de sesión
                System.out.println("Iniciar sesión.");
                System.out.println("Introduce tu nombre de usuario:");
                username = scanner.nextLine();
                System.out.println("Introduce tu contraseña:");
                String password = scanner.nextLine();

                registrado = server.iniciarSesion(username, password, new CallbackClientImpl(username));
            }

            if (!registrado) {
                System.out.println("No se pudo registrar o iniciar sesión.");
                System.exit(1);
            }

            CallbackClientInterface callbackObj = new CallbackClientImpl(username);
            server.registerCallback(callbackObj);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    server.unregisterCallback(callbackObj);
                } catch (RemoteException e) {
                    System.out.println("Excepción en el shutdown hook: " + e);
                }
            }));

            while (true) {
                System.out.println("Cliente listo (EXIT para salir, FRIENDS para ver amigos)");
                String input = scanner.nextLine();
                if ("EXIT".equalsIgnoreCase(input)) {
                    break;
                } else if ("FRIENDS".equalsIgnoreCase(input)) {

                    // Obtner lista de pososibles amigos
                    System.out.println("Lista de posibles amigos:");
                    System.out.println(server.obtenerUsuariosRecomendados(username));

                    // Preguntamos si desea mandar una solicitud de amistad a alguno de ellos
                    System.out.println("¿Desea mandar una solicitud de amistad? (sí/no)");
                    respuesta = scanner.nextLine().trim().toLowerCase();

                    if ("sí".equals(respuesta) || "si".equals(respuesta)) {
                        // Mandar solicitud de amistad
                        System.out.println("Introduce el nombre del usuario al que desea mandar la solicitud de amistad:");
                        int friendID = scanner.nextInt();
                        server.enviarSolicitudAmistad(username, friendID);
                    }
                    
                }
            }

            server.unregisterCallback(callbackObj);
            scanner.close();
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Excepción en el main de CallbackClient: " + e);
        }

    }
}
