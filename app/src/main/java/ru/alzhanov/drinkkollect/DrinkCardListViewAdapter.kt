package ru.alzhanov.drinkkollect

import android.app.Activity
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.alzhanov.drinkkollect.databinding.DrinkCardLayoutBinding
import ru.alzhanov.drinkkollect.models.Drink

class DrinkCardViewHolder(inflate: DrinkCardLayoutBinding): RecyclerView.ViewHolder(inflate.root) {
    val binding = inflate
    fun bind(drink: Drink) {
        binding.image.clipToOutline = true // https://issuetracker.google.com/issues/37036728
        binding.title.text = drink.name
        binding.description.text = drink.description
        binding.image.setImageResource(drink.image)
    }
}
class DrinkCardListViewAdapter(private val context: Activity, private val valuesList: ArrayList<Drink>) :
    RecyclerView.Adapter<DrinkCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrinkCardViewHolder {
        return DrinkCardViewHolder(DrinkCardLayoutBinding.inflate(context.layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: DrinkCardViewHolder, position: Int) {
        holder.bind(valuesList[position])
    }

    override fun getItemCount(): Int {
        return valuesList.size
    }
}