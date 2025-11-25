package chrismw.budgetcalc.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

/**
 * Module for providing utilities and helpers.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object UtilsModule {

    @Provides
    @Singleton
    @DefaultDispatcher
    internal fun providesDefaultDispatcher(): CoroutineContext {
        return Dispatchers.Default
    }

    @Provides
    @Singleton
    @MainDispatcher
    internal fun providesMainDispatcher(): CoroutineContext {
        return Dispatchers.Main
    }

    @Provides
    @Singleton
    @IODispatcher
    internal fun providesIODispatcher(): CoroutineContext {
        return Dispatchers.IO
    }

    @Provides
    @Singleton
    @UnconfinedDispatcher
    internal fun providesUnconfinedDispatcher(): CoroutineContext {
        return Dispatchers.Unconfined
    }

    @Provides
    @Singleton
    @ApplicationScope
    internal fun providesApplicationScope(
        @DefaultDispatcher coroutineContext: CoroutineContext,
    ): CoroutineScope {
        return CoroutineScope(SupervisorJob() + coroutineContext)
    }
}