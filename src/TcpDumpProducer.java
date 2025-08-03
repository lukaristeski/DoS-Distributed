// TcpDumpProducer.java (Windows)
import java.io.*;
import java.net.*;
import java.util.Random;

public class TcpDumpProducer {
    public static void main(String[] args) {
        String serverIP = "192.168.1.189";
        int port = 8080;

        try (Socket socket = new Socket(serverIP, port);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            System.out.println("PRODUCER >>> Connected to analyzer at " + serverIP + ":" + port);


            for (int i = 1; i <= 200; i++) {
                String mockLine = generateMockTcpDumpLine();
                writer.write(mockLine);
                writer.newLine();
                writer.flush();
                System.out.println("PRODUCER >>> " + mockLine);
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("PRODUCER >>> Error: " + e.getMessage());
        }
    }

    private static String generateMockTcpDumpLine() {
        String[] ips = {"10.0.0.1", "192.168.1.2", "172.16.0.3"};
        String src = ips[new Random().nextInt(ips.length)];
        String dst = ips[new Random().nextInt(ips.length)];
        int port = 80 + new Random().nextInt(1000);
        return String.format("%s > %s: TCP %d", src, dst, port);
    }
}
