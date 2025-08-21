package id.co.app.poccamera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scale
import id.co.app.poccamera.data.GridDot
import id.co.app.poccamera.data.GridRect
import id.co.app.poccamera.data.TagType
import id.co.app.poccamera.data.WoodPileData

/**
 * Created by Tuyu on 7/8/2025.
 * Sinarmas APP
 * christian_tuyu@app.co.id
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridTaggingScreen(
    imageBitmap: ImageBitmap,
    woodPileData: WoodPileData,
    scalingInfo: ScalingInfo,
    modifier: Modifier = Modifier
) {
    // State management for zoom functionality
    val gridDots = remember { mutableStateListOf<GridDot>() }
    val coroutineScope = rememberCoroutineScope()

    var selectedGrid by remember { mutableStateOf<GridRect?>(null) }
    var selectedTapOffset by remember { mutableStateOf<Offset?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    var screenState by remember { mutableStateOf("grid") }

    var showCropScreen by remember { mutableStateOf(false) }
    var cropRect by remember { mutableStateOf<Rect?>(null) }

    var showCroppedGrid by remember { mutableStateOf(false) }

    // Zoom screen logic
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grid Tagging") },
                navigationIcon = {
                    if (screenState == "zoom") {
                        IconButton(onClick = { screenState = "grid" }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (!showCropScreen) {
            Box(
                modifier = modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (screenState == "zoom" && selectedGrid != null) {
                    ZoomedGridScreen(
                        imageBitmap = imageBitmap,
                        grid = selectedGrid!!,
                        gridDots = gridDots,
                        scalingInfo = scalingInfo,
                        onBack = { screenState = "grid" },
                        onTap = { offset, size ->
                            selectedTapOffset = Offset(
                                x = offset.x / size.width,
                                y = offset.y / size.height
                            )
                            showSheet = true
                        }
                    )

                    // Bottom sheet for tag selection
                    if (showSheet && selectedTapOffset != null) {
                        ModalBottomSheet(
                            onDismissRequest = { showSheet = false },
                            sheetState = sheetState
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Text("Pilih Abnormalitas", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                TagType.entries.forEach { tag ->
                                    Button(
                                        onClick = {
                                            gridDots.add(
                                                GridDot(
                                                    selectedGrid!!,
                                                    selectedTapOffset!!,
                                                    tag
                                                )
                                            )
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
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GridImage(
                            modifier = Modifier.weight(1f),
                            imageBitmap = imageBitmap,
                            woodPileData = woodPileData,
                            scalingInfo = scalingInfo,
                            gridDots = gridDots,
                            onClickGrid = {
                                selectedGrid = it
                                screenState = "zoom"
                            }
                        )

                        Button(
                            onClick = {
                                showCropScreen = true
                            },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Re-crop")
                        }
                    }
                }
            }
        } else {
            CropImageScreen(
                modifier = modifier
                    .padding(innerPadding),
                imageBitmap = imageBitmap,
                onCropConfirmed = { rect ->
                    cropRect = rect
                    showCroppedGrid = true
                    showCropScreen = false
                },
                onCancel = { showCropScreen = false }
            )
        }

        if (showCroppedGrid && cropRect != null) {
            CroppedGridScreen(
                imageBitmap = imageBitmap,
                cropRect = cropRect!!,
                woodPileData = woodPileData,
                scalingInfo = scalingInfo,
                onBack = {
                    showCroppedGrid = false
                    cropRect = null
                }
            )
        }
    }
}

@Composable
fun GridImage(
    modifier: Modifier,
    imageBitmap: ImageBitmap,
    woodPileData: WoodPileData,
    scalingInfo: ScalingInfo,
    gridDots: SnapshotStateList<GridDot>,
    onClickGrid: (gridRect: GridRect?) -> Unit,
) {
    var imageDrawParams by remember { mutableStateOf<ImageDrawParams?>(null) }
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    imageDrawParams?.let { params ->
                        // Map tapOffset (canvas) to scaled image coordinates
                        val inImageX =
                            ((tapOffset.x - params.left) / params.scale).coerceIn(
                                0f,
                                imageBitmap.width.toFloat()
                            )
                        val inImageY =
                            ((tapOffset.y - params.top) / params.scale).coerceIn(
                                0f,
                                imageBitmap.height.toFloat()
                            )

                        // Find which grid cell was tapped
                        val gridRect = findGridFromData(
                            Offset(inImageX, inImageY),
                            Size(
                                imageBitmap.width.toFloat(),
                                imageBitmap.height.toFloat()
                            ),
                            woodPileData,
                            imageBitmap,
                            scalingInfo
                        )

                        gridRect?.let {
                            onClickGrid.invoke(gridRect)
                        }
                    }
                }
            }
    ) {
        // Calculate scale and position to fit image in canvas
        val scale = minOf(
            size.width / imageBitmap.width,
            size.height / imageBitmap.height
        )
        val imageWidth = imageBitmap.width * scale
        val imageHeight = imageBitmap.height * scale
        val left = (size.width - imageWidth) / 2f
        val top = (size.height - imageHeight) / 2f

        // Save for tap mapping
        imageDrawParams = ImageDrawParams(left, top, scale)

        // Draw the image, centered and scaled
        withTransform({
            translate(left, top)
            scale(scale, scale)
        }) {
            drawImage(imageBitmap)

            // Draw grid lines from woodPileData with scaled coordinates
            val strokeWidth = 3f / scale

            // Vertical lines - scale coordinates to match the scaled image
            woodPileData.gridLines.vertical.forEach { line ->
                drawLine(
                    color = Color.Yellow,
                    start = Offset(
                        line.p1[0].toFloat() * scalingInfo.scaleX,
                        line.p1[1].toFloat() * scalingInfo.scaleY
                    ),
                    end = Offset(
                        line.p2[0].toFloat() * scalingInfo.scaleX,
                        line.p2[1].toFloat() * scalingInfo.scaleY
                    ),
                    strokeWidth = strokeWidth
                )
            }

            // Horizontal lines - scale coordinates to match the scaled image
            woodPileData.gridLines.horizontal.forEach { line ->
                drawLine(
                    color = Color.Yellow,
                    start = Offset(
                        line.p1[0].toFloat() * scalingInfo.scaleX,
                        line.p1[1].toFloat() * scalingInfo.scaleY
                    ),
                    end = Offset(
                        line.p2[0].toFloat() * scalingInfo.scaleX,
                        line.p2[1].toFloat() * scalingInfo.scaleY
                    ),
                    strokeWidth = strokeWidth
                )
            }

            // Detection box - draw a rectangle using scaled coordinates
            val topLeft = Offset(
                woodPileData.detectionBox.topLeft[0].toFloat() * scalingInfo.scaleX,
                woodPileData.detectionBox.topLeft[1].toFloat() * scalingInfo.scaleY
            )
            val bottomRight = Offset(
                woodPileData.detectionBox.bottomRight[0].toFloat() * scalingInfo.scaleX,
                woodPileData.detectionBox.bottomRight[1].toFloat() * scalingInfo.scaleY
            )
            val rectWidth = bottomRight.x - topLeft.x
            val rectHeight = bottomRight.y - topLeft.y

            drawRect(
                color = Color.Red,
                topLeft = topLeft,
                size = Size(rectWidth, rectHeight),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth * 2)
            )

            // Draw existing grid dots
            gridDots.forEach { dot ->
                val rect = dot.grid.rect
                val position = Offset(
                    rect.left + dot.percentOffset.x * rect.width,
                    rect.top + dot.percentOffset.y * rect.height
                )
                drawCircle(
                    color = dot.tagType.color,
                    radius = 20f / scale, // Scale the radius too
                    center = position
                )
            }
        }
    }
}

// Helper data class for tap mapping
data class ImageDrawParams(
    val left: Float,
    val top: Float,
    val scale: Float
)

// Updated helper function to find grid cell from data with scaling info
private fun findGridFromData(
    tapOffset: Offset,
    canvasSize: androidx.compose.ui.geometry.Size,
    woodPileData: WoodPileData,
    imageBitmap: ImageBitmap,
    scalingInfo: ScalingInfo
): GridRect? {
    // Convert tap offset to original image coordinates
    val originalX = tapOffset.x / scalingInfo.scaleX
    val originalY = tapOffset.y / scalingInfo.scaleY

    // Find which grid cell the tap is in
    val verticalLines = woodPileData.gridLines.vertical.map { it.p1[0] }.sorted()
    val horizontalLines = woodPileData.gridLines.horizontal.map { it.p1[1] }.sorted()

    // Add detection box boundaries
    val allVertical = (listOf(woodPileData.detectionBox.topLeft[0]) + verticalLines + listOf(woodPileData.detectionBox.bottomRight[0])).sorted()
    val allHorizontal = (listOf(woodPileData.detectionBox.topLeft[1]) + horizontalLines + listOf(woodPileData.detectionBox.bottomRight[1])).sorted()

    // Find grid cell
    val col = allVertical.indexOfFirst { it > originalX } - 1
    val row = allHorizontal.indexOfFirst { it > originalY } - 1

    if (col >= 0 && row >= 0 && col < allVertical.size - 1 && row < allHorizontal.size - 1) {
        val rect = Rect(
            Offset(
                allVertical[col] * scalingInfo.scaleX,
                allHorizontal[row] * scalingInfo.scaleY
            ),
            Offset(
                allVertical[col + 1] * scalingInfo.scaleX,
                allHorizontal[row + 1] * scalingInfo.scaleY
            )
        )
        return GridRect(row, col, rect)
    }

    return null
}

@Composable
fun ZoomedGridScreen(
    imageBitmap: androidx.compose.ui.graphics.ImageBitmap,
    grid: GridRect,
    gridDots: List<GridDot>,
    scalingInfo: ScalingInfo,
    onBack: () -> Unit,
    onTap: (Offset, IntSize) -> Unit
) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CroppedGridScreen(
    modifier: Modifier = Modifier,
    imageBitmap: ImageBitmap,
    cropRect: Rect,
    woodPileData: WoodPileData,
    scalingInfo: ScalingInfo,
    onBack: () -> Unit
) {
    var selectedGrid by remember { mutableStateOf<GridRect?>(null) }
    var selectedTapOffset by remember { mutableStateOf<Offset?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    var screenState by remember { mutableStateOf("grid") }
    val gridDots = remember { mutableStateListOf<GridDot>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cropped Grid View") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (screenState == "zoom") {
                            screenState = "grid"
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (screenState == "zoom" && selectedGrid != null) {
                ZoomedGridScreen(
                    imageBitmap = imageBitmap,
                    grid = selectedGrid!!,
                    gridDots = gridDots,
                    scalingInfo = scalingInfo,
                    onBack = { screenState = "grid" },
                    onTap = { offset, size ->
                        selectedTapOffset = Offset(
                            x = offset.x / size.width,
                            y = offset.y / size.height
                        )
                        showSheet = true
                    }
                )

                // Bottom sheet for tag selection
                if (showSheet && selectedTapOffset != null) {
                    ModalBottomSheet(
                        onDismissRequest = { showSheet = false },
                        sheetState = sheetState
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Pilih Abnormalitas", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            TagType.entries.forEach { tag ->
                                Button(
                                    onClick = {
                                        gridDots.add(
                                            GridDot(
                                                selectedGrid!!,
                                                selectedTapOffset!!,
                                                tag
                                            )
                                        )
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
                CroppedGridImage(
                    modifier = Modifier.fillMaxSize(),
                    imageBitmap = imageBitmap,
                    cropRect = cropRect,
                    woodPileData = woodPileData,
                    gridDots = gridDots,
                    onClickGrid = {
                        selectedGrid = it
                        screenState = "zoom"
                    }
                )
            }
        }
    }
}

@Composable
fun CroppedGridImage(
    modifier: Modifier,
    imageBitmap: ImageBitmap,
    cropRect: Rect,
    woodPileData: WoodPileData,
    gridDots: SnapshotStateList<GridDot>,
    onClickGrid: (gridRect: GridRect?) -> Unit,
) {
    var imageDrawParams by remember { mutableStateOf<ImageDrawParams?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    imageDrawParams?.let { params ->
                        // Map tapOffset to image coordinates
                        val inImageX = ((tapOffset.x - params.left) / params.scale)
                            .coerceIn(0f, imageBitmap.width.toFloat())
                        val inImageY = ((tapOffset.y - params.top) / params.scale)
                            .coerceIn(0f, imageBitmap.height.toFloat())

                        // Find which grid cell was tapped within the new crop area
                        val gridRect = findGridInNewCropArea(
                            Offset(inImageX, inImageY),
                            cropRect,
                            woodPileData
                        )

                        gridRect?.let {
                            onClickGrid.invoke(gridRect)
                        }
                    }
                }
            }
    ) {
        // Calculate scale and position to fit full image in canvas
        val scale = minOf(
            size.width / imageBitmap.width,
            size.height / imageBitmap.height
        )
        val imageWidth = imageBitmap.width * scale
        val imageHeight = imageBitmap.height * scale
        val left = (size.width - imageWidth) / 2f
        val top = (size.height - imageHeight) / 2f

        // Save for tap mapping
        imageDrawParams = ImageDrawParams(left, top, scale)

        // Draw the full image, centered and scaled
        withTransform({
            translate(left, top)
            scale(scale, scale)
        }) {
            drawImage(imageBitmap)

            val strokeWidth = 3f / scale

            // Step 1: Draw red outer box using the crop rectangle
            drawRect(
                color = Color.Red,
                topLeft = Offset(cropRect.left, cropRect.top),
                size = Size(cropRect.width, cropRect.height),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth * 2)
            )

            // Step 2: Create grid lines inside the crop rectangle
            val verticalCount = woodPileData.gridLines.vertical.size
            val horizontalCount = woodPileData.gridLines.horizontal.size

            // Calculate grid cell dimensions within the crop area
            val cellWidth = cropRect.width / (verticalCount + 1)
            val cellHeight = cropRect.height / (horizontalCount + 1)

            // Draw vertical grid lines within the crop area
            for (i in 1..verticalCount) {
                val x = cropRect.left + (i * cellWidth)
                drawLine(
                    color = Color.Yellow,
                    start = Offset(x, cropRect.top),
                    end = Offset(x, cropRect.bottom),
                    strokeWidth = strokeWidth
                )
            }

            // Draw horizontal grid lines within the crop area
            for (i in 1..horizontalCount) {
                val y = cropRect.top + (i * cellHeight)
                drawLine(
                    color = Color.Yellow,
                    start = Offset(cropRect.left, y),
                    end = Offset(cropRect.right, y),
                    strokeWidth = strokeWidth
                )
            }

            // Draw existing grid dots
            gridDots.forEach { dot ->
                val rect = dot.grid.rect
                val position = Offset(
                    rect.left + dot.percentOffset.x * rect.width,
                    rect.top + dot.percentOffset.y * rect.height
                )
                drawCircle(
                    color = dot.tagType.color,
                    radius = 20f / scale,
                    center = position
                )
            }
        }
    }
}

// Helper function to find grid cell within the new crop area
private fun findGridInNewCropArea(
    tapOffset: Offset,
    cropRect: Rect,
    woodPileData: WoodPileData
): GridRect? {
    // Check if tap is within the crop rectangle
    if (!cropRect.contains(tapOffset)) {
        return null
    }

    val verticalCount = woodPileData.gridLines.vertical.size
    val horizontalCount = woodPileData.gridLines.horizontal.size

    // Calculate grid cell dimensions within the crop area
    val cellWidth = cropRect.width / (verticalCount + 1)
    val cellHeight = cropRect.height / (horizontalCount + 1)

    // Calculate relative position within the crop area
    val relativeX = tapOffset.x - cropRect.left
    val relativeY = tapOffset.y - cropRect.top

    val col = (relativeX / cellWidth).toInt()
    val row = (relativeY / cellHeight).toInt()

    if (col >= 0 && row >= 0 && col <= verticalCount && row <= horizontalCount) {
        val rect = Rect(
            Offset(
                cropRect.left + (col * cellWidth),
                cropRect.top + (row * cellHeight)
            ),
            Offset(
                cropRect.left + ((col + 1) * cellWidth),
                cropRect.top + ((row + 1) * cellHeight)
            )
        )
        return GridRect(row, col, rect)
    }

    return null
}