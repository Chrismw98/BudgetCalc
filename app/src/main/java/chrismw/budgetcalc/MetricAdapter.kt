import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import chrismw.budgetcalc.Metric
import chrismw.budgetcalc.MetricUnit
import chrismw.budgetcalc.R
import chrismw.budgetcalc.databinding.MetricItemBinding
import kotlin.math.abs


class MetricAdapter(private val metrics: ArrayList<Metric>) :
    RecyclerView.Adapter<MetricAdapter.ViewHolder>() {

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     * A binding variable is created in its constructor for the item layout
     */
    class ViewHolder(binding: MetricItemBinding) : RecyclerView.ViewHolder(binding.root) {
        // Holds the TextView that will add each item to
        val llMetricItem = binding.llMetricItem
        val tvMetricName = binding.tvMetricName
        val tvMetricValue = binding.tvMetricValue
        val tvMetricUnit = binding.tvMetricUnit
    }

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            MetricItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val metric = metrics[position]

        holder.tvMetricName.text = metric.name

        if (metric.unit == MetricUnit.DAYS) {
            holder.tvMetricValue.text = String.format("%.0f", metric.value)

            if (abs(metric.value) == 1.0) {
                holder.tvMetricUnit.text = holder.tvMetricUnit.context.getString(R.string.day)
            } else {
                holder.tvMetricUnit.text = holder.tvMetricUnit.context.getString(R.string.days)
            }

            if (metric.value < 0) {
                holder.tvMetricValue.setTextColor(ContextCompat.getColor(context, R.color.red))
                holder.tvMetricUnit.setTextColor(ContextCompat.getColor(context, R.color.red))
            } else {
                holder.tvMetricValue.setTextColor(ContextCompat.getColor(context, R.color.text_normal))
                holder.tvMetricUnit.setTextColor(ContextCompat.getColor(context, R.color.text_normal))
            }
        } else {
            holder.tvMetricValue.text = String.format("%.2f", metric.value)
            setMetricUnitStrings(holder, metric)
        }

        if (position % 2 == 0){
            holder.llMetricItem.setBackgroundColor(ContextCompat.getColor(context, R.color.color_background_faded))
        } else {
            holder.llMetricItem.setBackgroundColor(ContextCompat.getColor(context, R.color.color_background))
        }
    }

    private fun setMetricUnitStrings(holder: ViewHolder, metric: Metric) {
        val tvContext = holder.tvMetricUnit.context
        holder.tvMetricUnit.text = when (metric.unit) {
            MetricUnit.EURO -> tvContext.getString(R.string.EUR) //TODO: Update these to take the correct currency
            MetricUnit.EURO_PER_DAY -> tvContext.getString(R.string.EUR_per_day)
            MetricUnit.DAYS -> tvContext.getString(R.string.days)
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return metrics.size
    }

}