package network;

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

    // Simula la conexión inicial para registrarnos en el servidor
    public void connect() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);
        sendActionAsync("CONNECT", payload);
    }
}
