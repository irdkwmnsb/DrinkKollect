package ru.alzhanov.drinkkollect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import ru.alzhanov.drinkkollect.databinding.FragmentSearchUsersBinding

class SearchUsersFragment : Fragment() {
    private var _binding: FragmentSearchUsersBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var results: MutableList<String> = mutableListOf()
        val observer = object : Observer<MutableList<String>> {
            override fun onSubscribe(d: Disposable) {
                binding.usersList.visibility = View.GONE
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
                val customAdapter = UsersListViewAdapter(requireActivity(), results)
                binding.usersList.adapter = customAdapter
                binding.usersList.layoutManager =
                    androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            }

            override fun onNext(t: MutableList<String>) {
                binding.usersList.visibility = View.VISIBLE
                results = t
            }

        }
        binding.searchUsersView.isActivated = true
        binding.searchUsersView.onActionViewExpanded()
        binding.searchUsersView.clearFocus()
        binding.searchUsersView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    (activity as MainActivity).service.listUsersRequest(observer, newText)
                }

                return false
            }

        })
    }
}