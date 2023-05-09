package ru.alzhanov.drinkkollect

import android.app.Activity
import android.icu.text.RelativeDateTimeFormatter
import android.util.TypedValue
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.alzhanov.drinkkollect.databinding.DrinkCardLayoutBinding
import ru.alzhanov.drinkkollect.models.DrinkPost
import ru.alzhanov.drinkkollect.models.OtherDrinkPost
import ru.alzhanov.drinkkollect.models.OwnDrinkPost
import kotlin.math.roundToInt
import kotlin.time.DurationUnit


class DrinkCardViewHolder(inflate: DrinkCardLayoutBinding) : RecyclerView.ViewHolder(inflate.root) {
    val binding = inflate
    fun bind(drinkPost: DrinkPost) {
        binding.image.clipToOutline = true // https://issuetracker.google.com/issues/37036728
        binding.title.text = drinkPost.name
        binding.description.text = drinkPost.description
        binding.location.text = drinkPost.location
        binding.username.text = drinkPost.author
        binding.timestamp.text = getRelativeTimeAgo(drinkPost.timestamp)
        if (drinkPost is OwnDrinkPost) {
            binding.label.text = binding.root.resources.getQuantityString(
                R.plurals.people_want,
                drinkPost.likes.toInt(),
                drinkPost.likes
            )
        } else if (drinkPost is OtherDrinkPost) {
            binding.label.text = binding.root.resources.getText(R.string.want)
            // set background color from attr
            if (drinkPost.like) {
                binding.label.closeIcon = ResourcesCompat.getDrawable(
                    binding.root.resources,
                    R.drawable.ic_baseline_star_24,
                    null
                )
                val typedValue = TypedValue()
                binding.root.context.theme.resolveAttribute(
                    R.attr.colorSecondaryContainer,
                    typedValue,
                    true
                )
                binding.label.chipBackgroundColor = ResourcesCompat.getColorStateList(
                    binding.root.resources,
                    typedValue.resourceId,
                    null
                )

            } else {
                binding.label.closeIcon = ResourcesCompat.getDrawable(
                    binding.root.resources,
                    R.drawable.ic_baseline_star_border_24,
                    null
                )
                binding.label.chipBackgroundColor = ResourcesCompat.getColorStateList(
                    binding.root.resources,
                    R.color.transparent,
                    null
                )
                val dim = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    1f,
                    binding.root.resources.displayMetrics
                )
                val typedValue = TypedValue()
                binding.root.context.theme.resolveAttribute(
                    R.attr.colorOutline,
                    typedValue,
                    true
                )
                binding.label.chipStrokeWidth = dim
                binding.label.chipStrokeColor = ResourcesCompat.getColorStateList(
                    binding.root.resources,
                    typedValue.resourceId,
                    null
                )
            }
        }
        //binding.image.setImageResource(drinkPost.image.hashCode())
    }

    private val periods = listOf(
        RelativeDateTimeFormatter.RelativeUnit.SECONDS to 1,
        RelativeDateTimeFormatter.RelativeUnit.MINUTES to 60,
        RelativeDateTimeFormatter.RelativeUnit.HOURS to 60 * 60,
        RelativeDateTimeFormatter.RelativeUnit.DAYS to 24 * 60 * 60,
        RelativeDateTimeFormatter.RelativeUnit.WEEKS to 7 * 24 * 60 * 60,
        RelativeDateTimeFormatter.RelativeUnit.MONTHS to 30 * 24 * 60 * 60,
        RelativeDateTimeFormatter.RelativeUnit.YEARS to 365 * 24 * 60 * 60,
    )

    private fun getRelativeTimeAgo(date: Instant): String {
        val now = Clock.System.now()
        val formatter = RelativeDateTimeFormatter.getInstance()
        val durationS = (now - date).toDouble(DurationUnit.SECONDS)
        for ((unit, secs) in periods.reversed()) {
            val amount = durationS / secs
            if (amount >= 1)
                return formatter.format(
                    amount.roundToInt().toDouble(),
                    RelativeDateTimeFormatter.Direction.LAST,
                    unit
                )
        }
        return binding.root.context.getString(R.string.just_now)
    }
}

class DrinkCardListViewAdapter(
    private val context: Activity,
    private val valuesList: ArrayList<DrinkPost>
) :
    RecyclerView.Adapter<DrinkCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrinkCardViewHolder {
        return DrinkCardViewHolder(
            DrinkCardLayoutBinding.inflate(
                context.layoutInflater,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DrinkCardViewHolder, position: Int) {
        holder.bind(valuesList[position])
    }

    override fun getItemCount(): Int {
        return valuesList.size
    }
}