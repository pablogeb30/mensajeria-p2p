package agpg.client;

import agpg.GUI.login.LoginUI;
import agpg.server.CallbackServerInterface;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Properties;
import java.rmi.Naming;
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
            factory.createSocket(properties.getProperty("rmi.serverHost"),Integer.parseInt(properties.getProperty("rmi.serverPort")));
            Registry registry = LocateRegistry.getRegistry(Integer.parseInt(properties.getProperty("rmi.serverPort")));
            registry.list();


            // Obtenemos la referencia al servidor
            CallbackServerInterface server = (CallbackServerInterface) Naming.lookup("//" + properties.getProperty("server.host") + "/callback");

            // Iniciamos la interfaz de login
            new LoginUI(server, false);


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
            ks.load(new FileInputStream(properties.getProperty("ssl.keyStore")),properties.getProperty("ssl.keyStorePassword").toCharArray());

            // Crear el gestor de claves
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, properties.getProperty("ssl.keyStorePassword").toCharArray());

            // Cargar el almacen de confianza
            KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
            ts.load(new FileInputStream(properties.getProperty("ssl.trustStore")),properties.getProperty("ssl.trustStorePassword").toCharArray());

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
