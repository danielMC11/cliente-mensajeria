package data;

import domain.ports.ChatRepository;

public class H2ChatRepository implements ChatRepository {
    private final H2Database h2db;

    public H2ChatRepository(H2Database h2db) {
        this.h2db = h2db;
    }

    @Override
    public void saveMessage(String username, String content) {
        h2db.saveChatMessage(username, content);
    }

    @Override
    public void saveDocumentMetadata(String filename, long size, String extension, String mimeType, String username) {
        h2db.saveDocument(filename, size, extension, mimeType, username);
    }
}
