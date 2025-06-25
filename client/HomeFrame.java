package client;

import server.rmi.AuthInterface;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class HomeFrame extends JFrame {

    public HomeFrame(AuthInterface authService, String usuarioAtual) {
        setTitle("Usuários Online - WhatsUT");
        setSize(300, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            List<String> usuarios = authService.listarUsuarios();
            DefaultListModel<String> model = new DefaultListModel<>();
            for (String u : usuarios) {
                String texto = u.equals(usuarioAtual) ? u + " (você)" : u;
                model.addElement(texto);
            }

            JList<String> userList = new JList<>(model);
            add(new JScrollPane(userList), BorderLayout.CENTER);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar usuários.");
            e.printStackTrace();
        }

        setVisible(true);
    }
}
