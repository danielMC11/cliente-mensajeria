package network;

import models.MessageRequest;
import domain.ports.ChatRepository;
import domain.ports.UIEventPublisher;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TCPClient {
    private Socket socket;
    private PrintWriter out;
    private InputStream rawIn;
    private String ip;
    private int port;
    private String username;

    // MODIFICADO: Ahora la cola almacena PendingFile en lugar de solo File
    private Queue<PendingFile> pendingFiles = new ConcurrentLinkedQueue<>();
    private Queue<DownloadMetadata> pendingDownloadMetadata = new ConcurrentLinkedQueue<>();

    private ConnectionHandler connectionHandler;
    private ClientRouter router;
    private ChatRepository repository;
    private UIEventPublisher uiPublisher;

    public TCPClient(String ip, int port, String username, ChatRepository repository, UIEventPublisher uiPublisher) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.repository = repository;
        this.uiPublisher = uiPublisher;
    }

    public void connect() throws IOException {
        System.out.println("Intentando conectar a " + ip + ":" + port + "...");
        socket = new Socket(ip, port);
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        rawIn = socket.getInputStream();

        System.out.println("Conexión establecida. Enviando CONNECT...");
        sendConnectAction();
    }

    public void startListening(ClientRouter router) {
        this.router = router;
        this.connectionHandler = new ConnectionHandler(this, router, uiPublisher);
        new Thread(this.connectionHandler).start();
    }

    private void sendConnectAction() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);

        MessageRequest request = new MessageRequest("CONNECT", payload);
        sendMessage(JSONSerializer.serialize(request));
        requestInitialData();
    }

    public void requestInitialData() {
        sendListClientsAction();
        sendListLogsAction();
        sendListMessagesAction();
        sendListDocumentsAction();
    }

    public void sendListClientsAction() {
        MessageRequest request = new MessageRequest("LIST_CLIENTS", new HashMap<>());
        sendMessage(JSONSerializer.serialize(request));
    }

    public void sendListLogsAction() {
        MessageRequest request = new MessageRequest("LIST_LOGS", new HashMap<>());
        sendMessage(JSONSerializer.serialize(request));
    }

    public void sendListMessagesAction() {
        Map<String, Object> payload = new HashMap<>();
        if (this.username != null) {
            payload.put("username", this.username);
        }
        MessageRequest request = new MessageRequest("LIST_MESSAGES", payload);
        sendMessage(JSONSerializer.serialize(request));
    }

    public void sendListDocumentsAction() {
        Map<String, Object> payload = new HashMap<>();
        if (this.username != null) {
            payload.put("username", this.username);
        }
        MessageRequest request = new MessageRequest("LIST_DOCUMENTS", payload);
        sendMessage(JSONSerializer.serialize(request));
    }

    public void sendListPeerInfoAction() {
        MessageRequest request = new MessageRequest("LIST_PEER_INFO", new HashMap<>());
        sendMessage(JSONSerializer.serialize(request));
    }

    public void sendListPeerLogsAction() {
        MessageRequest request = new MessageRequest("LIST_PEER_LOGS", new HashMap<>());
        sendMessage(JSONSerializer.serialize(request));
    }

    public void sendDirectMessage(String targetUsername, String content) {
        if (repository != null) {
            repository.saveMessage(this.username, content);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", this.username);
        payload.put("message", content);
        payload.put("targetUsername", targetUsername);

        MessageRequest request = new MessageRequest("SEND_MESSAGE", payload);
        sendMessage(JSONSerializer.serialize(request));
    }

    public void sendAnalyzeMessage(String content) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("mensaje", content);

        MessageRequest request = new MessageRequest("ANALYZE_MESSAGE", payload);
        sendMessage(JSONSerializer.serialize(request));
    }

    /**
     * Envía una reseña asociada a un producto.
     * Flujo automático: 1) SEND_MESSAGE con productId, 2) ANALYZE_MESSAGE automático.
     */
    public void sendResena(String productoId, String contenido) {
        if (repository != null) {
            repository.saveResena(productoId, this.username, contenido);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", this.username);
        payload.put("message", contenido);
        payload.put("productId", productoId);

        MessageRequest request = new MessageRequest("SEND_MESSAGE", payload);
        sendMessage(JSONSerializer.serialize(request));

        // Análisis de sentimiento automático (sin botón manual)
        sendAnalyzeMessage(contenido);
    }

    /**
     * Solicita la lista de mensajes (incluye reseñas con productId).
     * El handler del lado cliente separará chat de reseñas.
     */
    public void sendListResenasAction(String productoId) {
        Map<String, Object> payload = new HashMap<>();
        if (this.username != null) {
            payload.put("username", this.username);
        }
        if (productoId != null) {
            payload.put("productId", productoId);
        }
        MessageRequest request = new MessageRequest("LIST_MESSAGES", payload);
        sendMessage(JSONSerializer.serialize(request));
    }

    public synchronized void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            System.out.println("Mensaje enviado: " + message);
        }
    }

    public String receiveMessage() throws IOException {
        if (rawIn != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b;
            while ((b = rawIn.read()) != -1) {
                if (b == '\n')
                    break;
                if (b != '\r')
                    baos.write(b);
            }
            if (b == -1 && baos.size() == 0)
                return null;
            return baos.toString(StandardCharsets.UTF_8.name());
        }
        return null;
    }

    public void sendChatMessage(String content) {
        if (repository != null) {
            repository.saveMessage(this.username, content);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", this.username);
        payload.put("message", content);

        MessageRequest request = new MessageRequest("SEND_MESSAGE", payload);
        sendMessage(JSONSerializer.serialize(request));
    }

    // NUEVA CLASE INTERNA: Para guardar el archivo y el usuario destino
    private static class PendingFile {
        File file;
        String targetUsername;

        PendingFile(File file, String targetUsername) {
            this.file = file;
            this.targetUsername = targetUsername;
        }
    }

    public void sendFile(File file, String targetUsername) {
        // MODIFICADO: Guardar el objeto completo en la cola
        pendingFiles.add(new PendingFile(file, targetUsername));

        String extension = "";
        String filename = file.getName();
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            extension = filename.substring(i + 1);
        }

        if (repository != null) {
            repository.saveDocumentMetadata(
                    filename,
                    file.length(),
                    extension,
                    "application/octet-stream",
                    this.username
            );
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("filename", filename);
        payload.put("size", file.length());
        payload.put("extension", extension);
        payload.put("mimeType", "application/octet-stream");
        payload.put("username", this.username);

        if (targetUsername != null && !targetUsername.trim().isEmpty()) {
            payload.put("targetUsername", targetUsername);
        }

        MessageRequest request = new MessageRequest("UPLOAD_INIT", payload);
        sendMessage(JSONSerializer.serialize(request));
    }

    public void startFileTransfer(String token) {
        PendingFile pending = pendingFiles.poll();
        if (pending == null)
            return;

        File file = pending.file;
        String targetUsername = pending.targetUsername;

        new Thread(() -> {
            System.out.println("Iniciando transferencia de subida (Nuevo Socket)...");

            // 1. Instanciamos el socket y aplicamos las configuraciones primero
            try (Socket fileSocket = new Socket(ip, port)) {

                // --- INICIO DE MODIFICACIONES ---
                // Activa el KeepAlive de TCP. Esto envía paquetes "fantasma" para
                // evitar que Windows, el router o el firewall cierren la conexión por inactividad.
                fileSocket.setKeepAlive(true);

                // Configura el Timeout en 10 minutos (600,000 milisegundos).
                // Obliga al cliente a ser paciente mientras el servidor cifra la OVA.
                fileSocket.setSoTimeout(600000);
                // --- FIN DE MODIFICACIONES ---

                // 2. Ahora sí, abrimos los streams dentro de otro try-with-resources
                try (OutputStream os = fileSocket.getOutputStream();
                     InputStream is = fileSocket.getInputStream();
                     FileInputStream fis = new FileInputStream(file)) {

                    os.write((token + "\n").getBytes(StandardCharsets.UTF_8));
                    os.flush();

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    os.flush();

                    // IMPORTANTE: Avisar al servidor que ya no enviaremos más bytes.
                    fileSocket.shutdownOutput();

                    // Quedarnos esperando a que el servidor termine de cifrar y nos responda
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    String respuestaServidor = reader.readLine(); // El hilo esperará aquí hasta 10 minutos

                    if ("SUCCESS".equals(respuestaServidor)) {
                        System.out.println("Subida finalizada, confirmada por el servidor.");
                        uiPublisher.onUploadStatus(true, "Archivo enviado con exito para", targetUsername);
                    } else {
                        System.out.println("El servidor rechazó el archivo o hubo un error.");
                        uiPublisher.onUploadStatus(false, "Error en el servidor al guardar el archivo para", targetUsername);
                    }
                }

            } catch (IOException e) {
                uiPublisher.onUploadStatus(false, "Error de red en subida de archivo para", targetUsername);
                System.err.println("Error en subida: " + e.getMessage());
            }
        }).start();
    }
    private static class DownloadMetadata {
        String docId;
        String filename;
        String format;

        DownloadMetadata(String docId, String filename, String format) {
            this.docId = docId;
            this.filename = filename;
            this.format = format;
        }
    }

    public void requestDownload(String docId, String filename, String format) {
        pendingDownloadMetadata.add(new DownloadMetadata(docId, filename, format));
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_id", Long.parseLong(docId));
        if (format != null && !format.isEmpty())
            payload.put("format", format);
        payload.put("username", this.username);

        MessageRequest request = new MessageRequest("DOWNLOAD_INIT", payload);
        sendMessage(JSONSerializer.serialize(request));
    }

    public void startDownloadTransfer(String token, long size) {
        DownloadMetadata metadata = pendingDownloadMetadata.poll();
        if (metadata == null)
            return;

        new Thread(() -> {
            System.out.println("Iniciando transferencia de descarga (Nuevo Socket) de " + size + " bytes...");

            String subDir = "";
            String finalFilename = metadata.filename;
            if ("ORG".equals(metadata.format))
                subDir = "original";
            else if ("ENC".equals(metadata.format))
                subDir = "encriptado";
            else if ("HSH".equals(metadata.format)) {
                subDir = "hash";
                finalFilename += ".txt";
            }

            File directory = new File("descargas/" + subDir);
            if (!directory.exists())
                directory.mkdirs();

            File targetFile = new File(directory, finalFilename);

            if (targetFile.exists()) {
                String nameOnly = finalFilename;
                String extension = "";
                int dotIndex = finalFilename.lastIndexOf('.');
                if (dotIndex > 0) {
                    nameOnly = finalFilename.substring(0, dotIndex);
                    extension = finalFilename.substring(dotIndex);
                }

                int count = 1;
                while (targetFile.exists()) {
                    targetFile = new File(directory, nameOnly + " (" + count + ")" + extension);
                    count++;
                }
            }

            boolean success = false;
            try (Socket fileSocket = new Socket(ip, port);
                 OutputStream os = fileSocket.getOutputStream();
                 InputStream is = fileSocket.getInputStream();
                 FileOutputStream fos = new FileOutputStream(targetFile)) {

                os.write((token + "\n").getBytes(StandardCharsets.UTF_8));
                os.flush();

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalRead = 0;
                long lastUpdate = 0;

                while (totalRead < size && (bytesRead = is.read(buffer, 0, (int) Math.min(buffer.length, size - totalRead))) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;

                    if (uiPublisher != null && (totalRead - lastUpdate > 1024 * 50 || totalRead == size)) {
                        uiPublisher.onDownloadProgress(targetFile.getName(), totalRead, size);
                        lastUpdate = totalRead;
                    }
                }
                fos.flush();
                success = (totalRead == size);
                System.out.println("Descarga finalizada. Éxito: " + success);

            } catch (IOException e) {
                System.err.println("Error en descarga: " + e.getMessage());
                success = false;
            }

            if (uiPublisher != null) {
                uiPublisher.onDownloadFinished(success, targetFile.getName());
            }
        }).start();
    }

    public void sendDisconnectAction() {
        Map<String, Object> payload = new HashMap<>();
        if (this.username != null) {
            payload.put("username", this.username);
        }
        MessageRequest request = new MessageRequest("DISCONNECT", payload);
        sendMessage(JSONSerializer.serialize(request));
    }

    public void disconnect() throws IOException {
        sendDisconnectAction();
        if (connectionHandler != null)
            connectionHandler.stop();
        if (out != null)
            out.close();
        if (rawIn != null)
            rawIn.close();
        if (socket != null)
            socket.close();
    }

    public void sendComment(String documentId, String content) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("documentId", documentId);
        payload.put("username", this.username); // Se usa el username del cliente TCP actual
        payload.put("content", content);

        // Se usa la acción COMMENT_DOCUMENT que el enrutador del servidor está esperando
        MessageRequest request = new MessageRequest("COMMENT_DOCUMENT", payload);
        sendMessage(JSONSerializer.serialize(request));
    }

    /**
     * Envía una petición al servidor para listar todos los comentarios asociados a un documento.
     * Alínea sus campos con lo esperado por ListCommentsHandlerClient.
     *
     * @param documentId Identificador del documento del cual se quieren recuperar los comentarios.
     */
    public void sendListComments(Long documentId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_id", documentId);

        // Se usa la acción LIST_COMMENTS asociada a ListCommentsHandlerClient
        MessageRequest request = new MessageRequest("LIST_COMMENTS", payload);
        sendMessage(JSONSerializer.serialize(request));
    }

}