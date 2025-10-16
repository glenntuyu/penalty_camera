package id.co.app.pocpenalty

import kotlinx.coroutines.CoroutineDispatcher


/**
 * Created by Angkoso Brami on 4/27/2021.
 * App Sinarmas
 * Angkoso.Brami@globalkomunikasimandiri.com
 */
class BaseDispatchers : Dispatchers {
	override fun io(): CoroutineDispatcher {
		return kotlinx.coroutines.Dispatchers.IO
	}

	override fun ui(): CoroutineDispatcher {
		return kotlinx.coroutines.Dispatchers.Main
	}
}