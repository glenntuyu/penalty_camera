package id.co.app.pocpenalty

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {
	@Provides
	@ViewModelScoped
	fun provideRepository(
		@ApplicationContext context: Context,
		client: PenaltyClient,
	): PenaltyRepository = PenaltyRepositoryImpl(
		context = context,
		client = client,
	)

	@Provides
	@ViewModelScoped
	fun provideNetworkStatusTracker(
		@ApplicationContext context: Context
	) = NetworkStatusTracker(context)
}