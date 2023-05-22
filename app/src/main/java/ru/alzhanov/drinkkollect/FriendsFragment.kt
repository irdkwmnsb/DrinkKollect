package ru.alzhanov.drinkkollect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import ru.alzhanov.drinkkollect.databinding.FragmentFriendsBinding

class FriendsFragment : Fragment() {
    private var _binding: FragmentFriendsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val sharedViewModel: UsernameViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val curUsername = (activity as MainActivity).service.getUsername()
        var resultsFriends: MutableList<String> = mutableListOf()
        val observerFriends = object : Observer<MutableList<String>> {
            override fun onSubscribe(d: Disposable) {
                binding.friendsList.visibility = View.GONE
            }

            override fun onError(e: Throwable) {
                Toast.makeText(
                    activity,
                    (e as io.grpc.StatusRuntimeException).status.toString(),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

            override fun onComplete() {
                val customAdapter = UsersListViewAdapter(
                    requireActivity(), resultsFriends,
                    showSendRequest = false,
                    showRemoveFromFriends = true
                )
                customAdapter.setOnItemClickListener { user ->
                    sharedViewModel.setUsername(user)
                    if (curUsername != user) {
                        findNavController().navigate(R.id.action_FriendsFragment_to_UserProfileFragment)
                    } else {
                        findNavController().navigate(R.id.action_FriendsFragment_to_ProfileFragment)
                    }
                }
                binding.friendsList.adapter = customAdapter
                binding.friendsList.layoutManager =
                    androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            }

            override fun onNext(t: MutableList<String>) {
                binding.friendsList.visibility = View.VISIBLE
                resultsFriends = t
            }

        }
        if (curUsername != null) {
            (activity as MainActivity).service.listFriendsRequest(observerFriends, curUsername)
        }
        var resultRequests: MutableList<String> = mutableListOf()
        val observerRequests = object : Observer<MutableList<String>> {
            override fun onSubscribe(d: Disposable) {
                binding.friendRequests.visibility = View.GONE
            }

            override fun onError(e: Throwable) {
                Toast.makeText(
                    activity,
                    (e as io.grpc.StatusRuntimeException).status.toString(),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

            override fun onComplete() {
                val customAdapter = UsersListViewAdapter(requireActivity(), resultRequests, false)
                customAdapter.setOnItemClickListener { user ->
                    sharedViewModel.setUsername(user)
                    if (curUsername != user) {
                        findNavController().navigate(R.id.action_FriendsFragment_to_UserProfileFragment)
                    } else {
                        findNavController().navigate(R.id.action_FriendsFragment_to_ProfileFragment)
                    }
                }
                binding.friendRequests.adapter = customAdapter
                binding.friendRequests.layoutManager =
                    androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            }

            override fun onNext(t: MutableList<String>) {
                binding.friendRequests.visibility = View.VISIBLE
                resultRequests = t
            }

        }
        if (curUsername != null) {
            (activity as MainActivity).service.listFriendRequestsRequest(observerRequests)
        }
    }
}