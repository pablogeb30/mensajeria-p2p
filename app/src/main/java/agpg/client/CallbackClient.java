package agpg.client;

import agpg.GUI.login.LoginUI;
import agpg.server.CallbackServerInterface;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.Scanner;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CallbackClient {
    public static void main(String[] args) {
        try {

            // Cargar la configuración del cliente desde el archivo
            Properties properties = new Properties();

            try {
                properties.load(new FileInputStream("client_config.properties"));

                if (!properties.containsKey("rmi.serverPort")) {
                    System.err.println("Falta configuración esencial en el archivo de propiedades.");
                    return;
                }

            } catch (Exception e) {
                System.err.println("Error al cargar la configuración del cliente: " + e.getMessage());
                return;
            }

            // Configuración SSL
            SSLSocketFactory factory = SSLconfig(properties);
            factory.createSocket(properties.getProperty("rmi.serverHost"),
                    Integer.parseInt(properties.getProperty("rmi.serverPort")));
            Registry registry = LocateRegistry.getRegistry(Integer.parseInt(properties.getProperty("rmi.serverPort")));
            registry.list();

            // Obtenemos la referencia al servidor
            CallbackServerInterface server = (CallbackServerInterface) Naming
                    .lookup("//" + properties.getProperty("server.host") + "/callback");

            // Iniciamos la interfaz de login
            new LoginUI(server);

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
            scanner.close();
        } catch (Exception e) {
            System.out.println("Excepción en el main de CallbackClient: " + e.getMessage());

        }

    }

    // Funcion para configurar el contexto SSL
    private static SSLSocketFactory SSLconfig(Properties properties) {

        SSLSocketFactory factory = null;

        try {

            System.setProperty("javax.net.debug", "ssl");

            // Cargar el almacen de claves
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(new FileInputStream(properties.getProperty("ssl.keyStore")),
                    properties.getProperty("ssl.keyStorePassword").toCharArray());

            // Crear el gestor de claves
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, properties.getProperty("ssl.keyStorePassword").toCharArray());

            // Cargar el almacen de confianza
            KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
            ts.load(new FileInputStream(properties.getProperty("ssl.trustStore")),
                    properties.getProperty("ssl.trustStorePassword").toCharArray());

            // Crear el gestor de confianza
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);

            // Crear el contexto SSL
            SSLContext sslContext = SSLContext.getInstance(properties.getProperty("ssl.protocol"));
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
            factory = sslContext.getSocketFactory();

            System.out.println("Configuración SSL del cliente completada.");

        } catch (Exception e) {
            System.err.println("Excepción en la configuración SSL del cliente: " + e.getMessage());
        }

        return factory;
    }

}
