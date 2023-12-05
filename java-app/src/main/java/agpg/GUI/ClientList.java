package agpg.GUI;

// Importamos las librerias necesarias
import javax.swing.*;
import agpg.client.CallbackClientImpl;
import java.awt.Font;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.rmi.RemoteException;

// Clase de la vista de la lista de cliente
public class ClientList<T> extends JList<T> {

    // Panel de chat
    private JPanel chatPanel;

    // Constructor de la clase
    public ClientList(ListModel<T> model, CallbackClientImpl cObj, MessageField messageField, JTextPane chatPane) {

        // Llamamos al constructor de la clase padre
        super(model);

        // Definimos el modo de seleccion de la lista
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Desactivamos el foco de la lista y definimos la fuente
        setFocusable(false);
        setFont(new Font("Arial", Font.PLAIN, 18));

        // Anhadimos el nombre de usuario como titulo a la lista
        try {
            TitledBorder t = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), cObj.getUsername());
            t.setTitleFont(new Font("Arial", Font.BOLD, 18));
            setBorder(t);
        } catch (RemoteException e) {
            System.out.println("Excepcion al obtener el nombre de usuario: " + e.getMessage());
        }

        // Listener para abrir el chat correspondiente al cliente seleccionado
        addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (getSelectedValue() != null) {
                        // T selectedClient = getSelectedValue();
                        messageField.setVisible(true);
                        chatPanel.revalidate();
                        // TODO: abrir chat con selectedClient
                    } else {
                        chatPane.setFont(new Font("Arial", Font.BOLD, 18));
                        chatPane.setText("NO HAY NINGUN USUARIO SELECCIONADO");
                        messageField.setVisible(false);
                        chatPanel.revalidate();
                    }
                }
            }
        });

    }

    public void setChatPanel(JPanel chatPanel) {
        this.chatPanel = chatPanel;
    }

}
