package data;

import domain.ports.ChatRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class H2ChatRepository implements ChatRepository {
    private final H2Database h2db;

    public H2ChatRepository(H2Database h2db) {
        this.h2db = h2db;
    }

    @Override
    public void saveMessage(String username, String content) {
        String sql = "INSERT INTO messages (username, content) VALUES (?, ?)";
        try (Connection conn = h2db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, content);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al guardar mensaje: " + e.getMessage());
        }
    }

    @Override
    public void saveDocumentMetadata(String filename, long size, String extension, String mimeType, String username) {
        String sql = "INSERT INTO documents (filename, file_size, extension, mime_type, username) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = h2db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, filename);
            pstmt.setLong(2, size);
            pstmt.setString(3, extension);
            pstmt.setString(4, mimeType);
            pstmt.setString(5, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al guardar documento: " + e.getMessage());
        }
    }

    @Override
    public void saveResena(String productoId, String autorUsername, String contenido) {
        String sql = "INSERT INTO resenas (producto_id, autor_username, contenido) VALUES (?, ?, ?)";
        try (Connection conn = h2db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productoId);
            pstmt.setString(2, autorUsername);
            pstmt.setString(3, contenido);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al guardar reseña: " + e.getMessage());
        }
    }
}
