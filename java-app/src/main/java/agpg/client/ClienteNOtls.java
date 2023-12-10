package agpg.client;

import agpg.server.CallbackServerInterface;
import java.rmi.Naming;
import java.util.Scanner;
import java.rmi.RemoteException;

public class ClienteNOtls {

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
                System.out.println("Introduce tu correo electrónico:");
                String correo = scanner.nextLine();

                registrado = server.registrarCliente(username, password, correo);
            } else {
                // Inicio de sesión
                System.out.println("Iniciar sesión.");
                System.out.println("Introduce tu nombre de usuario:");
                username = scanner.nextLine();
                System.out.println("Introduce tu contraseña:");
                String password = scanner.nextLine();

                registrado = server.iniciarSesion(username, password);
            }

            if (!registrado) {
                System.out.println("No se pudo registrar o iniciar sesión.");
                System.exit(1);
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    server.cerrarSesion(username);
                } catch (RemoteException e) {
                    System.out.println("Excepción en el shutdown hook: " + e);
                }
            }));

            while (true) {
                System.out.println("¿Qué desea hacer? (sugerencias/amigos/solicitudes/cambiar contrasena/exit)");
                String input = scanner.nextLine().toUpperCase();
                switch (input) {
                    case "EXIT":
                        break;

                    case "CAMBIAR CONTRASENA":
                        System.out.println("Introduce tu contraseña actual:");
                        String password = scanner.nextLine();
                        System.out.println("Introduce tu nueva contraseña:");
                        String newPassword = scanner.nextLine();
                        if (server.cambiarPassword(username, password, newPassword)) {
                            System.out.println("Contraseña cambiada correctamente.");
                        } else {
                            System.out.println("No se pudo cambiar la contraseña.");
                        }
                        break;

                    case "SUGERENCIAS":
                        // Obtner lista de pososibles amigos
                        System.out.println("Lista de posibles amigos:");
                        System.out.println(server.obtenerUsuariosRecomendados(username));

                        // Preguntamos si desea mandar una solicitud de amistad a alguno de ellos
                        System.out.println("¿Desea mandar una solicitud de amistad? (sí/no)");
                        respuesta = scanner.nextLine().trim().toLowerCase();

                        if ("sí".equals(respuesta) || "si".equals(respuesta)) {
                            // Mandar solicitud de amistad
                            System.out.println(
                                    "Introduce el nombre del usuario al que desea mandar la solicitud de amistad:");
                            String friendName = scanner.nextLine();
                            server.enviarSolicitudAmistad(username, friendName);
                        }
                        break;
                    case "AMIGOS":
                        System.out.println("Lista de amigos:");
                        System.out.println(server.obtenerAmigos(username));
                        break;
                    case "SOLICITUDES":
                        System.out.println("Lista de solicitudes de amistad pendientes:");
                        System.out.println(server.obtenerSolicitudesAmistad(username));

                        System.out.println("¿Desea aceptar alguna solicitud de amistad? (sí/no)");
                        respuesta = scanner.nextLine().trim().toLowerCase();

                        if ("sí".equals(respuesta) || "si".equals(respuesta)) {
                            // Aceptar solicitud de amistad
                            System.out
                                    .println("Introduce el nombre del usuario que ha enviado la solicitud de amistad:");
                            String friendName = scanner.nextLine();
                            server.aceptarSolicitudAmistad(username, friendName);
                        }

                        // Preguntamos si desea rechazar alguna de ellas
                        System.out.println("¿Desea rechazar alguna solicitud de amistad? (sí/no)");
                        respuesta = scanner.nextLine().trim().toLowerCase();

                        if ("sí".equals(respuesta) || "si".equals(respuesta)) {
                            // Rechazar solicitud de amistad
                            System.out
                                    .println("Introduce el nombre del usuario que ha enviado la solicitud de amistad:");
                            String friendName = scanner.nextLine();
                            server.rechazarSolicitudAmistad(username, friendName);
                        }
                        break;
                    default:
                        System.out.println("Opción inválida. Por favor, intente nuevamente.");
                        break;
                }
                if ("EXIT".equals(input)) {
                    break;
                }
            }

            server.cerrarSesion(username);
            scanner.close();
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Excepción en el main de CallbackClient: " + e);

        }

    }

}
