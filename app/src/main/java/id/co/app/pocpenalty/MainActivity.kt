package id.co.app.pocpenalty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import createSafeImageBitmapWithScaling
import id.co.app.pocpenalty.data.WoodPileDataConverter
import id.co.app.pocpenalty.ui.theme.POCPenaltyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            POCPenaltyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Camera(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Camera(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val woodPileData = WoodPileDataConverter.fromJsonFile(context, "woodpile145.json")
    val (imageBitmap, scalingInfo) = context.createSafeImageBitmapWithScaling(R.drawable.woodpile145)
//    GridTaggingScreen(
//        imageBitmap = imageBitmap,
//        woodPileData = woodPileData,
//        scalingInfo = scalingInfo,
////        onImageTap = { imageOffset ->
////            // Handle tap
////        },
//    )
    GridTaggingScreenSMDD()
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    POCPenaltyTheme {
        Camera()
    }
}