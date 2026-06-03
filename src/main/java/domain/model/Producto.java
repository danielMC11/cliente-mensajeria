package domain.model;

import java.util.Map;

/**
 * Value Object que representa un Producto (archivo subido tratado como entidad de negocio).
 */
public class Producto {
    private final String id;
    private final String nombre;
    private final long tamanoBytes;
    private final String extension;
    private final String propietario;
    private final String mimeType;

    public Producto(String id, String nombre, long tamanoBytes, String extension, String propietario, String mimeType) {
        this.id = id;
        this.nombre = nombre;
        this.tamanoBytes = tamanoBytes;
        this.extension = extension;
        this.propietario = propietario;
        this.mimeType = mimeType;
    }

    /** Factory method para construir desde el Map del servidor. */
    public static Producto fromMap(Map<String, Object> map) {
        String id = map.get("id") != null ? map.get("id").toString() : "";
        String nombre = (String) map.getOrDefault("nombre", "Sin nombre");
        long tamano = 0;
        Object sizeObj = map.get("tamano_bytes");
        if (sizeObj != null) {
            try { tamano = Long.parseLong(sizeObj.toString()); } catch (Exception e) { /* ignore */ }
        }
        String extension = (String) map.getOrDefault("extension", "");
        String propietario = (String) map.getOrDefault("propietario", "Desconocido");
        String mimeType = (String) map.getOrDefault("mime_type", "application/octet-stream");
        return new Producto(id, nombre, tamano, extension, propietario, mimeType);
    }

    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public long getTamanoBytes() { return tamanoBytes; }
    public String getExtension() { return extension; }
    public String getPropietario() { return propietario; }
    public String getMimeType() { return mimeType; }

    @Override
    public String toString() {
        return nombre + " (" + propietario + ")";
    }
}
