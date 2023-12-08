package agpg.client;

// Importamos las librerias necesarias (GUI, CallbackServerInterface y RMI)
import agpg.GUI.login.LoginUI;
import agpg.server.CallbackServerInterface;
import java.rmi.Naming;

// Clase principal del cliente
public class CallbackClient {

    // Metodo principal
    public static void main(String args[]) {

        try {

            // Comprobamos que el numero de argumentos sea correcto
            if (args.length != 1) {
                System.out.println("Uso: java CallbackClient <serverHost>");
                System.exit(1);
            }

            // Obtenemos la referencia al objeto remoto del servidor
            String registryURL = "rmi://" + args[0] + ":1099/callback";
            CallbackServerInterface server = (CallbackServerInterface) Naming.lookup(registryURL);

            // Iniciamos la interfaz de login
            new LoginUI(server, false);

        } catch (Exception e) {
            System.out.println("Excepción en el método principal del cliente: " + e.getMessage());
        }

    }
}
