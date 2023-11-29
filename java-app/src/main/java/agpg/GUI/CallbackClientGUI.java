package agpg.GUI;

// Importamos los paquetes y librerias necesarias
import agpg.client.CallbackClientImpl;
import javax.swing.*;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.awt.event.ActionEvent;

// Clase principal de GUI del cliente
public class CallbackClientGUI extends JFrame {

    // Modelo de la lista de clientes
    private DefaultListModel<String> listModel;

    // Vista de la lista de clientes
    private JList<String> clientList;

    // Panel de chat
    private JPanel chatPanel;

    // Area de chat
    private JTextArea chatArea;

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

        // Anhadimos la lista de clientes a la ventana
        JScrollPane listScrollPane = new JScrollPane(clientList);

        // Inicializamos el panel de chat
        chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatArea = new JTextArea();

        // Inicializamos el campo de mensaje (no editable)
        messageField = new JTextField();

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
                        chatArea.append(clientObject.getUsername() + ": " + message + "\n");
                    } catch (RemoteException e) {
                        System.out.println("Excepcion al mandar el mensaje: " + e);
                    }
                }
            }
        });

        // Anhadimos el area de chat y el campo de mensaje al panel de chat
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

    // Getter de la lista de clientes
    public JList<String> getClientList() {
        return clientList;
    }

    // Getter del area de chat
    public JTextArea getChatArea() {
        return chatArea;
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
