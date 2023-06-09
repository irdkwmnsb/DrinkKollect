package ru.alzhanov.drinkkollect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import drinkollect.v1.DrinkollectOuterClass
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import ru.alzhanov.drinkkollect.databinding.FragmentMainScrollBinding


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class MainScrollFragment : Fragment() {

    private var _binding: FragmentMainScrollBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val sharedViewModel: UsernameViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMainScrollBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val observer = object : Observer<MutableList<DrinkollectOuterClass.Post>> {
            override fun onSubscribe(d: Disposable) {
                binding.mainItemsList.visibility = View.GONE
                binding.mainScrollProgressBar.visibility = View.VISIBLE
            }

            override fun onNext(t: MutableList<DrinkollectOuterClass.Post>) {
                val customAdapter = DrinkCardListViewAdapter(requireActivity(), t)
                binding.mainItemsList.adapter = customAdapter
                binding.mainItemsList.layoutManager =
                    androidx.recyclerview.widget.LinearLayoutManager(requireContext())

            }

            override fun onError(e: Throwable) {
                Toast.makeText(
                    activity,
                    "Can't load posts. Check Internet connection",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onComplete() {
                binding.mainItemsList.visibility = View.VISIBLE
                binding.mainScrollProgressBar.visibility = View.GONE
            }
        }
        (activity as MainActivity).service.listPostsRequest(
            observer
        )
        val dividerItemDecoration = DividerItemDecoration(
            context,
            DividerItemDecoration.VERTICAL
        )
        context?.let {
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
                val curUsername = (activity as MainActivity).service.getUsername()
                when (menuItem.itemId) {
                    R.id.button_search -> {
                        findNavController().navigate(R.id.action_MainScrollFragment_to_SearchUsersFragment)
                    }

                    R.id.button_profile -> {
                        if (curUsername == null) {
                            UnauthDialog(getString(R.string.you_need_to_be_logged_in)).show(
                                requireActivity().supportFragmentManager,
                                "LoginDialog"
                            )
                        } else {
                            findNavController().navigate(R.id.action_MainScrollFragment_to_ProfileFragment)
                        }
                    }

                    else -> {
                        if (curUsername == null) {
                            findNavController().navigate(R.id.action_MainScrollFragment_to_LoginFragment)
                        }
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.addPostFab.setOnClickListener {
            if ((activity as MainActivity).service.getUsername() == null) {
                UnauthDialog(getString(R.string.you_need_to_be_logged_in_to_create_posts)).show(
                    requireActivity().supportFragmentManager,
                    "LoginDialog"
                )
            } else {
                findNavController().navigate(R.id.action_MainScrollFragment_to_NewPostFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}