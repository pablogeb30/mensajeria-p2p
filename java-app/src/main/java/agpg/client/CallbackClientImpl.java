package agpg.client;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.HashMap;
import javax.crypto.Cipher;
import agpg.GUI.chat.ChatUI;
import agpg.GUI.chat.Message;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;


// Implementacion de la interfaz del cliente
public class CallbackClientImpl extends UnicastRemoteObject implements CallbackClientInterface {

    
    private String username;                                                // Nombre del cliente
    private PrivateKey privateKey;                                          // Clave privada del cliente
    private HashMap<String, CallbackClientInterface> clientMap;             // Mapa de clientes registrados
    private ConcurrentHashMap<String, PublicKey> publicKeys;                // Mapa de las clave publicas de los clientes

    // GUI del cliente
    private ChatUI gui;

    // Constructor de la clase
    public CallbackClientImpl(String username) throws RemoteException {
        super();
        this.username = username;
        clientMap = new HashMap<>();
        publicKeys = new ConcurrentHashMap<>();
        // Creamos la interfaz grafica pasandole la referencia al objeto cliente
        gui = new ChatUI(this);
    }

    // Getter del nombre del cliente
    public String getUsername() throws RemoteException {
        return username;
    }

    // Getter del mapa de clientes
    public HashMap<String, CallbackClientInterface> getClientMap() throws RemoteException {
        return clientMap;
    }

    // Metodo ejecutado por el servidor para inicializar el mapa de clientes
    public void setFriends(HashMap<String, CallbackClientInterface> clientMap, ConcurrentHashMap<String, PublicKey> mapaClaves) throws RemoteException {

        for (CallbackClientInterface client : clientMap.values()) {

            this.clientMap.put(client.getUsername(), client);

            this.publicKeys.put(client.getUsername(), mapaClaves.get(client.getUsername()));
           
            gui.addClient(client.getUsername());  // Actualizamos la interfaz grafica anhadiendo los clientes
        }

    }

    // Metodo ejecutado por el servidor para actualizar el mapa de clientes
    public void updateFriends(CallbackClientInterface cObject) throws RemoteException {

        if (!(clientMap.containsKey(cObject.getUsername())) && !(cObject.getUsername().equals(this.getUsername()))) {

            clientMap.put(cObject.getUsername(), cObject);
        
            System.out.println("Nuevo usuario conectado: " + cObject.getUsername());
            
            gui.addClient(cObject.getUsername()); // Actualizamos la interfaz grafica anhadiendo los clientes

        } else {

            if (!cObject.getUsername().equals(this.getUsername())) {

                clientMap.remove(cObject.getUsername());
                System.out.println("Usuario desconectado: " + cObject.getUsername());

                gui.removeClient(cObject.getUsername()); // Actualizamos la interfaz grafica eliminando los clientes

            }
        }
    }

    // Metodo ejecutado por un cliente para mandar un mensaje a otro cliente
    public void sendMessage(String username, String message) throws RemoteException {
        try {
            String encryptedMessage = encryptMessage(message, this.publicKeys.get(username));

            clientMap.get(username).notifyMe(this.getUsername(), encryptedMessage);

            System.out.println("Mensaje enviado: " + encryptedMessage);

        } catch (Exception e) {

            
        

            System.out.println("Error al encriptar el mensaje: " + e.getMessage());
        }
    }

    // Metodo ejecutado por el cliente al que mandan el mensaje
    public void notifyMe(String username, String encryptedMessage) throws RemoteException {

        try {
            String decryptedMessage = decryptMessage(encryptedMessage, this.privateKey);

            gui.updateOtherChat(username, new Message(decryptedMessage, username, this.getUsername()));

        } catch (Exception e) {
            System.out.println("Error al desencriptar el mensaje: " + e.getMessage());
        }
    }

    

    // Metodo para crear la clave publica del cliente
    private KeyPair generarClave() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    // Metodo para registrar la clave publica del cliente en el servidor
    public PublicKey registroConServidor() throws Exception {
        KeyPair keyPair = generarClave();

        this.privateKey = keyPair.getPrivate();

        return keyPair.getPublic();
    }
    

    private String encryptMessage(String message, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());

        // Convertimos los bytes encriptsados a String por medio de la base 64
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private String decryptMessage(String encryptedMessage, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));

        return new String(decryptedBytes);
    }

}
