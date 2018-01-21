package game.checkers;

import java.util.Vector;

/**
 * Created by Forntoh on 11-Jan-18.
 */
class CheckersData {

    // Note that RED moves "up" the board (i.e. row number decreases)
    // while BLACK moves "down" the board (i.e. row number increases).

    static final int
            EMPTY = 0,
            RED = 1,
            RED_KING = 2,
            BLACK = 3,
            BLACK_KING = 4;

    private int[][] board;

    CheckersData() {
        board = new int[Unit.BOARD_SIZE][Unit.BOARD_SIZE];
        setUpGame();
    }

    void setUpGame() {
        for (int row = 0; row < Unit.BOARD_SIZE; row++) {
            for (int col = 0; col < Unit.BOARD_SIZE; col++) {
                if (row % 2 == col % 2) {
                    if (row < (Unit.BOARD_SIZE / 2) - 1)
                        board[row][col] = BLACK;
                    else if (row > Unit.BOARD_SIZE / 2)
                        board[row][col] = RED;
                    else
                        board[row][col] = EMPTY;
                } else {
                    board[row][col] = EMPTY;
                }
            }
        }
    }


    int pieceAt(int row, int col) {
        return board[row][col];
    }


    public void setPieceAt(int row, int col, int piece) {
        board[row][col] = piece;
    }


    void makeMove(CheckersMove move) {
        makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol);
    }


    private void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        // Make the move from (fromRow,fromCol) to (toRow,toCol).  It is
        // assumed that this move is legal.  If the move is a jump, the
        // jumped piece is removed from the board.  If a piece moves
        // the last row on the opponent's side of the board, the
        // piece becomes a king.
        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = EMPTY;
        if (fromRow - toRow == 2 || fromRow - toRow == -2) {
            // The move is a jump.  Remove the jumped piece from the board.
            int jumpRow = (fromRow + toRow) / 2;  // Row of the jumped piece.
            int jumpCol = (fromCol + toCol) / 2;  // Column of the jumped piece.
            board[jumpRow][jumpCol] = EMPTY;
        }
        if (toRow == 0 && board[toRow][toCol] == RED)
            board[toRow][toCol] = RED_KING;
        if (toRow == 7 && board[toRow][toCol] == BLACK)
            board[toRow][toCol] = BLACK_KING;
    }


    CheckersMove[] getLegalMoves(int player) {
        // Return an array containing all the legal CheckersMoves
        // for the specfied player on the current board.  If the player
        // has no legal moves, null is returned.  The value of player
        // should be one of the constants RED or BLACK; if not, null
        // is returned.  If the returned value is non-null, it consists
        // entirely of jump moves or entirely of regular moves, since
        // if the player can jump, only jumps are legal moves.

        if (player != RED && player != BLACK) return null;

        int playerKing;  // The constant representing a King belonging to player.
        if (player == RED) playerKing = RED_KING;
        else playerKing = BLACK_KING;

        Vector<CheckersMove> moves = new Vector<>();  // Moves will be stored in this vector.

      /*  First, check for any possible jumps.  Look at each square on the board.
          If that square contains one of the player's pieces, look at a possible
          jump in each of the four directions from that square.  If there is
          a legal jump in that direction, put it in the moves vector.
      */

        for (int row = 0; row < Unit.BOARD_SIZE; row++)
            for (int col = 0; col < Unit.BOARD_SIZE; col++)
                storeLegalJumpsMoves(moves, player, playerKing, row, col);

      /*  If any jump moves were found, then the user must jump, so we don't
          add any regular moves.  However, if no jumps were found, check for
          any legal regualar moves.  Look at each square on the board.
          If that square contains one of the player's pieces, look at a possible
          move in each of the four directions from that square.  If there is
          a legal move in that direction, put it in the moves vector.
      */

        if (moves.size() == 0)
            for (int row = 0; row < Unit.BOARD_SIZE; row++)
                for (int col = 0; col < Unit.BOARD_SIZE; col++)
                    if (board[row][col] == player || board[row][col] == playerKing) {
                        if (canMove(player, row, col, row + 1, col + 1))
                            moves.addElement(new CheckersMove(row, col, row + 1, col + 1));
                        if (canMove(player, row, col, row - 1, col + 1))
                            moves.addElement(new CheckersMove(row, col, row - 1, col + 1));
                        if (canMove(player, row, col, row + 1, col - 1))
                            moves.addElement(new CheckersMove(row, col, row + 1, col - 1));
                        if (canMove(player, row, col, row - 1, col - 1))
                            moves.addElement(new CheckersMove(row, col, row - 1, col - 1));
                    }

      /* If no legal moves have been found, return null.  Otherwise, create
         an array just big enough to hold all the legal moves, copy the
         legal moves from the vector into the array, and return the array. */
        return movesArray(moves);
    }  // end getLegalMoves

    CheckersMove[] getLegalJumpsFrom(int player, int row, int col) {
        // Return a list of the legal jumps that the specified player can
        // make starting from the specified row and column.  If no such
        // jumps are possible, null is returned.  The logic is similar
        // to the logic of the getLegalMoves() method.
        if (player != RED && player != BLACK)
            return null;
        int playerKing;  // The constant representing a King belonging to player.
        if (player == RED)
            playerKing = RED_KING;
        else
            playerKing = BLACK_KING;
        Vector<CheckersMove> moves = new Vector<>();  // The legal jumps will be stored in this vector.
        storeLegalJumpsMoves(moves, player, playerKing, row, col);
        return movesArray(moves);
    }  // end getLegalMovesFrom()

    private CheckersMove[] movesArray(Vector<CheckersMove> moves) {
        if (moves.size() == 0)
            return null;
        else {
            CheckersMove[] moveArray = new CheckersMove[moves.size()];
            for (int i = 0; i < moves.size(); i++)
                moveArray[i] = moves.elementAt(i);
            return moveArray;
        }
    }

    private boolean canJump(int player, int r1, int c1, int r2, int c2, int r3, int c3) {
        // This is called by the two previous methods to check whether the
        // player can legally jump from (r1,c1) to (r3,c3).  It is assumed
        // that the player has a piece at (r1,c1), that (r3,c3) is a position
        // that is 2 rows and 2 columns distant from (r1,c1) and that
        // (r2,c2) is the square between (r1,c1) and (r3,c3).

        if (r3 < 0 || r3 >= Unit.BOARD_SIZE || c3 < 0 || c3 >= Unit.BOARD_SIZE)
            return false;  // (r3,c3) is off the board.

        if (board[r3][c3] != EMPTY)
            return false;  // (r3,c3) already contains a piece.

        if (player == RED) {
            // Regular red piece can only move  up.
            return !(board[r1][c1] == RED && r3 > r1) && !(board[r2][c2] != BLACK && board[r2][c2] != BLACK_KING);
        } else {
            // Regular black piece can only move downn.
            return !(board[r1][c1] == BLACK && r3 < r1) && !(board[r2][c2] != RED && board[r2][c2] != RED_KING);
        }

    }  // end canJump()

    private boolean canMove(int player, int r1, int c1, int r2, int c2) {
        // This is called by the getLegalMoves() method to determine whether
        // the player can legally move from (r1,c1) to (r2,c2).  It is
        // assumed that (r1,r2) contains one of the player's pieces and
        // that (r2,c2) is a neighboring square.

        if (r2 < 0 || r2 >= Unit.BOARD_SIZE || c2 < 0 || c2 >= Unit.BOARD_SIZE)
            return false;  // (r2,c2) is off the board.

        if (board[r2][c2] != EMPTY)
            return false;  // (r2,c2) already contains a piece.

        if (player == RED) {
            return !(board[r1][c1] == RED && r2 > r1);
        } else {
            return !(board[r1][c1] == BLACK && r2 < r1);
        }

    }  // end canMove()

    private void storeLegalJumpsMoves(Vector<CheckersMove> moves, int player, int playerKing, int row, int col) {
        if (board[row][col] == player || board[row][col] == playerKing) {
            if (canJump(player, row, col, row + 1, col + 1, row + 2, col + 2))
                moves.addElement(new CheckersMove(row, col, row + 2, col + 2));
            if (canJump(player, row, col, row - 1, col + 1, row - 2, col + 2))
                moves.addElement(new CheckersMove(row, col, row - 2, col + 2));
            if (canJump(player, row, col, row + 1, col - 1, row + 2, col - 2))
                moves.addElement(new CheckersMove(row, col, row + 2, col - 2));
            if (canJump(player, row, col, row - 1, col - 1, row - 2, col - 2))
                moves.addElement(new CheckersMove(row, col, row - 2, col - 2));
        }
    }
}
