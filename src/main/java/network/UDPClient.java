package network;

import domain.ports.ChatRepository;
import domain.ports.UIEventPublisher;
import models.MessageRequest;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class UDPClient {
    private String ip;
    private int port;
    private String username;
    private ClientRouter router;

    private ChatRepository repository;
    private UIEventPublisher uiPublisher;
    private java.util.Queue<java.io.File> pendingFiles = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private java.util.Queue<DownloadMetadata> pendingDownloadMetadata = new java.util.concurrent.ConcurrentLinkedQueue<>();

    public UDPClient(String ip, int port, String username, ChatRepository repository, UIEventPublisher uiPublisher, ClientRouter router) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.repository = repository;
        this.uiPublisher = uiPublisher;
        this.router = router;
    }

    // Método genérico para disparar peticiones UDP en un hilo nuevo
    public void sendActionAsync(String action, Map<String, Object> payload) {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                MessageRequest request = new MessageRequest(action, payload);
                String json = JSONSerializer.serialize(request);
                byte[] sendData = json.getBytes();
                InetAddress serverAddress = InetAddress.getByName(ip);
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
                
                int maxRetries = 3;
                int timeoutMs = 2000;
                socket.setSoTimeout(timeoutMs);
                
                boolean receivedAck = false;
                int attempts = 0;
                
                while (!receivedAck && attempts < maxRetries) {
                    try {
                        attempts++;
                        System.out.println("UDP Enviado (Intento " + attempts + "): " + json);
                        socket.send(sendPacket);
                        
                        byte[] receiveData = new byte[65507];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        socket.receive(receivePacket); // Espera respuesta o lanza SocketTimeoutException
                        
                        String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        System.out.println("UDP Recibido ACK/Respuesta: " + response);
                        receivedAck = true;
                        
                        if (router != null) {
                            router.route(response);
                        }
                    } catch (java.net.SocketTimeoutException e) {
                        System.err.println("Timeout UDP. No se recibió respuesta. Reintentando...");
                    }
                }
                
                if (!receivedAck) {
                    System.err.println("Fallo UDP: No se recibió respuesta después de " + maxRetries + " intentos.");
                    // Opcional: Notificar a la UI del error de conexión
                }

            } catch (Exception e) {
                System.err.println("Error en comunicación UDP: " + e.getMessage());
            }
        }).start();
    }

    public void connect() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);
        sendActionAsync("CONNECT", payload);
    }

    public void sendDirectMessage(String targetUsername, String content) {
        if (repository != null) {
            repository.saveMessage(this.username, content);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", this.username);
        payload.put("message", content);

        if (targetUsername != null && !targetUsername.equals("Todos") && !targetUsername.equals("— Todos —")) {
            payload.put("targetUsername", targetUsername);
        }

        sendActionAsync("SEND_MESSAGE", payload);
    }

    public void sendChatMessage(String content) {
        if (repository != null) {
            repository.saveMessage(this.username, content);
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", this.username);
        payload.put("message", content);
        sendActionAsync("SEND_MESSAGE", payload);
    }

    public void sendAnalyzeMessage(String content) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("mensaje", content);
        sendActionAsync("ANALYZE_MESSAGE", payload);
    }

    public void sendResena(String productoId, String contenido) {
        if (repository != null) {
            repository.saveResena(productoId, this.username, contenido);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", this.username);
        payload.put("message", contenido);
        payload.put("productId", productoId);

        sendActionAsync("SEND_MESSAGE", payload);

        // Análisis automático
        sendAnalyzeMessage(contenido);
    }

    public void sendListResenasAction(String productoId) {
        Map<String, Object> payload = new HashMap<>();
        if (this.username != null) {
            payload.put("username", this.username);
        }
        if (productoId != null) {
            payload.put("productId", productoId);
        }
        sendActionAsync("LIST_MESSAGES", payload);
    }

    public void sendFile(java.io.File file, String targetUsername) {
        pendingFiles.add(file);

        String extension = "";
        int i = file.getName().lastIndexOf('.');
        if (i > 0) {
            extension = file.getName().substring(i);
        }

        if (repository != null) {
            repository.saveDocumentMetadata(
                    file.getName(),
                    file.length(),
                    extension,
                    "application/octet-stream",
                    this.username
            );
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("filename", file.getName());
        payload.put("size", file.length());
        payload.put("extension", extension);
        payload.put("mimeType", "application/octet-stream");
        payload.put("username", this.username);
        payload.put("targetUsername", targetUsername);


        sendActionAsync("UPLOAD_INIT", payload);
    }

    public void sendUploadConfirmation(Map<String, Object> payload) {
        sendActionAsync("UPLOAD_CONFIRMATION", payload);
    }

    public void startFileTransfer(String token) {
        java.io.File file = pendingFiles.poll();
        if (file == null)
            return;

        new Thread(() -> {
            System.out.println("Iniciando transferencia de subida (Nuevo Socket UDP->TCP)...");
            try (java.net.Socket fileSocket = new java.net.Socket(ip, port);
                 java.io.OutputStream os = fileSocket.getOutputStream();
                 java.io.FileInputStream fis = new java.io.FileInputStream(file)) {

                os.write((token + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
                os.flush();

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
                System.out.println("Subida finalizada y socket de archivo cerrado.");
            } catch (java.io.IOException e) {
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

        sendActionAsync("DOWNLOAD_INIT", payload);
    }

    public void startDownloadTransfer(String token, long size) {
        DownloadMetadata metadata = pendingDownloadMetadata.poll();
        if (metadata == null)
            return;

        new Thread(() -> {
            System.out.println("Iniciando transferencia de descarga (Nuevo Socket UDP->TCP) de " + size + " bytes...");

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

            java.io.File directory = new java.io.File("descargas/" + subDir);
            if (!directory.exists())
                directory.mkdirs();

            java.io.File targetFile = new java.io.File(directory, finalFilename);

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
                    targetFile = new java.io.File(directory, nameOnly + " (" + count + ")" + extension);
                    count++;
                }
            }

            boolean success = false;
            try (java.net.Socket fileSocket = new java.net.Socket(ip, port);
                 java.io.OutputStream os = fileSocket.getOutputStream();
                 java.io.InputStream is = fileSocket.getInputStream();
                 java.io.FileOutputStream fos = new java.io.FileOutputStream(targetFile)) {

                os.write((token + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
                os.flush();

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalRead = 0;
                long lastUpdate = 0;
                while (totalRead < size
                        && (bytesRead = is.read(buffer, 0, (int) Math.min(buffer.length, size - totalRead))) != -1) {
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
            } catch (java.io.IOException e) {
                System.err.println("Error en descarga: " + e.getMessage());
                success = false;
            }

            if (uiPublisher != null) {
                uiPublisher.onDownloadFinished(success, targetFile.getName());
            }
        }).start();
    }

    public void disconnect() {
        try (java.net.DatagramSocket socket = new java.net.DatagramSocket()) {
            Map<String, Object> payload = new HashMap<>();
            if (this.username != null) {
                payload.put("username", this.username);
            }
            MessageRequest request = new MessageRequest("DISCONNECT", payload);
            String json = JSONSerializer.serialize(request);
            byte[] sendData = json.getBytes();
            java.net.InetAddress serverAddress = java.net.InetAddress.getByName(ip);
            java.net.DatagramPacket sendPacket = new java.net.DatagramPacket(sendData, sendData.length, serverAddress, port);
            socket.send(sendPacket);
            System.out.println("UDP DISCONNECT enviado: " + json);
        } catch (Exception e) {
            System.err.println("Error enviando DISCONNECT por UDP: " + e.getMessage());
        }
    }
}
