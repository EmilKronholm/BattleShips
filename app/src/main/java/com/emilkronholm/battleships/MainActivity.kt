package com.emilkronholm.battleships

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.emilkronholm.battleships.ui.theme.BattleShipsTheme
import androidx.navigation.compose.composable
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BattleShipsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    BattleShipsApp()
                }
            }
        }
    }
}

object Routes {
    const val HOME = "home"
    const val ENTER_NAME = "enter_name"
    const val LOBBY = "lobby"
    const val PRE_GAME = "pregame"
    const val GAME = "game"
}

@Composable
fun DynamicBackground(imageID: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = imageID),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(0.dp)
        )
    }
}

@Composable
fun MusicPlayer(
    musicResId: Int
) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentMusic by remember { mutableIntStateOf(-1) }
    val lifecycleOwner = LocalLifecycleOwner.current


    //When MusicPlayer is created
    LaunchedEffect(musicResId) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, musicResId).apply {
                isLooping = true
                start()
                currentMusic = musicResId
            }
        }

        else if (musicResId != currentMusic) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, musicResId).apply {
                isLooping = true
                start()
                currentMusic = musicResId
            }
        }
    }

    //When MusicPlayer is killed
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> mediaPlayer?.pause()
                Lifecycle.Event.ON_START -> mediaPlayer?.start()
                else -> Unit
            }
        }

        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            mediaPlayer?.apply {
                stop()
                release()
            }
            mediaPlayer = null
        }
    }
}


@Composable
fun BattleShipsApp() {

    // MusicPlayer is used here
    var currentSong by remember { mutableIntStateOf(R.raw.lobbytheme) }
    MusicPlayer(currentSong)

    //Global background
    var currentBackground by remember { mutableIntStateOf(R.drawable.nightbackground) }
    DynamicBackground(currentBackground)

    //Global viewModel (could be singleton)
    val playerViewModel: PlayerViewModel = viewModel()
    val sharedPreferences =
        LocalContext.current.getSharedPreferences("TicTacToePrefs", Context.MODE_PRIVATE)
    LaunchedEffect(Unit) {
        playerViewModel.localUserID = sharedPreferences.getString("playerId", null).toString()
        playerViewModel.localUserName = sharedPreferences.getString("playerName", null)
    }

    //Create global NavHost
    //Each route has a may can update the currentBackground
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) }
    ) {
        composable(Routes.HOME) {
            currentBackground = R.drawable.nightbackground
            currentSong = R.raw.lobbytheme
            HomeScreen(navController, playerViewModel)
        }
        composable(Routes.ENTER_NAME) {
            currentBackground = R.drawable.nightbackground
            currentSong = R.raw.lobbytheme
            EnterNameScreen(navController, playerViewModel)
        }
        composable(Routes.LOBBY) {
            currentBackground = R.drawable.sunset
            currentSong = R.raw.lobbytheme
            LobbyScreen(navController, playerViewModel)
        }

        composable(Routes.PRE_GAME + "/{gameID}") { backStackEntry ->
            val gameID = backStackEntry.arguments?.getString("gameID") ?: ""
            currentBackground = R.drawable.lighthouse
            currentSong = R.raw.maintheme
            PreGameScreen(navController, playerViewModel, gameID)
        }
        composable(Routes.GAME + "/{gameID}") { backStackEntry ->
            val gameID = backStackEntry.arguments?.getString("gameID") ?: ""
            currentBackground = R.drawable.lighthouse
            currentSong = R.raw.maintheme
            GameScreen(navController, playerViewModel, gameID)
        }
    }
}