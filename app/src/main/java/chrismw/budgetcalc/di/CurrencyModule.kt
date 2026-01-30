package chrismw.budgetcalc.di

import android.content.Context
import chrismw.budgetcalc.data.currency.CurrencyJsonConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


/**
 * Module for providing currency related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object CurrencyModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideCurrencyJsonConfig(
        @ApplicationContext context: Context
    ): CurrencyJsonConfig {
        return CurrencyJsonConfig(
            assetsFileName = "xe_currencies.json",
        )
    }
}