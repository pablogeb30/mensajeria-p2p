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
import agpg.GUI.login.Utils.TextFieldUsername;
import agpg.GUI.login.Utils.UIUtils;
import agpg.client.CallbackClientInterface;
import agpg.server.CallbackServerInterface;
import agpg.GUI.login.Toaster.Toaster;

// Clase principal de GUI del clientes
public class ChatUI extends JFrame {

    // Referencia del servidor
    private CallbackServerInterface server;

    // Estilo del documento
    private StyledDocument doc;

    // Atributos del estilo del documento
    private SimpleAttributeSet attrs;

    // Campo de mensaje
    private JTextField messageField;

    // Panel de menu
    private JPanel menuPanel;

    // Usuario seleccionado
    private String selectedUser;

    // Paneles de cartas
    private CardLayout cardLayout;
    private JPanel cardsPanel;
    private CardLayout cardLayoutSecondary;
    private JPanel cardsPanelSecondary;

    // Lista de amigos, de envios de solicitudes y de recepcion de solicitudes
    private JPanel friendsListPanel;
    private JPanel sendRequestListPanel;
    private JPanel requestListPanel;
    private JPanel clientsListPanel;

    // Panel de amigos
    JPanel friendsPanel;

    // Panel de clientes
    JPanel clientsPanel;

    // Botones de solicitud
    private JButton manageRequestsButton;
    private JButton sendRequestButton;
    private JButton seeFriendsButton;

    // Boton de enviar mensaje
    private JButton sendButton;

    // Toaster
    private Toaster toaster;

    // Mapa de mensajes enviados por el cliente
    private HashMap<String, ArrayList<Message>> messagesMap;

    // Cliente actual
    private CallbackClientInterface clientObject;

    // Constructor de la clase
    public ChatUI(CallbackServerInterface server, CallbackClientInterface clientObject) {

        this.server = server;
        this.clientObject = clientObject;

        // Definimos el tamanho de la ventana y la operacion de cierre
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            setTitle(clientObject.getUsername());
        } catch (RemoteException e) {
            System.out.println("Error al obtener el username: " + e.getMessage());
        }

        // Inicializamos el mapa de mensajes
        messagesMap = new HashMap<>();

        // Inicializamos el estilo del documento y sus atributos
        doc = new DefaultStyledDocument();
        attrs = new SimpleAttributeSet();

        // Creamos el campo de mensaje
        messageField = new JTextField();

        // Lo ponemos invisible
        messageField.setVisible(false);

        // Definimos la fuente del campo de mensaje
        messageField.setFont(new Font("Arial", Font.PLAIN, 18));

        // Definimos el texto por defecto del campo de mensaje y su color
        messageField.setText(" Escribe un mensaje");
        messageField.setForeground(Color.WHITE);
        messageField.setBackground(UIUtils.COLOR_BACKGROUND);

        // Anhadimos un listener de accion al campo de mensaje (al pulsar enter)
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String message = messageField.getText();
                if (!message.trim().isEmpty()) {
                    try {
                        // Llamamos al metodo sendMessage del objeto cliente
                        clientObject.sendMessage(selectedUser, message);
                        messageField.setText("");
                        // Actualizamos el chat propio
                        updateMyChat(selectedUser, new Message(message, clientObject.getUsername(), selectedUser));
                    } catch (RemoteException e) {
                        System.out.println("Excepcion al mandar el mensaje: " + e.getMessage());
                    }
                }
            }
        });

        // Anhadimos un listener de foco al campo de mensaje
        messageField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (messageField.getText().equals(" Escribe un mensaje")) {
                    messageField.setText("");
                }
                setForeground(Color.BLACK);
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (messageField.getText().isEmpty()) {
                    messageField.setText(" Escribe un mensaje");
                }
                setForeground(Color.GRAY);
            }
        });

        // Creamos el boton de enviar mensaje
        sendButton = getSendButton(clientObject);

        // Anhadimos el boton de enviar mensaje al panel de chat
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        messagePanel.setBackground(UIUtils.COLOR_BACKGROUND);
        messagePanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        // Inicializamos el panel de chat
        JTextPane chatPane = new JTextPane();
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPane.setBackground(UIUtils.COLOR_BACKGROUND);
        chatPanel.setBackground(UIUtils.COLOR_BACKGROUND);
        chatPane.setDocument(doc);
        chatPane.setFont(UIUtils.FONT_GENERAL_UI);
        chatPane.setForeground(Color.WHITE);
        chatPane.setFocusable(false);
        chatPane.setEditable(false);

        // Anhadimos el area de chat y el campo de mensaje al panel de chat
        JScrollPane scrollPaneChat = new JScrollPane(chatPane);
        scrollPaneChat.setBorder(BorderFactory.createEmptyBorder());
        chatPanel.add(scrollPaneChat, BorderLayout.CENTER);
        chatPanel.add(messagePanel, BorderLayout.SOUTH);
        chatPanel.setVisible(true);

        // Creamos panel donde se encuentran los botones de configuracion
        JPanel configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        configPanel.setBackground(UIUtils.COLOR_INTERACTIVE_DARKER);
        configPanel.setVisible(false);

        // Anhadimos un espacio
        configPanel.add(Box.createVerticalGlue());

        // Creamos el boton de cambiar contrasenha
        JButton changePasswordButton = new JButton("                 Cambiar contraseña                 ") {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    // Cambia la transparencia del fondo cuando pasa el raton
                    g.setColor(new Color(UIUtils.COLOR_INTERACTIVE_LIGHTER.getRed(),
                            UIUtils.COLOR_INTERACTIVE_LIGHTER.getGreen(),
                            UIUtils.COLOR_INTERACTIVE_LIGHTER.getBlue(),
                            250));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };
        changePasswordButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        changePasswordButton.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_INTERACTIVE_LIGHTER, 2));
        changePasswordButton.setContentAreaFilled(false);
        changePasswordButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        changePasswordButton.setFont(UIUtils.FONT_FORGOT_PASSWORD);
        changePasswordButton.setRolloverEnabled(true);
        changePasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPasswordField oldPassword = new JPasswordField();
                JPasswordField newPassword = new JPasswordField();
                String oldPasswordString = null;
                String newPasswordString = null;

                // Mostramos un cuadro de dialogo para introducir la contrasenha actual
                int oldPass = JOptionPane.showConfirmDialog(null, oldPassword, "Contraseña actual",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (oldPass == JOptionPane.OK_OPTION && oldPassword.getPassword().length != 0) {
                    char[] currentPassword = oldPassword.getPassword();
                    oldPasswordString = new String(currentPassword);
                } else {
                    return;
                }

                // Mostramos un cuadro de dialogo para introducir la contrasenha nueva
                int newPass = JOptionPane.showConfirmDialog(null, newPassword, "Contraseña nueva",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (newPass == JOptionPane.OK_OPTION && newPassword.getPassword().length != 0) {
                    char[] currentPassword = newPassword.getPassword();
                    newPasswordString = new String(currentPassword);
                } else {
                    return;
                }

                // Llamamos al metodo cambiarPassword del servidor
                try {
                    if (server.cambiarPassword(clientObject.getUsername(), oldPasswordString, newPasswordString)) {
                        // Mensaje de exito
                        JOptionPane.showMessageDialog(null, "Contraseña cambiada con éxito.");
                    } else {
                        // Mensaje de error
                        JOptionPane.showMessageDialog(null, "Contraseña actual incorrecta.");
                    }
                } catch (RemoteException ex) {
                    System.out.println("Excepcion al cambiar la contrasenha: " + ex.getMessage());
                }
            }
        });

        // Anhadimos el boton de cambiar contrasenha y un espacio para que quede
        // centrado
        configPanel.add(changePasswordButton);
        configPanel.add(Box.createVerticalStrut(20));

        // Creamos el boton de eliminar cuenta
        JButton deleteAccountButton = new JButton("                     Eliminar cuenta                     ") {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    // Cambia la transparencia del fondo cuando pasa el raton
                    g.setColor(new Color(UIUtils.COLOR_INTERACTIVE_LIGHTER.getRed(),
                            UIUtils.COLOR_INTERACTIVE_LIGHTER.getGreen(),
                            UIUtils.COLOR_INTERACTIVE_LIGHTER.getBlue(),
                            250));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };
        deleteAccountButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        deleteAccountButton.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_INTERACTIVE_LIGHTER, 2));
        deleteAccountButton.setContentAreaFilled(false);
        deleteAccountButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteAccountButton.setFont(UIUtils.FONT_FORGOT_PASSWORD);
        deleteAccountButton.setRolloverEnabled(true);
        deleteAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPasswordField password = new JPasswordField();
                String passwordString = null;
                // Mostramos un cuadro de dialogo para introducir la contrasenha actual
                int result = JOptionPane.showConfirmDialog(null, password, "Introduzca contraseña actual",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION && password.getPassword().length != 0) {
                    char[] currentPassword = password.getPassword();
                    passwordString = new String(currentPassword);
                } else {
                    return;
                }
                // Mostrar un cuadro de dialogo de confirmacion
                int confirm = JOptionPane.showConfirmDialog(null, "¿Estás seguro de que quieres eliminar tu cuenta?",
                        "Confirmación", JOptionPane.YES_NO_OPTION);
                // Si el usuario confirma, realiza la accion de eliminar la cuenta
                if (confirm == JOptionPane.YES_OPTION) {
                    // El usuario confirmo, realiza la accion de eliminar la cuenta aqui
                    try {
                        server.eliminarCuenta(clientObject.getUsername(), passwordString);
                    } catch (RemoteException ex) {
                        System.out.println("Excepcion al eliminar la cuenta: " + ex.getMessage());
                    }
                    // Mensaje de exito y termina el programa
                    JOptionPane.showMessageDialog(null, "Cuenta eliminada con éxito.");
                    System.exit(0);
                }
            }
        });

        // Anhadimos el boton de eliminar cuenta y un espacio para que quede centrado
        configPanel.add(deleteAccountButton);
        configPanel.add(Box.createVerticalGlue());

        // Panel por defecto principal
        JPanel defaultPanel = new JPanel(new GridBagLayout());
        defaultPanel.setBackground(UIUtils.COLOR_INTERACTIVE_DARKER);

        // JLabel del panel principal
        JLabel defaultLabel = new JLabel("No hay información a mostrar");
        defaultLabel.setFont(UIUtils.FONT_MAIN_TEXT);
        defaultLabel.setAlignmentX(CENTER_ALIGNMENT);
        defaultLabel.setHorizontalAlignment(JLabel.CENTER);
        defaultLabel.setVerticalAlignment(JLabel.CENTER);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        defaultPanel.add(defaultLabel, gbc);

        // Panel de solicitudes de amistad
        JPanel requestsPanel = new JPanel();
        requestsPanel.setLayout(new BoxLayout(requestsPanel, BoxLayout.Y_AXIS));
        requestsPanel.setBackground(UIUtils.COLOR_INTERACTIVE_DARKER);
        requestsPanel.setVisible(false);

        // Anhadimos un espacio
        requestsPanel.add(Box.createVerticalGlue());

        // Creamos el boton de ver amigos
        seeFriendsButton = new JButton("               Ver amigos conectados              ") {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    // Cambia la transparencia del fondo cuando pasa el raton
                    g.setColor(new Color(UIUtils.COLOR_INTERACTIVE_LIGHTER.getRed(),
                            UIUtils.COLOR_INTERACTIVE_LIGHTER.getGreen(),
                            UIUtils.COLOR_INTERACTIVE_LIGHTER.getBlue(),
                            250));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };
        seeFriendsButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        seeFriendsButton.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_INTERACTIVE_LIGHTER, 2));
        seeFriendsButton.setContentAreaFilled(false);
        seeFriendsButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        seeFriendsButton.setFont(UIUtils.FONT_FORGOT_PASSWORD);
        seeFriendsButton.setRolloverEnabled(true);

        // Anhadimos el boton de ver amigos y un espacio
        requestsPanel.add(seeFriendsButton);
        requestsPanel.add(Box.createVerticalStrut(20));

        // Creamos el boton de enviar solicitudes de amistad
        sendRequestButton = new JButton(
                "                    Enviar solicitudes                    ") {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    // Cambia la transparencia del fondo cuando pasa el raton
                    g.setColor(new Color(UIUtils.COLOR_INTERACTIVE_LIGHTER.getRed(),
                            UIUtils.COLOR_INTERACTIVE_LIGHTER.getGreen(),
                            UIUtils.COLOR_INTERACTIVE_LIGHTER.getBlue(),
                            250));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };
        sendRequestButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        sendRequestButton.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_INTERACTIVE_LIGHTER, 2));
        sendRequestButton.setContentAreaFilled(false);
        sendRequestButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendRequestButton.setFont(UIUtils.FONT_FORGOT_PASSWORD);
        sendRequestButton.setRolloverEnabled(true);

        // Anhadimos el boton de enviar solicitudes y un espacio
        requestsPanel.add(sendRequestButton);
        requestsPanel.add(Box.createVerticalStrut(20));

        // Creamos el boton de gestionar solicitudes
        manageRequestsButton = new JButton("                 Gestionar solicitudes                  ") {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    // Cambia la transparencia del fondo cuando pasa el raton
                    g.setColor(new Color(UIUtils.COLOR_INTERACTIVE_LIGHTER.getRed(),
                            UIUtils.COLOR_INTERACTIVE_LIGHTER.getGreen(),
                            UIUtils.COLOR_INTERACTIVE_LIGHTER.getBlue(),
                            250));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };
        manageRequestsButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        manageRequestsButton.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_INTERACTIVE_LIGHTER, 2));
        manageRequestsButton.setContentAreaFilled(false);
        manageRequestsButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        manageRequestsButton.setFont(UIUtils.FONT_FORGOT_PASSWORD);
        manageRequestsButton.setRolloverEnabled(true);

        // Anhadimos el boton de gestionar solicitudess y un espacio para que quede
        // centrado
        requestsPanel.add(manageRequestsButton);
        requestsPanel.add(Box.createVerticalGlue());

        seeFriendsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Cambiar a la vista de ver amigos
                cardLayoutSecondary.show(cardsPanelSecondary, "friendsPanel");
                try {
                    if (clientObject.getNumClients() == 0) {
                        cardLayoutSecondary.show(cardsPanelSecondary, "defaultSecondaryPanel");
                    }
                } catch (RemoteException ex) {
                    System.out.println("Excepcion al obtener el numero de clientes: " + ex.getMessage());
                }
                seeFriendsButton.setContentAreaFilled(true);
                seeFriendsButton.setBackground(UIUtils.COLOR_INTERACTIVE_LIGHTER);
                sendRequestButton.setContentAreaFilled(false);
                manageRequestsButton.setContentAreaFilled(false);
            }
        });
        sendRequestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Cambiar a la vista de enviar solicitudes
                cardLayoutSecondary.show(cardsPanelSecondary, "sendRequestsPanel");
                seeFriendsButton.setContentAreaFilled(false);
                sendRequestButton.setContentAreaFilled(true);
                sendRequestButton.setBackground(UIUtils.COLOR_INTERACTIVE_LIGHTER);
                manageRequestsButton.setContentAreaFilled(false);
            }
        });
        manageRequestsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Cambiar a la vista de gestionar solicitudes
                cardLayoutSecondary.show(cardsPanelSecondary, "requestPanel");
                try {
                    if (server.obtenerSolicitudesAmistad(clientObject.getUsername()).size() == 0) {
                        cardLayoutSecondary.show(cardsPanelSecondary, "defaultSecondaryPanel");
                    }
                } catch (RemoteException ex) {
                    System.out.println("Excepcion al obtener el numero de clientes: " + ex.getMessage());
                }
                seeFriendsButton.setContentAreaFilled(false);
                sendRequestButton.setContentAreaFilled(false);
                manageRequestsButton.setContentAreaFilled(true);
                manageRequestsButton.setBackground(UIUtils.COLOR_INTERACTIVE_LIGHTER);
            }
        });

        // Panel de lista de clientes
        clientsPanel = new JPanel();
        // Fondo del panel
        clientsPanel.setBackground(UIUtils.COLOR_INTERACTIVE_DARKER);
        // Habra un margen de 10px en la parte superior
        clientsPanel.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));
        // El panel tendra un titulo en la parte superior
        JLabel clientsLabel = new JLabel("Amigos");
        clientsLabel.setFont(UIUtils.FONT_GENERAL_UI);
        clientsLabel.setForeground(Color.BLACK);
        clientsLabel.setAlignmentX(CENTER_ALIGNMENT);
        clientsLabel.setHorizontalAlignment(JLabel.CENTER);
        clientsLabel.setVerticalAlignment(JLabel.CENTER);
        // Anhadimos el titulo al panel
        clientsPanel.setLayout(new BoxLayout(clientsPanel, BoxLayout.Y_AXIS));
        clientsPanel.add(clientsLabel);
        clientsPanel.add(Box.createVerticalStrut(10));
        // El panel tendra una lista de paneles scrolleables
        clientsListPanel = new JPanel();
        clientsListPanel.setPreferredSize(new Dimension(80, 400));
        clientsListPanel.setLayout(new BoxLayout(clientsListPanel, BoxLayout.Y_AXIS));
        clientsListPanel.setBackground(UIUtils.COLOR_INTERACTIVE_DARKER);
        JScrollPane clientsListScrollPane = new JScrollPane(clientsListPanel);
        clientsListScrollPane.setBorder(BorderFactory.createEmptyBorder());
        clientsPanel.add(clientsListScrollPane);

        // Anhadimos los paneles al panel de cartas
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.add(defaultPanel, "defaultPanel");
        cardsPanel.add(clientsPanel, "clientsPanel");
        cardsPanel.add(requestsPanel, "requestsPanel");
        cardsPanel.add(configPanel, "configPanel");

        // Panel por defecto secundario
        JPanel defaultSecondaryPanel = new JPanel(new GridBagLayout());
        defaultSecondaryPanel.setBackground(UIUtils.COLOR_BACKGROUND);

        // JLabel del panel por defecto secundario
        JLabel defaultSecondaryLabel = new JLabel("No hay información a mostrar");
        defaultSecondaryLabel.setFont(UIUtils.FONT_GENERAL_UI);
        defaultSecondaryLabel.setForeground(Color.WHITE);
        defaultSecondaryLabel.setAlignmentX(CENTER_ALIGNMENT);
        defaultSecondaryLabel.setHorizontalAlignment(JLabel.CENTER);
        defaultSecondaryLabel.setVerticalAlignment(JLabel.CENTER);
        defaultSecondaryPanel.add(defaultSecondaryLabel, gbc);

        // Panel de lista de amigos
        friendsPanel = new JPanel();
        // Fondo del panel
        friendsPanel.setBackground(UIUtils.COLOR_BACKGROUND);
        // Habra un margen de 10px en la parte superior
        friendsPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
        // El panel tendra un titulo en la parte superior
        JLabel friendsLabel = new JLabel("Lista de amigos conectados");
        friendsLabel.setFont(UIUtils.FONT_GENERAL_UI);
        friendsLabel.setForeground(Color.WHITE);
        friendsLabel.setAlignmentX(CENTER_ALIGNMENT);
        friendsLabel.setHorizontalAlignment(JLabel.CENTER);
        friendsLabel.setVerticalAlignment(JLabel.CENTER);
        // Anhadimos el titulo al panel
        friendsPanel.setLayout(new BoxLayout(friendsPanel, BoxLayout.Y_AXIS));
        friendsPanel.add(friendsLabel);
        friendsPanel.add(Box.createVerticalStrut(20));
        // El panel tendra una lista de paneles scrolleables
        friendsListPanel = new JPanel();
        friendsListPanel.setPreferredSize(new Dimension(300, 400));
        friendsListPanel.setLayout(new BoxLayout(friendsListPanel, BoxLayout.Y_AXIS));
        friendsListPanel.setBackground(UIUtils.COLOR_BACKGROUND);
        JScrollPane friendsListScrollPane = new JScrollPane(friendsListPanel);
        friendsListScrollPane.setBorder(BorderFactory.createEmptyBorder());
        friendsPanel.add(friendsListScrollPane);

        // Panel de enviar solicitudes
        JPanel sendRequestsPanel = new JPanel();
        // Fondo del panel
        sendRequestsPanel.setBackground(UIUtils.COLOR_BACKGROUND);
        // Habra un margen de 10px en la parte superior
        sendRequestsPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
        // El panel tendra un titulo en la parte superior
        JLabel sendRequestsLabel = new JLabel("Enviar solicitudes de amistad");
        sendRequestsLabel.setFont(UIUtils.FONT_GENERAL_UI);
        sendRequestsLabel.setForeground(Color.WHITE);
        sendRequestsLabel.setAlignmentX(CENTER_ALIGNMENT);
        sendRequestsLabel.setHorizontalAlignment(JLabel.CENTER);
        sendRequestsLabel.setVerticalAlignment(JLabel.CENTER);
        // Anhadimos el titulo al panel
        sendRequestsPanel.setLayout(new BoxLayout(sendRequestsPanel, BoxLayout.Y_AXIS));
        sendRequestsPanel.add(sendRequestsLabel);
        sendRequestsPanel.add(Box.createVerticalStrut(20));
        addUsernameTextField(sendRequestsPanel);
        sendRequestsPanel.add(Box.createVerticalStrut(20));
        // El panel tendra una lista de paneles scrolleables
        sendRequestListPanel = new JPanel();
        sendRequestListPanel.setPreferredSize(new Dimension(300, 400));
        sendRequestListPanel.setLayout(new BoxLayout(sendRequestListPanel, BoxLayout.Y_AXIS));
        sendRequestListPanel.setBackground(UIUtils.COLOR_BACKGROUND);
        try {
            for (String request : server.obtenerSolicitudesEnviadas(clientObject.getUsername())) {
                addSendRequest(request);
            }
        } catch (RemoteException e) {
            System.out.println("Excepcion al obtener las solicitudes de amistad: " + e.getMessage());
        }
        JScrollPane sendRequestListScrollPane = new JScrollPane(sendRequestListPanel);
        sendRequestListScrollPane.setBorder(BorderFactory.createEmptyBorder());
        sendRequestsPanel.add(sendRequestListScrollPane);

        // Panel de lista de solicitudes
        JPanel requestPanel = new JPanel();
        // Fondo del panel
        requestPanel.setBackground(UIUtils.COLOR_BACKGROUND);
        // Habra un margen de 10px en la parte superior
        requestPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
        // El panel tendra un titulo en la parte superior
        JLabel requestLabel = new JLabel("Solicitudes de amistad");
        requestLabel.setFont(UIUtils.FONT_GENERAL_UI);
        requestLabel.setForeground(Color.WHITE);
        requestLabel.setAlignmentX(CENTER_ALIGNMENT);
        requestLabel.setHorizontalAlignment(JLabel.CENTER);
        requestLabel.setVerticalAlignment(JLabel.CENTER);
        // Anhadimos el titulo al panel
        requestPanel.setLayout(new BoxLayout(requestPanel, BoxLayout.Y_AXIS));
        requestPanel.add(requestLabel);
        requestPanel.add(Box.createVerticalStrut(20));
        // El panel tendra una lista de paneles scrolleables
        requestListPanel = new JPanel();
        requestListPanel.setPreferredSize(new Dimension(300, 400));
        requestListPanel.setLayout(new BoxLayout(requestListPanel, BoxLayout.Y_AXIS));
        requestListPanel.setBackground(UIUtils.COLOR_BACKGROUND);
        try {
            for (String request : server.obtenerSolicitudesAmistad(clientObject.getUsername())) {
                addRequest(request);
            }
        } catch (RemoteException e) {
            System.out.println("Excepcion al obtener las solicitudes de amistad: " + e.getMessage());
        }

        JScrollPane requestListScrollPane = new JScrollPane(requestListPanel);
        requestListScrollPane.setBorder(BorderFactory.createEmptyBorder());
        requestPanel.add(requestListScrollPane);

        // Anhadimos los paneles al panel de cartas secundario
        cardLayoutSecondary = new CardLayout();
        cardsPanelSecondary = new JPanel(cardLayoutSecondary);
        cardsPanelSecondary.add(defaultSecondaryPanel, "defaultSecondaryPanel");
        cardsPanelSecondary.add(chatPanel, "chatPanel");
        cardsPanelSecondary.add(friendsPanel, "friendsPanel");
        cardsPanelSecondary.add(sendRequestsPanel, "sendRequestsPanel");
        cardsPanelSecondary.add(requestPanel, "requestPanel");

        // Configuramos el JSplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cardsPanel, cardsPanelSecondary);
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

        // Centramos la ventana
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);

        toaster = new Toaster(sendRequestsPanel);

    }

    private JButton getSendButton(CallbackClientInterface clientObject) {
        // Boton de enviar mensaje
        ImageIcon send = new ImageIcon(
                Objects.requireNonNull(getClass().getClassLoader().getResource("Send.png")).getFile());
        JButton sendButton = new JButton(send);
        sendButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        sendButton.setBorder(BorderFactory.createEmptyBorder());
        sendButton.setContentAreaFilled(false);
        sendButton.setVisible(false);
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText();
                if (!message.trim().isEmpty()) {
                    try {
                        // Llamamos al metodo sendMessage del objeto cliente
                        clientObject.sendMessage(selectedUser, message);
                        messageField.setText("");
                        // Actualizamos el chat propio
                        updateMyChat(selectedUser, new Message(message, clientObject.getUsername(), selectedUser));
                    } catch (RemoteException ex) {
                        toaster.error("Error al mandar el mensaje.");
                    }
                }
            }
        });
        return sendButton;
    }

    // Metodo para seleccionar un cliente de la lista
    public String selectClient() {
        String aux = null;
        // Buscar el panel correspondiente al amigo a eliminar
        for (Component component : clientsListPanel.getComponents()) {
            if (component instanceof JPanel) {
                JPanel clientPanel = (JPanel) component;
                // Obtener el JLabel que contiene el nombre de usuario
                JLabel usernameLabel = null;
                for (Component panelComponent : clientPanel.getComponents()) {
                    if (panelComponent instanceof JLabel && ((JLabel) panelComponent).getIcon() == null) {
                        usernameLabel = (JLabel) panelComponent;
                        break;
                    }
                }
                // Si se encuentra el amigo, eliminar el panel
                if (usernameLabel != null) {
                    aux = usernameLabel.getText();
                    break;
                }
            }
        }
        return aux;
    }

    // Metodo para definir el chat actual
    public void setChat(String username) {
        // Limpiamos el area de chat
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(doc.getLength(), "\n", null);
        } catch (BadLocationException e) {
            System.out.println("Excepcion al definir el chat: " + e.getMessage());
        }
        if (messagesMap.containsKey(username)) {
            ArrayList<Message> messages = messagesMap.get(username);
            for (Message message : messages) {
                try {
                    if (message.getSender().equals(username)) {
                        StyleConstants.setForeground(attrs, UIUtils.COLOR_INTERACTIVE_LIGHTER);
                        StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_LEFT);
                        doc.setParagraphAttributes(doc.getLength(), 1, attrs, false);
                        doc.insertString(doc.getLength(), "  " + message.getSender() + "\n", null);
                        StyleConstants.setForeground(attrs, Color.WHITE);
                        StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_LEFT);
                        doc.setParagraphAttributes(doc.getLength(), 1, attrs, false);
                        doc.insertString(doc.getLength(), "  " + message.getMessage() + "\n\n", null);
                    } else {
                        StyleConstants.setForeground(attrs, Color.cyan);
                        StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_RIGHT);
                        doc.setParagraphAttributes(doc.getLength(), 1, attrs, false);
                        doc.insertString(doc.getLength(), message.getSender() + "  \n", null);
                        StyleConstants.setForeground(attrs, Color.WHITE);
                        StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_RIGHT);
                        doc.setParagraphAttributes(doc.getLength(), 1, attrs, false);
                        doc.insertString(doc.getLength(), message.getMessage() + "  \n\n", null);
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
            if (!message.getReceiver().equals(selectedUser)) {
                return;
            }
            System.out.println(message.getSender() + " -> " + message.getReceiver() + " " + message.getDate());
            System.out.println(message.getMessage());
            StyleConstants.setForeground(attrs, Color.cyan);
            StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_RIGHT);
            doc.setParagraphAttributes(doc.getLength(), 1, attrs, false);
            doc.insertString(doc.getLength(), message.getSender() + "  \n", null);
            StyleConstants.setForeground(attrs, Color.WHITE);
            StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_RIGHT);
            doc.setParagraphAttributes(doc.getLength(), 1, attrs, false);
            doc.insertString(doc.getLength(), message.getMessage() + "  \n\n", null);
        } catch (BadLocationException e) {
            System.out.println("Excepcion al actualizar el area de chat: " + e.getMessage());
        }
    }

    public void updateOtherChat(String sender, Message message) {
        try {
            if (!messagesMap.containsKey(sender)) {
                messagesMap.put(sender, new ArrayList<>());
            }
            messagesMap.get(sender).add(message);
            if (!message.getSender().equals(selectedUser)) {
                return;
            }
            System.out.println(message.getSender() + " -> " + message.getReceiver() + " " + message.getDate());
            System.out.println(message.getMessage());
            StyleConstants.setForeground(attrs, UIUtils.COLOR_INTERACTIVE_LIGHTER);
            StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_LEFT);
            doc.setParagraphAttributes(doc.getLength(), 1, attrs, false);
            doc.insertString(doc.getLength(), "  " + message.getSender() + "\n", null);
            StyleConstants.setForeground(attrs, Color.WHITE);
            StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_LEFT);
            doc.setParagraphAttributes(doc.getLength(), 1, attrs, false);
            doc.insertString(doc.getLength(), "  " + message.getMessage() + "\n\n", null);
        } catch (BadLocationException e) {
            System.out.println("Excepcion al actualizar el area de chat: " + e.getMessage());
        }
    }

    public void addFriend(String username) {
        // Anhadir a la lista de amigos el nuevo amigo
        friendsListPanel.add(new JPanel() {
            {
                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                setPreferredSize(new Dimension(300, 40));
                setBackground(UIUtils.COLOR_BACKGROUND);
                setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
                ImageIcon user = new ImageIcon(
                        Objects.requireNonNull(getClass().getClassLoader().getResource("User.png")).getFile());
                add(new JLabel(user) {
                    {
                        setAlignmentX(CENTER_ALIGNMENT);
                        setHorizontalAlignment(JLabel.CENTER);
                        setVerticalAlignment(JLabel.CENTER);
                    }
                });
                add(Box.createHorizontalGlue());
                add(new JLabel(username) {
                    {
                        setFont(UIUtils.FONT_GENERAL_UI);
                        setForeground(Color.WHITE);
                        setAlignmentX(CENTER_ALIGNMENT);
                        setHorizontalAlignment(JLabel.CENTER);
                        setVerticalAlignment(JLabel.CENTER);
                    }
                });
                add(Box.createHorizontalGlue());
                ImageIcon remove = new ImageIcon(
                        Objects.requireNonNull(getClass().getClassLoader().getResource("Remove.png")).getFile());
                add(new JButton(remove) {
                    {
                        setAlignmentX(CENTER_ALIGNMENT);
                        setHorizontalAlignment(JLabel.CENTER);
                        setVerticalAlignment(JLabel.CENTER);
                        setBorder(BorderFactory.createEmptyBorder());
                        setContentAreaFilled(false);
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        // Establecer un tamaño máximo
                        setPreferredSize(new Dimension(40, 40));
                        addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                // Mostrar un cuadro de dialogo de confirmacion
                                int confirm = JOptionPane.showConfirmDialog(null,
                                        "¿Estás seguro de que quieres eliminar a " + username + " como amigo?",
                                        "Confirmación", JOptionPane.YES_NO_OPTION);
                                // Si el usuario confirma, realiza la accion de eliminar el amigo
                                if (confirm == JOptionPane.YES_OPTION) {
                                    // El usuario confirmo, realiza la accion de eliminar el amigo aqui
                                    try {
                                        server.eliminarAmigo(clientObject.getUsername(), username);
                                    } catch (RemoteException ex) {
                                        System.out.println("Excepcion al eliminar amigo: " + ex.getMessage());
                                    }
                                    // Eliminar amigo
                                    removeFriend(username);
                                    removeClient(username);
                                    friendsListPanel.revalidate();
                                    friendsListPanel.repaint();
                                    return;
                                }
                            }
                        });
                    }
                });
            }
        });
        friendsListPanel.revalidate();
        friendsListPanel.repaint();
        friendsListPanel.add(Box.createVerticalStrut(10));
        // Anhadirlo a la vista de chat
        clientsListPanel.add(new JPanel() {
            {
                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                setPreferredSize(new Dimension(60, 40));
                setBackground(UIUtils.COLOR_INTERACTIVE_DARKER);
                setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                ImageIcon user = new ImageIcon(
                        Objects.requireNonNull(getClass().getClassLoader().getResource("UserN.png")).getFile());
                add(new JLabel(user) {
                    {
                        setAlignmentX(CENTER_ALIGNMENT);
                        setHorizontalAlignment(JLabel.CENTER);
                        setVerticalAlignment(JLabel.CENTER);
                    }
                });
                add(Box.createHorizontalGlue());
                add(new JLabel(username) {
                    {
                        setFont(UIUtils.FONT_GENERAL_UI);
                        setForeground(Color.BLACK);
                        setAlignmentX(CENTER_ALIGNMENT);
                        setHorizontalAlignment(JLabel.CENTER);
                        setVerticalAlignment(JLabel.CENTER);
                    }
                });
                add(Box.createHorizontalGlue());
                ImageIcon open = new ImageIcon(
                        Objects.requireNonNull(getClass().getClassLoader().getResource("Open.png")).getFile());
                add(new JButton(open) {
                    {
                        setAlignmentX(CENTER_ALIGNMENT);
                        setHorizontalAlignment(JLabel.CENTER);
                        setVerticalAlignment(JLabel.CENTER);
                        setBorder(BorderFactory.createEmptyBorder());
                        setContentAreaFilled(false);
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        // Establecer un tamaño máximo
                        setPreferredSize(new Dimension(40, 40));
                        addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                setChat(username);
                                messageField.setVisible(true);
                                sendButton.setVisible(true);
                                cardLayoutSecondary.show(cardsPanelSecondary, "chatPanel");
                                selectedUser = username;
                            }
                        });
                    }
                });
            }
        });
        clientsListPanel.revalidate();
        clientsListPanel.repaint();
        clientsListPanel.add(Box.createVerticalStrut(10));
    }

    public void removeFriend(String username) {
        // Buscar el panel correspondiente al amigo a eliminar
        for (Component component : friendsListPanel.getComponents()) {
            if (component instanceof JPanel) {
                JPanel friendPanel = (JPanel) component;
                // Obtener el JLabel que contiene el nombre de usuario
                JLabel usernameLabel = null;
                for (Component panelComponent : friendPanel.getComponents()) {
                    if (panelComponent instanceof JLabel && ((JLabel) panelComponent).getIcon() == null) {
                        if (((JLabel) panelComponent).getText().equals(username)) {
                            usernameLabel = (JLabel) panelComponent;
                            break;
                        }
                    }
                }
                // Si se encuentra el amigo, eliminar el panel
                if (usernameLabel != null) {
                    friendsListPanel.remove(friendPanel);
                    break;
                }
            }
        }
        try {
            if (clientObject.getNumClients() == 0) {
                cardLayout.show(cardsPanel, "defaultPanel");
                cardLayoutSecondary.show(cardsPanelSecondary, "defaultSecondaryPanel");
            }
        } catch (RemoteException ex) {
            System.out.println("Excepcion al obtener el num clientes: " + ex.getMessage());
        }
        // Actualizar la interfaz
        friendsListPanel.revalidate();
        friendsListPanel.repaint();
    }

    public void removeClient(String username) {
        // Buscar el panel correspondiente al amigo a eliminar
        for (Component component : clientsListPanel.getComponents()) {
            if (component instanceof JPanel) {
                JPanel clientPanel = (JPanel) component;
                // Obtener el JLabel que contiene el nombre de usuario
                JLabel usernameLabel = null;
                for (Component panelComponent : clientPanel.getComponents()) {
                    if (panelComponent instanceof JLabel && ((JLabel) panelComponent).getIcon() == null) {
                        if (((JLabel) panelComponent).getText().equals(username)) {
                            usernameLabel = (JLabel) panelComponent;
                            break;
                        }
                    }
                }
                // Si se encuentra el amigo, eliminar el panel
                if (usernameLabel != null) {
                    clientsListPanel.remove(clientPanel);
                    break;
                }
            }
        }
        try {
            if (clientObject.getNumClients() == 0) {
                cardLayout.show(cardsPanel, "defaultPanel");
                cardLayoutSecondary.show(cardsPanelSecondary, "defaultSecondaryPanel");
            }
        } catch (RemoteException ex) {
            System.out.println("Excepcion al obtener el num clientes: " + ex.getMessage());
        }
        // Actualizar la interfaz
        clientsListPanel.revalidate();
        clientsListPanel.repaint();
    }

    public void addSendRequest(String username) {
        // Anhadir a la lista de amigos el nuevo amigo
        sendRequestListPanel.add(Box.createVerticalStrut(10));
        sendRequestListPanel.add(new JPanel() {
            {
                try {
                    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                    setPreferredSize(new Dimension(300, 40));
                    setBackground(UIUtils.COLOR_BACKGROUND);
                    setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

                    add(Box.createHorizontalGlue());
                    add(new JLabel(clientObject.getUsername() + " --> " + username) {
                        {
                            setFont(UIUtils.FONT_GENERAL_UI);
                            setForeground(Color.WHITE);
                            setAlignmentX(CENTER_ALIGNMENT);
                            setHorizontalAlignment(JLabel.CENTER);
                            setVerticalAlignment(JLabel.CENTER);
                        }
                    });
                    add(Box.createHorizontalGlue());
                    ImageIcon pending = new ImageIcon(
                            Objects.requireNonNull(getClass().getClassLoader().getResource("Pending.png")).getFile());
                    add(new JLabel(pending) {
                        {
                            setMaximumSize(new Dimension(40, 40));
                            setAlignmentX(CENTER_ALIGNMENT);
                            setHorizontalAlignment(JLabel.CENTER);
                            setVerticalAlignment(JLabel.CENTER);
                            setBorder(BorderFactory.createEmptyBorder());
                            // Establecer un tamaño máximo
                            setPreferredSize(new Dimension(40, 40));
                        }
                    });
                } catch (RemoteException e) {
                    System.out.println("Error al obtener el username en el sendRequestsPanel: " + e.getMessage());
                }
            }
        });
        sendRequestListPanel.add(Box.createVerticalStrut(10));
    }

    private void addUsernameTextField(JPanel panel1) {
        TextFieldUsername usernameField = new TextFieldUsername();
        usernameField.setText(UIUtils.PLACEHOLDER_TEXT_SEARCH);
        usernameField.setForeground(UIUtils.COLOR_OUTLINE);
        usernameField.setBorderColor(UIUtils.COLOR_OUTLINE);

        usernameField.setName("usernameField");

        usernameField.setBounds(423, 139, 250, 44);

        usernameField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (usernameField.getText().equals(UIUtils.PLACEHOLDER_TEXT_SEARCH)) {
                    usernameField.setText("");
                }
                usernameField.setForeground(Color.white);
                usernameField.setBorderColor(UIUtils.COLOR_INTERACTIVE);
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (!usernameField.getText().equals(UIUtils.PLACEHOLDER_TEXT_SEARCH)) {
                    usernameField.setText(UIUtils.PLACEHOLDER_TEXT_SEARCH);
                }
                usernameField.setForeground(UIUtils.COLOR_OUTLINE);
                usernameField.setBorderColor(UIUtils.COLOR_OUTLINE);
            }
        });

        usernameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String username = usernameField.getText();
                if (!username.trim().isEmpty()) {
                    try {
                        server.enviarSolicitudAmistad(clientObject.getUsername(), username);
                        usernameField.setText("");
                        addSendRequest(username);
                        sendRequestListPanel.revalidate();
                        sendRequestListPanel.repaint();
                    } catch (RemoteException e) {
                        toaster.error("Usuario no válido.");
                    }
                }
            }
        });
        panel1.add(usernameField);
    }

    public void removeSendRequest(String username) {
        // Buscar el panel correspondiente al amigo a eliminar
        for (Component component : sendRequestListPanel.getComponents()) {
            if (component instanceof JPanel) {
                JPanel sendRequestPanel = (JPanel) component;
                // Obtener el JLabel que contiene el nombre de usuario
                JLabel requestLabel = null;
                for (Component panelComponent : sendRequestPanel.getComponents()) {
                    if (panelComponent instanceof JLabel && ((JLabel) panelComponent).getIcon() == null) {
                        try {
                            if (((JLabel) panelComponent).getText()
                                    .equals(clientObject.getUsername() + " --> " + username)) {
                                requestLabel = (JLabel) panelComponent;
                                break;
                            }
                        } catch (RemoteException e) {
                            System.out.println("Error al obtener el username: " + e.getMessage());
                        }
                    }
                }
                // Si se encuentra el amigo, eliminar el panel
                if (requestLabel != null) {
                    sendRequestListPanel.remove(sendRequestPanel);
                    break;
                }
            }
        }
        // Actualizar la interfaz
        sendRequestListPanel.revalidate();
        sendRequestListPanel.repaint();
    }

    public void addRequest(String username) {
        // Anhadir a la lista de amigos el nuevo amigo
        requestListPanel.add(new JPanel() {
            {
                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                setPreferredSize(new Dimension(300, 40));
                setBackground(UIUtils.COLOR_BACKGROUND);
                setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
                ImageIcon user = new ImageIcon(
                        Objects.requireNonNull(getClass().getClassLoader().getResource("User.png")).getFile());
                add(new JLabel(user) {
                    {
                        setAlignmentX(CENTER_ALIGNMENT);
                        setHorizontalAlignment(JLabel.CENTER);
                        setVerticalAlignment(JLabel.CENTER);
                    }
                });
                add(Box.createHorizontalGlue());
                add(new JLabel(username) {
                    {
                        setFont(UIUtils.FONT_GENERAL_UI);
                        setForeground(Color.WHITE);
                        setAlignmentX(CENTER_ALIGNMENT);
                        setHorizontalAlignment(JLabel.CENTER);
                        setVerticalAlignment(JLabel.CENTER);
                    }
                });
                add(Box.createHorizontalGlue());
                ImageIcon accept = new ImageIcon(
                        Objects.requireNonNull(getClass().getClassLoader().getResource("Accept.png")).getFile());
                add(new JButton(accept) {
                    {
                        setAlignmentX(CENTER_ALIGNMENT);
                        setHorizontalAlignment(JLabel.CENTER);
                        setVerticalAlignment(JLabel.CENTER);
                        setBorder(BorderFactory.createEmptyBorder());
                        setContentAreaFilled(false);
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        // Establecer un tamaño máximo
                        setPreferredSize(new Dimension(40, 40));
                        addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                try {
                                    server.aceptarSolicitudAmistad(clientObject.getUsername(), username);
                                    // Mostrar un cuadro de dialogo de exito
                                    JOptionPane.showMessageDialog(null,
                                            "Se ha aceptado la solicitud de amistad correctamente.",
                                            "Solicitud aceptada", JOptionPane.INFORMATION_MESSAGE);
                                    // Eliminar de la lista de solicitudes
                                    removeRequest(username);
                                    try {
                                        if (server.obtenerSolicitudesAmistad(clientObject.getUsername()).size() == 0) {
                                            cardLayoutSecondary.show(cardsPanelSecondary, "defaultSecondaryPanel");
                                        }
                                    } catch (RemoteException ex) {
                                        System.out.println(
                                                "Excepcion al obtener el numero de clientes: " + ex.getMessage());
                                    }
                                    requestListPanel.revalidate();
                                    requestListPanel.repaint();
                                } catch (RemoteException ex) {
                                    System.out.println(
                                            "Excepcion al aceptar la solicitud de amistad: " + ex.getMessage());
                                }
                            }
                        });
                    }
                });
                ImageIcon remove = new ImageIcon(
                        Objects.requireNonNull(getClass().getClassLoader().getResource("Remove.png")).getFile());
                add(new JButton(remove) {
                    {
                        setAlignmentX(CENTER_ALIGNMENT);
                        setHorizontalAlignment(JLabel.CENTER);
                        setVerticalAlignment(JLabel.CENTER);
                        setBorder(BorderFactory.createEmptyBorder());
                        setContentAreaFilled(false);
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        // Establecer un tamaño máximo
                        setPreferredSize(new Dimension(40, 40));
                        addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                // Mostrar un cuadro de dialogo de confirmacion
                                int confirm = JOptionPane.showConfirmDialog(null,
                                        "¿Estás seguro de que quieres rechazar la solicitud de " + username + "?",
                                        "Confirmación", JOptionPane.YES_NO_OPTION);
                                // Si el usuario confirma, realiza la accion de rechazar la solicitud
                                if (confirm == JOptionPane.YES_OPTION) {
                                    try {
                                        server.rechazarSolicitudAmistad(clientObject.getUsername(), username);
                                        // Ocultamos este panel de requestListPanel
                                        removeRequest(username);
                                        try {
                                            if (server.obtenerSolicitudesAmistad(clientObject.getUsername())
                                                    .size() == 0) {
                                                cardLayoutSecondary.show(cardsPanelSecondary, "defaultSecondaryPanel");
                                            }
                                        } catch (RemoteException ex) {
                                            System.out.println(
                                                    "Excepcion al obtener el numero de clientes: " + ex.getMessage());
                                        }
                                        requestListPanel.revalidate();
                                        requestListPanel.repaint();
                                    } catch (RemoteException ex) {
                                        System.out.println("Excepcion al rechazar la solicitud: " + ex.getMessage());
                                    }
                                    return;
                                }
                            }
                        });
                    }
                });
            }
        });
        requestListPanel.revalidate();
        requestListPanel.repaint();
        requestListPanel.add(Box.createVerticalStrut(10));
    }

    public void removeRequest(String username) {
        // Buscar el panel correspondiente al amigo a eliminar
        for (Component component : requestListPanel.getComponents()) {
            if (component instanceof JPanel) {
                JPanel requestPanel = (JPanel) component;
                // Obtener el JLabel que contiene el nombre de usuario
                JLabel requestLabel = null;
                for (Component panelComponent : requestPanel.getComponents()) {
                    if (panelComponent instanceof JLabel && ((JLabel) panelComponent).getIcon() == null) {
                        if (((JLabel) panelComponent).getText().equals(username)) {
                            requestLabel = (JLabel) panelComponent;
                            break;
                        }
                    }
                }
                // Si se encuentra el amigo, eliminar el panel
                if (requestLabel != null) {
                    requestListPanel.remove(requestPanel);
                    break;
                }
            }
        }
        // Actualizar la interfaz
        requestListPanel.revalidate();
        requestListPanel.repaint();
    }

    private JPanel addMenuPanel() {

        // Configuracion del panel
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setPreferredSize(new Dimension(50, getHeight()));
        menuPanel.setBackground(UIUtils.COLOR_INTERACTIVE);

        // Imagenes de los botones
        ImageIcon logo = new ImageIcon(
                Objects.requireNonNull(getClass().getClassLoader().getResource("logov2n.png")).getFile());
        ImageIcon home = new ImageIcon(
                Objects.requireNonNull(getClass().getClassLoader().getResource("Home.png")).getFile());
        ImageIcon requests = new ImageIcon(
                Objects.requireNonNull(getClass().getClassLoader().getResource("Requests.png")).getFile());
        ImageIcon config = new ImageIcon(
                Objects.requireNonNull(getClass().getClassLoader().getResource("Config.png")).getFile());
        ImageIcon exit = new ImageIcon(
                Objects.requireNonNull(getClass().getClassLoader().getResource("Exit.png")).getFile());

        // Label del logo
        JLabel logoLabel = new JLabel(logo);

        // Boton de home
        JButton homeButton = new JButton(home) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    // Cambia la transparencia del fondo cuando pasa el raton
                    g.setColor(new Color(UIUtils.COLOR_INTERACTIVE_LIGHTER.getRed(),
                            UIUtils.COLOR_INTERACTIVE_LIGHTER.getGreen(),
                            UIUtils.COLOR_INTERACTIVE_LIGHTER.getBlue(),
                            250));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };
        homeButton.setBorder(BorderFactory.createEmptyBorder());
        homeButton.setContentAreaFilled(false);
        homeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        homeButton.setRolloverIcon(home);

        // Boton de solicitudes de amistad
        JButton requestsButton = new JButton(requests) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    // Cambia la transparencia del fondo cuando pasa el raton
                    g.setColor(new Color(UIUtils.COLOR_INTERACTIVE_LIGHTER.getRed(),
                            UIUtils.COLOR_INTERACTIVE_LIGHTER.getGreen(),
                            UIUtils.COLOR_INTERACTIVE_LIGHTER.getBlue(),
                            250));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };
        requestsButton.setBorder(BorderFactory.createEmptyBorder());
        requestsButton.setContentAreaFilled(false);
        requestsButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        requestsButton.setRolloverIcon(requests);

        // Boton de configuracion
        JButton configButton = new JButton(config) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    // Cambia la transparencia del fondo cuando pasa el raton
                    g.setColor(new Color(UIUtils.COLOR_INTERACTIVE_LIGHTER.getRed(),
                            UIUtils.COLOR_INTERACTIVE_LIGHTER.getGreen(),
                            UIUtils.COLOR_INTERACTIVE_LIGHTER.getBlue(),
                            250));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };
        configButton.setBorder(BorderFactory.createEmptyBorder());
        configButton.setContentAreaFilled(false);
        configButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        configButton.setRolloverIcon(config);

        // Boton de salida de la aplicacion
        JButton exitButton = new JButton(exit) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    // Cambia la transparencia del fondo cuando pasa el raton
                    g.setColor(new Color(UIUtils.COLOR_INTERACTIVE_LIGHTER.getRed(),
                            UIUtils.COLOR_INTERACTIVE_LIGHTER.getGreen(),
                            UIUtils.COLOR_INTERACTIVE_LIGHTER.getBlue(),
                            250));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };
        exitButton.setBorder(BorderFactory.createEmptyBorder());
        exitButton.setContentAreaFilled(false);
        exitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exitButton.setRolloverIcon(exit);

        // Anhadimos action listeners a los botones
        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Cambiar a la vista de chat
                homeButton.setContentAreaFilled(true);
                homeButton.setBackground(UIUtils.COLOR_INTERACTIVE_LIGHTER);
                requestsButton.setContentAreaFilled(false);
                configButton.setContentAreaFilled(false);
                exitButton.setContentAreaFilled(false);
                cardLayout.show(cardsPanel, "clientsPanel");
                cardLayoutSecondary.show(cardsPanelSecondary, "defaultSecondaryPanel");
                manageRequestsButton.setContentAreaFilled(false);
                sendRequestButton.setContentAreaFilled(false);
                seeFriendsButton.setContentAreaFilled(false);
                try {
                    if (clientObject.getNumClients() == 0) {
                        cardLayout.show(cardsPanel, "defaultPanel");
                    }
                } catch (RemoteException ex) {
                    System.out.println("Excepcion al obtener el num clientes: " + ex.getMessage());
                }
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
                exitButton.setContentAreaFilled(false);
                cardLayout.show(cardsPanel, "requestsPanel");
                cardLayoutSecondary.show(cardsPanelSecondary, "defaultSecondaryPanel");
                manageRequestsButton.setContentAreaFilled(false);
                sendRequestButton.setContentAreaFilled(false);
                seeFriendsButton.setContentAreaFilled(false);
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
                exitButton.setContentAreaFilled(false);
                cardLayout.show(cardsPanel, "configPanel");
                cardLayoutSecondary.show(cardsPanelSecondary, "defaultSecondaryPanel");
                manageRequestsButton.setContentAreaFilled(false);
                sendRequestButton.setContentAreaFilled(false);
                seeFriendsButton.setContentAreaFilled(false);
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

        // Anhadiendo botones al panel de menú
        // Anhadir un pequenho espacio entre la parte superior y el logo
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(logoLabel);
        menuPanel.add(Box.createVerticalGlue());
        menuPanel.add(homeButton);
        menuPanel.add(requestsButton);
        menuPanel.add(configButton);
        menuPanel.add(Box.createVerticalGlue());
        menuPanel.add(exitButton);

        return menuPanel;
    }

}
