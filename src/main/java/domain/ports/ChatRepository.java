package domain.ports;

public interface ChatRepository {
    void saveMessage(String username, String content);
    void saveDocumentMetadata(String filename, long size, String extension, String mimeType, String username);
}
