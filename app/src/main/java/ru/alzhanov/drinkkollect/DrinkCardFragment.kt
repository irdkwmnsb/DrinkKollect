package ru.alzhanov.drinkkollect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.alzhanov.drinkkollect.databinding.FragmentDrinkCardBinding

class DrinkCardFragment : Fragment() {

    private lateinit var binding: FragmentDrinkCardBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDrinkCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.image.clipToOutline = true // https://issuetracker.google.com/issues/37036728
//        val drink = arguments?.getParcelable<Drink>("drink")
//        binding.drinkName.text = drink?.name
//        binding.drinkDescription.text = drink?.description
//        binding.drinkImage.setImageResource(drink?.image ?: R.drawable.ic_launcher_background)
    }
}