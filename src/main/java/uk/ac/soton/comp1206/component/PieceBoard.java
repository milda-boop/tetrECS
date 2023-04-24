package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

/**
 * The Piece Board is a visual component that will display the upcoming piece in thye game.
 * It extends GameBoard to hold a grid of GameBlocks and implements its visual representation.
 */
public class PieceBoard extends GameBoard{

  public PieceBoard(Grid grid, double width, double height) {
    super(grid, width, height);
  }
  public void setPiece(GamePiece piece)
  {
    for(int i = 0;i<3;i++)
    {
      for(int j = 0;j<3;j++)
      {
        grid.set(i,j,0);
        if(piece.getBlocks()[i][j] > 0)
        {
          grid.set(i,j, piece.getValue());
        }
      }
    }

  }
}
