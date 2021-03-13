import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    public static void main(String[] args) {
        int portNumber = 4444; //Integer.parseInt(args[0]);
        boolean listening = true;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (listening) {
                new KKMultiServerThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }
}

class KKMultiServerThread extends Thread {

    private final Socket socket;
    // IP / username
    private static final HashMap<SocketAddress, String> ip_to_username = new HashMap<>();
    private static final ArrayList<Socket> sockets = new ArrayList<>();

    public KKMultiServerThread(Socket socket) {
        super("KKMultiServerThread");
        this.socket = socket;
        sockets.add(socket);
    }

    public void run() {

        try (
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String fromUser;
                String toUser;
                String text;
                PrintWriter pw;
                System.out.println(inputLine);
                final String trim = inputLine.substring(inputLine.indexOf("/") + 1).trim();
                if (inputLine.startsWith("POST_PRIVATE")) {
                    try {
                        toUser = inputLine.split("/")[0].trim().split("POST_PRIVATE")[1].trim();
                        fromUser = ip_to_username.get(socket.getRemoteSocketAddress()).trim();
                        text = trim;
                        System.out.println("Sending from " + fromUser.trim() + " to:");
                        for (SocketAddress key : ip_to_username.keySet()) {
                            System.out.print(ip_to_username.get(key));
                            if (toUser.equals(ip_to_username.get(key).trim())) {
                                System.out.println(" YES");
                                for (Socket socket : sockets) {
                                    if (socket.getRemoteSocketAddress().equals(key)) {
                                        pw = new PrintWriter(socket.getOutputStream(), true);
                                        pw.println("POST_PRIVATE " + fromUser.trim() + " / " + text);
                                    }
                                }

                            } else {
                                System.out.println(" NO");
                            }
                        }
                    } catch (Exception ignored) { }
                } else if (inputLine.startsWith("POST")) {
                    try {
                        fromUser = ip_to_username.get(socket.getRemoteSocketAddress()).trim();
                        text = trim;
                        System.out.println("Sending from " + fromUser.trim() + " to:");
                        for (SocketAddress key : ip_to_username.keySet()) {
                            System.out.print(ip_to_username.get(key));
                            if (!ip_to_username.get(key).equals(fromUser.trim())) {
                                System.out.println(" YES");
                                for (Socket socket : sockets) {
                                    if (socket.getRemoteSocketAddress().equals(key)) {
                                        pw = new PrintWriter(socket.getOutputStream(), true);
                                        pw.println("POST " + fromUser.trim() + " / " + text);
                                    }
                                }

                            } else {
                                System.out.println(" NO");
                            }
                        }
                    } catch (Exception ignored) { }
                } else if (inputLine.startsWith("LOGIN")) {
                    System.out.println("New user login:");
                    ip_to_username.put(socket.getRemoteSocketAddress(), inputLine.split("LOGIN")[1].trim());
                    System.out.println(socket.getRemoteSocketAddress() + " " + ip_to_username.get(socket.getRemoteSocketAddress()));
                } else if (inputLine.startsWith("LOGOUT")) {
                    System.out.println("User disconnected:");
                    System.out.println(socket.getRemoteSocketAddress() + " " + ip_to_username.get(socket.getRemoteSocketAddress()));
                    ip_to_username.remove(socket.getRemoteSocketAddress());
                }
            }
            socket.close();
        } catch (java.net.SocketException e){
            System.out.println("Connection reset");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
