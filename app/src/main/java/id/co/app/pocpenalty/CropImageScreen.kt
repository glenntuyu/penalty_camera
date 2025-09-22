package id.co.app.pocpenalty

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

enum class DragHandle {
    NONE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT,
    TOP, BOTTOM, LEFT, RIGHT, MOVE
}

@Composable
fun CropImageScreen(
    modifier: Modifier = Modifier,
    imageBitmap: ImageBitmap,
    onCropConfirmed: (Rect) -> Unit,
    onCancel: () -> Unit
) {
    var cropRect by remember { mutableStateOf<Rect?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var dragHandle by remember { mutableStateOf(DragHandle.NONE) }
    var dragStartOffset by remember { mutableStateOf(Offset.Zero) }
    var initialCropRect by remember { mutableStateOf<Rect?>(null) }

    // Store canvas size and scaling info
    var canvasSize by remember { mutableStateOf<androidx.compose.ui.geometry.Size?>(null) }
    var imageScale by remember { mutableStateOf(1f) }
    var imageLeft by remember { mutableStateOf(0f) }
    var imageTop by remember { mutableStateOf(0f) }

    val handleSize = 30f

    fun getHandleAtPosition(position: Offset, rect: Rect): DragHandle {
        val tolerance = handleSize

        // Check corners first
        if (abs(position.x - rect.left) <= tolerance && abs(position.y - rect.top) <= tolerance) {
            return DragHandle.TOP_LEFT
        }
        if (abs(position.x - rect.right) <= tolerance && abs(position.y - rect.top) <= tolerance) {
            return DragHandle.TOP_RIGHT
        }
        if (abs(position.x - rect.left) <= tolerance && abs(position.y - rect.bottom) <= tolerance) {
            return DragHandle.BOTTOM_LEFT
        }
        if (abs(position.x - rect.right) <= tolerance && abs(position.y - rect.bottom) <= tolerance) {
            return DragHandle.BOTTOM_RIGHT
        }

        // Check edges
        if (abs(position.y - rect.top) <= tolerance && position.x > rect.left && position.x < rect.right) {
            return DragHandle.TOP
        }
        if (abs(position.y - rect.bottom) <= tolerance && position.x > rect.left && position.x < rect.right) {
            return DragHandle.BOTTOM
        }
        if (abs(position.x - rect.left) <= tolerance && position.y > rect.top && position.y < rect.bottom) {
            return DragHandle.LEFT
        }
        if (abs(position.x - rect.right) <= tolerance && position.y > rect.top && position.y < rect.bottom) {
            return DragHandle.RIGHT
        }

        // Check if inside rectangle for moving
        if (position.x > rect.left && position.x < rect.right &&
            position.y > rect.top && position.y < rect.bottom) {
            return DragHandle.MOVE
        }

        return DragHandle.NONE
    }

    fun updateCropRect(dragOffset: Offset, handle: DragHandle, initialRect: Rect): Rect {
        val imageWidth = imageBitmap.width * imageScale
        val imageHeight = imageBitmap.height * imageScale
        val imageRight = imageLeft + imageWidth
        val imageBottom = imageTop + imageHeight

        return when (handle) {
            DragHandle.TOP_LEFT -> {
                val newLeft = (initialRect.left + dragOffset.x).coerceIn(imageLeft, initialRect.right - 20f)
                val newTop = (initialRect.top + dragOffset.y).coerceIn(imageTop, initialRect.bottom - 20f)
                Rect(newLeft, newTop, initialRect.right, initialRect.bottom)
            }
            DragHandle.TOP_RIGHT -> {
                val newRight = (initialRect.right + dragOffset.x).coerceIn(initialRect.left + 20f, imageRight)
                val newTop = (initialRect.top + dragOffset.y).coerceIn(imageTop, initialRect.bottom - 20f)
                Rect(initialRect.left, newTop, newRight, initialRect.bottom)
            }
            DragHandle.BOTTOM_LEFT -> {
                val newLeft = (initialRect.left + dragOffset.x).coerceIn(imageLeft, initialRect.right - 20f)
                val newBottom = (initialRect.bottom + dragOffset.y).coerceIn(initialRect.top + 20f, imageBottom)
                Rect(newLeft, initialRect.top, initialRect.right, newBottom)
            }
            DragHandle.BOTTOM_RIGHT -> {
                val newRight = (initialRect.right + dragOffset.x).coerceIn(initialRect.left + 20f, imageRight)
                val newBottom = (initialRect.bottom + dragOffset.y).coerceIn(initialRect.top + 20f, imageBottom)
                Rect(initialRect.left, initialRect.top, newRight, newBottom)
            }
            DragHandle.TOP -> {
                val newTop = (initialRect.top + dragOffset.y).coerceIn(imageTop, initialRect.bottom - 20f)
                Rect(initialRect.left, newTop, initialRect.right, initialRect.bottom)
            }
            DragHandle.BOTTOM -> {
                val newBottom = (initialRect.bottom + dragOffset.y).coerceIn(initialRect.top + 20f, imageBottom)
                Rect(initialRect.left, initialRect.top, initialRect.right, newBottom)
            }
            DragHandle.LEFT -> {
                val newLeft = (initialRect.left + dragOffset.x).coerceIn(imageLeft, initialRect.right - 20f)
                Rect(newLeft, initialRect.top, initialRect.right, initialRect.bottom)
            }
            DragHandle.RIGHT -> {
                val newRight = (initialRect.right + dragOffset.x).coerceIn(initialRect.left + 20f, imageRight)
                Rect(initialRect.left, initialRect.top, newRight, initialRect.bottom)
            }
            DragHandle.MOVE -> {
                val newLeft = (initialRect.left + dragOffset.x).coerceIn(imageLeft, imageRight - initialRect.width)
                val newTop = (initialRect.top + dragOffset.y).coerceIn(imageTop, imageBottom - initialRect.height)
                Rect(newLeft, newTop, newLeft + initialRect.width, newTop + initialRect.height)
            }
            else -> initialRect
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val currentRect = cropRect
                            if (currentRect != null) {
                                dragHandle = getHandleAtPosition(offset, currentRect)
                                if (dragHandle != DragHandle.NONE) {
                                    dragStartOffset = offset
                                    initialCropRect = currentRect
                                    isDragging = true
                                }
                            } else {
                                // Create new crop rect
                                cropRect = Rect(offset, offset)
                                dragHandle = DragHandle.BOTTOM_RIGHT
                                dragStartOffset = offset
                                initialCropRect = Rect(offset, offset)
                                isDragging = true
                            }
                        },
                        onDrag = { change, _ ->
                            if (isDragging && initialCropRect != null) {
                                val dragOffset = change.position - dragStartOffset
                                cropRect = updateCropRect(dragOffset, dragHandle, initialCropRect!!)
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            dragHandle = DragHandle.NONE
                            initialCropRect = null
                        }
                    )
                }
        ) {
            // Store canvas size
            canvasSize = size

            // Calculate scale and position to center image
            val scale = minOf(
                size.width / imageBitmap.width,
                size.height / imageBitmap.height
            )
            val imageWidth = imageBitmap.width * scale
            val imageHeight = imageBitmap.height * scale
            val left = (size.width - imageWidth) / 2f
            val top = (size.height - imageHeight) / 2f

            // Store scaling info for coordinate conversion
            imageScale = scale
            imageLeft = left
            imageTop = top

            // Draw the image, centered and scaled
            withTransform({
                translate(left, top)
                scale(scale, scale)
            }) {
                drawImage(imageBitmap)
            }

            // Draw crop rectangle and handles
            cropRect?.let { rect ->
                // Draw rectangle
                drawRect(
                    color = Color.Red,
                    topLeft = rect.topLeft,
                    size = androidx.compose.ui.geometry.Size(rect.width, rect.height),
                    style = Stroke(width = 2f)
                )

                // Draw corner handles
                val handleColor = Color.White
                val handleStroke = Color.Red

                listOf(
                    rect.topLeft,
                    Offset(rect.right, rect.top),
                    Offset(rect.left, rect.bottom),
                    rect.bottomRight
                ).forEach { corner ->
                    drawCircle(
                        color = handleColor,
                        radius = handleSize / 2,
                        center = corner
                    )
                    drawCircle(
                        color = handleStroke,
                        radius = handleSize / 2,
                        center = corner,
                        style = Stroke(width = 2f)
                    )
                }

                // Draw edge handles
                listOf(
                    Offset((rect.left + rect.right) / 2, rect.top), // Top
                    Offset((rect.left + rect.right) / 2, rect.bottom), // Bottom
                    Offset(rect.left, (rect.top + rect.bottom) / 2), // Left
                    Offset(rect.right, (rect.top + rect.bottom) / 2) // Right
                ).forEach { edge ->
                    drawRect(
                        color = handleColor,
                        topLeft = Offset(edge.x - handleSize/4, edge.y - handleSize/4),
                        size = androidx.compose.ui.geometry.Size(handleSize/2, handleSize/2)
                    )
                    drawRect(
                        color = handleStroke,
                        topLeft = Offset(edge.x - handleSize/4, edge.y - handleSize/4),
                        size = androidx.compose.ui.geometry.Size(handleSize/2, handleSize/2),
                        style = Stroke(width = 2f)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        ) {
            Button(
                onClick = {
                    cropRect?.let { rect ->
                        // Convert canvas coordinates back to original image coordinates
                        val cropLeftImage = (rect.left - imageLeft) / imageScale
                        val cropTopImage = (rect.top - imageTop) / imageScale
                        val cropRightImage = (rect.right - imageLeft) / imageScale
                        val cropBottomImage = (rect.bottom - imageTop) / imageScale

                        onCropConfirmed(Rect(cropLeftImage, cropTopImage, cropRightImage, cropBottomImage))
                    }
                },
                enabled = cropRect != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm Crop")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}