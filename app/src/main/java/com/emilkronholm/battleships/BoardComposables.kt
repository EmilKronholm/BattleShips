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

                GridItem(index, board.getState(coordinate), invalidCoordinates.contains(coordinate))
            }
        }
    }

    @Composable
    fun GridItem(index: Int, state : BoardSquareState, isError: Boolean, modifier: Modifier = Modifier) {
        var status by remember { mutableStateOf(false) }
        var color by remember { mutableStateOf(Color.Transparent) }
        var id by remember { mutableIntStateOf(0) }

        if (state == BoardSquareState.HIDDEN) {
            id = R.drawable.metal_tile
        }
        else {
            id = R.drawable.water_tile
        }

        color = if (isError) Color.Red else Color.Transparent

        Box(
            modifier = Modifier
                .fillMaxWidth(0.05f)
                .aspectRatio(1f)
                .padding(1.dp)
        ) {

            Image(
                painter = painterResource(id = id),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(0.5.dp)
            )
            if (isError) {
                Box (
                    modifier = Modifier.fillMaxSize()
                        .background(color.copy(alpha = 0.5f))
                )
            }

        }
    }