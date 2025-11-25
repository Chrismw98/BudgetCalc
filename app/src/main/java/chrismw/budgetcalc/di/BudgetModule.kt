package chrismw.budgetcalc.di

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import chrismw.budgetcalc.data.budget.BudgetDataPreferences
import chrismw.budgetcalc.data.budget.BudgetDataPreferencesSerializer
import chrismw.budgetcalc.data.budget.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

/**
 * Module for providing budget related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object BudgetModule {

    @Provides
    @Singleton
    fun providesBudgetDataStore(
        context: Application,
        @IODispatcher dispatcher: CoroutineContext,
        @ApplicationScope scope: CoroutineScope,
    ): DataStore<BudgetDataPreferences> {
        return DataStoreFactory.create(
            serializer = BudgetDataPreferencesSerializer,
            scope = CoroutineScope(scope.coroutineContext + dispatcher),
            migrations = listOf()
        ) {
            context.dataStoreFile(Constants.DATASTORE_FILE)
        }
    }
}