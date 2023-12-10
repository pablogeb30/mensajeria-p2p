package agpg.GUI.login;

import agpg.GUI.login.Toaster.Toaster;
import agpg.GUI.login.Utils.*;
import agpg.client.CallbackClientImpl;
import agpg.client.CallbackClientInterface;
import agpg.server.CallbackServerInterface;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import javax.swing.*;
import java.rmi.RemoteException;

public class LoginUI extends JFrame {

    private final Toaster toaster;
    private CallbackServerInterface server;
    private boolean flag;

    public LoginUI(CallbackServerInterface server, boolean f) {

        this.server = server;
        flag = f;

        JPanel mainJPanel = getMainJPanel();

        addLogo(mainJPanel);

        addSeparator(mainJPanel);

        addUsernameTextField(mainJPanel);

        if (flag) {
            addMailTextField(mainJPanel);
        }

        addPasswordTextField(mainJPanel);

        addLoginButton(mainJPanel);

        addRegisterButton(mainJPanel);

        this.add(mainJPanel);
        this.pack();
        this.setVisible(true);
        this.toFront();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);

        toaster = new Toaster(mainJPanel);
    }

    private JPanel getMainJPanel() {
        this.setUndecorated(true);

        Dimension size = new Dimension(800, 400);

        JPanel panel1 = new JPanel();
        panel1.setSize(size);
        panel1.setPreferredSize(size);
        panel1.setBackground(UIUtils.COLOR_BACKGROUND);
        panel1.setLayout(null);

        MouseAdapter ma = new MouseAdapter() {
            int lastX, lastY;

            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getXOnScreen();
                lastY = e.getYOnScreen();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();
                setLocation(getLocationOnScreen().x + x - lastX, getLocationOnScreen().y + y - lastY);
                lastX = x;
                lastY = y;
            }
        };

        panel1.addMouseListener(ma);
        panel1.addMouseMotionListener(ma);

        return panel1;
    }

    private void addSeparator(JPanel panel1) {
        JSeparator separator1 = new JSeparator();
        separator1.setOrientation(SwingConstants.VERTICAL);
        separator1.setForeground(UIUtils.COLOR_OUTLINE);
        panel1.add(separator1);
        separator1.setBounds(310, 80, 1, 240);
    }

    private void addLogo(JPanel panel1) {
        ImageIcon logo = new ImageIcon(
                Objects.requireNonNull(getClass().getClassLoader().getResource("logov2.png")).getFile());
        ImageIcon scaledIcon = new ImageIcon(logo.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH));
        JLabel label1 = new JLabel();
        label1.setFocusable(false);
        label1.setIcon(scaledIcon);
        panel1.add(label1);
        label1.setBounds(50, 50, 300, 300);
    }

    private void addUsernameTextField(JPanel panel1) {
        TextFieldUsername usernameField = new TextFieldUsername();
        usernameField.setText(UIUtils.PLACEHOLDER_TEXT_USERNAME);
        usernameField.setForeground(UIUtils.COLOR_OUTLINE);

        if (flag) {
            usernameField.setBorderColor(UIUtils.COLOR_OUTLINE);
        }

        usernameField.setName("usernameField");

        usernameField.setBounds(423, 139, 250, 44);

        usernameField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (usernameField.getText().equals(UIUtils.PLACEHOLDER_TEXT_USERNAME)) {
                    usernameField.setText("");
                }
                usernameField.setForeground(Color.white);
                usernameField.setBorderColor(UIUtils.COLOR_INTERACTIVE);
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (usernameField.getText().isEmpty()) {
                    usernameField.setText(UIUtils.PLACEHOLDER_TEXT_USERNAME);
                }
                usernameField.setForeground(UIUtils.COLOR_OUTLINE);
                usernameField.setBorderColor(UIUtils.COLOR_OUTLINE);
            }
        });

        panel1.add(usernameField);
    }

    private void addMailTextField(JPanel panel1) {
        TextFieldUsername mailField = new TextFieldUsername();
        mailField.setText(UIUtils.PLACEHOLDER_TEXT_MAIL);
        mailField.setForeground(UIUtils.COLOR_OUTLINE);

        mailField.setName("mailField");

        mailField.setBounds(423, 80, 250, 44);

        mailField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (mailField.getText().equals(UIUtils.PLACEHOLDER_TEXT_MAIL)) {
                    mailField.setText("");
                }
                mailField.setForeground(Color.white);
                mailField.setBorderColor(UIUtils.COLOR_INTERACTIVE);
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (mailField.getText().isEmpty()) {
                    mailField.setText(UIUtils.PLACEHOLDER_TEXT_MAIL);
                }
                mailField.setForeground(UIUtils.COLOR_OUTLINE);
                mailField.setBorderColor(UIUtils.COLOR_OUTLINE);
            }
        });

        panel1.add(mailField);
    }

    private void addPasswordTextField(JPanel panel1) {
        TextFieldPassword passwordField = new TextFieldPassword();
        passwordField.setText(UIUtils.PLACEHOLDER_TEXT_PASSWORD);
        passwordField.setForeground(UIUtils.COLOR_OUTLINE);
        passwordField.setName("passwordField");

        passwordField.setBounds(423, 198, 250, 44);
        passwordField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (new String(passwordField.getPassword()).equals(UIUtils.PLACEHOLDER_TEXT_PASSWORD)) {
                    passwordField.setText("");
                }
                passwordField.setForeground(Color.white);
                passwordField.setBorderColor(UIUtils.COLOR_INTERACTIVE);
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (new String(passwordField.getPassword()).isEmpty()) {
                    passwordField.setText(UIUtils.PLACEHOLDER_TEXT_PASSWORD);
                }
                passwordField.setForeground(UIUtils.COLOR_OUTLINE);
                passwordField.setBorderColor(UIUtils.COLOR_OUTLINE);
            }
        });

        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    String username = null;
                    String password = null;
                    String mail = null;
                    for (Component c : panel1.getComponents()) {
                        if ("usernameField".equals(c.getName())) {
                            username = ((TextFieldUsername) c).getText();
                            continue;
                        }
                        if ("passwordField".equals(c.getName())) {
                            password = new String(passwordField.getPassword());
                            continue;
                        }
                        if ("mailField".equals(c.getName())) {
                            mail = ((TextFieldUsername) c).getText();
                            continue;
                        }
                    }
                    loginEventHandler(username, password, mail);
                }
            }
        });

        panel1.add(passwordField);
    }

    private void addLoginButton(JPanel panel1) {
        final Color[] loginButtonColors = { UIUtils.COLOR_INTERACTIVE, Color.white };

        JLabel loginButton = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = UIUtils.get2dGraphics(g);
                super.paintComponent(g2);

                Insets insets = getInsets();
                int w = getWidth() - insets.left - insets.right;
                int h = getHeight() - insets.top - insets.bottom;
                g2.setColor(loginButtonColors[0]);
                g2.fillRoundRect(insets.left, insets.top, w, h, UIUtils.ROUNDNESS, UIUtils.ROUNDNESS);

                FontMetrics metrics = g2.getFontMetrics(UIUtils.FONT_GENERAL_UI);
                if (!flag) {
                    int x2 = (getWidth() - metrics.stringWidth(UIUtils.BUTTON_TEXT_LOGIN)) / 2;
                    int y2 = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
                    g2.setFont(UIUtils.FONT_GENERAL_UI);
                    g2.setColor(loginButtonColors[1]);
                    g2.drawString(UIUtils.BUTTON_TEXT_LOGIN, x2, y2);
                }
                if (flag) {
                    int x2 = (getWidth() - metrics.stringWidth(UIUtils.BUTTON_TEXT_REGISTER)) / 2;
                    int y2 = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
                    g2.setFont(UIUtils.FONT_GENERAL_UI);
                    g2.setColor(loginButtonColors[1]);
                    g2.drawString(UIUtils.BUTTON_TEXT_REGISTER, x2, y2);
                }
            }
        };

        loginButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                String username = null;
                String password = null;
                String mail = null;
                for (Component c : panel1.getComponents()) {
                    if ("usernameField".equals(c.getName())) {
                        username = ((TextFieldUsername) c).getText();
                        continue;
                    }
                    if ("passwordField".equals(c.getName())) {
                        password = new String(((TextFieldPassword) c).getPassword());
                        continue;
                    }
                    if ("mailField".equals(c.getName())) {
                        mail = ((TextFieldUsername) c).getText();
                        continue;
                    }
                }
                loginEventHandler(username, password, mail);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                loginButtonColors[0] = UIUtils.COLOR_INTERACTIVE_DARKER;
                loginButtonColors[1] = UIUtils.OFFWHITE;
                loginButton.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginButtonColors[0] = UIUtils.COLOR_INTERACTIVE;
                loginButtonColors[1] = Color.white;
                loginButton.repaint();
            }
        });

        loginButton.setBackground(UIUtils.COLOR_BACKGROUND);
        loginButton.setBounds(423, 277, 250, 44);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel1.add(loginButton);
    }

    private void addRegisterButton(JPanel panel1) {
        if (!flag) {
            panel1.add(new HyperlinkText(UIUtils.BUTTON_TEXT_REGISTER, 524, 330, () -> {
                this.dispose();
                new LoginUI(server, true);
            }));
        }
        if (flag) {
            panel1.add(new HyperlinkText(UIUtils.BUTTON_TEXT_LOGIN, 524, 330, () -> {
                this.dispose();
                new LoginUI(server, false);
            }));
        }
    }

    private void loginEventHandler(String username, String password, String mail) {
        try {
            if (!flag) {
                boolean registrado = server.iniciarSesion(username, password);
                if (!registrado) {
                    toaster.error("Usuario y/o contrasenha no validos.");
                    return;
                }
                this.dispose();
                CallbackClientInterface callbackObj = new CallbackClientImpl(username);
                server.registerCallback(callbackObj);
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        server.unregisterCallback(callbackObj);
                    } catch (RemoteException e) {
                        System.out.println("Excepcion en el shutdown hook: " + e.getMessage());
                    }
                }));
            }
            if (flag) {
                boolean registrado = server.registrarCliente(username, password, mail);
                if (!registrado) {
                    toaster.error("Usuario ya existente.");
                    return;
                }
                this.dispose();
                CallbackClientInterface callbackObj = new CallbackClientImpl(username);
                server.registerCallback(callbackObj);
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        server.unregisterCallback(callbackObj);
                    } catch (RemoteException e) {
                        System.out.println("Excepcion en el shutdown hook: " + e.getMessage());
                    }
                }));
            }
        } catch (Exception e) {
            toaster.error("Error al iniciar sesion: " + e.getMessage());
        }
    }
}
