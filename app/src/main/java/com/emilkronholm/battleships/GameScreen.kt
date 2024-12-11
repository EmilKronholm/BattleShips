package com.emilkronholm.battleships

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.asStateFlow

object VerboseColors {
    val empty =  Color(99, 21, 206, 255)
    val hidden = Color(173, 173, 227, 255)
    val hit = Color(236, 0, 111, 255)
    val missed = Color(18, 18, 23, 255)
    val sunk = Color(93, 0, 0, 255)
}

object Colors {
    val empty =  Color(99, 21, 206, 255)
    val hidden = Color(99, 21, 206, 255)
    val hit = Color(236, 0, 111, 255)
    val missed = Color(18, 18, 23, 255)
    val sunk = Color(93, 0, 0, 255)
}

@Composable
fun GameScreen(navController: NavController, playerViewModel: PlayerViewModel, gameID: String) {
    val gameViewModel: GameViewModel = viewModel()
    val games by gameViewModel.gamesMap.asStateFlow().collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isMyTurn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        gameViewModel.observeGame(gameID, playerViewModel.localUserID)
    }

    if (games[gameID] == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Loading...",
                fontSize = 50.sp,
                fontFamily = PixelFont,
                lineHeight = 70.sp,
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }
        return
    }
    val game = games[gameID]!!

    var board by remember { mutableStateOf(Board()) }
    val isPlayer1 = playerViewModel.localUserID == game.player1ID
    isMyTurn = if (isPlayer1) game.gameState == GameState.PLAYER1_TURN
               else game.gameState == GameState.PLAYER2_TURN

    //Check for winner
    when (game.gameState) {
        GameState.PLAYER1_WIN, GameState.PLAYER2_WIN -> {
            val result = if (game.gameState == GameState.PLAYER1_WIN) isPlayer1 else !isPlayer1
            navController.navigate(Routes.POST_GAME + if (result) "win" else "loose")
        }

        else -> {
            //Noting dudu
        }
    }

    println("LocaluserID: ${playerViewModel.localUserID}")
    println("player1ID: ${game.player1ID}")

    Column (
        modifier = Modifier.fillMaxSize(),
        //horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Box() {
            OpponentGrid(gameViewModel, if (isPlayer1) game.board2 else game.board1, onClick = { coordinate ->
                //Player makes a move
                //Is it players turn?
                if ((isPlayer1 && game.gameState == GameState.PLAYER1_TURN) ||
                    (!isPlayer1 && game.gameState == GameState.PLAYER2_TURN)) {
                    gameViewModel.makeMove(coordinate, onError = {
                        println("Something went wrong during move")
                    })
                } else {
                    Toast.makeText(
                        context,
                        "Not your turn!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

            if (!isMyTurn) {
                Box(
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f))
                        .matchParentSize()
                )
            }
        }

        PlayerGrid(gameViewModel, if (isPlayer1) game.board1 else game.board2, onClick = { coordinate ->

            if (isMyTurn) {
                Box(
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f))
                        .matchParentSize()
                )
            }
        }


        //Resign button
        Button(
            modifier = Modifier.width(200.dp),
            onClick = {
                gameViewModel.resignGame()
            }
        ) {
            Text("Resign", fontSize = 20.sp, fontFamily = PixelFont)
        }

        var msg = ""
        print("isplayer1: ")
        println(isPlayer1)
        if (isPlayer1) msg = if (game.gameState == GameState.PLAYER1_TURN) "Your turn" else "Waiting for opponent..."
        if (!isPlayer1) msg = if (game.gameState == GameState.PLAYER2_TURN) "Your turn" else "Waiting for opponent..."
        Text(msg, fontSize = 20.sp, fontFamily = PixelFont, color = Color.White)

    }
}

@Composable
fun PlayerGrid(gameViewModel: GameViewModel, list: List<BoardSquareState>, onClick: (Coordinate) -> Unit)
{
    LazyVerticalGrid(
        columns = GridCells.Fixed(10),
        modifier = Modifier
            .padding(8.dp)
            .background(Color(70, 21, 100, 200))
            .size(200.dp)
    ) {
        items(100) { index ->
            GridItemPlaying(index, list[index], true) {

            }
        }
    }
}

@Composable
fun OpponentGrid(gameViewModel: GameViewModel, list: List<BoardSquareState>, onClick: (Coordinate) -> Unit)
{
    LazyVerticalGrid(
        columns = GridCells.Fixed(10),
        modifier = Modifier
            .padding(8.dp)
            .background(Color(70, 21, 100, 200))
    ) {
        items(100) { index ->
            GridItemPlaying(index, list[index], false) {
                onClick(Coordinate(index%10, index/10))
            }
        }
    }
}

@Composable
fun GridItemPlaying(index: Int, state : BoardSquareState, isVerbose: Boolean = true, onClick: () -> Unit) {
    var status by remember { mutableStateOf(false) }
    var color by remember { mutableStateOf(Color.Red) }

    if (isVerbose) {
        if (state == BoardSquareState.HIT) {
            color = VerboseColors.hit
        } else if (state == BoardSquareState.EMPTY) {
            color = VerboseColors.empty
        } else if (state == BoardSquareState.HIDDEN) {
            color = VerboseColors.hidden
        } else if (state == BoardSquareState.SUNK) {
            color = VerboseColors.sunk
        } else if (state == BoardSquareState.MISSED) {
            color = VerboseColors.missed
        }
    } else {
        if (state == BoardSquareState.HIT) {
            color = Colors.hit
        } else if (state == BoardSquareState.EMPTY) {
            color = Colors.empty
        } else if (state == BoardSquareState.HIDDEN) {
            color = Colors.hidden
        } else if (state == BoardSquareState.SUNK) {
            color = Colors.sunk
        } else if (state == BoardSquareState.MISSED) {
            color = Colors.missed
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.05f)
            .aspectRatio(1f)
            .padding(2.dp)
            .background(color)
            .clickable {
                onClick()
            }
    )
}

