package config;

import java.io.IOException;

public class ConnectionHandler implements Runnable {
    private TCPClient client;
    private ClientRouter router;
    private volatile boolean running = true;

    private volatile boolean downloading = false;
    private java.io.File targetFile;
    private long fileSize;

    public ConnectionHandler(TCPClient client, ClientRouter router) {
        this.client = client;
        this.router = router;
    }

    public void setDownloading(boolean downloading, java.io.File targetFile, long size) {
        this.targetFile = targetFile;
        this.fileSize = size;
        this.downloading = downloading;
    }

    @Override
    public void run() {
        System.out.println("ConnectionHandler: Iniciando escucha de mensajes del servidor.");
        while (running) {
            try {
                if (downloading) {
                    handleDownload();
                    continue;
                }

                String message = client.receiveMessage();
                if (message != null && !message.isEmpty()) {
                    router.route(message);
                } else if (message == null) {
                    System.out.println("ConnectionHandler: El servidor ha cerrado la conexión.");
                    break;
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("ConnectionHandler: Error de lectura o conexión perdida con el servidor.");
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void handleDownload() throws IOException {
        System.out.println("ConnectionHandler: Iniciando descarga binaria de " + fileSize + " bytes...");
        java.io.InputStream is = client.getSocket().getInputStream();
        boolean success = false;
        
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(targetFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalRead = 0;
            
            while (totalRead < fileSize) {
                int toRead = (int) Math.min(buffer.length, fileSize - totalRead);
                bytesRead = is.read(buffer, 0, toRead);
                if (bytesRead == -1) break;
                
                fos.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
            }
            fos.flush();
            success = (totalRead == fileSize);
        } catch (IOException e) {
            System.err.println("Error durante la descarga: " + e.getMessage());
            success = false;
        }

        System.out.println("ConnectionHandler: Descarga completada. Éxito: " + success);
        downloading = false;
        
        if (router != null) {
            router.notifyDownloadResult(success, targetFile.getName());
        }
    }

    public void stop() {
        running = false;
    }
}
