package com.emilkronholm.battleships

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController

@Composable
fun PostGameScreen(navController: NavController, isWinner: Boolean) {

    val text = if (isWinner) "YOU WON" else "YOU LOST"
    val color = if (isWinner) Color.Green else Color.Red

    Column() {
        Text(text, color = color)
        Button(
            onClick = {
                navController.navigate(Routes.LOBBY)
            }
        ) {
            Text("Back to lobby")
        }
    }
}
