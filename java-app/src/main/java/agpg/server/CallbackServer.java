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


public class CallbackServer {

    private static final String CONFIG_FILE = "server_config.properties";

    public static void main(String args[]) {
        try {

            // Configuración SSL
            System.setProperty("javax.net.ssl.keyStore", "keystoreCodis.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "Codis2023");

            //Debug SSL
            //System.setProperty("javax.net.debug", "ssl,handshake");

            SSLContext sslContext = SSLContext.getInstance("TLS");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(new FileInputStream("keystoreCodis.jks"), "Codis2023".toCharArray());
            kmf.init(ks, "Codis2023".toCharArray());

            sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());


            

            // Cargar la configuración del servidor desde el archivo
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(CONFIG_FILE));
            } catch (IOException e) {
                System.err.println("Error al cargar la configuración del servidor: " + e.getMessage());
                return;
            }

            int rmiPort;
            try {
                rmiPort = Integer.parseInt(properties.getProperty("rmi.port", "1099"));
            } catch (NumberFormatException e) {
                System.err.println("Puerto RMI no válido en la configuración: " + e.getMessage());
                return;
            }

            String rmiUrl = "rmi://localhost:" + rmiPort + "/callback";


            sleep(1000);
            startRegistry(rmiPort);

            CallbackServerInterface server = new CallbackServerImpl();

           // Hacemos el bind del objeto en el registro pero con SSL
            Naming.rebind(rmiUrl, server);

            // Espera antes de mostrar el mensaje "Servidor listo"
            Thread.sleep(1000);
            System.out.println("Servidor listo (CTRL-C para salir)");
        } catch (Exception e) {
            System.err.println("Excepción en el main de CallbackServer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    // Normal RMI registry
    private static void startRegistry(int port) throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(port);
            registry.list();

        } catch (RemoteException e) {
            // No hay ningún registro en el puerto indicado
            Registry registry = LocateRegistry.createRegistry(port);

            //Usamos el registry
            registry.list();
        }
    }
    
}


