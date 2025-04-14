package com.evanmccormick.chessevaluator.ui.evaluation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evanmccormick.chessevaluator.R
import com.evanmccormick.chessevaluator.ui.theme.ExtendedTheme
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square

@Composable
fun ChessBoard(
    fen: String,
    modifier: Modifier = Modifier,
    reversed: Boolean = false,
    showCoordinates: Boolean = true
) {
    val board = remember(fen) {
        Board().apply {
            loadFromFen(fen)
        }
    }

    val lightSquareColor = ExtendedTheme.colors.chessWhite
    val darkSquareColor = ExtendedTheme.colors.chessBlack
    val borderColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .border(1.dp, borderColor)
    ) {
        for (rank in if (reversed) 0..7 else 7 downTo 0) {
            Row(
                modifier = Modifier.weight(1f)
            ) {
                for (file in if (reversed) 7 downTo 0 else 0..7) {
                    val square = Square.squareAt(file + rank * 8)
                    val piece = board.getPiece(square)
                    val isDarkSquare = (file + rank) % 2 != 0
                    val squareColor = if (isDarkSquare) darkSquareColor else lightSquareColor

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(squareColor),
                        contentAlignment = Alignment.Center
                    ) {
                        // Draw the piece if there is one
                        if (piece != Piece.NONE) {
                            Image(
                                painter = painterResource(id = getPieceDrawableResource(piece)),
                                contentDescription = piece.name,
                                modifier = Modifier
                                    .fillMaxSize(0.8f) // Scale the piece to 80% of the square
                            )
                        }

                        // Draw coordinates in the corner of the square
                        if (showCoordinates) {
                            if (file == if (reversed) 7 else 0) {
                                // Rank numbers on the left
                                Text(
                                    text = (rank + 1).toString(),
                                    fontSize = 10.sp,
                                    color = if (isDarkSquare) lightSquareColor else darkSquareColor,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(2.dp)
                                )
                            }

                            if (rank == if (reversed) 0 else 7) {
                                // File letters on the bottom
                                Text(
                                    text = ('a' + file).toString(),
                                    fontSize = 10.sp,
                                    color = if (isDarkSquare) lightSquareColor else darkSquareColor,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Map chess pieces to drawable resource IDs
private fun getPieceDrawableResource(piece: Piece): Int {
    return when (piece) {
        Piece.WHITE_PAWN -> R.drawable.pawn_w
        Piece.WHITE_KNIGHT -> R.drawable.knight_w
        Piece.WHITE_BISHOP -> R.drawable.bishop_w
        Piece.WHITE_ROOK -> R.drawable.rook_w
        Piece.WHITE_QUEEN -> R.drawable.queen_w
        Piece.WHITE_KING -> R.drawable.king_w
        Piece.BLACK_PAWN -> R.drawable.pawn_b
        Piece.BLACK_KNIGHT -> R.drawable.knight_b
        Piece.BLACK_BISHOP -> R.drawable.bishop_b
        Piece.BLACK_ROOK -> R.drawable.rook_b
        Piece.BLACK_QUEEN -> R.drawable.queen_b
        Piece.BLACK_KING -> R.drawable.king_b
        else -> throw IllegalArgumentException("Invalid chess piece: $piece")
    }
}

@Composable
fun AnalysisBoard(
    fen: String,
    modifier: Modifier = Modifier
) {
    // Use ChessLib to determine who's turn it is
    val board = remember(fen) {
            Board().apply { loadFromFen(fen) }
    }

    val sideToMove = board.sideToMove

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (fen.isBlank()) {
                // Display placeholder if no FEN is provided
                Text(
                    text = "Chess Position Here",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Use our custom chess board component to render the FEN
                ChessBoard(
                    fen = fen,
                    // Display the board from black's perspective if it's black's turn
                    reversed = sideToMove == Side.BLACK,
                    showCoordinates = true
                )
            }
        }
    }
}