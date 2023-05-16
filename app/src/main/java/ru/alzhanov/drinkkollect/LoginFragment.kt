package ru.alzhanov.drinkkollect

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import ru.alzhanov.drinkkollect.databinding.FragmentLoginBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        if (binding.buttonRegister.lineCount > 1) {
//            binding.buttonRegister.android:layout_below="@id/buttonLogIn"
//
//        }
        binding.buttonRegister.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_RegisterFragment)
        }

        binding.buttonLogIn.setOnClickListener {
            val observer = object: Observer<Unit> {
                override fun onSubscribe(d: Disposable) {
                    binding.buttonLogIn.visibility = View.GONE
                    binding.buttonRegister.visibility = View.GONE
                    binding.buttonIWantToWatch.visibility = View.GONE
                    binding.loginEditTextPasswordLayout.visibility = View.GONE
                    binding.loginEditTextUsernameLayout.visibility = View.GONE
                    binding.viewTextNameDrinkKollect.visibility = View.GONE
                    binding.viewTextWelcomeRU.visibility = View.GONE
                    binding.loginProgressBar.visibility = View.VISIBLE
                }

                override fun onNext(t: Unit) {}

                override fun onError(e: Throwable) {
                    if(e is io.grpc.StatusRuntimeException) {
                        binding.loginEditTextPasswordLayout.error = e.status.description
                    } else {
                        binding.loginEditTextPasswordLayout.error = e.message
                    }
                    binding.buttonLogIn.visibility = View.VISIBLE
                    binding.buttonRegister.visibility = View.VISIBLE
                    binding.buttonIWantToWatch.visibility = View.VISIBLE
                    binding.loginEditTextPasswordLayout.visibility = View.VISIBLE
                    binding.loginEditTextUsernameLayout.visibility = View.VISIBLE
                    binding.viewTextNameDrinkKollect.visibility = View.VISIBLE
                    binding.viewTextWelcomeRU.visibility = View.VISIBLE
                    binding.loginProgressBar.visibility = View.GONE
                }

                override fun onComplete() {
                    findNavController().navigate(R.id.action_LoginFragment_to_MainScrollFragment)
                }
            }

            (activity as MainActivity).service.loginRequest(
                observer,
                binding.loginEditTextUsername.text.toString(),
                binding.loginEditTextPassword.text.toString()
            )
        }

        binding.buttonIWantToWatch.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_MainScrollFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}