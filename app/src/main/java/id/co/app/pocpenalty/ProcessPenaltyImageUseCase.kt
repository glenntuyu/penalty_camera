package id.co.app.pocpenalty

import id.co.app.pocpenalty.data.WoodPileData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject

class ProcessPenaltyImageUseCase @Inject constructor(
    private val repository: PenaltyRepository,
    private val dispatchers: Dispatchers
) : UseCase<WoodPileData>() {

    private val _imageFile = MutableStateFlow<File?>(null)
    private val imageFile: StateFlow<File?> get() = _imageFile

    fun setImageFile(file: File) {
        _imageFile.value = file
    }

    override fun execute(): Flow<WoodPileData> =
        repository.processImage(imageFile.value ?: error("Image file is null"))
            .retry(2) { e -> (e is Exception).also { if (it) delay(500) } }
            .flowOn(dispatchers.io())
}

abstract class UseCase<T> {
    open fun execute(): Flow<T> {
        TODO("Your default implementation here")
    }
    open fun execute(variable: String): Flow<T> {
        TODO("Your default implementation here")
    }
}

interface Dispatchers {
    fun io(): CoroutineDispatcher
    fun ui(): CoroutineDispatcher
}