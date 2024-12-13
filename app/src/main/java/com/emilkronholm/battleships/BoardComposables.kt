    package com.emilkronholm.battleships
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.border
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.aspectRatio
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.lazy.grid.GridCells
    import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.blur
    import androidx.compose.ui.geometry.Rect
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.input.pointer.changedToDown
    import androidx.compose.ui.input.pointer.changedToUp
    import androidx.compose.ui.input.pointer.pointerInput
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.layout.boundsInRoot
    import androidx.compose.ui.layout.onGloballyPositioned
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.unit.dp

    @Composable
    fun PreGameGrid(board: Board, onMove: () -> Unit) {

        var selectedBoat : Ship? by remember { mutableStateOf(null) }
        var offset by remember { mutableStateOf(Coordinate(0, 0)) }
        val invalidCoordinates = board.getListOfInvalidCoordinates()

        var rect : Rect = Rect(0f, 0f, 0f, 0f)

        LazyVerticalGrid(
            columns = GridCells.Fixed(10),
            modifier = Modifier
                .padding(8.dp)
                .background(Color(58, 123, 224, 255))
                .onGloballyPositioned { layoutCoordinates ->
                    rect = layoutCoordinates.boundsInRoot()
                }
                .border(2.dp, Color(44, 79, 138))
                .pointerInput(Unit) {
                    awaitPointerEventScope {

                        var hasMoved = false

                        while (true) {
                            val event = awaitPointerEvent()
                            val currentPosition = event.changes.first().position

                            when {
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
                                            onMove()
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


                                    if (selectedBoat != null && selectedBoat!!.parts[0].coordinate != coordinate + offset) {
                                        val newPos = coordinate.copy(
                                            x = (coordinate + offset).x.coerceIn(0, 9),
                                            y = (coordinate + offset).y.coerceIn(0, 9)
                                        )

                                        hasMoved = true
                                        board.ships.remove(selectedBoat!!)
                                        selectedBoat?.moveShipTo(newPos

                                                + offset)
                                        board.ships.add(0, selectedBoat!!)
                                        onMove()

                                    } else {
                                        val selectedShipInfo = board.getShipAt(coordinate)
                                        selectedBoat = selectedShipInfo?.ship
                                        if (selectedShipInfo != null) {
                                            offset = if (selectedBoat!!.isVertical) {
                                                Coordinate(0, selectedShipInfo.headPos.y - y)
                                            } else {
                                                Coordinate(selectedShipInfo.headPos.x - x, 0)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                })
            {

            val listOfInvalidCoordinates = board.getListOfInvalidCoordinates()
            items(100) { index ->
                val coordinate = Coordinate(index%10, index/10)
                val state = board.getState(coordinate)
                val imageID =


                GridItem(
                    imageID = if (state == BoardSquareState.HIDDEN) R.drawable.metal_tile
                            else R.drawable.water_tile,
                    isError = listOfInvalidCoordinates.contains(coordinate)
                )
            }
        }
    }

    @Composable
    fun GridItem(imageID: Int = 0, isError: Boolean = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.05f)
                .aspectRatio(1f)
                .padding(1.dp)
        ) {

            Image(
                painter = painterResource(id = imageID),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(0.5.dp)
            )
            if (isError) {
                Box (
                    modifier = Modifier.fillMaxSize()
                        .background(Color.Red.copy(alpha = 0.5f))
                )
            }

        }
    }