package com.emilkronholm.battleships

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun LobbyScreen(navController: NavController, playerViewModel: PlayerViewModel, modifier: Modifier = Modifier) {
    val challengeViewModel : ChallengeViewModel = viewModel()
    val gameViewModel : GameViewModel = viewModel()

    val onlinePlayers by playerViewModel.players.asStateFlow().collectAsStateWithLifecycle()
    val challenges by challengeViewModel.challenges.asStateFlow().collectAsStateWithLifecycle()
    val games by gameViewModel.gamesMap.asStateFlow().collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        playerViewModel.scanForPlayers()
        challengeViewModel.scanChallengesForPlayer(playerViewModel.localUserID)
        gameViewModel.startScanForGames(playerViewModel.localUserID)
    }

    //Iterate through all challenges (when updated) and display all popups
    ChallengePopup(challenges, onlinePlayers, challengeViewModel, gameViewModel)

    LaunchedEffect(games) {
        for (game in games) {
            if (game.value.player1ID == playerViewModel.localUserID ||
                game.value.player2ID == playerViewModel.localUserID) {
                navController.navigate("${Routes.PRE_GAME}/${game.key}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Transparent),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "WELCOME, \n ${playerViewModel.localUserName}",
            fontSize = 70.sp,
            fontFamily = PixelFont,
            lineHeight = 50.sp,
            textAlign = TextAlign.Center,
            color = Color.White
        )

        Text("Online Players", fontFamily = PixelFont, fontSize = 25.sp, color = Color.White)
        HorizontalDivider(
            color = Color.White
        )

        OnlinePlayersLazyColumn(onlinePlayers, playerViewModel, challengeViewModel)

    }
}

@Composable
fun OnlinePlayersLazyColumn(onlinePlayers: Map<String, Player>,playerViewModel: PlayerViewModel, challengeViewModel: ChallengeViewModel)
{
    if (onlinePlayers.isNotEmpty()) {
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
    else {
        Box(Modifier.height(350.dp))
        {
            Text("Loading...", fontFamily = PixelFont, fontSize = 30.sp, color = Color.White)
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
        Text(player.name, color = Color.White, fontFamily = PixelFont, fontSize = 25.sp)
        Spacer(modifier = Modifier.weight(1f)) // Spacer expands to push the button to the right
        Button(
            onClick = {
                onClick()
            },
            colors = ButtonColors(
                containerColor = Color.White.copy(alpha = 0.7f),
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
            Text("Battle", fontFamily = PixelFont, fontSize = 20.sp)
        }
    }
}

//TODO(): PopUp is used as global object, move it to a more appropiate file
@Composable
fun PopUp(
    title: String,
    message: String,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
    isPrompt: Boolean = true
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(250,250, 250, 220), shape = RoundedCornerShape(4.dp))
                .padding(16.dp)
        ) {
            if (isPrompt) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clickable { onDismiss() }
                        .padding(8.dp)
                ) {
                    Text("X", fontFamily = PixelFont, fontSize = 20.sp)
                }
            }

            Column (
                modifier = Modifier.padding(top=14.dp)
            ){
                Text(title, fontFamily = PixelFont, fontSize = 30.sp)
                Text(message, fontFamily = PixelFont, fontSize = 19.sp, modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Button(
                        onClick = { onConfirm() },
                        colors = ButtonColors(
                            contentColor = Color.White,
                            containerColor = Color.Green,
                            disabledContentColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        )
                    ) {
                        Text(if (isPrompt) "Confirm" else "OK", fontFamily = PixelFont, fontSize = 18.sp)
                    }

                    if (isPrompt) {
                        Button(
                            onClick = { onDismiss() },
                            colors = ButtonColors(
                                contentColor = Color.White,
                                containerColor = Color.Red,
                                disabledContentColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            )
                        ) {
                            Text("Dismiss", fontFamily = PixelFont, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ChallengePopup(challenges : Map<String, Challenge>, onlinePlayers: Map<String, Player>, challengeViewModel: ChallengeViewModel, gameViewModel: GameViewModel) {
    val context = LocalContext.current
    val plingSoundEffect = remember { MediaPlayer.create(context, R.raw.pling)}

    LaunchedEffect (plingSoundEffect) {
        plingSoundEffect.setVolume(1f, 1f)
    }

    DisposableEffect(Unit) {
        onDispose {
            plingSoundEffect.release()
        }
    }

    challenges.forEach { challenge ->
        val playerName = onlinePlayers[challenge.value.challenger]?.name
        plingSoundEffect.start()

        PopUp(
            title = "New challenge",
            message = "$playerName has challenged you to a game!",
            onDismiss = {
                challengeViewModel.declineChallenge(
                    challenge.key
                )
            },
            onConfirm = {
                // Accept the challenge
                challengeViewModel.acceptChallenge(challenge.key)
                gameViewModel.createGame(challenge.value.recipient, challenge.value.challenger)
            }
        )
    }

}