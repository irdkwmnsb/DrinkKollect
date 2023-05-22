package ru.alzhanov.drinkkollect

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import drinkollect.v1.DrinkollectOuterClass
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import ru.alzhanov.drinkkollect.databinding.FragmentUserProfileBinding

class UserProfileFragment : Fragment() {
    private var _binding: FragmentUserProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val sharedViewModel: UsernameViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val curUsername = (activity as MainActivity).service.getUsername()
        val profileUsername = sharedViewModel.username.value
        binding.chipSendFriendRequest.setOnClickListener {
            if (curUsername == null) {
                UnauthDialog((activity as MainActivity).getString(R.string.log_in_to_send_friend_requests)).show(
                    (activity as MainActivity).supportFragmentManager,
                    "LoginDialog"
                )
            } else {
                val observer = object : Observer<Unit> {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(
                            (activity as MainActivity),
                            "Something went wrong. Try again",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    override fun onComplete() {
                        manipulate()
                    }

                    override fun onNext(t: Unit) {
                    }

                }
                if (profileUsername != null) {
                    (activity as MainActivity).service.sendFriendRequest(
                        observer,
                        profileUsername
                    )
                }
            }
        }
        manipulate()
        binding.chipSendFriendRequest.visibility = View.VISIBLE
        binding.profileName.text = profileUsername
        val observer = object : Observer<MutableList<DrinkollectOuterClass.Post>> {
            override fun onSubscribe(d: Disposable) {
                binding.mainItemsList.visibility = View.GONE
                binding.profileProgressBar.visibility = View.VISIBLE
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
                binding.profileProgressBar.visibility = View.GONE
            }
        }
        if (profileUsername != null) {
            (activity as MainActivity).service.listUserPostsRequest(
                observer,
                profileUsername
            )
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun manipulate() {
        //TODO add a check if the request was already sent
//        if (req not sent) {
        binding.chipSendFriendRequest.text =
            (activity as MainActivity).getString(R.string.send_friend_request)
        binding.chipSendFriendRequest.closeIcon = ResourcesCompat.getDrawable(
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
        binding.chipSendFriendRequest.chipBackgroundColor =
            ResourcesCompat.getColorStateList(
                binding.root.resources,
                typedValue.resourceId,
                null
            )
        /*
        DO NOT DELETE THIS
         */
//        } else {
//            binding.chipSendFriendRequest.text =
//                (activity as MainActivity).getString(R.string.cancel_friend_request)
//            binding.chipSendFriendRequest.closeIcon = ResourcesCompat.getDrawable(
//                binding.root.resources,
//                R.drawable.ic_baseline_star_border_24,
//                null
//            )
//            binding.chipSendFriendRequest.chipBackgroundColor =
//                ResourcesCompat.getColorStateList(
//                    binding.root.resources,
//                    R.color.transparent,
//                    null
//                )
//            val dim = TypedValue.applyDimension(
//                TypedValue.COMPLEX_UNIT_DIP,
//                1f,
//                binding.root.resources.displayMetrics
//            )
//            val typedValue = TypedValue()
//            binding.root.context.theme.resolveAttribute(
//                R.attr.colorOutline,
//                typedValue,
//                true
//            )
//            binding.chipSendFriendRequest.chipStrokeWidth = dim
//            binding.chipSendFriendRequest.chipStrokeColor = ResourcesCompat.getColorStateList(
//                binding.root.resources,
//                typedValue.resourceId,
//                null
//            )
//        }
    }
}