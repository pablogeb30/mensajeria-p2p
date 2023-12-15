package agpg.GUI.chat;

// Importamos las librerias necesarias (Swing, CallbackClientImpl, awt y RMI)
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import agpg.client.IClient;
import agpg.GUI.login.Utils.UIUtils;
import java.awt.Font;
import java.util.HashMap;
import java.util.ArrayList;

// Clase de la vista de la lista de cliente
public class ClientList extends JList<String> {

    // Constructor de la clase
    public ClientList(ListModel<String> model, IClient cObj, MessageField messageField,
            JButton sendButton,
            ChatUI gui, JPanel chatPanel, HashMap<String, ArrayList<Message>> messagesMap, JTextPane chatPane) {

        // Llamamos al constructor de la clase padre
        super(model);

        // Definimos el modo de seleccion de la lista y su color de fondo
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setBackground(UIUtils.COLOR_INTERACTIVE_DARKER);

        // Desactivamos el foco de la lista y definimos la fuente
        setFocusable(false);
        setFont(new Font("Arial", Font.PLAIN, 18));

        // Quitamos el borde
        setBorder(BorderFactory.createEmptyBorder());

        JLabel label1 = new JLabel(" No hay ningún cliente seleccionado");
        label1.setFont(new Font("Arial", Font.BOLD, 18));
        label1.setHorizontalAlignment(JLabel.CENTER);
        // Hay que arreglarlo
        // chatPanel.add(label1);

        // Listener para abrir el chat correspondiente al cliente seleccionado
        addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (getSelectedValue() != null) {
                        String selectedClient = getSelectedValue();
                        if (selectedClient != null) {
                            gui.setChat(selectedClient);
                            messageField.setVisible(true);
                            sendButton.setVisible(true);
                            chatPanel.revalidate();
                        }
                    } else {
                        chatPane.setText("");
                        chatPane.setEditable(false);
                        messageField.setText(" Escribe un mensaje");
                        messageField.setVisible(false);
                        sendButton.setVisible(false);
                        chatPanel.revalidate();
                    }
                }
            }
        });

    }

}
