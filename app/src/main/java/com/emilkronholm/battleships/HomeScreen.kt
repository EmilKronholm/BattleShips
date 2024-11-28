package com.emilkronholm.battleships

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController

val PixelFont = FontFamily(Font(R.font.pixel, FontWeight.Normal))



@Composable
fun HomeScreenBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.nightbackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(2.dp)
        )
    }
}

@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Transparent),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "BATTLE SHIPS",
            fontSize = 116.sp,
            fontFamily = PixelFont,
            lineHeight = 70.sp,
            textAlign = TextAlign.Center,
            color = Color.White
        )
        Button(
            modifier = Modifier.width(300.dp),
            onClick = {
                navController.navigate(Routes.ENTER_NAME)
            }
        ) {
            Text("New Player", fontSize = 30.sp, fontFamily = PixelFont)
        }

        Button(
            modifier = Modifier.width(300.dp),
            onClick = {}
        ) {
            Text("Continue as *playername*", fontSize = 20.sp, fontFamily = PixelFont)
        }
    }
}


@Composable
fun EnterNameScreen(navController: NavController, modifier: Modifier = Modifier) {
    var inputName by remember { mutableStateOf("") }

    Button(
        onClick = {
            navController.popBackStack()
        },
        colors = ButtonColors(
            contentColor = Color.White,
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.Transparent
        )
    ) {
        Text("Back",
            fontSize = 30.sp,
            fontFamily = PixelFont,
            lineHeight = 70.sp,
            textAlign = TextAlign.Center,
            color = Color.White)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Transparent)
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Please enter your name",
            fontSize = 50.sp,
            fontFamily = PixelFont,
            lineHeight = 40.sp,
            textAlign = TextAlign.Center,
            color = Color.White
        )

        TextField(
            value = inputName,
            onValueChange = {inputName = it},
            maxLines = 1,
            singleLine = true,
            modifier = Modifier.padding(20.dp)
        )

        Button(
            modifier = Modifier.width(300.dp),
            onClick = {}
        ) {
            Text("Join", fontSize = 30.sp, fontFamily = PixelFont)
        }
    }
}
