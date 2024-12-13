package com.emilkronholm.battleships

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore.Audio.Media
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
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
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
    const val POST_GAME = "game"
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
fun BattleShipsApp() {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = viewModel()
    val context = LocalContext.current
    val sharedPreferences =
        LocalContext.current.getSharedPreferences("TicTacToePrefs", Context.MODE_PRIVATE)

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    LaunchedEffect(Unit) {
        playerViewModel.localUserID = sharedPreferences.getString("playerId", null).toString()
        playerViewModel.localUserName = sharedPreferences.getString("playerName", null)

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.maintheme).apply {
                isLooping = true
                start()
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    mediaPlayer?.pause()
                }
                Lifecycle.Event.ON_START -> {
                    mediaPlayer?.start()
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

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.apply {
                stop()
                release()
            }
            mediaPlayer = null
        }
    }


    NavHost(
        navController = navController,
        //startDestination = "game/96Rgrm2x9U0fCaTByQTF",
        startDestination = Routes.HOME,
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) }
    ) {
        composable(Routes.HOME) {
            DynamicBackground(R.drawable.nightbackground)
            HomeScreen(navController, playerViewModel)
        }
        composable(Routes.ENTER_NAME) {
            DynamicBackground(R.drawable.nightbackground)
            EnterNameScreen(navController, playerViewModel)
        }
        composable(Routes.LOBBY) {
            DynamicBackground(R.drawable.sunset)
            LobbyScreen(navController, playerViewModel)
        }

        composable(Routes.PRE_GAME + "/{gameID}") { backStackEntry ->
            val gameID = backStackEntry.arguments?.getString("gameID") ?: ""
            DynamicBackground(R.drawable.lighthouse)
            PreGameScreen(navController, playerViewModel, gameID)
        }
        composable(Routes.GAME + "/{gameID}") { backStackEntry ->
            val gameID = backStackEntry.arguments?.getString("gameID") ?: ""
            DynamicBackground(R.drawable.lighthouse)
            GameScreen(navController, playerViewModel, gameID)
        }
        composable(Routes.POST_GAME + "{result}") { backStackEntry ->
            val result = backStackEntry.arguments?.getString("result") ?: ""
            DynamicBackground(R.drawable.sunset)
            PostGameScreen(navController, result == "win")
        }
    }
}