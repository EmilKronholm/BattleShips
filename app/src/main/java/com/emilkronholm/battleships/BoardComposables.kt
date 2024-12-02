package com.emilkronholm.battleships

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Grid()
{
    var board by remember {mutableStateOf(Board())}
    var selectedBoat : Ship? by remember { mutableStateOf(null) }
    var offset by remember { mutableIntStateOf(0) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(10),
        modifier = Modifier.padding(8.dp).background(Color(70, 21, 100, 200))
    ) {
        items(100) { index ->
            val coordinate = Coordinate(index/10, index%10)
            GridItem(index, board.getState(coordinate)) {
                //If null, try to select a new boat
                if (selectedBoat == null)
                {
                    selectedBoat = board.getShipAt(coordinate)



                    println("Tried to get boat at $coordinate")
                    println(" is null? ")
                    println(selectedBoat == null)
                }
                //If not null, try to move selected boat
                else
                {
                    val pos = selectedBoat!!.parts[0].coordinate

                    selectedBoat?.moveShipTo(coordinate)
                    board.ships.remove(selectedBoat!!)

                    if (!board.isShipValid(selectedBoat!!))
                    {
                        println("Ooops, pos is not valid...")
                        selectedBoat?.moveShipTo(pos)
                        board.ships.add(selectedBoat!!)


                        var temp = board
                        board = Board()
                        board = temp
                    }
                    else
                    {
                        board.ships.add(selectedBoat!!)
                        var temp = board
                        board = Board()
                        board = temp

                        println("Tried to move boat to $coordinate")
                        selectedBoat = null
                    }

                    selectedBoat = null
                }
            }
        }
    }
}

@Composable
fun GridItem(index: Int, state : BoardSquareState, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var status by remember { mutableStateOf(false) }
    var color by remember { mutableStateOf(Color.Red) }

    if (state == BoardSquareState.HIDDEN) {
        color = Color(173, 173, 227, 255)
    } else {
        color = Color(99, 21, 206, 255)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.05f)
            .aspectRatio(1f)
            .padding(2.dp)
            .background(color)
            .clickable {
                onClick()
            }
    )
}