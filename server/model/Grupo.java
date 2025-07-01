package server.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Grupo implements Serializable {
    private int id;
    private int idAdmin;
    private String nome;
    private String descricao;
    private String criador;
    private List<Integer> membros;

    public Grupo(int id, int idAdmin, String nome, String descricao, String criador, List<Integer> membros) {
        this.id = id;
        this.idAdmin = idAdmin;
        this.nome = nome;
        this.descricao = descricao;
        this.criador = criador;
        this.membros = new ArrayList<>(membros); // Create new list to avoid external modification
    }

    public int getId() { return id; }
    public int getIdAdmin() { return idAdmin; }
    public void setIdAdmin(int newIdAdmin) { this.idAdmin = newIdAdmin; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public String getCriador() { return criador; }
    public List<Integer> getMembros() { return new ArrayList<>(membros); } // Return copy for safety

    public void adicionarMembro(int userId) {
        if (!membros.contains(userId)) {
            membros.add(userId);
        }
    }

    public void removerMembro(int userId) {
        membros.remove(Integer.valueOf(userId));
    }

    public boolean isMembro(int userId) {
        return membros.contains(userId);
    }
}