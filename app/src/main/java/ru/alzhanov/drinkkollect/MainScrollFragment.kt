package ru.alzhanov.drinkkollect

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.alzhanov.drinkkollect.databinding.FragmentMainScrollBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class MainScrollFragment : Fragment() {

    private var _binding: FragmentMainScrollBinding? = null
    private var listItems = arrayListOf("Monster Energy", "Adrenalin", "Red bull")

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMainScrollBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val customAdapter = CustomListViewAdapter(requireActivity(), listItems)
        binding.mainItemsList.adapter = customAdapter

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
            val dialog = DialogAddPost(requireActivity())
            dialog.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}