package com.emilkronholm.battleships

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun LobbyScreen(navController: NavController, modifier: Modifier = Modifier) {
    val playerModel : PlayerViewModel = viewModel()
    val challengeModel : ChallengeViewModel = viewModel()

    val onlinePlayers = playerModel.players.collectAsState()
    val challenges = challengeModel.challenges.collectAsState()

    LaunchedEffect(Unit) {
        playerModel.scanForPlayers()
        challengeModel.scanChallengesForPlayer("localplayer");
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Transparent),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "WELCOME, \n playername",
            fontSize = 70.sp,
            fontFamily = PixelFont,
            lineHeight = 50.sp,
            textAlign = TextAlign.Center,
            color = Color.White
        )
    }
}

@Composable
fun InviteRow(navController: NavController, modifier: Modifier = Modifier) {
    Row {
    }
}

