package agpg.GUI;

// Importamos los paquetes y librerias necesarias
import agpg.client.CallbackClientImpl;
import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.awt.Font;
import java.awt.BorderLayout;
import java.rmi.RemoteException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.text.BadLocationException;

// Clase principal de GUI del cliente
public class CallbackClientGUI extends JFrame {

    // Modelo de la lista de clientes
    private DefaultListModel<String> listModel;

    // Vista de la lista de clientes
    private JList<String> clientList;

    // Panel de chat
    private JPanel chatPanel;

    // Area de chat
    private JTextPane chatPane;

    // Documento del area de chat
    private StyledDocument doc;

    // Campo de mensaje
    private JTextField messageField;

    // JSplitPane para dividir la ventana
    private JSplitPane splitPane;

    // Constructor de la clase
    public CallbackClientGUI(CallbackClientImpl clientObject) {

        // Creamos la ventana principal
        setTitle("ChatApp");
        setSize(1000, 650);

        // Definimos operacion de cierre
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Anhadimos un titulo a la ventana
        try {
            JLabel titleLabel = new JLabel(clientObject.getUsername());
            titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(titleLabel, BorderLayout.NORTH);
        } catch (RemoteException e) {
            System.out.println("Excepcion al obtener el nombre de usuario: " + e);
        }

        // Inicializamos el modelo y la vista de la lista de clientes
        listModel = new DefaultListModel<>();
        clientList = new JList<>(listModel);
        clientList.setFocusable(false);

        // Anhadimos la lista de clientes a la ventana
        JScrollPane listScrollPane = new JScrollPane(clientList);

        // Inicializamos el panel de chat
        chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        doc = new DefaultStyledDocument();
        chatPane = new JTextPane();
        chatPane.setDocument(doc);
        chatPane.setFocusable(false);

        // Inicializamos el campo de mensaje (no editable)
        messageField = new JTextField();

        // Definimos los atributos
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_RIGHT);

        // Anhadimos un listener al campo de mensaje
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // Obtenemos el mensaje del campo de mensaje
                String message = messageField.getText();
                // Comprobamos que el mensaje no este vacio
                if (!message.trim().isEmpty()) {
                    try {
                        // Llamamos al metodo sendMessage del objeto cliente
                        clientObject.sendMessage(message);
                        // Limpiamos el campo de mensaje
                        messageField.setText("");
                        // Mostramos el mensaje en el area de chat
                        doc.setParagraphAttributes(doc.getLength(), 1, attrs, false);
                        doc.insertString(doc.getLength(), clientObject.getUsername() + ": " + message + "\n", attrs);
                    } catch (RemoteException | BadLocationException e) {
                        System.out.println("Excepcion al mandar el mensaje: " + e);
                    }
                }
            }
        });

        // Anhadimos el area de chat y el campo de mensaje al panel de chat
        chatPanel.add(new JScrollPane(chatPane), BorderLayout.CENTER);
        chatPanel.add(messageField, BorderLayout.SOUTH);

        // Configuramos el JSplitPane
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, chatPanel);
        splitPane.setDividerLocation(300);
        splitPane.setOneTouchExpandable(false);
        splitPane.setDividerSize(0);

        // Anhadimos el JSplitPane a la ventana
        add(splitPane, BorderLayout.CENTER);

        // Mostramos la ventana
        setVisible(true);

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

    // Metodo para actualizar el area de chat
    public void updateChat(String username, String message) {
        try {
            doc.insertString(doc.getLength(), username + ": " + message + "\n", null);
        } catch (BadLocationException e) {
            System.out.println("Excepcion al actualizar el area de chat: " + e);
        }
    }

}
