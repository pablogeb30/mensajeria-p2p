package agpg.GUI.chat;

// Importamos las librerias necesarias (CallbackClientImpl, Swing, awt, util, RMI y GUI)
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.ArrayList;
import java.rmi.RemoteException;

import agpg.GUI.login.Utils.HyperlinkText;
import agpg.GUI.login.Utils.UIUtils;
import agpg.client.CallbackClientInterface;

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

    // Panel de cartas
    private CardLayout cardLayout;
    private JPanel cardsPanel;

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
        setUndecorated(true);

        // Inicializamos el mapa de mensajes
        messagesMap = new HashMap<>();

        // Inicializamos el estilo del documento y sus atributos
        doc = new DefaultStyledDocument();
        attrs = new SimpleAttributeSet();

        // Creamos el campo de mensaje
        messageField = new MessageField(clientObject, this);
        messageField.setVisible(false);

        // Creamos el boton de enviar mensaje
        JButton sendButton = getSendButton(clientObject);

        // Anhadimos el boton de enviar mensaje al panel de chat
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);

        // Inicializamos el panel de chat
        JTextPane chatPane = new JTextPane();
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPane.setBackground(UIUtils.COLOR_BACKGROUND);
        chatPanel.setBackground(UIUtils.COLOR_BACKGROUND);
        chatPane.setDocument(doc);
        chatPane.setFont(new Font("Arial", Font.PLAIN, 18));
        chatPane.setFocusable(false);
        chatPane.setEditable(false);

        // Anhadimos el area de chat y el campo de mensaje al panel de chat
        JScrollPane scrollPaneChat = new JScrollPane(chatPane);
        scrollPaneChat.setBorder(BorderFactory.createEmptyBorder());
        chatPanel.add(scrollPaneChat, BorderLayout.CENTER);
        chatPanel.add(messagePanel, BorderLayout.SOUTH);
        chatPanel.setVisible(true);

        // Inicializamos el modelo y la vista de la lista de clientes
        listModel = new DefaultListModel<>();
        clientList = new ClientList(listModel, clientObject, messageField, sendButton, this, chatPanel, messagesMap,
                chatPane);
        clientList.setVisible(false);

        // Creamos panel donde se encuentran los botones de configuracion
        JPanel configPanel = new JPanel();
        configPanel.setBackground(UIUtils.COLOR_INTERACTIVE_DARKER);
        configPanel.setVisible(false);

        // Creamos el boton de cambiar contrasenha
        configPanel.add(new HyperlinkText("Cambiar contraseña", -1, -1, () -> {
            // Cambiar a la vista de cambiar contraseña

        }));

        // Incluimos la lista dentro de un JScrollPane
        JScrollPane listScrollPane = new JScrollPane(clientList);
        listScrollPane.setBorder(BorderFactory.createEmptyBorder());

        // En tu constructor o método de inicialización
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.add(clientList, "clientList");
        cardsPanel.add(configPanel, "configPanel");

        // Configuramos el JSplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cardsPanel, chatPanel);
        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(0);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        // Anhadimos el JSplitPane a la ventana
        add(splitPane, BorderLayout.CENTER);

        // Anhadimos el panel de menu a la ventana
        add(addMenuPanel(), BorderLayout.WEST);

        // Hacemos visible la ventana y la centramos en la pantalla
        setVisible(true);
        toFront();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);

    }

    private JButton getSendButton(CallbackClientInterface clientObject) {
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
        return sendButton;
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

    private JPanel addMenuPanel() {

        // Configuracion del panel
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setPreferredSize(new Dimension(50, getHeight()));
        menuPanel.setBackground(UIUtils.COLOR_INTERACTIVE);

        // Imagenes de los botones
        ImageIcon home = new ImageIcon(
                Objects.requireNonNull(getClass().getClassLoader().getResource("Home.png")).getFile());
        ImageIcon requests = new ImageIcon(
                Objects.requireNonNull(getClass().getClassLoader().getResource("Requests.png")).getFile());
        ImageIcon config = new ImageIcon(
                Objects.requireNonNull(getClass().getClassLoader().getResource("Config.png")).getFile());
        ImageIcon exit = new ImageIcon(
                Objects.requireNonNull(getClass().getClassLoader().getResource("Exit.png")).getFile());

        // Boton de home
        JButton homeButton = new JButton(home);
        homeButton.setBorder(BorderFactory.createEmptyBorder());
        homeButton.setContentAreaFilled(false);

        // Boton de solicitudes de amistad
        JButton requestsButton = new JButton(requests);
        requestsButton.setBorder(BorderFactory.createEmptyBorder());
        requestsButton.setContentAreaFilled(false);

        // Boton de configuracion
        JButton configButton = new JButton(config);
        configButton.setBorder(BorderFactory.createEmptyBorder());
        configButton.setContentAreaFilled(false);

        // Boton de salida de la aplicacion
        JButton exitButton = new JButton(exit);
        exitButton.setBorder(BorderFactory.createEmptyBorder());
        exitButton.setContentAreaFilled(false);

        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Cambiar a la vista de chat
                homeButton.setContentAreaFilled(true);
                homeButton.setBackground(UIUtils.COLOR_INTERACTIVE_LIGHTER);
                requestsButton.setContentAreaFilled(false);
                configButton.setContentAreaFilled(false);
                cardLayout.show(cardsPanel, "clientList");
            }
        });

        requestsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Cambiar a la vista de solicitudes de amistad
                homeButton.setContentAreaFilled(false);
                requestsButton.setContentAreaFilled(true);
                requestsButton.setBackground(UIUtils.COLOR_INTERACTIVE_LIGHTER);
                configButton.setContentAreaFilled(false);
            }
        });

        configButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Cambiar a la vista de configuracion
                homeButton.setContentAreaFilled(false);
                requestsButton.setContentAreaFilled(false);
                configButton.setContentAreaFilled(true);
                configButton.setBackground(UIUtils.COLOR_INTERACTIVE_LIGHTER);
                cardLayout.show(cardsPanel, "configPanel");
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Salir de la aplicacion
                ChatUI.this.dispose();
                System.exit(0);
            }
        });

        // Añadiendo botones al panel de menú
        menuPanel.add(homeButton);
        menuPanel.add(requestsButton);
        menuPanel.add(Box.createVerticalGlue());
        menuPanel.add(configButton);
        menuPanel.add(exitButton);

        return menuPanel;
    }

}
