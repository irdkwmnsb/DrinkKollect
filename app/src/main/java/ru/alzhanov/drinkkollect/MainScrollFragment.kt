package ru.alzhanov.drinkkollect

import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import ru.alzhanov.drinkkollect.databinding.FragmentMainScrollBinding
import ru.alzhanov.drinkkollect.models.Drink


val drinks = arrayListOf<Drink>(
    Drink("Monster - VR46", "На вкус как дрянь", R.drawable.rich),
    Drink("Монстр от Льюиса Хамильтона", "Я ОБОЖАЮ ФОРМУЛУ ОДИН\n" +
            "В МОНАКО РАЗДАВАЛИ ВОТ ТАКИЕ", R.drawable.rich),
    Drink("Розовый монстр", "Вкус как попа", R.drawable.rich),
    Drink("Adrenaline rush", "Ну во первых, это не монстр, а адреналин. Во вторых, это вкус как попа", R.drawable.rich),
)

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class MainScrollFragment : Fragment() {

    private var _binding: FragmentMainScrollBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMainScrollBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val customAdapter = DrinkCardListViewAdapter(requireActivity(), drinks)
        binding.mainItemsList.adapter = customAdapter
        binding.mainItemsList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        val dividerItemDecoration = DividerItemDecoration(
            context,
            DividerItemDecoration.VERTICAL
        )
        getContext()?.let {
            ContextCompat.getDrawable(it, R.drawable.drink_list_divider)
                ?.let { dividerItemDecoration.setDrawable(it) }
        }
        binding.mainItemsList.addItemDecoration(
            dividerItemDecoration
        )

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_scroll_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                //TODO if user is unknown, then ProfileFragment else LoginFragment
                findNavController().navigate(R.id.action_MainScrollFragment_to_ProfileFragment)
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.addPostFab.setOnClickListener {
            val dialog = AddPostFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}