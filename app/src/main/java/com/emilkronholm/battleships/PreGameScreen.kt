package com.emilkronholm.battleships

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun PreGameScreen(navController: NavController, playerViewModel: PlayerViewModel, gameID: String)
{
    val gameViewModel: GameViewModel = viewModel()
    val games by gameViewModel.gamesMap.asStateFlow().collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        gameViewModel.observeGame(gameID)
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

    var board by remember { mutableStateOf(Board()) }
    val isPlayer1 = playerViewModel.localUserID == game.player1ID

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
        Grid(board, updateBoard = {}
        )
        Button(
            modifier = Modifier.width(300.dp),
            onClick = {
                board = Board()
            }
        ) {

            Text("Shuffle", fontSize = 20.sp, fontFamily = PixelFont)
        }

        Button(
            modifier = Modifier.width(300.dp),
            onClick = {
                var newValue = true
                if (isPlayer1) newValue = !game.player1Ready
                if (!isPlayer1) newValue = !game.player2Ready

                gameViewModel.setPlayerReady(gameID, isPlayer1, newValue)
                gameViewModel.uploadBoard(gameID, isPlayer1, board)

                if (isPlayer1 && game.player2Ready && newValue){
                    gameViewModel.startGame(gameID)
                }
                if (!isPlayer1 && game.player1Ready && newValue){
                    gameViewModel.startGame(gameID)
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