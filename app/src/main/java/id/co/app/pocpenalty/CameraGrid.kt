@file:OptIn(ExperimentalMaterial3Api::class)

package id.co.app.pocpenalty

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowCircleLeft
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.scale
import androidx.lifecycle.viewmodel.compose.viewModel
import createSafeImageBitmapWithScaling
import id.co.app.pocpenalty.data.AbnormalityOption
import id.co.app.pocpenalty.data.GridDot
import id.co.app.pocpenalty.data.GridRect
import id.co.app.pocpenalty.data.PenaltyEntry
import id.co.app.pocpenalty.data.PenaltyReport
import id.co.app.pocpenalty.data.PenaltyRule
import id.co.app.pocpenalty.data.PenaltySummary
import id.co.app.pocpenalty.data.TagType
import id.co.app.pocpenalty.data.WoodPileData
import id.co.app.pocpenalty.data.decodePenaltyRules
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.random.Random

/**
 * Created by Tuyu on 7/8/2025.
 * Sinarmas APP
 * christian_tuyu@app.co.id
 */
enum class ScreenState {
    CAPTURE,         // main capture
    GRID,            // main grid
    ZOOM,            // main zoom
    CROPPED_GRID,    // cropped grid list
    CROPPED_ZOOM     // zoomed-in cropped cell
}

enum class ScreenState2 {
    CAPTURE,         // main capture
    GRID,            // main grid
    ZOOM,    // zoomed-in cropped cell
    CROP,
    REPORT
}

val Teal = Color(0xFF13A9B2) // adjust if you need an exact brand color
val Pill = RoundedCornerShape(24.dp)
val Black = Color(0xFF2E2E2E)
val Grey = Color(0xFFE0E0E0)
val WarningYellow = Color(0xFFFFC107)
val BgColor1 = Color(0xFFF9E2AF)
val BgColor2 = Color(0xFFEBEBEB)

private val TextPrimary = Color(0xFF2E2E2E)
private val TextSecondary = Color(0xFF4B4B4B)

val CardCorner = RoundedCornerShape(24.dp)

@Composable
fun GridTaggingScreenSMDD(
    modifier: Modifier = Modifier,
    viewModel: PenaltyViewModel = viewModel()
) {
    val state by viewModel.processState.collectAsState()

    var woodPileData by remember { mutableStateOf<WoodPileData?>(null) }
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var scalingInfo by remember { mutableStateOf<ScalingInfo?>(null) }
    val gridDots = remember { mutableStateListOf<GridDot>() }
    var selectedGrid by remember { mutableStateOf<GridRect?>(null) }
    var selectedTapOffset by remember { mutableStateOf<Offset?>(null) }
    var selectedTagType by remember { mutableStateOf<TagType?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    var cropRect by remember { mutableStateOf<Rect?>(null) }

    var screenState by remember { mutableStateOf(ScreenState2.CAPTURE) }
    val penaltyReport by remember { mutableStateOf(PenaltyReport()) }
    val showWarnMissingPenalty = remember { mutableStateOf(false) }
    val showWarnDoubleInputPenalty = remember { mutableStateOf(false) }
    var jsonFile by remember { mutableStateOf<String?>(null) }
    var showNoDetectionDialog by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (screenState == ScreenState2.REPORT) penaltyReport.title else if (screenState != ScreenState2.CAPTURE) jsonFile
                            ?: "Penalty" else "Penalty",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Black,
                    )
                },
                navigationIcon = {
                    if (screenState == ScreenState2.GRID || screenState == ScreenState2.ZOOM || screenState == ScreenState2.REPORT) {
                        IconButton(onClick = {
                            screenState = when (screenState) {
                                ScreenState2.GRID -> ScreenState2.CAPTURE
                                ScreenState2.ZOOM -> ScreenState2.GRID
                                ScreenState2.REPORT -> ScreenState2.GRID
                                else -> screenState
                            }
                        }) {
                            Icon(
                                Icons.Default.ArrowCircleLeft, contentDescription = "Back",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarColors(
                    containerColor = BgColor1,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Black,
                    actionIconContentColor = Color.White,
                    scrolledContainerColor = BgColor1
                ),
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            BgColor1,
                            BgColor2
                        )
                    )
                )
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            when (screenState) {
                ScreenState2.CAPTURE ->
                    when (state) {
                        is Result.Done -> TODO()
                        Result.Empty -> CaptureImage()
                        is Result.Failure -> {
                            val msg = (state as Result.Failure).message
                            Text(
                                text = "Error: $msg",
                                color = Color.Red,
                            )
                        }

                        is Result.FailureNetwork -> {
                            val msg = (state as Result.FailureNetwork).message
                            Text(
                                text = "Network error: $msg",
                                color = Color.Red,
                            )
                        }

                        is Result.Failures<*> -> TODO()
                        Result.Loading -> FullScreenLoading()
                        is Result.Success<*> -> {

                            val result = state as Result.Success<WoodPileData>
                            woodPileData = result.value
                            imageBitmap = result.imageBitmap
                            scalingInfo = result.scalingInfo
                            jsonFile = result.jsonFile

                            // 👇 Check for detection
                            val hasDetection =
                                woodPileData?.detectionBox != null && woodPileData?.gridLines != null

                            viewModel.capture()
                            if (!hasDetection) {
                                showNoDetectionDialog = true
                            } else {
                                screenState = ScreenState2.GRID
                                gridDots.clear()
                            }
                        }
                    }

                ScreenState2.GRID -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GridImage(
                        modifier = modifier.weight(1f),
                        imageBitmap = imageBitmap!!,
                        woodPileData = woodPileData!!,
                        scalingInfo = scalingInfo!!,
                        gridDots = gridDots,
                        onClickGrid = {
                            selectedGrid = it
                            screenState = ScreenState2.ZOOM
                        },
                        cropRect = cropRect
                    )

                    Box(
                        modifier = Modifier
                            .background(Color.White)
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AbnormalityLegend(modifier = modifier.padding(bottom = 8.dp))

                            Row(
                                modifier = modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left: white pill – "Retake"
//                                OutlinedButton(
//                                    onClick = { screenState = ScreenState2.CROP },
//                                    shape = Pill,
//                                    modifier = Modifier
//                                        .weight(1f)
//                                        .height(48.dp),
//                                    colors = ButtonDefaults.outlinedButtonColors(
//                                        containerColor = Color.White,
//                                        contentColor = Teal
//                                    ),
//                                    border = BorderStroke(1.dp, SolidColor(Teal))
//                                ) {
//                                    Icon(
//                                        imageVector = Icons.Filled.Refresh,
//                                        contentDescription = "Recrop",
//                                        modifier = Modifier.size(24.dp)
//                                    )
//                                    Spacer(Modifier.width(8.dp))
//                                    Text("Retake", fontWeight = FontWeight.Medium)
//                                }

                                // Right: teal pill – "Lihat Laporan"
                                Button(
                                    onClick = {
                                        if (gridDots.isNotEmpty()) {
                                            screenState = ScreenState2.REPORT
                                        } else {
                                            showWarnMissingPenalty.value = true
                                        }
                                    },
                                    shape = Pill,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Teal,
                                        contentColor = Color.White
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Article,
                                        contentDescription = "Lihat Laporan",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Lihat Laporan", fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                    }
                }

                ScreenState2.ZOOM -> ZoomedGridScreen(
                    imageBitmap = imageBitmap!!,
                    grid = selectedGrid!!,
                    gridDots = gridDots,
                    scalingInfo = scalingInfo!!,
                    onBack = { screenState = ScreenState2.GRID },
                    onTap = { offset, size ->
                        selectedTapOffset = Offset(
                            x = offset.x / size.width,
                            y = offset.y / size.height
                        )
                        showSheet = true
                    }
                )

                ScreenState2.CROP -> CropImageScreen(
                    modifier = modifier
                        .padding(innerPadding),
                    imageBitmap = imageBitmap!!,
                    onCropConfirmed = { rect ->
                        cropRect = rect
                        gridDots.clear()
                        screenState = ScreenState2.GRID
                    },
                    onCancel = { screenState = ScreenState2.GRID }
                )

                ScreenState2.REPORT -> {
                    val context = LocalContext.current
                    val jsonString =
                        context.assets.open("penalty_calculation.json").bufferedReader()
                            .use { it.readText() }
                    val rules: List<PenaltyRule> = decodePenaltyRules(jsonString)
                    val rulesMap = rules.associateBy { it.id }

                    val tagList: List<TagType> = gridDots.toTagTypes { it.tagType }

                    val summary = computePenaltyByTagCount(rulesMap, tagList)

                    println("Total penalty = ${summary.total}")
                    summary.lines.forEach {
                        println("${it.rule.id} x${it.quantity.toInt()} = ${it.subtotal}")
                    }
//                    val kiri = PenaltySection(
//                        title = "Kiri",
//                        items = listOf(
//                            PenaltyItem(sentence(TagType.RANTING.name), 5),
//                            PenaltyItem(sentence(TagType.DAUN.name), 3),
//                            PenaltyItem(sentence(TagType.TANAH.name), 4),
//                            PenaltyItem(sentence(TagType.ABAIKAN.name), 3),
//                        ),
//                        totalLabel = "Total Penalty Kiri",
//                        totalValue = 1500
//                    )
//                    val belakang = PenaltySection(
//                        title = "Belakang",
//                        items = listOf(
//                            PenaltyItem(sentence(TagType.RANTING.name), null),
//                            PenaltyItem(sentence(TagType.DAUN.name), null),
//                            PenaltyItem(sentence(TagType.TANAH.name), null),
//                            PenaltyItem(sentence(TagType.ABAIKAN.name), null),
//                        ),
//                        totalLabel = "Total Penalty Belakang",
//                        totalValue = 0
//                    )
                    PenaltyReportScreen(
                        summary = summary,
                        onBack = { screenState = ScreenState2.GRID },
                        onSave = {
                            gridDots.clear()
                            screenState = ScreenState2.CAPTURE
                        })
                }
            }
            if (showSheet && selectedTapOffset != null) {
                val list = remember { mutableStateListOf<AbnormalityOption>() }
                TagType.entries.forEach { tag ->
                    list.add(
                        AbnormalityOption(
                            tag.name,
                            sentence(tag.name),
                            tag.color,
                            tag,
                            painterResource(id = tag.icon)
                        )
                    )
                }
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    sheetState = sheetState
                ) {
                    AbnormalitySelector(options = list, onSelect = { gridDot ->
                        val filter = gridDots.filter { gridDot -> gridDot.grid == selectedGrid }
                        var isDoubleInput = false
                        filter.forEach {
                            if (it.tagType == gridDot.tagType) {
                                isDoubleInput = true
                            }
                        }
                        if (!isDoubleInput) {
                            gridDots.add(
                                GridDot(
                                    selectedGrid!!,
                                    selectedTapOffset!!,
                                    gridDot.tagType
                                )
                            )
                        } else {
                            selectedTagType = gridDot.tagType
                            showWarnDoubleInputPenalty.value = true
                        }
                        showSheet = false
                    })
                }
            }
        }
        MissingPenaltyDialog(
            visible = showWarnMissingPenalty.value,
            onDismiss = {
                showWarnMissingPenalty.value = false
            },
        )
        DoubleInputPenaltyDialog(
            visible = showWarnDoubleInputPenalty.value,
            onDismiss = {
                showWarnDoubleInputPenalty.value = false
            },
            selectedTagType = selectedTagType
        )
        NoDetectionDialog(
            visible = showNoDetectionDialog,
            onDismiss = {
                showNoDetectionDialog = false
                screenState = ScreenState2.CAPTURE
            }
        )
    }
}

@Composable
fun NoDetectionDialog(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.warning),
                contentDescription = "warning",
                modifier = Modifier.size(48.dp),
                tint = Color.Unspecified
            )
        },
        title = {
            Text(
                text = "Tidak Ada Deteksi",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = "Tidak ditemukan area deteksi atau grid pada gambar. " +
                        "Silakan ambil foto yang lebih jelas atau ulangi proses pemindaian.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Teal,
                    contentColor = Color.White
                )
            ) {
                Text("OK")
            }
        }
    )
}

@Composable
fun MissingPenaltyDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.warning),
                contentDescription = "warning",
                modifier = Modifier.size(48.dp),
                tint = Color.Unspecified
            )
        },
        title = {
            Text(
                "Belum ada penalty",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                "Silakan input penalty terlebih dahulu sebelum melanjutkan.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Teal,
                    contentColor = Color.White
                )
            ) {
                Text("Isi Penalty")
            }
        },
    )
}

@Composable
fun DoubleInputPenaltyDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    selectedTagType: TagType?,
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.warning),
                contentDescription = "warning",
                modifier = Modifier.size(48.dp),
                tint = Color.Unspecified
            )
        },
        title = {
            Text(
                "Penalty ${selectedTagType?.name} sudah ada",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                "Silakan input penalty lain.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Teal,
                    contentColor = Color.White
                )
            ) {
                Text("OK")
            }
        },
    )
}

fun <T> Iterable<T>.toTagTypes(extractor: (T) -> TagType?): List<TagType> =
    mapNotNull(extractor)

/**
 * Count TagType occurrences and convert to PenaltyEntry list.
 * Unknown ruleIds (not present in rulesById) are ignored.
 */
fun tagsToEntriesByCount(
    tags: List<TagType>,
    rulesById: Map<String, PenaltyRule>
): List<PenaltyEntry> {
    val counts: Map<String, Int> = tags
        .groupingBy { it.id }
        .eachCount()

    return counts.mapNotNull { (ruleId, c) ->
        if (rulesById.containsKey(ruleId)) PenaltyEntry(ruleId = ruleId, quantity = c.toDouble())
        else null // ignore tags without a known rule
    }
}

/**
 * Compute penalties where each tag occurrence counts as 1 unit.
 * Percent rules (UoM = Persen) still use baseForPercent like before.
 */
fun computePenaltyByTagCount(
    rulesById: Map<String, PenaltyRule>,
    tags: List<TagType>,
    baseForPercent: Double = 0.0
): PenaltySummary {
    val entries = tagsToEntriesByCount(tags, rulesById)
    return computePenalties(rulesById, entries, baseForPercent)
}

@Composable
fun PenaltyReportScreen(
    summary: PenaltySummary,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 98.dp) // leave room for bottom bar
        ) {
            // Card
            Surface(
                shape = CardCorner,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                Column {
                    // Header (coral)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Teal)
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                                contentDescription = "Penalty Summary",
                                modifier = Modifier.size(54.dp),
                                tint = Color.White
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "TOTAL PENALTY",
                                color = Color.White,
                                fontSize = 14.sp,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "${(summary.total.toInt() * 10).toString()} KG",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }

                    // Content (white)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
//                        summary.lines.forEachIndexed { index, section ->
                        SectionBlock(summary)
//                            if (index != summary.lines.lastIndex) {
//                                Spacer(Modifier.height(8.dp))
//                                DashedDivider(color = Grey)
//                                Spacer(Modifier.height(8.dp))
//                            }
//                        }
//                        Spacer(Modifier.height(16.dp)) // extra space at end for comfort
                    }
                }
            }
        }

        // Bottom action bar
        BottomActionBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            onBack = onBack,
            onSave = onSave
        )
    }

}

@Composable
private fun SectionBlock(summary: PenaltySummary) {
    Text(
        text = "Penalty",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = Black,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    summary.lines.forEach {
        KeyValueRow(
            label = it.rule.name,
//            valueText = "${it.subtotal.toInt()} ${it.rule.uom} (${it.quantity.toInt()} titik X ${it.rule.unitValue.toInt()} ${it.rule.uom})"
            valueText = "${it.subtotal.toInt()} KG(${it.quantity.toInt()} titik X ${it.rule.unitValue.toInt()} KG)"
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun KeyValueRow(label: String, valueText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF4B4B4B)
        )
        Text(
            text = valueText,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF8A8A8A)
        )
    }
}

/* ---------- Dashed divider ---------- */

@Composable
private fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = Color.LightGray
) {
    val strokeWidth = 1.5.dp
    val intervals = floatArrayOf(12f, 10f)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(strokeWidth)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(intervals, 0f)
        )
    }
}

/* ---------- Bottom bar ---------- */

@Composable
private fun BottomActionBar(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left pill: Kembali (white)
        OutlinedButton(
            onClick = onBack,
            shape = Pill,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Teal
            ),
            border = BorderStroke(1.dp, SolidColor(Teal))
        ) {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = "Kembali",
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Kembali", fontWeight = FontWeight.Medium)
        }

        // Right pill: Simpan Penalty (teal)
        Button(
            onClick = onSave,
            shape = Pill,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Teal,
                contentColor = Color.White
            )
        ) {
            Icon(
                Icons.Filled.Save,
                contentDescription = "Simpan Penalty",
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Simpan", style = MaterialTheme.typography.labelLarge)
        }
    }

}

data class LinkedItem(
    val jsonPath: String = "",
    val drawableRes: Int = 0
)

@Composable
fun CaptureImage(
    viewModel: PenaltyViewModel = viewModel(),
) {
    val context = LocalContext.current

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Launcher for picking content from gallery
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            selectedBitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }
        }
    }


    // Camera capture
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                decodeBitmapFromUri(context, uri)?.let { bmp ->
                    val (imgBitmap, scalingInfo) = context.createSafeImageBitmapWithScaling(bmp)
                    viewModel.onProcessImage(
                        selectedBitmap = bmp,
                        imageBitmap = imgBitmap,
                        scalingInfo = scalingInfo,
                        jsonFile = null
                    )
                }
            }
        }
    }

    // ==== CAMERA PERMISSION ====
    var showCameraDeniedDialog by remember { mutableStateOf(false) }
    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Permission now granted – proceed to create the Uri and launch camera
            val uri = createImageUri(context)
            if (uri != null) {
                currentPhotoUri = uri
                takePictureLauncher.launch(uri)
            }
        } else {
            showCameraDeniedDialog = true
        }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        refreshGallery(context)
        IconButton(
            onClick = {
//                val item = listOf(
//                    LinkedItem("woodpile145.json", R.drawable.woodpile145),
//                    LinkedItem("woodpile146.json", R.drawable.woodpile146),
//                    LinkedItem("woodpile148.json", R.drawable.woodpile148),
//                    LinkedItem("woodpile149.json", R.drawable.woodpile149),
//                    LinkedItem("woodpile151.json", R.drawable.woodpile151),
//                    LinkedItem("woodpile152.json", R.drawable.woodpile152),
//                    LinkedItem("woodpile154.json", R.drawable.woodpile154),
//                    LinkedItem("woodpile155.json", R.drawable.woodpile155),
//                    LinkedItem("woodpile157.json", R.drawable.woodpile157),
//                    LinkedItem("woodpile158.json", R.drawable.woodpile158),
//                    LinkedItem("woodpile160.json", R.drawable.woodpile160),
//                    LinkedItem("woodpile179.json", R.drawable.woodpile179),
//                    LinkedItem("woodpile182.json", R.drawable.woodpile182),
//                    LinkedItem("woodpile183.json", R.drawable.woodpile183),
//                    LinkedItem("woodpile184.json", R.drawable.woodpile184),
//                    LinkedItem("woodpile185.json", R.drawable.woodpile185),
//                    LinkedItem("woodpile186.json", R.drawable.woodpile186),
//                    LinkedItem("woodpile188.json", R.drawable.woodpile188),
//                    LinkedItem("woodpile189.json", R.drawable.woodpile189),
//                )
//
//                val random = item[Random.nextInt(0, item.size)]
//                    val woodPileData = WoodPileDataConverter.fromJsonFile(context, random.jsonPath)
//                val (imageBitmap, scalingInfo) = context.createSafeImageBitmapWithScaling(random.drawableRes)
//                val options = BitmapFactory.Options().apply {
//                    inScaled = false
//                }
//                val bitmap = BitmapFactory.decodeResource(context.resources, random.drawableRes, options)
//                viewModel.onProcessImage(
//                    selectedBitmap = bitmap,
//                    imageBitmap = imageBitmap,
//                    scalingInfo = scalingInfo,
//                    jsonFile = random.jsonPath
//                )
                val hasPermission = androidx.core.content.ContextCompat
                    .checkSelfPermission(context, android.Manifest.permission.CAMERA) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    val uri = createImageUri(context) ?: return@IconButton
                    currentPhotoUri = uri
                    takePictureLauncher.launch(uri)
                } else {
                    requestCameraPermission.launch(android.Manifest.permission.CAMERA)
                }
            },
            modifier = Modifier.size(112.dp)
        ) {
            Icon(
                Icons.Default.Camera,
                contentDescription = "Camera",
                modifier = Modifier.size(56.dp)
            )
        }
        Text(
            "Capture Image",
            style = MaterialTheme.typography.titleLarge
        )
        selectedBitmap?.let { bitmap ->
            val (imageBitmap, scalingInfo) = context.createSafeImageBitmapWithScaling(bitmap)
            viewModel.onProcessImage(
                selectedBitmap = bitmap,
                imageBitmap = imageBitmap,
                scalingInfo = scalingInfo,
                jsonFile = null
            )
        }
        IconButton(
            onClick = {
                launcher.launch("image/*")
            },
            modifier = Modifier.size(112.dp)
        ) {
            Icon(
                Icons.Default.ImageSearch,
                contentDescription = "ImageSearch",
                modifier = Modifier.size(56.dp)
            )
        }
        Text(
            "Pick Image",
            style = MaterialTheme.typography.titleLarge
        )
    }
// Simple “permission denied” dialog
    if (showCameraDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showCameraDeniedDialog = false },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.warning),
                    contentDescription = "warning",
                    modifier = Modifier.size(48.dp),
                    tint = Color.Unspecified
                )
            },
            title = { Text("Izin Kamera dibutuhkan") },
            text = { Text("Aplikasi memerlukan akses kamera untuk mengambil foto.") },
            confirmButton = {
                Button(onClick = {
                    showCameraDeniedDialog = false
                    openAppSettings(context)
                }) { Text("Buka Pengaturan") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCameraDeniedDialog = false }) { Text("Batal") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AbnormalityLegend(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        InfoBanner(
            text = "Ketuk grid pada foto untuk menandai abnormalitas",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TagType.entries.forEach {
                LegendChip(
                    text = sentence(it.name),
                    baseColor = it.color,
                    indicator = { RingIndicator(color = it.color) }
                )
            }
        }
    }
}

@Composable
private fun InfoBanner(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, SolidColor(Color(0xFFE5E5E5)))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = Color(0xFF7E7E7E),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun LegendChip(
    text: String,
    baseColor: Color,
    modifier: Modifier = Modifier,
    indicator: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(baseColor.copy(alpha = 0.2f))
    ) {

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            indicator()
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                color = TextPrimary,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
private fun RingIndicator(
    color: Color,
    indicatorSize: Dp = 18.dp
) {
    Canvas(modifier = Modifier.size(indicatorSize)) {
        // this.size is the DrawScope's Size (in px)
        val r = this.size.minDimension / 2f

        drawCircle(
            color = color,
            radius = r - 1.dp.toPx(),
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = color,
            radius = r * 0.55f
        )
    }
}

@Composable
private fun SquareIndicator(
    color: Color,
    size: Dp = 16.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(3.dp))
            .background(color)
    )
}

@Composable
fun AbnormalitySelector(
    options: List<AbnormalityOption>,
    onSelect: (AbnormalityOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Pilih Abnormalitas",
            color = Black,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEach { opt ->
                AbnormalityCard(
                    option = opt,
                    onClick = { onSelect(opt) }
                )
            }
        }
    }
}

@Composable
private fun AbnormalityCard(
    option: AbnormalityOption,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = Color.White,
        border = BorderStroke(1.dp, SolidColor(Grey)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left colored circle with white icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(option.circleColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    option.icon,
                    contentDescription = option.title,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Text(
                text = option.title,
                color = Black,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
private fun openAppSettings(context: android.content.Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

private fun createImageUri(context: android.content.Context): Uri? {
    val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        .format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_$name.jpg")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Penalty")
            put(MediaStore.Images.Media.IS_PENDING, 0)
        }
    }
    return context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    )
}

private fun decodeBitmapFromUri(
    context: android.content.Context,
    uri: Uri
): Bitmap? {
    return if (Build.VERSION.SDK_INT < 28) {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    } else {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}

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
    var selectedGrid by remember { mutableStateOf<GridRect?>(null) }
    var selectedTapOffset by remember { mutableStateOf<Offset?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }

    var showCropScreen by remember { mutableStateOf(false) }
    var cropRect by remember { mutableStateOf<Rect?>(null) }

    var showCroppedGrid by remember { mutableStateOf(false) }

    var screenState by remember { mutableStateOf(ScreenState.GRID) }

    // Zoom screen logic
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Penalty") },
                navigationIcon = {
                    if (screenState == ScreenState.ZOOM || screenState == ScreenState.CROPPED_ZOOM) {
                        IconButton(onClick = {
                            screenState = when (screenState) {
                                ScreenState.ZOOM -> ScreenState.GRID
                                ScreenState.CROPPED_ZOOM -> ScreenState.CROPPED_GRID
                                else -> screenState
                            }
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (!showCropScreen && !showCroppedGrid) {
            Box(
                modifier = modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (screenState == ScreenState.ZOOM && selectedGrid != null) {
                    ZoomedGridScreen(
                        imageBitmap = imageBitmap,
                        grid = selectedGrid!!,
                        gridDots = gridDots,
                        scalingInfo = scalingInfo,
                        onBack = { screenState = ScreenState.GRID },
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
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    "Pilih Abnormalitas",
                                    style = MaterialTheme.typography.titleMedium
                                )
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
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
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
                                screenState = ScreenState.ZOOM
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
        } else if (showCropScreen) {
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
        } else if (showCroppedGrid && cropRect != null) {
            CroppedGridScreen(
                imageBitmap = imageBitmap,
                cropRect = cropRect!!,
                woodPileData = woodPileData,
                scalingInfo = scalingInfo,
                screenState = screenState,
                onScreenStateChange = { screenState = it },
                onBack = {
                    showCroppedGrid = false
                    cropRect = null
                    screenState = ScreenState.GRID
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
    cropRect: Rect? = null
) {
    var drawParams by remember { mutableStateOf<ImageDrawParams?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(imageBitmap, woodPileData, scalingInfo, cropRect) {
                detectTapGestures { tap ->
                    drawParams?.let { p ->
                        // canvas → image coordinates
                        val x =
                            ((tap.x - p.left) / p.scale).coerceIn(0f, imageBitmap.width.toFloat())
                        val y =
                            ((tap.y - p.top) / p.scale).coerceIn(0f, imageBitmap.height.toFloat())

                        val grid = findGridFromData(Offset(x, y), woodPileData, scalingInfo)
                        onClickGrid(grid)
                    }
                }
            }
    ) {
        // fit image into canvas (center + uniform scale)
        val scale = minOf(
            size.width / imageBitmap.width.toFloat(),
            size.height / imageBitmap.height.toFloat()
        )
        val imgW = imageBitmap.width * scale
        val imgH = imageBitmap.height * scale
        val left = (size.width - imgW) / 2f
        val top = (size.height - imgH) / 2f
        drawParams = ImageDrawParams(left, top, scale)

        withTransform({
            translate(left, top)
            scale(scale, scale)
        }) {
            drawImage(imageBitmap)

            val stroke = 3f / scale

            // detection box (in image space, already scaled by scalingInfo)
            val topLeft = Offset(
                woodPileData.detectionBox.topLeft[0].toFloat() * scalingInfo.scaleX,
                woodPileData.detectionBox.topLeft[1].toFloat() * scalingInfo.scaleY
            )
            val bottomRight = Offset(
                woodPileData.detectionBox.bottomRight[0].toFloat() * scalingInfo.scaleX,
                woodPileData.detectionBox.bottomRight[1].toFloat() * scalingInfo.scaleY
            )
            val rectW = bottomRight.x - topLeft.x
            val rectH = bottomRight.y - topLeft.y

            // draw provided grid lines (yellow)
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
                    strokeWidth = stroke
                )
            }
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
                    strokeWidth = stroke
                )
            }

            // detection outline (red)
            drawRect(
                color = Color.Red,
                topLeft = topLeft,
                size = Size(rectW, rectH),
                style = Stroke(width = stroke * 2)
            )


            // dots (common)
            gridDots.forEach { dot ->
                val r = dot.grid.rect
                val pos = Offset(
                    r.left + dot.percentOffset.x * r.width,
                    r.top + dot.percentOffset.y * r.height
                )
                drawCircle(color = dot.tagType.color, radius = 10f / scale, center = pos)
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
    woodPileData: WoodPileData,
    scalingInfo: ScalingInfo
): GridRect? {
    // Convert tap offset to original image coordinates
    val originalX = tapOffset.x / scalingInfo.scaleX
    val originalY = tapOffset.y / scalingInfo.scaleY

    // Find which grid cell the tap is in
    val verticalLines = woodPileData.gridLines.vertical.map { it.p1[0] }.sorted()
    val horizontalLines = woodPileData.gridLines.horizontal.map { it.p1[1] }.sorted()

    // Add detection box boundaries
    val allVertical =
        (listOf(woodPileData.detectionBox.topLeft[0]) + verticalLines + listOf(woodPileData.detectionBox.bottomRight[0])).sorted()
    val allHorizontal =
        (listOf(woodPileData.detectionBox.topLeft[1]) + horizontalLines + listOf(woodPileData.detectionBox.bottomRight[1])).sorted()

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
    imageBitmap: ImageBitmap,
    grid: GridRect,
    gridDots: List<GridDot>,
    scalingInfo: ScalingInfo,
    onBack: () -> Unit,
    onTap: (Offset, IntSize) -> Unit
) {
    // Safe integer crop rect
    val left = floor(grid.rect.left).toInt().coerceIn(0, imageBitmap.width - 1)
    val top = floor(grid.rect.top).toInt().coerceIn(0, imageBitmap.height - 1)
    val right = ceil(grid.rect.right).toInt().coerceIn(left + 1, imageBitmap.width)
    val bottom = ceil(grid.rect.bottom).toInt().coerceIn(top + 1, imageBitmap.height)

    val width = (right - left).coerceAtLeast(1)
    val height = (bottom - top).coerceAtLeast(1)

    val rawBitmap = Bitmap.createBitmap(
        imageBitmap.asAndroidBitmap(),
        left, top, width, height
    )

    val canvasSize = with(LocalDensity.current) {
        val w = LocalConfiguration.current.screenWidthDp.dp.toPx().toInt()
        val h = (w * (height.toFloat() / width.toFloat())).toInt().coerceAtLeast(1)
        IntSize(w, h)
    }

    val scaledBitmap = rawBitmap.scale(canvasSize.width, canvasSize.height).asImageBitmap()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(width.toFloat() / height.toFloat())
            .pointerInput(Unit) { detectTapGestures { offset -> onTap(offset, canvasSize) } }
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
    screenState: ScreenState,
    onScreenStateChange: (ScreenState) -> Unit,
    onBack: () -> Unit
) {
    var selectedGrid by remember { mutableStateOf<GridRect?>(null) }
    var selectedTapOffset by remember { mutableStateOf<Offset?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    val gridDots = remember { mutableStateListOf<GridDot>() }

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (screenState == ScreenState.CROPPED_ZOOM && selectedGrid != null) {
            ZoomedGridScreen(
                imageBitmap = imageBitmap,
                grid = selectedGrid!!,
                gridDots = gridDots,
                scalingInfo = scalingInfo,
                onBack = {
                    onScreenStateChange(ScreenState.CROPPED_GRID)
                },
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
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
                    onScreenStateChange(ScreenState.CROPPED_ZOOM)
                }
            )
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
                style = Stroke(width = strokeWidth * 2)
            )

            // Step 2: Create grid lines inside the crop rectangle
            val verticalCount = woodPileData.gridLines.vertical.size
            val horizontalCount = woodPileData.gridLines.horizontal.size

            // Calculate grid cell dimensions within the crop area
            val cellWidth = cropRect.width / (verticalCount)
            val cellHeight = cropRect.height / (horizontalCount)

            // Draw vertical grid lines within the crop area
            for (i in 1 until verticalCount) {
                val x = cropRect.left + (i * cellWidth)
                drawLine(
                    color = Color.Yellow,
                    start = Offset(x, cropRect.top),
                    end = Offset(x, cropRect.bottom),
                    strokeWidth = strokeWidth
                )
            }

            // Draw horizontal grid lines within the crop area
            for (i in 1 until horizontalCount) {
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
    // Quick reject
    if (!cropRect.contains(tapOffset)) return null

    val verticalCount = woodPileData.gridLines.vertical.size    // cols
    val horizontalCount = woodPileData.gridLines.horizontal.size // rows

    // Must be at least 1×1 to form cells
    if (verticalCount <= 0 || horizontalCount <= 0) return null

    // MATCH the drawing logic
    val cellWidth = cropRect.width / verticalCount
    val cellHeight = cropRect.height / horizontalCount

    val relativeX = tapOffset.x - cropRect.left
    val relativeY = tapOffset.y - cropRect.top

    var col = (relativeX / cellWidth).toInt()
    var row = (relativeY / cellHeight).toInt()

    // Clamp to last cell when the tap hits the right/bottom edge due to float rounding
    if (col == verticalCount) col = verticalCount - 1
    if (row == horizontalCount) row = horizontalCount - 1

    if (col !in 0 until verticalCount || row !in 0 until horizontalCount) return null

    val left = cropRect.left + col * cellWidth
    val top = cropRect.top + row * cellHeight
    val right = left + cellWidth
    val bottom = top + cellHeight

    return GridRect(
        row = row,
        col = col,
        rect = Rect(Offset(left, top), Offset(right, bottom))
    )
}

private fun findGridFromBoxLayout(
    tapOffset: Offset,
    woodPileData: WoodPileData,
    scalingInfo: ScalingInfo
): GridRect? {
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

    val verticalCount = woodPileData.gridLines.vertical.size
    val horizontalCount = woodPileData.gridLines.horizontal.size

    val cellWidth = rectWidth / verticalCount
    val cellHeight = rectHeight / horizontalCount

    if (tapOffset.x !in topLeft.x..bottomRight.x || tapOffset.y !in topLeft.y..bottomRight.y) {
        return null
    }

    val col = ((tapOffset.x - topLeft.x) / cellWidth).toInt()
    val row = ((tapOffset.y - topLeft.y) / cellHeight).toInt()

    val cellTopLeft = Offset(
        x = topLeft.x + col * cellWidth,
        y = topLeft.y + row * cellHeight
    )
    val cellBottomRight = Offset(
        x = cellTopLeft.x + cellWidth,
        y = cellTopLeft.y + cellHeight
    )

    return GridRect(
        row = row,
        col = col,
        rect = Rect(cellTopLeft, cellBottomRight)
    )
}

@Composable
fun FullScreenLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 4.dp,
            modifier = Modifier.size(64.dp)
        )
    }
}
