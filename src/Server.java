import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

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
    private Socket socket;

    public KKMultiServerThread(Socket socket) {
        super("KKMultiServerThread");
        this.socket = socket;
    }

    public void run() {

        try (
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            String inputLine, outputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Client: " + inputLine);
                Scanner scanner = new Scanner(System.in);
                outputLine = scanner.nextLine();
                out.println(outputLine);
                System.out.println("Server: " + outputLine);
                if (outputLine.equals("Bye"))
                    break;
            }
            socket.close();
        } catch (java.net.SocketException e){
            System.out.println("Connection reset");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
