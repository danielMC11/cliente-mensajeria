package domain.ports;

public interface ChatRepository {
    void saveMessage(String username, String content);
    void saveDocumentMetadata(String filename, long size, String extension, String mimeType, String username);

    /** Guarda una reseña asociada a un producto en la BD local. */
    void saveResena(String productoId, String autorUsername, String contenido);
}
