package id.co.app.poccamera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import createSafeImageBitmapOptimized
import createSafeImageBitmapWithScaling
import id.co.app.poccamera.data.WoodPileDataConverter
import id.co.app.poccamera.ui.theme.POCCameraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            POCCameraTheme {
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

    GridTaggingScreen(
        imageBitmap = imageBitmap,
        woodPileData = woodPileData,
        scalingInfo = scalingInfo,
//        onImageTap = { imageOffset ->
//            // Handle tap
//        },
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    POCCameraTheme {
        Camera()
    }
}