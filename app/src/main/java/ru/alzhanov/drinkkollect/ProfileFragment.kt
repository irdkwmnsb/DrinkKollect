package ru.alzhanov.drinkkollect

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import drinkollect.v1.DrinkollectOuterClass
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.datetime.Instant
import ru.alzhanov.drinkkollect.databinding.FragmentProfileBinding
import ru.alzhanov.drinkkollect.models.DrinkPost
import ru.alzhanov.drinkkollect.models.OwnDrinkPost

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val username = (activity as MainActivity).service.getUsername()
        if (username == null) {
            findNavController().navigate(R.id.action_ProfileFragment_to_LoginFragment)
            return
        }
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.profile_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_item_change_password -> {
                        ChangePasswordDialog().show(
                            requireActivity().supportFragmentManager,
                            "ChangePasswordDialog"
                        )
                        true
                    }

                    R.id.menu_item_change_account -> {
                        LogoutDialog().show(requireActivity().supportFragmentManager, "Logout")
                        true
                    }

                    R.id.menu_item_delete_account -> {
                        AccountDeletionDialog().show(
                            requireActivity().supportFragmentManager,
                            "AccountDeletionDialog"
                        )
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val observer = object : Observer<MutableList<DrinkollectOuterClass.Post>> {
            override fun onSubscribe(d: Disposable) {
                binding.mainItemsList.visibility = View.GONE
                binding.profileProgressBar.visibility = View.VISIBLE
            }

            override fun onNext(t: MutableList<DrinkollectOuterClass.Post>) {
                val drinkPosts: ArrayList<DrinkPost> = ArrayList()
                if (t.size != 0) {
                    for (post in t) {
                        drinkPosts.add(
                            OwnDrinkPost(
                                post.title,
                                post.description,
                                post.image,
                                post.location,
                                post.creator,
                                Instant.fromEpochSeconds(
                                    post.timestamp.seconds,
                                    post.timestamp.nanos
                                ),
                                post.likes,
                                post.id
                            )
                        )
                    }
                }
                val customAdapter = DrinkCardListViewAdapter(requireActivity(), drinkPosts)
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
                binding.profileProgressBar.visibility = View.GONE
            }
        }
        (activity as MainActivity).service.listUserPostsRequest(
            observer,
            username
        )


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}