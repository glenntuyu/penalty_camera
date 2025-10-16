package id.co.app.pocpenalty

import id.co.app.pocpenalty.R.string
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.co.app.pocpenalty.data.WoodPileData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PenaltyViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatcher: Dispatchers,
    private val networkStatusTracker: NetworkStatusTracker,
    private val processPenaltyImageUseCase: ProcessPenaltyImageUseCase
) : ViewModel() {

    private val _processState = MutableStateFlow<Result<WoodPileData>>(Result.Empty)
    val processState: StateFlow<Result<WoodPileData>> = _processState

    fun onProcessImage(selectedBitmap: Bitmap, imageBitmap: ImageBitmap, scalingInfo: ScalingInfo, jsonFile: String?) {
        viewModelScope.launch(dispatcher.io()) {
            _processState.value = Result.Loading
            networkStatusTracker.triggerEventClick()
                .catch { t -> _processState.value = Result.Failure(t.handleExceptionString()) }
                .collect { isConnected ->
                    if (!isConnected) {
                        _processState.value = context.getString(string.label_not_connected_detail)
                            .let { Result.FailureNetwork(it) }
                        return@collect
                    }

                    // 1) Convert ImageBitmap → temp file (JPEG/WEBP)
                    val tmp = saveImageBitmapToFile(context, selectedBitmap, format = ImgFormat.JPEG)

                    // 2) Execute use case
                    processPenaltyImageUseCase.setImageFile(tmp)
                    processPenaltyImageUseCase.execute()
                        .catch { t ->
                            Log.e("PenaltyViewModel", t.handleExceptionString())
                            _processState.value = Result.Failure(t.handleExceptionString())
                        }
                        .collect { woodPileData ->
                            // success
                            _processState.value =
                                Result.Success(woodPileData, imageBitmap, scalingInfo, jsonFile)
                        }
                }
        }
    }

    fun capture() {
        _processState.value = Result.Empty
    }
}
