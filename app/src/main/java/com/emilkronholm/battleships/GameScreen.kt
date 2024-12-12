package com.emilkronholm.battleships

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.packInts
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.asStateFlow

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

        Box() {
            PlayerGrid(gameViewModel, if (isPlayer1) game.board1 else game.board2, onClick = { coordinate ->

            })

            if (isMyTurn) {
                Box(
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f))
                        .matchParentSize()
                        .padding(8.dp)
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
        msg = if (isMyTurn) "Your turn" else "Waiting for opponent..."
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
    var imageID = 0
    var icon = ""

    if (isVerbose) {
        when (state) {
            BoardSquareState.HIDDEN, BoardSquareState.HIT, BoardSquareState.SUNK ->
                imageID = R.drawable.metal_tile
            BoardSquareState.EMPTY, BoardSquareState.MISSED ->
                imageID = R.drawable.water_tile
        }
    } else {
        when (state) {
            BoardSquareState.SUNK ->
                imageID = R.drawable.metal_tile
            else ->
                imageID = R.drawable.water_tile
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.05f)
            .aspectRatio(1f)
            .padding(1.dp)
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = imageID),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(0.5.dp)
        )

        if (state == BoardSquareState.MISSED){
            Text("¤", fontFamily = PixelFont, fontSize = 40.sp, color = Color.Black, modifier = Modifier.align(Alignment.Center))
        }
        if (state == BoardSquareState.HIT || state == BoardSquareState.SUNK){
            Text("X", fontFamily = PixelFont, fontSize = 40.sp, color = Color.Red, modifier = Modifier.align(Alignment.Center))
        }


    }
}

