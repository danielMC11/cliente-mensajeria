package config;

import java.io.IOException;

public class ConnectionHandler implements Runnable {
    private TCPClient client;
    private ClientRouter router;
    private volatile boolean running = true;

    public ConnectionHandler(TCPClient client, ClientRouter router) {
        this.client = client;
        this.router = router;
    }

    @Override
    public void run() {
        System.out.println("ConnectionHandler: Iniciando escucha de mensajes del servidor.");
        while (running) {
            try {
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

    public void stop() {
        running = false;
    }
}
