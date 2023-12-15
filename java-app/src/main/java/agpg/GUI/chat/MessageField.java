package agpg.GUI.chat;

// Importamos las librerias necesarias (Swing, CallbackClientImpl, awt, RMI y util)
import javax.swing.*;
import agpg.client.IClient;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.*;
import java.rmi.RemoteException;

// Clase del campo de mensaje
public class MessageField extends JTextField {

    // Constructor de la clase
    public MessageField(IClient cObj, ChatUI gui) {

        // Definimos la fuente del campo de mensaje
        setFont(new Font("Arial", Font.PLAIN, 18));

        // Definimos el texto por defecto del campo de mensaje y su color
        setText(" Escribe un mensaje");
        setForeground(Color.GRAY);

        // Anhadimos un listener de accion al campo de mensaje (al pulsar enter)
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String message = getText();
                if (!message.trim().isEmpty()) {
                    try {
                        String selectedClient = gui.selectClient();
                        // Llamamos al metodo sendMessage del objeto cliente
                        cObj.sendMessage(selectedClient, message);
                        setText("");
                        // Actualizamos el chat propio
                        gui.updateMyChat(selectedClient, new Message(message, cObj.getUsername(), selectedClient));
                    } catch (RemoteException e) {
                        System.out.println("Excepcion al mandar el mensaje: " + e.getMessage());
                    }
                }
            }
        });

        // Anhadimos un listener de foco al campo de mensaje
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getText().equals(" Escribe un mensaje")) {
                    setText("");
                }
                setForeground(Color.BLACK);
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setText(" Escribe un mensaje");
                }
                setForeground(Color.GRAY);
            }
        });

    }

}
