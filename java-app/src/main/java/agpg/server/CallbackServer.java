package agpg.server;

// Importamos las librerias necesarias
import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

// Clase principal del servidor
public class CallbackServer {

    // Constantes
    private static final int RMIPORT = 1099;
    private static final String REGISTRYURL = "rmi://localhost:" + RMIPORT + "/callback";

    // Metodo principal
    public static void main(String args[]) {
        try {
            startRegistry(RMIPORT);
            CallbackServerImpl exportedObj = new CallbackServerImpl();
            Naming.rebind(REGISTRYURL, exportedObj);
            System.out.println("Servidor de callback listo");
        } catch (Exception e) {
            System.out.println("Excepcion en el main de CallbackServer: " + e);
        }

    }

    // Este metodo inicializa el registro RMI en el host local, si no existe ya
    private static void startRegistry(int RMIPortNum) throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(RMIPortNum);
            registry.list();
        } catch (RemoteException e) {
            Registry registry = LocateRegistry.createRegistry(RMIPortNum);
            registry.list();
        }
    }

}
