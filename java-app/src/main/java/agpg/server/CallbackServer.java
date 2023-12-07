package agpg.server;

import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import static java.lang.Thread.sleep;

public class CallbackServer {

    private static final String CONFIG_FILE = "server_config.properties";

    public static void main(String args[]) {

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

        try {
            
            startRegistry(rmiPort);
            CallbackServerImpl exportedObj = new CallbackServerImpl();
            Naming.rebind(rmiUrl, exportedObj);
            sleep(1000);
            System.out.println("Servidor listo (CTRL-C para salir)");
        } catch (Exception e) {
            System.err.println("Excepción en el main de CallbackServer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void startRegistry(int rmiPortNum) throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(rmiPortNum);
            registry.list();
        } catch (RemoteException e) {
            System.out.println("Creando un nuevo registro RMI en el puerto " + rmiPortNum);
            Registry registry = LocateRegistry.createRegistry(rmiPortNum);
            registry.list();
        }
    }
}
