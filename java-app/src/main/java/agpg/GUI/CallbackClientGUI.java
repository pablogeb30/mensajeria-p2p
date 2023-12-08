package agpg.GUI;

// Importamos las librerias necesarias (CallbackClientImpl, Swing y awt)
import agpg.client.CallbackClientImpl;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.BorderLayout;

// Clase principal de GUI del cliente
public class CallbackClientGUI extends JFrame {

    // Modelo de la lista de clientes
    private DefaultListModel<String> listModel;

    // Vista de la lista de clientes
    private ClientList<String> clientList;

    // Estilo del documento
    private StyledDocument doc;

    // Campo de mensaje
    private MessageField messageField;

    // Constructor de la clase
    public CallbackClientGUI(CallbackClientImpl clientObject) {

        // Definimos el tamanho de la ventana y la operacion de cierre
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Inicializamos el estilo del documento y el campo de mensaje
        doc = new DefaultStyledDocument();
        messageField = new MessageField(clientObject, doc);
        messageField.setVisible(false);

        // Inicializamos el panel de chat
        JTextPane chatPane = new JTextPane();
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPane.setDocument(doc);
        chatPane.setFocusable(false);

        // Inicializamos el modelo y la vista de la lista de clientes
        listModel = new DefaultListModel<>();
        clientList = new ClientList<>(listModel, clientObject, messageField, chatPane);

        // Incluimos la lista dentro de un JScrollPane
        JScrollPane listScrollPane = new JScrollPane(clientList);

        // Anhadimos el area de chat y el campo de mensaje al panel de chat
        chatPanel.add(new JScrollPane(chatPane), BorderLayout.CENTER);
        chatPanel.add(messageField, BorderLayout.SOUTH);
        clientList.setChatPanel(chatPanel);

        // Configuramos el JSplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, chatPanel);
        splitPane.setDividerLocation(300);
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
            System.out.println("Excepcion al actualizar el area de chat: " + e.getMessage());
        }
    }

}
