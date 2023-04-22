package ru.alzhanov.drinkkollect

import android.app.Activity
import android.icu.text.RelativeDateTimeFormatter
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.alzhanov.drinkkollect.databinding.DrinkCardLayoutBinding
import ru.alzhanov.drinkkollect.models.DrinkPost
import ru.alzhanov.drinkkollect.models.OtherDrinkPost
import ru.alzhanov.drinkkollect.models.OwnDrinkPost
import java.util.*
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
                drinkPost.likes,
                drinkPost.likes
            )
        } else if (drinkPost is OtherDrinkPost) {
            binding.label.text = binding.root.resources.getText(R.string.want_ru)
            binding.label.chipIcon = ResourcesCompat.getDrawable(
                binding.root.resources,
                if (drinkPost.like)
                    R.drawable.ic_baseline_star_24 else
                    R.drawable.ic_baseline_star_border_24,
                null
            )
            // I have no idea why this doesn't work
//            binding.label.chipIconTint = ResourcesCompat.getColorStateList(
//                binding.root.resources,
//                if (drinkPost.like)
//                    R.color.pcolor else
//                    R.color.colorSecondary,
//                null
//            )
        }
        binding.image.setImageResource(drinkPost.image)
    }

    companion object {
        private fun getRelativeTimeAgo(date: Instant): String {
            val now = Clock.System.now()
            val formatter = RelativeDateTimeFormatter.getInstance()
            return formatter.format(
                (now - date).toDouble(DurationUnit.SECONDS),
                RelativeDateTimeFormatter.Direction.LAST,
                RelativeDateTimeFormatter.RelativeUnit.SECONDS
            )
        }
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