import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PacketAnalyzer {
    private static final int PORT = 8080;
    private static final int WINDOW_DURATION_MS = 10_000;
    private static final int THRESHOLD = 20;

    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private long windowStartTime = System.currentTimeMillis();

    public static void main(String[] args) throws IOException {
        new PacketAnalyzer().start();
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Analyzer server listening on port " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New producer connected: " + clientSocket.getRemoteSocketAddress());
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    private void handleClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    private synchronized void processLine(String line) {
        String ip = extractIp(line);
        if (ip == null) return;

        long now = System.currentTimeMillis();
        if (now - windowStartTime > WINDOW_DURATION_MS) {
            requestCounts.clear();
            windowStartTime = now;
        }

        int count = requestCounts.getOrDefault(ip, 0) + 1;
        requestCounts.put(ip, count);

        System.out.println("ANALYZER >>> Extracted IP: " + ip + " | Count: " + count);

        if (count > THRESHOLD) {
            System.out.println("Possible DoS from " + ip + " with " + count + " requests");
        }
    }

    private String extractIp(String line){
        String regex = "(\\d+\\.\\d+\\.\\d+\\.\\d+)";
        Matcher matcher = Pattern.compile(regex).matcher(line);
        return matcher.find() ? matcher.group(1) : null;
    }
}
