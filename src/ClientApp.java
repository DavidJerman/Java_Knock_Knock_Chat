import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientApp extends Application {

    String hostName;
    int portNumber;
    String username;

    MessengerThread messengerThread;
    ClientApp appReference;

    TextField serverIPTextField;
    TextField serverPortTextField;
    TextField usernameTextField;
    TextField msgTextField;
    TextArea chatArea;
    Label connectionEndedInfoLabel;
    Label connectionStartedInfoLabel;

    public ClientApp() {
        messengerThread = null;
        appReference = this;
    }

    public void printText(String user, String text) {
        String display = chatArea.getText();
        if (!display.equals("")) display = display + "\n";
        display += "<" + user + "> " + text;
        chatArea.setText(display);
    }

    private void logMessage(String msg) {
        try {
            FileWriter fw = new FileWriter("server.log", true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(msg);
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logMessage(String user, String msg) {
        try {
            FileWriter fw = new FileWriter("server.log", true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println("<" + user + "> " + msg);
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean parseClientData() {
        if (!serverIPTextField.getText().equals("") && !serverPortTextField.getText().equals("")) {
            hostName = serverIPTextField.getText();
            try {
                portNumber = Integer.parseInt(serverPortTextField.getText());
            } catch (Exception e) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Parsing rules
     *
     * POST / Public message sent to all users
     * POST_PRIVATE username / Private message to a specified person
     * GET / A message from a user
     * GET_PRIVATE username / A private message from a user
    **/
    String parseClientMessage(String text) {
        if (text.startsWith("/tell ")) {
            text = text.substring("/tell ".length());
            String sendTo;
            if (text.length() >= 1) {
                String[] splitText = text.split(" ");
                if (splitText.length >= 2) {
                    sendTo = splitText[0].trim();
                    StringBuilder toSend = new StringBuilder();
                    for (int i = 1; i < splitText.length; i++) toSend.append(splitText[i]).append(" ");
                    printText("you to " + sendTo, toSend.toString());
                    return "POST_PRIVATE " + sendTo + " / " + toSend.toString();
                } else return null;
            } else return null;
        } else {
            printText("you", msgTextField.getText());
            return "POST / " + text;
        }
    }

    String parseServerMessage(String text) {

        return text;
    }

    public static void main(String[] args) {
        // Create the app
        ClientApp client = new ClientApp();
        client.start();
    }

    public void start() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Chatter");
        GridPane mainPane = new GridPane();

        mainPane.setVgap(10);
        mainPane.setHgap(10);
        mainPane.setPadding(new Insets(10));

        GridPane dataPane = new GridPane();
        Label connectionStartedLabel = new Label("Connection started:");
        Label connectionEndedLabel = new Label("Connection ended:");
        Label serverIPLabel = new Label("Server IP:");
        Label serverPortLabel = new Label("Server Port:");
        Label usernameLabel = new Label("Username:");
        connectionStartedInfoLabel = new Label("N/A");
        connectionEndedInfoLabel = new Label("N/A");
        serverIPTextField = new TextField("4.tcp.ngrok.io");
        serverPortTextField = new TextField("18768");
        usernameTextField = new TextField();
        Button connectButton = new Button("Connect");
        chatArea = new TextArea();
        msgTextField = new TextField();
        Button sendButton = new Button("Send");
        Button clearButton = new Button("Clear");
        Button disconnectButton = new Button("Disconnect");

        dataPane.setVgap(10);
        dataPane.setHgap(10);
        chatArea.setEditable(false);
        msgTextField.setMinWidth(400);
        sendButton.setMinWidth(80);
        connectButton.setMinWidth(110);
        clearButton.setMinWidth(110);
        disconnectButton.setMinWidth(110);

        // Adding data to the data pane
        dataPane.add(connectionStartedLabel, 0, 0, 1, 1);
        dataPane.add(connectionEndedLabel, 0, 1, 1, 1);
        dataPane.add(serverIPLabel, 0, 2, 1, 1);
        dataPane.add(serverPortLabel, 0, 3, 1, 1);
        dataPane.add(usernameLabel, 0, 4, 1, 1);
        dataPane.add(clearButton, 0, 6, 1, 1);
        dataPane.add(connectionStartedInfoLabel, 1, 0, 1, 1);
        dataPane.add(disconnectButton, 0, 7, 1, 1);
        dataPane.add(connectionEndedInfoLabel, 1, 1, 1, 1);
        dataPane.add(serverIPTextField, 1, 2, 1, 1);
        dataPane.add(serverPortTextField, 1, 3, 1, 1);
        dataPane.add(usernameTextField, 1, 4, 1, 1);
        dataPane.add(connectButton, 0, 5, 2, 1);

        connectButton.setOnAction((event) -> {
            if (messengerThread == null)
                if (parseClientData()) {
                    printText("client", "Connecting...");
                    username = usernameTextField.getText();
                    if (username.equals("")) username = (int)(Math.random()*99999) + "";
                    usernameTextField.setText(username);

                    // Set start time
                    connectionEndedInfoLabel.setText("N/A");
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();
                    connectionStartedInfoLabel.setText(dtf.format(now));
                    logMessage("Chat started: " + dtf.format(now));

                    messengerThread = new MessengerThread(hostName, portNumber, username, appReference);
                    messengerThread.start();
                    chatArea.setText("");
                    printText("client", "You are connected to the server!");
                    serverIPTextField.setEditable(false);
                    serverPortTextField.setEditable(false);
                    usernameTextField.setEditable(false);
                } else {
                    printText("client", "Could not parse data: missing or wrong data.");
                }
            else {
                printText("client", "You are already connected to a chat server!");
            }
        });

        clearButton.setOnAction((event) -> chatArea.setText(""));

        disconnectButton.setOnAction((event) -> {
            stop();
            serverIPTextField.setEditable(true);
            serverPortTextField.setEditable(true);
            usernameTextField.setEditable(true);
            printText("client", "Disconnected from the server!");
        });

        sendButton.setOnAction((event) -> {
            String text = parseClientMessage(msgTextField.getText());
            if (text != null) {
                if (!text.equals("")) {
                    messengerThread.sendMsg(text);
                    logMessage(username, text);
                }
                msgTextField.setText("");
            }
        });

        mainPane.add(dataPane, 0, 0, 1, 2);
        mainPane.add(chatArea, 1, 0, 2, 1);
        mainPane.add(msgTextField, 1, 1, 1, 1);
        mainPane.add(sendButton, 2, 1, 1, 1);

        Scene scene = new Scene(mainPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // TODO
    @Override
    public void stop() {
        chatArea.setText("");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        connectionEndedInfoLabel.setText(dtf.format(now));
        messengerThread.stopThread();
        if (messengerThread != null) logMessage("Chat ended: " + dtf.format(now) + "\n");
        messengerThread = null;
    }
}

class MessengerThread extends Thread {

    String hostName;
    int portNumber;
    String username;
    ClientApp appReference;
    boolean stopped;
    PrintWriter out;
    BufferedReader bufferedReader;
    Socket kkSocket;
    String remoteHostIP;
    int remoteHostPort;

    public MessengerThread(String hostName, int portNumber, String username, ClientApp appReference) {
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.username = username;
        stopped = false;
        this.appReference = appReference;
    }

    @Override
    public void run() {
        try (Socket kkSocket = new Socket(hostName, portNumber);
             BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(kkSocket.getInputStream()));
        ) {
            out = new PrintWriter(kkSocket.getOutputStream(), true);
            String fromServer;
            // Getting server data
            remoteHostIP = kkSocket.getRemoteSocketAddress().toString();
            remoteHostPort = kkSocket.getPort();
            System.out.println(remoteHostIP + "\n" + remoteHostPort);
            while ((fromServer = bufferedReader.readLine()) != null) {
                if (!stopped) {
                    appReference.printText("server", fromServer);
                    appReference.logMessage("server", fromServer);
                }
            }
            // TODO
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }

    public void sendMsg(String msg) {
        out.println(msg);
    }

    public void stopThread() {
        try {
            stopped = true;
            bufferedReader.close();
            kkSocket.close();
            out.close();
        } catch (IOException | NullPointerException ignored) { }
    }
}
