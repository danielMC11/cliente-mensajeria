package config;

import config.models.MessageRequest;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
    private Queue<File> pendingFiles = new ConcurrentLinkedQueue<>();
    private Queue<DownloadMetadata> pendingDownloadMetadata = new ConcurrentLinkedQueue<>();

    private ConnectionHandler connectionHandler;
    private ClientRouter router;

    public TCPClient(String ip, int port, String username) {
        this.ip = ip;
        this.port = port;
        this.username = username;
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
        this.connectionHandler = new ConnectionHandler(this, router);
        new Thread(this.connectionHandler).start();
    }

    private void sendConnectAction() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);

        MessageRequest request = new MessageRequest("CONNECT", payload);
        String json = JSONSerializer.serialize(request);

        sendMessage(json);
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
        MessageRequest request = new MessageRequest("LIST_MESSAGES", new HashMap<>());
        sendMessage(JSONSerializer.serialize(request));
    }

    public void sendListDocumentsAction() {
        MessageRequest request = new MessageRequest("LIST_DOCUMENTS", new HashMap<>());
        sendMessage(JSONSerializer.serialize(request));
    }

    public void sendMessage(String message) {
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
                if (b == '\n') break;
                if (b != '\r') baos.write(b);
            }
            if (b == -1 && baos.size() == 0) return null;
            return baos.toString(StandardCharsets.UTF_8.name());
        }
        return null;
    }
    public void sendChatMessage(String content) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", this.username);
        payload.put("message", content);

        MessageRequest request = new MessageRequest("SEND_MESSAGE", payload);
        sendMessage(JSONSerializer.serialize(request));
    }

    public void sendFile(File file) {
        pendingFiles.add(file);

        String extension = "";
        int i = file.getName().lastIndexOf('.');
        if (i > 0) {
            extension = file.getName().substring(i);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("filename", file.getName());
        payload.put("size", file.length());
        payload.put("extension", extension);
        payload.put("mimeType", "application/octet-stream");
        payload.put("username", this.username);

        MessageRequest request = new MessageRequest("UPLOAD_INIT", payload);
        String json = JSONSerializer.serialize(request);
        sendMessage(json);
    }

    public void startFileTransfer(String token) {
        File file = pendingFiles.poll();
        if (file == null) return;

        new Thread(() -> {
            System.out.println("Iniciando transferencia de subida (Nuevo Socket)...");
            try (Socket fileSocket = new Socket(ip, port);
                 OutputStream os = fileSocket.getOutputStream();
                 FileInputStream fis = new FileInputStream(file)) {

                // Enviar token
                os.write((token + "\n").getBytes(StandardCharsets.UTF_8));
                os.flush();

                // Enviar archivo
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
                System.out.println("Subida finalizada y socket de archivo cerrado.");
            } catch (IOException e) {
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
        if (format != null && !format.isEmpty()) payload.put("format", format);

        MessageRequest request = new MessageRequest("DOWNLOAD_INIT", payload);
        sendMessage(JSONSerializer.serialize(request));
    }

    public void startDownloadTransfer(String token, long size) {
        DownloadMetadata metadata = pendingDownloadMetadata.poll();
        if (metadata == null) return;

        new Thread(() -> {
            System.out.println("Iniciando transferencia de descarga (Nuevo Socket) de " + size + " bytes...");
            
            String subDir = "";
            String finalFilename = metadata.filename;
            if ("ORG".equals(metadata.format)) subDir = "original";
            else if ("ENC".equals(metadata.format)) subDir = "encriptado";
            else if ("HSH".equals(metadata.format)) { subDir = "hash"; finalFilename += ".txt"; }

            File directory = new File("descargas/" + subDir);
            if (!directory.exists()) directory.mkdirs();
            File targetFile = new File(directory, finalFilename);

            boolean success = false;
            try (Socket fileSocket = new Socket(ip, port);
                 OutputStream os = fileSocket.getOutputStream();
                 InputStream is = fileSocket.getInputStream();
                 FileOutputStream fos = new FileOutputStream(targetFile)) {

                // Enviar token
                os.write((token + "\n").getBytes(StandardCharsets.UTF_8));
                os.flush();

                // Recibir archivo
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalRead = 0;
                while (totalRead < size && (bytesRead = is.read(buffer, 0, (int)Math.min(buffer.length, size - totalRead))) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                }
                fos.flush();
                success = (totalRead == size);
                System.out.println("Descarga finalizada. Éxito: " + success);
            } catch (IOException e) {
                System.err.println("Error en descarga: " + e.getMessage());
                success = false;
            }

            if (router != null) {
                router.notifyDownloadResult(success, targetFile.getName());
            }
        }).start();
    }

    public void disconnect() throws IOException {
        if (connectionHandler != null) connectionHandler.stop();
        if (out != null) out.close();
        if (rawIn != null) rawIn.close();
        if (socket != null) socket.close();
    }
}
