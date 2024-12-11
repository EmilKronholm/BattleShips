package com.emilkronholm.battleships

import android.content.Context
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
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BattleShipsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
fun BattleShipsApp() {
    val navController = rememberNavController()
    val playerViewModel = PlayerViewModel()

    val sharedPreferences =
        LocalContext.current.getSharedPreferences("TicTacToePrefs", Context.MODE_PRIVATE)

    LaunchedEffect(Unit) {
        playerViewModel.localUserID = sharedPreferences.getString("playerId", null).toString()
        playerViewModel.localUserName = sharedPreferences.getString("playerName", null)
    }

    HomeScreenBackground()
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
            DynamicBackground(R.drawable.sandybech)
            PreGameScreen(navController, playerViewModel, gameID)
        }
        composable(Routes.GAME + "/{gameID}") { backStackEntry ->
            val gameID = backStackEntry.arguments?.getString("gameID") ?: ""
            DynamicBackground(R.drawable.sandybech)
            GameScreen(navController, playerViewModel, gameID)
        }
        composable(Routes.POST_GAME + "{result}") { backStackEntry ->
            val result = backStackEntry.arguments?.getString("result") ?: ""
            DynamicBackground(R.drawable.sunset)
            PostGameScreen(navController, result == "win")
        }
    }
}