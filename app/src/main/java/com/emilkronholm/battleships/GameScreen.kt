package com.emilkronholm.battleships

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun LoadingScreen() {
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
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun GameScreen(navController: NavController, playerViewModel: PlayerViewModel, gameID: String) {
    val gameViewModel: GameViewModel = viewModel()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        gameViewModel.observeGame(gameID, playerViewModel.localUserID)
    }

    val games by gameViewModel.gamesMap.asStateFlow().collectAsStateWithLifecycle()
    val requestedGame = games[gameID]

    if (requestedGame == null) {
        LoadingScreen()
    } else {

        val opponentUsername = playerViewModel.players.value[gameViewModel.getOpponentID()]!!.name
        val title = "${playerViewModel.localUserName} (you) vs $opponentUsername"
        GameScreenP(navController, gameViewModel, requestedGame, title)


        //Auto-resign game if quitting app
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_STOP -> {
                        if (games[gameID]!!.gameState == GameState.PLAYER1_TURN ||
                            games[gameID]!!.gameState == GameState.PLAYER2_TURN)
                        {
                            println("PLAYER LEFT GAME")
                            gameViewModel.resignGame()
                        }
                    }
                    else -> Unit
                }
            }

            val lifecycle = lifecycleOwner.lifecycle
            lifecycle.addObserver(observer)

            onDispose {
                lifecycle.removeObserver(observer)
            }
        }
    }
}

@Composable
fun GameScreenP(navController: NavController, gameViewModel: GameViewModel, game: Game, title: String)
{
    val context = LocalContext.current

    val missSoundEffect = remember { MediaPlayer.create(context, R.raw.splash) }
    val hitSoundEffect = remember { MediaPlayer.create(context, R.raw.boom) }
    DisposableEffect(Unit) {
        onDispose {
            missSoundEffect.release()
            hitSoundEffect.release()
        }
    }

    //Get state variables (for this recomposition)
    val isMyTurn = gameViewModel.isMyTurn()
    val isPlayer1 = gameViewModel.isPlayer1()

    var showResignPopUp by remember { mutableStateOf(false) }

    if (showResignPopUp) {
        PopUp("Are you sure?", "Do you want to quit and loose this game?", onDismiss = {
            showResignPopUp = false
        }, onConfirm = {
            gameViewModel.resignGame()
            showResignPopUp = false
        })
    }

    //Take care of BackHandler ourself
    BackHandler {
        showResignPopUp = !showResignPopUp
    }

    Column (
        modifier = Modifier.fillMaxSize(),
        //horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Column (modifier = Modifier.fillMaxWidth()){
            Text(
                title,
                fontSize = 40.sp,
                fontFamily = PixelFont,
                lineHeight = 40.sp,
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )


            Text(if (isMyTurn) "YOUR TURN" else "WAITING FOR OPPONENTS MOVES...",
                fontSize = 20.sp,
                fontFamily = PixelFont,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

        }



        PlayingGrid(
            gameViewModel,
            if (isPlayer1) game.board2 else game.board1,
            enabled = isMyTurn,
            size = 1f,
            isVerbose = false,
            onClick = {  coordinate ->
                if (isMyTurn) {
                    gameViewModel.makeMove(coordinate, onError = {
                        println("Something went wrong during move")
                    },
                    onResult = { state ->

                        if (state == BoardSquareState.MISSED)
                        {
                            missSoundEffect.start()
                        } else if (state == BoardSquareState.HIT) {
                            hitSoundEffect.start()
                        }


                    })
                } else {
                    Toast.makeText(
                        context,
                        "Not your turn!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        PlayingGrid(
            gameViewModel,
            if (isPlayer1) game.board1 else game.board2,
            enabled = !isMyTurn,
            size = 0.6f,
            isVerbose = true,
        )


        //Resign button
        Button(
            modifier = Modifier
                .width(200.dp)
                .padding(8.dp),
            onClick = {
                showResignPopUp = true
            }
        ) {
            Text("Resign", fontSize = 20.sp, fontFamily = PixelFont)
        }
    }

    //Check if gamestate is winning
    when (game.gameState) {
        GameState.PLAYER1_WIN, GameState.PLAYER2_WIN -> {
            val isWinner = if (game.gameState == GameState.PLAYER1_WIN) isPlayer1 else !isPlayer1

            PopUp(
                title = if (isWinner) "🥂YOU WON 🎶😍" else "😒YOU LOST😵",
                message = if (isWinner) "Congrats and well played! Press OK to return to lobby." else "Don't worry, you played well! Press OK to return to lobby.",
                isPrompt = false,
                onConfirm = {
                    navController.navigate(Routes.LOBBY) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        else -> {}
    }
}

@Composable
fun PlayingGrid(gameViewModel: GameViewModel, list: List<BoardSquareState>, enabled: Boolean, size: Float, isVerbose: Boolean, onClick: (Coordinate) -> Unit = {}) {

    Box {
        LazyVerticalGrid(
            columns = GridCells.Fixed(10),
            modifier = Modifier
                .padding(8.dp)
                .background(Color(70, 21, 100, 200))
                .fillMaxWidth(size)
        ) {
            items(100) { index ->
                GridItemPlaying(index, list[index], isVerbose) {
                    onClick(Coordinate(index % 10, index / 10))
                }
            }
        }

        if (!enabled) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .matchParentSize()
            )
        }
    }
}

@Composable
fun GridItemPlaying(index: Int, state : BoardSquareState, isVerbose: Boolean = true, onClick: () -> Unit) {
    var imageID = 0
    var icon = ""
    var iconSize = 0.sp

    if (isVerbose) {
        iconSize = 20.sp
        when (state) {
            BoardSquareState.HIDDEN, BoardSquareState.HIT, BoardSquareState.SUNK ->
                imageID = R.drawable.metal_tile
            BoardSquareState.EMPTY, BoardSquareState.MISSED ->
                imageID = R.drawable.water_tile
        }
    } else {
        iconSize = 40.sp
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
            Text("¤", fontFamily = PixelFont, fontSize = iconSize, color = Color.Black, modifier = Modifier.align(Alignment.Center))
        }
        if (state == BoardSquareState.HIT || state == BoardSquareState.SUNK){
            Text("X", fontFamily = PixelFont, fontSize = iconSize, color = Color.Red, modifier = Modifier.align(Alignment.Center))
        }


    }
}

