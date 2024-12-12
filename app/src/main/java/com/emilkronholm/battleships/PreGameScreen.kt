package com.emilkronholm.battleships

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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

@Composable
fun PreGameScreen(navController: NavController, playerViewModel: PlayerViewModel, gameID: String)
{
    val gameViewModel: GameViewModel = viewModel()
    val games by gameViewModel.gamesMap.asStateFlow().collectAsStateWithLifecycle()
    val context = LocalContext.current

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

    if (game.gameState == GameState.PLAYER1_TURN)
    {
        navController.navigate(Routes.GAME + "/${gameID}")
    }

    if (game.gameState == GameState.PLAYER2_WIN || game.gameState == GameState.PLAYER1_WIN) {
        navController.navigate(Routes.LOBBY) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
        }
    }

    if (game.player1Ready && game.player2Ready) {
        gameViewModel.startGame()
    }

    var board by remember { mutableStateOf(Board()) }
    val isPlayer1 = playerViewModel.localUserID == game.player1ID

    Button(
        modifier = Modifier.padding(20.dp),
        colors = ButtonColors(
            contentColor = Color.White,
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.Transparent
        ),
        onClick = {
            gameViewModel.resignGame()
        }
    ) {
        Text("Abandon game", fontSize = 20.sp, fontFamily = PixelFont, color = Color.White)
    }

    Column (
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(
            "PLACE YOUR SHIPS",
            fontSize = 80.sp,
            fontFamily = PixelFont,
            lineHeight = 70.sp,
            textAlign = TextAlign.Center,
            color = Color.White
        )
        PreGameGrid(board, onMove = {
            gameViewModel.setPlayerReady(false)
        })
        Button(
            modifier = Modifier.width(300.dp),
            onClick = {
                //Shuffle board
                board.shuffle()
            }
        ) {

            Text("Shuffle", fontSize = 20.sp, fontFamily = PixelFont)
        }

        val isBoardValid = board.isValid()
        var buttonColor = Color.Black
        if (isPlayer1) buttonColor = if (game.player1Ready) Color.Green else Color.Red
        if (!isPlayer1) buttonColor = if (game.player2Ready) Color.Green else Color.Red
        Button(
            colors = ButtonColors(
                containerColor = buttonColor,
                disabledContainerColor = Color(70, 0, 0, 200),
                contentColor = Color.White,
                disabledContentColor = Color.Gray
            ),
            modifier = Modifier.width(300.dp),
            enabled = isBoardValid,
            onClick = {

                if (!isBoardValid) {
                    Toast.makeText(
                        context,
                        "Cannot be ready. The board contains invalid boats",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                var newValue = true
                if (isPlayer1) newValue = !game.player1Ready
                if (!isPlayer1) newValue = !game.player2Ready

                gameViewModel.setPlayerReady(newValue)
                gameViewModel.uploadBoard(gameID, isPlayer1, board)

                if (isPlayer1 && game.player2Ready && newValue){
                    gameViewModel.startGame()
                }
                if (!isPlayer1 && game.player1Ready && newValue){
                    gameViewModel.startGame()
                }
            }
        ) {
            var msg = "READY"
            if (isPlayer1 && game.player1Ready || (!isPlayer1 && game.player2Ready))
            {
                msg = "UNREADY"
            }
            Text(msg, fontSize = 20.sp, fontFamily = PixelFont)
        }

        var msg = ""
        if (isPlayer1) msg = if (game.player2Ready) "Opponent is ready" else "Waiting for opponent..."
        if (!isPlayer1) msg = if (game.player1Ready) "Opponent is ready" else "Waiting for opponent..."
        Text(msg, fontSize = 20.sp, fontFamily = PixelFont, color = Color.White)

    }
}