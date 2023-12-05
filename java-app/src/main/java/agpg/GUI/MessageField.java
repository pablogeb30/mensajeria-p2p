package agpg.GUI;

// Importamos las librerias necesarias
import javax.swing.*;
import agpg.client.CallbackClientImpl;
import javax.swing.text.*;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

// Clase del campo de mensaje
public class MessageField extends JTextField {

    // Constructor de la clase
    public MessageField(CallbackClientImpl clientObject, StyledDocument doc) {

        // Definimos los atributos
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_RIGHT);

        // Definimos la fuente del campo de mensaje
        setFont(new Font("Arial", Font.PLAIN, 18));

        // Definimos el texto por defecto del campo de mensaje
        setText(" Escribe un mensaje");
        setForeground(Color.GRAY);

        // Anhadimos un listener al campo de mensaje
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // Obtenemos el mensaje del campo de mensaje
                String message = getText();
                // Comprobamos que el mensaje no este vacio
                if (!message.trim().isEmpty()) {
                    try {
                        // Llamamos al metodo sendMessage del objeto cliente
                        clientObject.sendMessage(message);
                        // Limpiamos el campo de mensaje
                        setText("");
                        // Mostramos el mensaje en el area de chat
                        doc.setParagraphAttributes(doc.getLength(), 1, attrs, false);
                        doc.insertString(doc.getLength(), clientObject.getUsername() + ": " + message + "\n", attrs);
                    } catch (RemoteException | BadLocationException e) {
                        System.out.println("Excepcion al mandar el mensaje: " + e.getMessage());
                    }
                }
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (getText().equals(" Escribe un mensaje")) {
                    setText("");
                    setForeground(Color.BLACK);
                }
            }
        });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setForeground(Color.GRAY);
                    setText(" Escribe un mensaje");

                }
            }
        });

    }

}
