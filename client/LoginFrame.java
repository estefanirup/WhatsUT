package client;

import server.rmi.AuthInterface;

import javax.swing.*;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class LoginFrame extends JFrame {

    private JTextField usuarioField;
    private JPasswordField senhaField;
    private AuthInterface authService;

    public LoginFrame() {
        setTitle("Login WhatsUT");
        setSize(300, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(5, 1));

        usuarioField = new JTextField();
        senhaField = new JPasswordField();
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Registrar");

        add(new JLabel("Usuário:"));
        add(usuarioField);
        add(new JLabel("Senha:"));
        add(senhaField);

        JPanel panel = new JPanel(new FlowLayout());
        panel.add(loginBtn);
        panel.add(registerBtn);
        add(panel);

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            authService = (AuthInterface) registry.lookup("AuthService");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao conectar com servidor RMI.", "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        loginBtn.addActionListener(e -> fazerLogin());
        registerBtn.addActionListener(e -> fazerRegistro());

        setVisible(true);
    }

    private void fazerLogin() {
        String usuario = usuarioField.getText();
        String senha = new String(senhaField.getPassword());
        try {
            if (authService.login(usuario, senha)) {
                JOptionPane.showMessageDialog(this, "Login bem-sucedido!");
                new HomeFrame(authService, usuario);
                dispose(); // fecha a janela de login
            } else {
                JOptionPane.showMessageDialog(this, "Usuário ou senha incorretos.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao realizar login.");
            e.printStackTrace();
        }
    }

    private void fazerRegistro() {
        String usuario = usuarioField.getText();
        String senha = new String(senhaField.getPassword());
        try {
            if (authService.registrar(usuario, senha)) {
                JOptionPane.showMessageDialog(this, "Usuário registrado com sucesso!");
            } else {
                JOptionPane.showMessageDialog(this, "Usuário já existe.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao registrar usuário.");
            e.printStackTrace();
        }
    }
}
