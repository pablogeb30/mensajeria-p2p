package agpg.server;

import static java.lang.Thread.sleep;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import javax.net.ssl.SSLContext;
import javax.net.ssl.KeyManagerFactory;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;

public class CallbackServer {

    private static final String CONFIG_FILE = "server_config.properties";

    public static void main(String args[]) {
        try {

            // Cargar la configuración del servidor desde el archivo
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(CONFIG_FILE));

                 if (!properties.containsKey("rmi.port")) {
                    System.err.println("Falta configuración esencial en el archivo de propiedades.");
                    return;
                }

            } catch (IOException e) {
                System.err.println("Error al cargar la configuración del servidor: " + e.getMessage());
                return;
            }

            // Configuración SSL
            sslConfiguracion(properties);

            int rmiPort;
            try {

               

                rmiPort = Integer.parseInt(properties.getProperty("rmi.port", "1099"));
            } catch (NumberFormatException e) {
                System.err.println("Puerto RMI no válido en la configuración: " + e.getMessage());
                return;
            }

            String rmiUrl = "rmi://localhost:" + rmiPort + "/callback";

            startRegistry(rmiPort);

            CallbackServerInterface server = new CallbackServerImpl();

            // Hacemos el bind del objeto en el registro pero con SSL
            Naming.rebind(rmiUrl, server);

            sleep(1500);
            System.out.println("Servidor listo (CTRL-C para salir)");

        } catch (Exception e) {
            System.err.println("Excepción en el main de CallbackServer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Registro RMI
    private static void startRegistry(int port) throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(port);
            registry.list();

        } catch (RemoteException e) {
            // No hay un registry en el puerto especificado

            try {
                Registry registry = LocateRegistry.createRegistry(port);
                // Usamos el registry
                registry.list();
            } catch (RemoteException ex) {
                System.err.println("Error al crear el registry: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    // Configuración SSL
    private static void sslConfiguracion(Properties properties) {

        try {

            // Comprobamos que el keystore existe
            if (!java.nio.file.Files.exists(java.nio.file.Paths.get("keystoreCodis.jks"))) {
                System.err.println("No se encuentra el keystore");
                System.exit(1);
            }

            char[] password = loadPassword(properties);

            try {

                // Configuración SSL
                System.setProperty("javax.net.ssl.keyStore", "keystoreCodis.jks");
                System.setProperty("javax.net.ssl.keyStorePassword", new String(password));

                // Debug SSL
                System.setProperty("javax.net.debug", "ssl,handshake");

                SSLContext sslContext = SSLContext.getInstance("TLS");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(new FileInputStream("keystoreCodis.jks"), password);
                kmf.init(ks, password);

                sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());

                //Aceptamos conexiones
                SSLContext.setDefault(sslContext);

            } finally {

                Arrays.fill(password, '0');
            }

        } catch (Exception e) {
            System.err.println("Excepción la configuracion TLS del servidor: " + e.getMessage());
            e.printStackTrace();
        }

    }

    // Cargar la contraseña del archivo de propiedades
    private static char[] loadPassword(Properties properties) {

        String password = properties.getProperty("keystore.password");
        if (password == null || password.isEmpty()) {
            throw new IllegalStateException("La contraseña no está configurada en el archivo de propiedades.");
        }
        return password.toCharArray();
    }

}
