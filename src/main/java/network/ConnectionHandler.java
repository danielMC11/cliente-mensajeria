package network;

import domain.ports.UIEventPublisher;

import java.io.IOException;

public class ConnectionHandler implements Runnable {
    private TCPClient client;
    private ClientRouter router;
    private UIEventPublisher uiPublisher;
    private volatile boolean running = true;

    public ConnectionHandler(TCPClient client, ClientRouter router, UIEventPublisher uiPublisher) {
        this.client = client;
        this.router = router;
        this.uiPublisher = uiPublisher;
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
                    notifyDisconnected();
                    break;
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("ConnectionHandler: Error de lectura o conexión perdida con el servidor.");
                    e.printStackTrace();
                    notifyDisconnected();
                }
                break;
            }
        }
    }

    private void notifyDisconnected() {
        if (uiPublisher != null) {
            uiPublisher.onServerDisconnected();
        }
    }

    public void stop() {
        running = false;
    }
}
