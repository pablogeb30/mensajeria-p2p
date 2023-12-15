package agpg.client;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
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

    private String username; // Nombre del cliente
    private PrivateKey privateKey; // Clave privada del cliente
    private ConcurrentHashMap<String, CallbackClientInterface> clients; // Mapa de clientes registrados
    private ConcurrentHashMap<String, PublicKey> publicKeys; // Mapa de las clave publicas de los clientes

    // GUI del cliente
    private ChatUI gui;

    // Constructor de la clase
    public CallbackClientImpl(String username) throws RemoteException {
        super();
        this.username = username;
        clients = new ConcurrentHashMap<>();
        publicKeys = new ConcurrentHashMap<>();
        gui = new ChatUI(this);
    }

    // Getter del nombre del cliente
    public String getUsername() throws RemoteException {
        return username;
    }

    // Metodo ejecutado por el servidor para inicializar el mapa de clientes
    public void setClients(ConcurrentHashMap<String, CallbackClientInterface> clients, ConcurrentHashMap<String, PublicKey> keys) throws RemoteException {

        try {
            for (String nombreCliente : clients.keySet()) {
                addClient(clients.get(nombreCliente), keys.get(nombreCliente));
            }

        } catch (Exception e) {
            System.out.println("Error al inicializar el mapa de clientes: " + e.getMessage());
        }

    }

    // Metodo ejecutado por el servidor para anhadir un cliente
    public void addClient(CallbackClientInterface client, PublicKey key) throws RemoteException {

        try {
            // Actualizamos el mapa de clientes
            clients.put(client.getUsername(), client);

            publicKeys.put(client.getUsername(), key);

            System.out.println("Nuevo usuario conectado: " + client.getUsername());

            // Actualizamos la interfaz de amigos
            gui.addClient(client.getUsername());

        } catch (Exception e) {
            System.out.println("Error al anhadir un cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Metodo ejecutado por el servidor para eliminar un cliente
    public void removeClient(CallbackClientInterface client) throws RemoteException {

        // Actualizamos el mapa de clientes
        clients.remove(client.getUsername());

        publicKeys.remove(client.getUsername());

        System.out.println("Usuario desconectado: " + client.getUsername());

        // Actualizamos la interfaz de amigos
        gui.removeClient(client.getUsername());
    }

    // Metodo ejecutado por un cliente para mandar un mensaje a otro cliente
    public void sendMessage(String username, String message) throws RemoteException {
        try {
            String encryptedMessage = encryptMessage(message, this.publicKeys.get(username));

            clients.get(username).notifyMe(this.getUsername(), encryptedMessage);

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
