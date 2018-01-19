package game.communication;

import game.checkers.CheckersBoard;
import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Checkers Created by Forntoh on 16-Jan-18.
 */
public class Server implements NetInterface {


    private CheckersBoard board;
    int port;
    Socket clientSocket;
    private ServerSocket serverSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Server(CheckersBoard board, int port) {
        this.port = port;
        this.board = board;
    }

    public boolean connect() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server running on " + port);
            clientSocket = serverSocket.accept();
            System.out.println("Connected to " + clientSocket.getInetAddress());
            setStreams();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    void setStreams() {
        System.out.println("Setting up streams");
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Streams okay");
    }

    public void readMessage() {
        if (clientSocket != null) {
            System.out.println("Reading messages");
            while (true) {
                try {
                    checkMessage((String) in.readObject());
                } catch (IOException | ClassNotFoundException e) {
                    break;
                }
            }
        }
    }

    private void checkMessage(String message) throws IOException, ClassNotFoundException {
        switch (message) {
            case "NEW_GAME":
                Platform.runLater(() -> board.doNewGame());
                break;
            case "RESIGN":
                Platform.runLater(() -> board.doResign());
                break;
            case "CLICK_SQUARE":
                int row = (int) in.readObject();
                int col = (int) in.readObject();
                Platform.runLater(() -> board.doClickSquare(row, col));
                break;
        }
    }

    private void sendMessage(Object message) {
        if (clientSocket != null) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                closeConnection();
            }
        }
    }

    public void closeConnection() {
        System.out.println("Connection Closed");
        try {
            if (serverSocket != null) serverSocket.close();
            if (clientSocket != null) clientSocket.close();
            out.close();
        } catch (Exception ignored) {}
    }

    @Override
    public void onNewGame() {
        sendMessage("NEW_GAME");
    }

    @Override
    public void onResign() {
        sendMessage("RESIGN");
    }

    @Override
    public void onClickSquare(int row, int col) {
        sendMessage("CLICK_SQUARE");
        sendMessage(row);
        sendMessage(col);
    }
}
