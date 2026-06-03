package domain.model;

import java.util.Map;

/**
 * Value Object que representa una reseña/comentario asociado a un Producto.
 */
public class Resena {

    /** Resultado del análisis de sentimiento. */
    public enum Sentimiento {
        PENDIENTE, POSITIVO, NEGATIVO
    }

    private final String id;
    private final String productoId;
    private final String autorUsername;
    private final String contenido;
    private Sentimiento sentimiento;
    private double confianza;

    public Resena(String id, String productoId, String autorUsername, String contenido) {
        this.id = id;
        this.productoId = productoId;
        this.autorUsername = autorUsername;
        this.contenido = contenido;
        this.sentimiento = Sentimiento.PENDIENTE;
        this.confianza = 0.0;
    }

    /** Factory method para construir desde el Map del servidor. */
    public static Resena fromMap(Map<String, Object> map) {
        String id = map.get("id") != null ? map.get("id").toString() : "";
        if (id.isEmpty() && map.get("document_id") != null) {
            id = map.get("document_id").toString();
        }
        String productoId = map.get("productId") != null ? map.get("productId").toString() : "";
        String autor = (String) map.getOrDefault("propietario", "Anónimo");
        String contenido = (String) map.getOrDefault("contenido", "");

        Resena resena = new Resena(id, productoId, autor, contenido);

        // Parsear sentimiento si viene del servidor
        String sent = (String) map.get("sentimiento");
        if (sent != null) {
            if ("Positivo".equalsIgnoreCase(sent)) {
                resena.sentimiento = Sentimiento.POSITIVO;
            } else if ("Negativo".equalsIgnoreCase(sent)) {
                resena.sentimiento = Sentimiento.NEGATIVO;
            }
        }
        Object conf = map.get("confianza_porcentaje");
        if (conf instanceof Number) {
            resena.confianza = ((Number) conf).doubleValue();
        }

        return resena;
    }

    // Getters
    public String getId() { return id; }
    public String getProductoId() { return productoId; }
    public String getAutorUsername() { return autorUsername; }
    public String getContenido() { return contenido; }
    public Sentimiento getSentimiento() { return sentimiento; }
    public double getConfianza() { return confianza; }

    // Setters for sentiment analysis results
    public void setSentimiento(Sentimiento sentimiento) { this.sentimiento = sentimiento; }
    public void setConfianza(double confianza) { this.confianza = confianza; }
}
