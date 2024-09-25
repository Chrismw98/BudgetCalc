package chrismw.budgetcalc.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

@Module
@InstallIn(SingletonComponent::class)
internal object DeviceSettingsModule {

    @Provides
    internal fun provideZoneId(): ZoneId {
        return ZoneId.systemDefault()
    }

    @Provides
    @DateTimeNow
    internal fun provideNowDateTime(zone: ZoneId): OffsetDateTime {
        return OffsetDateTime.now(zone)
    }

    @Provides
    @DateNow
    internal fun provideNowDate(): LocalDate {
        return LocalDate.now()
    }
}