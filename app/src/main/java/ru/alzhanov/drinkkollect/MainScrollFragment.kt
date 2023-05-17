package ru.alzhanov.drinkkollect

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import drinkollect.v1.DrinkollectOuterClass
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.datetime.Instant
import ru.alzhanov.drinkkollect.databinding.FragmentMainScrollBinding
import ru.alzhanov.drinkkollect.models.DrinkPost
import ru.alzhanov.drinkkollect.models.OtherDrinkPost
import ru.alzhanov.drinkkollect.models.OwnDrinkPost


//val drinkPosts = arrayListOf<DrinkPost>(
//    OwnDrinkPost("Monster - VR46",
//        "На вкус как дрянь. Их новая линейка оказалось не такой вкусной. Если увидите - не берите",
//        R.drawable.rich,
//        "Монако",
//        "irdkwmnsb",
//        Clock.System.now(),
//        5),
//    OtherDrinkPost("Жигуль", "Если б было море пива. Я б дельфином стал красивым.", R.drawable.zhigulevskoye, "Дикси у дома", "vasya916",  Clock.System.now() - 1.minutes, true),
//    OtherDrinkPost("Розовый монстр", "TestTest Вкус как попа", R.drawable.rich, "где то", "irdkwmnsb",  Clock.System.now() - 1.days, false),
//    OtherDrinkPost("Adrenaline rush", "TestTest Ну во первых, это не монстр, а адреналин. Во вторых, это вкус как попа", R.drawable.rich, "где то", "irdkwmnsb",  Clock.System.now() - 2.days, false),
//)

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
        val observer = object : Observer<MutableList<DrinkollectOuterClass.Post>> {
            override fun onSubscribe(d: Disposable) {
                binding.mainItemsList.visibility = View.GONE
                binding.mainScrollProgressBar.visibility = View.VISIBLE
            }

            override fun onNext(t: MutableList<DrinkollectOuterClass.Post>) {
                val drinkPosts: ArrayList<DrinkPost> = ArrayList()
                if (t.size != 0) {
                    for (post in t) {
                        if (post.creator != (activity as MainActivity).service.getUsername()) {
                            drinkPosts.add(
                                OtherDrinkPost(
                                    post.title,
                                    post.description,
                                    post.image,
                                    post.location,
                                    post.creator,
                                    Instant.fromEpochSeconds(
                                        post.timestamp.seconds,
                                        post.timestamp.nanos
                                    ),
                                    post.liked,
                                    post.id
                                )
                            )
                        } else {
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
                if ((activity as MainActivity).service.getUsername() == null) {
                    UnauthDialog(getString(R.string.you_need_to_be_logged_in)).show(
                        requireActivity().supportFragmentManager,
                        "LoginDialog"
                    )
                } else {
                    findNavController().navigate(R.id.action_MainScrollFragment_to_ProfileFragment)
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