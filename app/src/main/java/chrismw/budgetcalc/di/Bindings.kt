package chrismw.budgetcalc.di

import chrismw.budgetcalc.data.budget.BudgetDataRepository
import chrismw.budgetcalc.data.budget.BudgetDataRepositoryImpl
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
}