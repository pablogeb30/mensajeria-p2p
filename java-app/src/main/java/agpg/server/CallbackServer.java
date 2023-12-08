package agpg.server;

// Importamos las librerias necesarias de RMI
import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

// Importamos librerias para conectarnos a la base de datos
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

// Clase principal del servidor
public class CallbackServer {

    // Constantes
    private static final int RMIPORT = 1099;
    private static final String REGISTRYURL = "rmi://localhost:" + RMIPORT + "/callback";

    // Metodo principal
    public static void main(String args[]) {

        try {

            // Nos conectamos a la base de datos PostgreSQL
            String url = "jdbc:postgresql://localhost:5432/usuariosChat";

            // Creamos la conexion
            try (Connection conn = DriverManager.getConnection(url, "postgres", "myPassword")) {

                // Creamos el statement
                Statement stmt = conn.createStatement();

                // Ejecutamos la query
                ResultSet rs = stmt.executeQuery("SELECT * FROM usuarios");

                // Imprimimos los resultados
                while (rs.next()) {
                    System.out.println(rs.getString("username") + " " + rs.getString("password"));
                }

            } catch (SQLException e) {
                System.out.println("Error en la conexion con la BBDD: " + e.getMessage());
            }

            // Iniciamos el registro RMI y exportamos el objeto remoto
            startRegistry(RMIPORT);
            CallbackServerImpl exportedObj = new CallbackServerImpl();
            Naming.rebind(REGISTRYURL, exportedObj);
            System.out.println("Servidor listo (CTRL-C para salir)");

        } catch (Exception e) {
            System.out.println("Excepcion en el main del servidor: " + e.getMessage());
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
