package chrismw.budgetcalc.data.budget

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object BudgetDataPreferencesSerializer : Serializer<BudgetDataPreferences> {
    override val defaultValue: BudgetDataPreferences = BudgetDataPreferences.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): BudgetDataPreferences {
        try {
            return BudgetDataPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: BudgetDataPreferences, output: OutputStream) = t.writeTo(output)
}