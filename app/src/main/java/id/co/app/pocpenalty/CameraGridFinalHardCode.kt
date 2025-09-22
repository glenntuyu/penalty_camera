package id.co.app.pocpenalty

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import id.co.app.pocpenalty.data.GridDot
import id.co.app.pocpenalty.data.GridRect
import id.co.app.pocpenalty.data.TagType
import androidx.core.graphics.scale

/**
 * Created by Tuyu on 7/8/2025.
 * Sinarmas APP
 * christian_tuyu@app.co.id
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridTaggingScreen3(modifier: Modifier = Modifier, imageResId: Int = R.drawable.woodpile145) {
    val context = LocalContext.current
    val imageBitmap = decodeSampledBitmapFromResource(
        context.resources,
        imageResId,
        300,
        400,
    ).asImageBitmap()
    val gridRows = 5
    val gridCols = 6
    val gridDots = remember { mutableStateListOf<GridDot>() }
    val coroutineScope = rememberCoroutineScope()

    var selectedGrid by remember { mutableStateOf<GridRect?>(null) }
    var selectedTapOffset by remember { mutableStateOf<Offset?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    var screenState by remember { mutableStateOf("grid") }

    if (screenState == "zoom" && selectedGrid != null) {
        ZoomedGridScreen2(
            imageBitmap = imageBitmap,
            grid = selectedGrid!!,
            gridDots = gridDots,
            onBack = { screenState = "grid" },
            onTap = { offset, size ->
                selectedTapOffset = Offset(
                    x = offset.x / size.width,
                    y = offset.y / size.height
                )
                showSheet = true
            }
        )

        if (showSheet && selectedTapOffset != null) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Pilih Abnormalitas", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    TagType.values().forEach { tag ->
                        Button(
                            onClick = {
                                gridDots.add(GridDot(selectedGrid!!, selectedTapOffset!!, tag))
                                showSheet = false
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = tag.color)
                        ) {
                            Text(tag.name)
                        }
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(imageBitmap.width / imageBitmap.height.toFloat())
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val cellWidth = size.width / gridCols
                        val cellHeight = size.height / gridRows
                        val col = (tapOffset.x / cellWidth).toInt()
                        val row = (tapOffset.y / cellHeight).toInt()
                        val rect = Rect(
                            Offset((col * cellWidth).toFloat(), (row * cellHeight).toFloat()),
                            Offset(((col + 1) * cellWidth).toFloat(), ((row + 1) * cellHeight).toFloat())
                        )
                        selectedGrid = GridRect(row, col, rect)
                        screenState = "zoom"
                    }
                }
            ) {
                drawImage(imageBitmap)

                val cellWidth = size.width / gridCols
                val cellHeight = size.height / gridRows
                val strokeWidth = 3f
                for (i in 0..gridCols) {
                    drawLine(
                        color = Color.Yellow,
                        start = Offset(i * cellWidth, 0f),
                        end = Offset(i * cellWidth, size.height),
                        strokeWidth = strokeWidth
                    )
                }
                for (j in 0..gridRows) {
                    drawLine(
                        color = Color.Yellow,
                        start = Offset(0f, j * cellHeight),
                        end = Offset(size.width, j * cellHeight),
                        strokeWidth = strokeWidth
                    )
                }

                gridDots.forEach { dot ->
                    val rect = dot.grid.rect
                    val position = Offset(
                        rect.left + dot.percentOffset.x * rect.width,
                        rect.top + dot.percentOffset.y * rect.height
                    )
                    drawCircle(
                        color = dot.tagType.color,
                        radius = 20f,
                        center = position
                    )
                }
            }
        }
    }
}

@Composable
fun ZoomedGridScreen2(
    imageBitmap: androidx.compose.ui.graphics.ImageBitmap,
    grid: GridRect,
    gridDots: List<GridDot>,
    onBack: () -> Unit,
    onTap: (Offset, IntSize) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val rawBitmap = android.graphics.Bitmap.createBitmap(
            imageBitmap.asAndroidBitmap(),
            grid.rect.left.toInt(),
            grid.rect.top.toInt(),
            grid.rect.width.toInt(),
            grid.rect.height.toInt()
        )

        val canvasSize = with(LocalDensity.current) {
            val width = LocalConfiguration.current.screenWidthDp.dp.toPx().toInt()
            val height = (width * (grid.rect.height / grid.rect.width)).toInt()
            IntSize(width, height)
        }

        val scaledBitmap = rawBitmap.scale(canvasSize.width, canvasSize.height).asImageBitmap()

        Canvas(modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(grid.rect.width / grid.rect.height)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onTap(offset, canvasSize)
                }
            }
        ) {
            drawImage(scaledBitmap)
            gridDots.filter { it.grid == grid }.forEach { dot ->
                val position = Offset(
                    dot.percentOffset.x * size.width,
                    dot.percentOffset.y * size.height
                )
                drawCircle(
                    color = dot.tagType.color,
                    radius = 20f,
                    center = position
                )
            }
        }

        IconButton(
            onClick = { onBack() },
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
    }
}
