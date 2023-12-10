package agpg.GUI.chat;

// Importamos las librerias necesarias (CallbackClientImpl, Swing, awt, util y RMI)
import agpg.client.CallbackClientInterface;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.rmi.RemoteException;

// Clase principal de GUI del clientes
public class ChatUI extends JFrame {

    // Modelo de la lista de clientes
    private DefaultListModel<String> listModel;

    // Vista de la lista de clientes
    private ClientList clientList;

    // Estilo del documento
    private StyledDocument doc;

    // Atributos del estilo del documento
    private SimpleAttributeSet attrs;

    // Campo de mensaje
    private MessageField messageField;

    // Mapa de mensajes enviados por el cliente
    private HashMap<String, ArrayList<Message>> messagesMap;

    // Cliente actual
    private CallbackClientInterface clientObject;

    // Constructor de la clase
    public ChatUI(CallbackClientInterface clientObject) {

        this.clientObject = clientObject;

        // Definimos el tamanho de la ventana y la operacion de cierre
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Inicializamos el mapa de mensajes
        messagesMap = new HashMap<>();

        // Inicializamos el estilo del documento y sus atributos
        doc = new DefaultStyledDocument();
        attrs = new SimpleAttributeSet();

        // Creamos el campo de mensaje
        messageField = new MessageField(clientObject, this);
        messageField.setVisible(false);

        // Boton de enviar mensaje
        JButton sendButton = new JButton("Enviar");
        sendButton.setFont(new Font("Arial", Font.PLAIN, 18));
        sendButton.setVisible(false);
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText();
                if (!message.trim().isEmpty()) {
                    try {
                        String selectedClient = selectClient();
                        // Llamamos al metodo sendMessage del objeto cliente
                        clientObject.sendMessage(selectedClient, message);
                        messageField.setText("");
                        // Actualizamos el chat propio
                        updateMyChat(selectedClient, new Message(message, clientObject.getUsername(), selectedClient));
                    } catch (RemoteException ex) {
                        System.out.println("Excepcion al mandar el mensaje: " + ex.getMessage());
                    }
                }
            }
        });

        // Anhadimos el boton de enviar mensaje al panel de chat
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);

        // Inicializamos el panel de chat
        JTextPane chatPane = new JTextPane();
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPane.setDocument(doc);
        chatPane.setFont(new Font("Arial", Font.PLAIN, 18));
        chatPane.setFocusable(false);
        chatPane.setEditable(false);

        // Anhadimos el area de chat y el campo de mensaje al panel de chat
        chatPanel.add(new JScrollPane(chatPane), BorderLayout.CENTER);
        chatPanel.add(messagePanel, BorderLayout.SOUTH);
        chatPanel.setVisible(true);

        // Inicializamos el modelo y la vista de la lista de clientes
        listModel = new DefaultListModel<>();
        clientList = new ClientList(listModel, clientObject, messageField, sendButton, this, chatPanel, messagesMap,
                chatPane);

        // Incluimos la lista dentro de un JScrollPane
        JScrollPane listScrollPane = new JScrollPane(clientList);

        // Configuramos el JSplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, chatPanel);
        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(0);

        // Anhadimos el JSplitPane a la ventana
        add(splitPane, BorderLayout.CENTER);

        // Hacemos visible la ventana y la centramos en la pantalla
        setVisible(true);
        toFront();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);

    }

    // Metodo para anhadir clientes a la lista
    public void addClient(String username) {
        listModel.addElement(username);
    }

    // Metodo para eliminar clientes de la lista
    public void removeClient(String username) {
        listModel.removeElement(username);
    }

    // Metodo para seleccionar un cliente de la lista
    public String selectClient() {
        return clientList.getSelectedValue();
    }

    // Metodo para definir el chat actual
    public void setChat(String username) {
        if (messagesMap.containsKey(username)) {
            ArrayList<Message> messages = messagesMap.get(username);
            for (Message message : messages) {
                try {
                    if (message.getSender().equals(username)) {
                        StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_LEFT);
                        doc.setParagraphAttributes(doc.getLength(), 1, attrs, false);
                        doc.insertString(doc.getLength(),
                                message.getSender() + ": " + message.getMessage() + "\n", null);
                    } else {
                        StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_RIGHT);
                        doc.setParagraphAttributes(doc.getLength(), 1, attrs, false);
                        doc.insertString(doc.getLength(), message.getSender() + ": " + message.getMessage() + "\n",
                                null);
                    }
                } catch (BadLocationException e) {
                    System.out.println("Excepcion al definir el chat: " + e.getMessage());
                }
            }
        }
    }

    public void updateMyChat(String receiver, Message message) {
        try {
            if (!messagesMap.containsKey(receiver)) {
                messagesMap.put(receiver, new ArrayList<>());
            }
            messagesMap.get(receiver).add(message);
            System.out.println(message.getSender() + " -> " + message.getReceiver() + " " + message.getDate());
            System.out.println(message.getMessage());
            StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_RIGHT);
            doc.setParagraphAttributes(doc.getLength(), 1, attrs, false);
            doc.insertString(doc.getLength(), clientObject.getUsername() + ": " + message.getMessage() + "\n", null);
        } catch (RemoteException | BadLocationException e) {
            System.out.println("Excepcion al actualizar el area de chat: " + e.getMessage());
        }
    }

    public void updateOtherChat(String sender, Message message) {
        try {
            if (!messagesMap.containsKey(sender)) {
                messagesMap.put(sender, new ArrayList<>());
            }
            messagesMap.get(sender).add(message);
            System.out.println(message.getSender() + " -> " + message.getReceiver() + " " + message.getDate());
            System.out.println(message.getMessage());
            StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_LEFT);
            doc.setParagraphAttributes(doc.getLength(), 1, attrs, false);
            doc.insertString(doc.getLength(), sender + ": " + message.getMessage() + "\n", null);
        } catch (BadLocationException e) {
            System.out.println("Excepcion al actualizar el area de chat: " + e.getMessage());
        }

    }

}
