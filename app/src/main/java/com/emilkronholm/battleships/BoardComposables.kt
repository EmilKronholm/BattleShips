    package com.emilkronholm.battleships
    import android.widget.Toast
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.gestures.detectDragGestures
    import androidx.compose.foundation.gestures.detectTapGestures
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
    import androidx.compose.ui.geometry.Rect
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.input.pointer.changedToDown
    import androidx.compose.ui.input.pointer.changedToUp
    import androidx.compose.ui.input.pointer.pointerInput
    import androidx.compose.ui.layout.boundsInRoot
    import androidx.compose.ui.layout.onGloballyPositioned
    import androidx.compose.ui.platform.LocalConfiguration
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.unit.dp

    @Composable
    fun Grid(board: Board) {

        var selectedBoat : Ship? by remember { mutableStateOf(null) }
        var offset by remember { mutableStateOf(Coordinate(0, 0)) }
        val invalidCoordinates = board.getListOfInvalidCoordinates()
        println(board.ships)
        println(invalidCoordinates)

        val configuration = LocalConfiguration.current
        var rect : Rect = Rect(0f, 0f, 0f, 0f)

        LazyVerticalGrid(
            columns = GridCells.Fixed(10),
            modifier = Modifier
                .padding(8.dp)
                .background(Color(70, 21, 100, 200))
                .onGloballyPositioned { layoutCoordinates ->
                    rect = layoutCoordinates.boundsInRoot()
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {

                        var hasMoved = false;

                        while (true) {
                            val event = awaitPointerEvent()
                            val currentPosition = event.changes.first().position

                            when {
                                event.changes.any { it.changedToDown() } -> {

                                }
                                event.changes.any { it.changedToUp() } -> {
                                    //Rotate boat if...
                                    if (!hasMoved) {
                                        val x = (currentPosition.x / (rect.size.width / 10)).toInt()
                                        val y = (currentPosition.y / (rect.size.height / 10)).toInt()
                                        val coordinate = Coordinate(x, y)

                                        val ship = board.getShipAt(coordinate)
                                        if (ship != null)
                                        {
                                            board.ships.remove(ship.ship)
                                            ship.ship.rotate()
                                            board.ships.add(0, ship.ship)
                                        }
                                    }

                                    hasMoved = false;
                                    selectedBoat = null
                                }
                                event.changes.any { it.pressed } -> {
                                    // Dragging gesture detected
                                    val x = (currentPosition.x / (rect.size.width / 10)).toInt()
                                    val y = (currentPosition.y / (rect.size.height / 10)).toInt()
                                    val coordinate = Coordinate(x, y)

                                    println("Dragging at grid coordinates: $coordinate")

                                    if (selectedBoat != null && selectedBoat!!.parts[0].coordinate != coordinate + offset) {
                                        hasMoved = true
                                        board.ships.remove(selectedBoat!!)
                                        selectedBoat?.moveShipTo(coordinate + offset)
                                        board.ships.add(0, selectedBoat!!)
                                        println("Boat moved to $coordinate")
                                    } else {
                                        val selectedShipInfo = board.getShipAt(coordinate)
                                        selectedBoat = selectedShipInfo?.ship
                                        if (selectedShipInfo != null) {
                                            offset = if (selectedBoat!!.isVertical) {
                                                Coordinate(0, selectedShipInfo.headPos.y - y)
                                            } else {
                                                Coordinate(selectedShipInfo.headPos.x - x, 0)
                                            }
                                            println("Boat selected: $selectedBoat")
                                        }
                                    }
                                }
                            }
                        }
                    }
                })
            {
            items(100) { index ->
                val coordinate = Coordinate(index%10, index/10)
                val state = if (invalidCoordinates.contains(coordinate))
                    BoardSquareState.HIT else board.getState(coordinate)

                GridItem(index, state) {


//                    //If null, try to select a new boat
//                    if (selectedBoat == null)
//                    {
//                        selectedBoat = board.getShipAt(coordinate)
//                        println("Tried to get boat at $coordinate")
//                    }
//                    //If not null, try to move selected boat
//                    else
//                    {
//                        val pos = selectedBoat!!.parts[0].coordinate
//
//                        if (pos == coordinate)
//                        {
//                            selectedBoat!!.isVertical = !selectedBoat!!.isVertical
//                        }
//
//                        board.ships.remove(selectedBoat!!)
//                        selectedBoat?.moveShipTo(coordinate)
//                        board.ships.add(0, selectedBoat!!)
//                        selectedBoat = null
//                    }
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
        } else if (state == BoardSquareState.HIT) {
            color = Colors.hit
        } else {
            color = Color(99, 21, 206, 255)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.05f)
                .aspectRatio(1f)
                .padding(2.dp)
                .background(color)

//                .clickable {
//                    onClick()
//                }
        )
    }