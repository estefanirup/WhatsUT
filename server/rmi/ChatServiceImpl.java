package server.rmi;

import server.model.Message;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatServiceImpl extends UnicastRemoteObject implements ChatService {
    private final ConcurrentHashMap<Integer, List<Message>> messages = new ConcurrentHashMap<>();
    private final AtomicInteger messageIdCounter = new AtomicInteger(0);

    public ChatServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public void sendMessage(Message message) throws RemoteException {
        int conversationId = getConversationId(message.getUserId(), message.getDestinatarioId());
        messages.computeIfAbsent(conversationId, k -> new ArrayList<>()).add(message);
    }

    @Override
    public List<Message> getMessages(int userId, int destinatarioId) throws RemoteException {
        int conversationId = getConversationId(userId, destinatarioId);
        return messages.getOrDefault(conversationId, new ArrayList<>());
    }

    @Override
    public List<Message> getNewMessages(int userId, int lastMessageId) throws RemoteException {
        // Implement logic to get only new messages
        List<Message> allMessages = new ArrayList<>();
        for (List<Message> convMessages : messages.values()) {
            for (Message msg : convMessages) {
                if (msg.getDestinatarioId() == userId && msg.getId() > lastMessageId) {
                    allMessages.add(msg);
                }
            }
        }
        return allMessages;
    }

    private int getConversationId(int userId1, int userId2) {
        return userId1 < userId2 ? 
            (userId1 * 10000 + userId2) : 
            (userId2 * 10000 + userId1);
    }

    public int getNextMessageId() {
        return messageIdCounter.incrementAndGet();
    }
}