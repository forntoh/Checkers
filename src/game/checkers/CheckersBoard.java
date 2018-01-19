package game.checkers;

import game.communication.NetInterface;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;

/**
 * CheckersController Created by Forntoh on 11-Jan-18.
 */
public class CheckersBoard extends Group implements EventHandler<MouseEvent> {

    private Button resignButton;    // Current player can resign by clicking this button.
    private Button newGameButton;   // This button starts a new game.  It is enabled only
                                    // when the current game has ended.

    private Label message;   // A label for displaying messages to the user.

    private CheckersData board; // The data for the checkers board is kept here.
                                // This board is also responsible for generating
                                // lists of legal moves.

    public void setGameInProgress(boolean gameInProgress) {
        this.gameInProgress = gameInProgress;
    }

    // Is a game currently in progress?
    private boolean gameInProgress;

    // Whose turn is it now?  The possible values
    // are CheckersData.RED and CheckersData.BLACK.
    private int currentPlayer;

    public void setMyPlayer(int myPlayer) {
        this.myPlayer = myPlayer;
    }

    private int myPlayer;

    // If the current player has selected a piece to
    // move, these give the row and column
    // containing that piece.  If no piece is
    // yet selected, then selectedRow is -1.
    private int selectedRow, selectedCol;

    // An array containing the legal moves for the current player
    private CheckersMove[] legalMoves;

    private Rectangle[][] rectangles = new Rectangle[8][8];
    private Circle[][] checkers = new Circle[8][8];

    private NetInterface netInterface;

    public void setNetInterface(NetInterface netInterface) {
        this.netInterface = netInterface;
    }

    public CheckersBoard(Button resignButton, Button newGameButton, Label message) {
        this.resignButton = resignButton;
        this.newGameButton = newGameButton;
        this.message = message;
        this.resignButton.setOnMouseClicked(this);
        this.newGameButton.setOnMouseClicked(this);
        board = new CheckersData();
        doNewGame();
    }

    public void doNewGame() {
        // Begin a new game.
        if (gameInProgress) {
            message.setText("Finish the current game first!");
            return;
        }
        board.setUpGame();                                   // Set up the pieces.
        currentPlayer = CheckersData.RED;                    // RED moves first.
        legalMoves = board.getLegalMoves(CheckersData.RED);  // Get RED's legal moves.
        selectedRow = -1;                                    // RED has not yet selected a piece to move.
        message.setText("Red:  Make your move.");
        gameInProgress = true;
        newGameButton.setDisable(true);
        resignButton.setDisable(false);
        repaint();
    }

    public void doResign() {
        // Current player resigns.  Game ends.  Opponent wins.
        if (!gameInProgress) {
            message.setText("There is no game in progress!");
            return;
        }
        if (currentPlayer == CheckersData.RED)
            gameOver("RED resigns.  BLACK wins.");
        else
            gameOver("BLACK resigns.  RED winds.");
    }

    private void gameOver(String str) {
        // The game ends.  The parameter, str, is displayed as a message
        // to the user.  The states of the buttons are adjusted so playes
        // can start a new game.
        message.setText(str);
        newGameButton.setDisable(false);
        resignButton.setDisable(true);
        gameInProgress = false;
    }

    public void doClickSquare(int row, int col) {
        // This is called by mousePressed() when a player clicks on the
        // square in the specified row and col.  It has already been checked
        // that a game is, in fact, in progress.

      /* If the player clicked on one of the pieces that the player
         can move, mark this row and col as selected and return.  (This
         might change a previous selection.)  Reset the message, in
         case it was previously displaying an error message. */

        for (CheckersMove legalMove : legalMoves)
            if (legalMove.fromRow == row && legalMove.fromCol == col) {
                selectedRow = row;
                selectedCol = col;
                if (currentPlayer == CheckersData.RED)
                    message.setText("RED:  Make your move.");
                else
                    message.setText("BLACK:  Make your move.");
                repaint();
                return;
            }

      /* If no piece has been selected to be moved, the user must first
         select a piece.  Show an error message and return. */

        if (selectedRow < 0) {
            message.setText("Click the piece you want to move.");
            return;
        }

      /* If the user clicked on a squre where the selected piece can be
         legally moved, then make the move and return. */

        for (CheckersMove legalMove : legalMoves)
            if (legalMove.fromRow == selectedRow && legalMove.fromCol == selectedCol
                    && legalMove.toRow == row && legalMove.toCol == col) {
                doMakeMove(legalMove);
                return;
            }

      /* If we get to this point, there is a piece selected, and the square where
         the user just clicked is not one where that piece can be legally moved.
         Show an error message. */

        message.setText("Click the square you want to move to.");
    }

    private void doClickSquare(String x, String y) {
        doClickSquare(Integer.parseInt(x), Integer.parseInt(y));
    }

    private void doMakeMove(CheckersMove move) {
        // This is called when the current player has chosen the specified
        // move.  Make the move, and then either end or continue the game
        // appropriately.

        board.makeMove(move);

      /* If the move was a jump, it's possible that the player has another
         jump.  Check for legal jumps starting from the square that the player
         just moved to.  If there are any, the player must jump.  The same
         player continues moving.
      */

        if (move.isJump()) {
            legalMoves = board.getLegalJumpsFrom(currentPlayer, move.toRow, move.toCol);
            if (legalMoves != null) {
                if (currentPlayer == CheckersData.RED)
                    message.setText("RED:  You must continue jumping.");
                else
                    message.setText("BLACK:  You must continue jumping.");
                selectedRow = move.toRow;  // Since only one piece can be moved, select it.
                selectedCol = move.toCol;
                repaint();
                return;
            }
        }

      /* The current player's turn is ended, so change to the other player.
         Get that player's legal moves.  If the player has no legal moves,
         then the game ends. */

        if (currentPlayer == CheckersData.RED) {
            currentPlayer = CheckersData.BLACK;
            legalMoves = board.getLegalMoves(currentPlayer);
            if (legalMoves == null)
                gameOver("BLACK has no moves.  RED wins.");
            else if (legalMoves[0].isJump())
                message.setText("BLACK:  Make your move.  You must jump.");
            else
                message.setText("BLACK:  Make your move.");
        } else {
            currentPlayer = CheckersData.RED;
            legalMoves = board.getLegalMoves(currentPlayer);
            if (legalMoves == null)
                gameOver("RED has no moves.  BLACK wins.");
            else if (legalMoves[0].isJump())
                message.setText("RED:  Make your move.  You must jump.");
            else
                message.setText("RED:  Make your move.");
        }

      /* Set selectedRow = -1 to record that the player has not yet selected
          a piece to move. */

        selectedRow = -1;

      /* As a courtesy to the user, if all legal moves use the same piece, then
         select that piece automatically so the use won't have to click on it
         to select it. */

        if (legalMoves != null) {
            boolean sameStartSquare = true;
            for (int i = 1; i < legalMoves.length; i++)
                if (legalMoves[i].fromRow != legalMoves[0].fromRow
                        || legalMoves[i].fromCol != legalMoves[0].fromCol) {
                    sameStartSquare = false;
                    break;
                }
            if (sameStartSquare) {
                selectedRow = legalMoves[0].fromRow;
                selectedCol = legalMoves[0].fromCol;
            }
        }

      /* Make sure the board is redrawn in its new state. */
        repaint();
    }

    private void repaint() {
        paint();
    }

    private void paint() {
        /* Draw the squares of the checkerboard and the checkers. */
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Rectangle rectangle = new Rectangle(col * Unit.SIZE, row * Unit.SIZE, Unit.SIZE, Unit.SIZE);
                rectangle.setId(row + "-" + col);

                if (row % 2 == col % 2)
                    rectangle.setFill(Color.BURLYWOOD);
                else
                    rectangle.setFill(Color.SADDLEBROWN);
                rectangles[row][col] = rectangle;

                Circle checker = new Circle(col * Unit.SIZE + (Unit.SIZE / 2), row * Unit.SIZE + (Unit.SIZE / 2), Unit.RADIUS);
                checker.setId(row + "-" + col);
                switch (board.pieceAt(row, col)) {
                    case CheckersData.RED:
                        checker.setFill(Color.DARKRED);
                        checkers[row][col] = checker;
                        break;
                    case CheckersData.BLACK:
                        checker.setFill(Color.BLACK);
                        checkers[row][col] = checker;
                        break;
                    case CheckersData.RED_KING:
                        checker.setFill(Color.BLUE);
                        checkers[row][col] = checker;
                        break;
                    case CheckersData.BLACK_KING:
                        checker.setFill(Color.GREEN);
                        checkers[row][col] = checker;
                        break;
                    default:
                        checkers[row][col] = null;
                        break;
                }
            }
        }

      /* If a game is in progress, highlight the legal moves.   Note that legalMoves
         is never null while a game is in progress. */

        if (gameInProgress) {
            // First, draw a cyan border around the pieces that can be moved.
            for (CheckersMove legalMove : legalMoves)
                rectangles[legalMove.fromRow][legalMove.fromCol].setFill(Color.LIGHTSALMON);

         /* If a piece is selected for moving (i.e. if selectedRow >= 0), then
            draw a 2-pixel white border around that piece and draw green borders
            around each square that that piece can be moved to. */

            if (selectedRow >= 0) {
                //rectangles[selectedRow][selectedCol].setFill(Color.WHITE);
                checkers[selectedRow][selectedCol].setStroke(Color.GOLD);
                checkers[selectedRow][selectedCol].setStrokeWidth(3);
            }
        }

        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++) {
                rectangles[row][col].setOnMouseClicked(this);
                getChildren().add(rectangles[row][col]);
                if (checkers[row][col] != null) {
                    checkers[row][col].setOnMouseClicked(this);
                    getChildren().add(checkers[row][col]);
                }
            }
    }

    @Override
    public void handle(MouseEvent event) {
        Object src = event.getSource();
        if (src == newGameButton) {
            doNewGame();
            if (netInterface != null)
                netInterface.onNewGame();
        } else if (src == resignButton) {
            doResign();
            if (netInterface != null)
                netInterface.onResign();
        } else if (src instanceof Rectangle) {
            String[] coordinates = ((Rectangle) src).getId().split("-");
            if (netInterface != null) {
                if (myPlayer != currentPlayer) {
                    message.setText("That is not your piece");
                    return;
                }
                netInterface.onClickSquare(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]));
            }
            doClickSquare(coordinates[0], coordinates[1]);
        } else if (src instanceof Circle) {
            String[] coordinates = ((Circle) src).getId().split("-");
            if (netInterface != null) {
                if (myPlayer != currentPlayer) {
                    message.setText("That is not your piece");
                    return;
                }
                netInterface.onClickSquare(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]));
            }
            doClickSquare(coordinates[0], coordinates[1]);
        }
    }
}
