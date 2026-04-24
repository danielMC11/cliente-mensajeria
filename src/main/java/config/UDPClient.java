package config;

import config.models.MessageRequest;

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

    public UDPClient(String ip, int port, String username, ClientRouter router) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.router = router;
    }

    // Método genérico para disparar peticiones UDP en un hilo nuevo
    public void sendActionAsync(String action, Map<String, Object> payload) {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                // 1. Preparar envío
                MessageRequest request = new MessageRequest(action, payload);
                String json = JSONSerializer.serialize(request);
                byte[] sendData = json.getBytes();
                InetAddress serverAddress = InetAddress.getByName(ip);

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
                socket.send(sendPacket);
                System.out.println("UDP Enviado: " + json);

                // 2. Esperar respuesta (con timeout de 5 seg para no colgar la app si el paquete se pierde)
                socket.setSoTimeout(5000);
                byte[] receiveData = new byte[65507];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                // 3. Procesar y rutear respuesta
                String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("UDP Recibido: " + response);

                if (router != null) {
                    router.route(response);
                }

            } catch (Exception e) {
                System.err.println("Error en comunicación UDP (Timeout o red): " + e.getMessage());
            }
        }).start();
    }

    // Simula la conexión inicial para registrarnos en el servidor
    public void connect() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);
        sendActionAsync("CONNECT", payload);
    }
}
