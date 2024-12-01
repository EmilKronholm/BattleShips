package com.emilkronholm.battleships

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun LobbyScreen(navController: NavController, playerViewModel: PlayerViewModel, modifier: Modifier = Modifier) {
    val challengeViewModel : ChallengeViewModel = viewModel()
    val gameViewModel = GameViewModel()

    val onlinePlayers by playerViewModel.players.asStateFlow().collectAsStateWithLifecycle()
    val challenges by challengeViewModel.challenges.asStateFlow().collectAsStateWithLifecycle()
    val games by gameViewModel.gamesMap.asStateFlow().collectAsStateWithLifecycle()

    println(challenges.size)

    LaunchedEffect(Unit) {
        playerViewModel.scanForPlayers()
        challengeViewModel.scanChallengesForPlayer(playerViewModel.localUserID)
        gameViewModel.startScanForGames(playerViewModel.localUserID)
    }

    ChallengePopup(challenges, onlinePlayers, challengeViewModel, gameViewModel)

    for (game in games) {
        println("GAME JUST STARTED!!!")
        //we have found a game oh yeah
        navController.navigate(Routes.GAME+"/"+game.key)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Transparent),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "WELCOME, \n ${onlinePlayers[playerViewModel.localUserID]?.name}",
            fontSize = 70.sp,
            fontFamily = PixelFont,
            lineHeight = 50.sp,
            textAlign = TextAlign.Center,
            color = Color.White
        )

        Text("Online Players", fontSize = 25.sp, color = Color.White)
        HorizontalDivider(
            color = Color.White
        )

        OnlinePlayersLazyColumn(onlinePlayers, playerViewModel, challengeViewModel)

    }
}

@Composable
fun FindGame(games: Map<String, Game>)
{

}

@Composable
fun OnlinePlayersLazyColumn(onlinePlayers: Map<String, Player>,playerViewModel: PlayerViewModel, challengeViewModel: ChallengeViewModel)
{
    LazyColumn (
        modifier = Modifier.height(350.dp)
    ) {
        val playersList =  onlinePlayers.entries.toList()
        items(playersList) { (playerID, player) ->
            if (playerViewModel.localUserID != playerID) {
                InviteRow(player, onClick = {
                    challengeViewModel.createChallenge(
                        playerID = playerViewModel.localUserID,
                        opponentID = playerID
                    )
                })
            }
        }
    }
}


@Composable
fun InviteRow(player: Player, modifier: Modifier = Modifier, onClick : () -> Unit) {
    Row(
        modifier = modifier
            .padding(
                top = 10.dp,
                start = 22.dp,
                end = 22.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(player.name, color = Color.White)
        Spacer(modifier = Modifier.weight(1f)) // Spacer expands to push the button to the right
        Button(
            onClick = {
                onClick()
            },
            colors = ButtonColors(
                containerColor = Color.White,
                disabledContentColor = Color.Black,
                disabledContainerColor = Color.Gray,
                contentColor = Color.Black
            )
        ) {
            Image(
                painter = painterResource(id = R.drawable.swords),
                contentDescription = "Invite-Logo",
                modifier = Modifier.size(18.dp)
            )
            Text("Battle")
        }
    }
}

@Composable
fun ChallengePopup(challenges : Map<String, Challenge>, onlinePlayers: Map<String, Player>, challengeViewModel: ChallengeViewModel, gameViewModel: GameViewModel) {
    challenges.forEach { challenge ->
        AlertDialog(
            onDismissRequest = {
                challengeViewModel.declineChallenge(
                    challenge.key
                )
            },
            title = { Text("New Challenge!") },
            text = {
                val playerName = onlinePlayers[challenge.value.challenger]?.name
                Text("$playerName has challenged you to a game!")
            },
            confirmButton = {
                Button(onClick = {
                    // Accept the challenge
                    challengeViewModel.acceptChallenge(challenge.key)
                    gameViewModel.createGame(challenge.value.recipient, challenge.value.challenger)
                }) {
                    Text("Accept")
                }
            },
            dismissButton = {
                Button(onClick = {
                    challengeViewModel.declineChallenge(challenge.key)
                }) {
                    Text("Decline")
                }
            }
        )
    }
}