package game.communication;

/**
 * Checkers Created by Forntoh on 17-Jan-18.
 */
public interface NetInterface {
    void onNewGame();
    void onResign();
    void onClickSquare(int row, int col);
}
