package chrismw.budgetcalc.di

import chrismw.budgetcalc.data.budget.BudgetDataRepository
import chrismw.budgetcalc.data.budget.BudgetDataRepositoryImpl
import chrismw.budgetcalc.data.currency.CurrencyRepository
import chrismw.budgetcalc.data.currency.CurrencyRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module for binding implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class Bindings {

    @Binds
    @Singleton
    internal abstract fun bindBudgetDataRepository(
        budgetDataRepository: BudgetDataRepositoryImpl
    ): BudgetDataRepository

    @Binds
    @Singleton
    internal abstract fun bindCurrencyRepository(
        currencyRepositoryImpl: CurrencyRepositoryImpl
    ): CurrencyRepository
}