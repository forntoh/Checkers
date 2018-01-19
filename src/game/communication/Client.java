package game.communication;

import game.checkers.CheckersBoard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Checkers Created by Forntoh on 17-Jan-18.
 */
public class Client extends Server {

    private String ip;

    public Client(String ip, int port, CheckersBoard board) {
        super(board, port);
        this.ip = ip;
    }

    @Override
    public boolean connect() {
        try {
            clientSocket = new Socket();
            System.out.println("Created new client socket - " + clientSocket);
            clientSocket.connect(new InetSocketAddress(ip, port), 5000);
            System.out.println("Connected to server " + clientSocket.getRemoteSocketAddress());
            setStreams();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
