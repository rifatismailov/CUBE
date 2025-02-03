package org.example.server;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Handtest {
    public static Map<String, Socket> getOnlineUsers = new HashMap<>();
    public static Map<String, String> getClientsKey = new HashMap<>();
    static String key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtRMcHgpL+0JLQu5+mcjy9DvuvfGj6IugjODEljbPPGqlMp21uP018IuBq/q4wBp/gQSIm5L9GFOE39Y0osNBgsVAQ/WEzKiNS6l8ZxF/xiW0JPLMFbOBIeP7zNvGFRoBi1TQqpoqFxWGyjZLQITlobfka59dkHSyJe4wcwNhXwcFdRv/jM5lzaeR38RW8+rRDp0n0kP/TWbTCIgT/2c9yard46wV5NHW+/WA3NUCR0TbBPu0zvH4Uc8TKTMXtzDjpvJi8zOE9jOxMVHWNEbAU676B34DxVslIlz0INA+Y4r9nY0BouWLT79hU8M9m8DScgbdlZh5eI8sIcR5bLP3AwIDAQAB";

    public static void main(String[] args) {
        new Handshake();
    }

    public static class Handshake implements HandshakeMap.HandshakeListener {
        public Handshake() {
            Socket socket = new Socket();
            getOnlineUsers.put("AAA", socket);
            getOnlineUsers.put("AAC", socket);
            getOnlineUsers.put("AAE", socket);
            getOnlineUsers.put("AAD", socket);
            getOnlineUsers.put("AAD", socket);
            getOnlineUsers.put("BBB", socket);
            getOnlineUsers.put("BUE", socket);
            HandshakeMap handshakeMap = new HandshakeMap(this);
            handshakeMap.handleHandshake("AAA", "BBB", "KEYABCD" + key);
            handshakeMap.handleHandshake("AAC", "BBC", "KEYABCD" + key);
            handshakeMap.handleHandshake("AAD", "BBD", "KEYABCD" + key);
            handshakeMap.handleHandshake("BBB", "AAA", "KEYABCD" + key);
            handshakeMap.handleHandshake("AAA", "BBB", "KEYABCD" + key + "sdsds");
            handshakeMap.handleHandshake("AAE", "BBE", "KEYABCD" + key);
            handshakeMap.handleHandshake("BBE", "AAE", "KEYABCD" + key);
            handshakeMap.handleHandshake("BUA", "BUE", "KEYABCD" + key);


        }

        @Override
        public void sendMessage(String receiverId, String jsonMessage) throws IOException {

        }

        @Override
        public Map<String, Socket> getOnlineUsers() {
            return getOnlineUsers;
        }

        @Override
        public Map<String, String> getClientsKey() {
            return getClientsKey;
        }

        @Override
        public void saveOfflineMessage(String receiverId, String message) {

        }
    }

}
