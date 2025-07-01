package server.model;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    private int id;
    private int userId;
    private int destinatarioId;
    private String texto;
    private Date horario;
    private boolean lido;
    
    public Message(int id, int userId, int destinatarioId, String texto) {
        this.id = id;
        this.userId = userId;
        this.destinatarioId = destinatarioId; //pode ser id de um grupo
        this.texto = texto;
        this.horario = new Date(); 
        this.lido = false;
    }
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getDestinatarioId() { return destinatarioId; }
    public String getTexto() { return texto; }
    public Date getHorario() { return horario; }
    public boolean isLido() { return lido; }
    public void setLido(boolean lido) { this.lido = lido; }
    public void setHorario(Date date) { this.horario = date; }
    public void setDestinatarioId(int newDestinatarioId) {this.destinatarioId = newDestinatarioId; }
}