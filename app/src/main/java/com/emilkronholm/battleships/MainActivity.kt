package com.emilkronholm.battleships

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
import androidx.compose.runtime.currentRecomposeScope
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
    const val PRE_GAME = "pregame/{gameID}"
    const val GAME = "game/{gameID}"
    const val POST_GAME = "game/{gameID}"
}

@Composable
fun BattleShipsApp() {
    val navController = rememberNavController()
    val playerViewModel = PlayerViewModel()

    HomeScreenBackground()
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) }
    ) {
        composable(Routes.HOME) { HomeScreen(navController, playerViewModel) }
        composable(Routes.ENTER_NAME) { EnterNameScreen(navController, playerViewModel) }
        composable(Routes.LOBBY) { LobbyScreen(navController, playerViewModel) }

        composable(Routes.PRE_GAME) {  }
        composable(Routes.GAME) { }
        composable(Routes.POST_GAME) {  }
    }
}