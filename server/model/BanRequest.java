package server.model;

import java.io.Serializable;
import java.util.Date;

public class BanRequest implements Serializable {
    private int id;
    private int requesterUserId; // ID do usuário que solicitou o banimento
    private int targetUserId;    // ID do usuário a ser banido
    private String reason;       // Razão do pedido de banimento
    private Date requestDate;    // Data da solicitação
    private String status;       // Status: PENDING, APPROVED, REJECTED
    private int adminId;         // ID do administrador que processou (se houver)
    private Date processDate;    // Data de processamento (se houver)

    public BanRequest(int id, int requesterUserId, int targetUserId, String reason) {
        this.id = id;
        this.requesterUserId = requesterUserId;
        this.targetUserId = targetUserId;
        this.reason = reason;
        this.requestDate = new Date();
        this.status = "PENDING"; // Status inicial
        this.adminId = -1; // Nenhum admin processou ainda
        this.processDate = null;
    }

    // Getters
    public int getId() { return id; }
    public int getRequesterUserId() { return requesterUserId; }
    public int getTargetUserId() { return targetUserId; }
    public String getReason() { return reason; }
    public Date getRequestDate() { return requestDate; }
    public String getStatus() { return status; }
    public int getAdminId() { return adminId; }
    public Date getProcessDate() { return processDate; }

    // Setters para atualização de status pelo admin
    public void setStatus(String status) { this.status = status; }
    public void setAdminId(int adminId) { this.adminId = adminId; }
    public void setProcessDate(Date processDate) { this.processDate = processDate; }

    @Override
    public String toString() {
        return "BanRequest{" +
               "id=" + id +
               ", requesterUserId=" + requesterUserId +
               ", targetUserId=" + targetUserId +
               ", reason='" + reason + '\'' +
               ", requestDate=" + requestDate +
               ", status='" + status + '\'' +
               ", adminId=" + adminId +
               ", processDate=" + processDate +
               '}';
    }
}
