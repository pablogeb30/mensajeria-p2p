package agpg.client;

// Importamos las librerias necesarias
import javax.swing.*;
import java.awt.BorderLayout;

// Clase principal de GUI del cliente
public class CallbackClientGUI extends JFrame {

    // Modelo de la lista de clientes
    private DefaultListModel<String> listModel;

    // Vista de la lista de clientes
    private JList<String> clientList;

    // Area de chat y campo de mensaje
    private JTextArea chatArea;
    private JTextField messageField;

    // JSplitPane para dividir la ventana
    private JSplitPane splitPane;

    // Constructor de la clase
    public CallbackClientGUI() {
        // Creamos la ventana principal
        setTitle("ChatApp");
        setSize(1000, 650);

        // Definimos operacion de cierre
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Inicializamos el modelo y la vista de la lista de clientes
        listModel = new DefaultListModel<>();
        clientList = new JList<>(listModel);

        // Anhadimos la lista de clientes a la ventana
        JScrollPane listScrollPane = new JScrollPane(clientList);

        // Inicializamos el panel de chat
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        messageField = new JTextField();
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatPanel.add(messageField, BorderLayout.SOUTH);

        // Configuramos el JSplitPane
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, chatPanel);
        splitPane.setDividerLocation(300);

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

}
