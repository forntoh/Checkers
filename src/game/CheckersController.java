package game;

import game.checkers.CheckersBoard;
import game.checkers.Unit;
import game.communication.Client;
import game.communication.Server;
import game.utils.Network;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import java.net.URL;
import java.util.ResourceBundle;

public class CheckersController implements Initializable {

    @FXML
    Text txt_ipAddress, txt_port;
    @FXML
    TextField ipAddress, port;
    @FXML
    private Button btn_newGame, btn_resign, btn_hostGame, btn_stopHosting, btn_joinGame, btn_stopJoin;
    @FXML
    private Label statusLabel, netStatusLabel;
    private CheckersBoard board;
    private Server server;
    private Client client;
    private Rotate rotation;
    private Translate translation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GridPane grid = ((GridPane) btn_newGame.getParent().getParent());
        grid.add(Unit.OPEN ? board = new CheckersBoard(btn_resign, btn_newGame, statusLabel) : null, 0, 0);
        grid.setBackground(new Background(new BackgroundFill(Color.WHEAT, null, null)));
        txt_ipAddress.setText(Network.ipAddresses());
    }

    public void hostGame() {
        int port = Network.getFreePort();
        txt_port.setText(port + "");
        server = new Server(board, port);
        changeButtonStates(true, 1);
        netStatusLabel.setText("Waiting for peer");
        new Thread(() -> {
            System.out.println("Thread Started");
            boolean state = server.connect();
            System.out.println(state);
            if (state) {
                Platform.runLater(() -> {
                    netStatusLabel.setText("Peer connected");
                    board.setGameInProgress(false);
                    board.setMyPlayer(1);
                    board.doNewGame();
                });
                board.setNetInterface(this.server);
                server.readMessage();
            } else Platform.runLater(() -> changeButtonStates(false, 1));
        }).start();
    }

    public void stopHosting() {
        txt_port.setText("N/A");
        server.closeConnection();
        netStatusLabel.setText("Not hosting any game");
        changeButtonStates(false, 1);
    }

    public void joinGame() {
        int port = Integer.parseInt(this.port.getText());
        String ip = this.ipAddress.getText();
        // TODO: Validate IP and Port
        client = new Client(ip, port, board);
        rotation = new Rotate(180, 0, 0);
        translation = new Translate(Unit.SIZE * -8, Unit.SIZE * -8);

        changeButtonStates(true, 2);
        netStatusLabel.setText("Connecting...");
        new Thread(() -> {
            System.out.println("Thread Started");
            boolean state = client.connect();
            System.out.println(state);
            if (state) {
                Platform.runLater(() -> {
                    netStatusLabel.setText("Connected to " + ip + ":" + port);
                    board.setGameInProgress(false);
                    board.setMyPlayer(3);
                    board.doNewGame();
                    board.getTransforms().addAll(rotation, translation);
                });
                board.setNetInterface(this.client);
                client.readMessage();
            } else Platform.runLater(() -> changeButtonStates(false, 2));
        }).start();
    }

    public void exitGame() {
        client.closeConnection();
        board.getTransforms().removeAll(rotation, translation);
        netStatusLabel.setText("Exited game");
        changeButtonStates(false, 2);
    }

    private void changeButtonStates(boolean b, int a) {
        btn_hostGame.setDisable(b);
        btn_joinGame.setDisable(b);
        btn_stopHosting.setDisable((a == 1) == !b);
        btn_stopJoin.setDisable((a == 1) == b);

        ipAddress.setDisable(b);
        port.setDisable(b);

        if (!b) {
            ipAddress.setText("");
            port.setText("");
        }
    }

}