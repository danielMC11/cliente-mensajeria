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
    private BufferedReader in;
    private String ip;
    private int port;
    private String username;
    private Queue<File> pendingFiles = new ConcurrentLinkedQueue<>();

    private ConnectionHandler connectionHandler;

    public TCPClient(String ip, int port, String username) {
        this.ip = ip;
        this.port = port;
        this.username = username;
    }

    public void connect() throws IOException {
        System.out.println("Intentando conectar a " + ip + ":" + port + "...");
        socket = new Socket(ip, port);
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        System.out.println("Conexión establecida. Enviando CONNECT...");
        sendConnectAction();
    }

    public void startListening(ClientRouter router) {
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
        if (in != null) {
            return in.readLine();
        }
        return null;
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
        payload.put("mimeType", "application/octet-stream"); // Placeholder
        payload.put("username", this.username);

        MessageRequest request = new MessageRequest("UPLOAD_INIT", payload);
        String json = JSONSerializer.serialize(request);
        sendMessage(json);
    }

    public void startFileTransfer(String token) {
        File file = pendingFiles.poll();
        if (file == null) {
            System.err.println("No hay archivos pendientes para transferir.");
            return;
        }

        new Thread(() -> {
            try {
                OutputStream os = socket.getOutputStream();

                // Enviar el token seguido de un salto de línea
                String tokenLine = token + "\n";
                os.write(tokenLine.getBytes(StandardCharsets.UTF_8));

                // Enviar el contenido del archivo
                System.out.println("Enviando bytes del archivo: " + file.getName());
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
                os.flush(); // IMPORTANTE: no cerrar el socket, solo hacer flush
                System.out.println("Bytes del archivo " + file.getName() + " enviados.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void disconnect() throws IOException {
        if (connectionHandler != null) {
            connectionHandler.stop();
        }
        if (out != null)
            out.close();
        if (in != null)
            in.close();
        if (socket != null) {
            socket.close();
        }
        System.out.println("Desconectado.");
    }

}
