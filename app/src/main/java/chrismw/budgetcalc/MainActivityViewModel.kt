package chrismw.budgetcalc

import androidx.lifecycle.ViewModel
import chrismw.budgetcalc.prefdatastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val settingsStateFlow: Flow<SettingsState> = dataStoreManager
        .getFromDataStore()
        .map { budgetData ->
            budgetData.toSettingsState()
        }
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5_000),
//            initialValue = SettingsState()
//        )


    data class MainActivityState(
        val startDate: LocalDate? = null,
        val targetDate: LocalDate? = null,
        val maxBudget: Float? = null,
        
        val lengthOfPaymentCycleInDays: Int? = null,
        val dailyBudget : Float? = null,
        val daysSinceStart  : Int? = null,
        val daysRemaining : Int? = null,
        val currentBudget : Float? = null,
        val remainingBudget : Float? = null,
//        val lengthOfPaymentCycleInDays = startDate.lengthOfMonth()
//        val dailyBudget = maxBudget / lengthOfPaymentCycleInDays
//
//        val daysSinceStart = ChronoUnit.DAYS.between(startDate, targetDate)
//        val daysRemaining = lengthOfPaymentCycleInDays - daysSinceStart
//
//        val currentBudget = if (daysSinceStart <= lengthOfPaymentCycleInDays) daysSinceStart * dailyBudget else maxBudget
//        val remainingBudget = if (daysSinceStart <= lengthOfPaymentCycleInDays) maxBudget - currentBudget else 0f
    )
}