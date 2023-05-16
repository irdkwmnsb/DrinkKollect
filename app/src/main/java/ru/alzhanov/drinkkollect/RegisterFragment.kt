package ru.alzhanov.drinkkollect

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import ru.alzhanov.drinkkollect.databinding.FragmentRegisterBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerTextViewError.visibility = View.GONE
        binding.buttonRegister.setOnClickListener {
            val username = binding.registerEditTextUsername.text.toString()
            val password = binding.registerEditTextPassword.text.toString()
            val passwordRepeat = binding.registerEditTextPasswordRepeat.text.toString()
            if (password != passwordRepeat) {
                binding.registerTextViewError.text = resources.getString(R.string.passwords_mismatch)
                binding.registerTextViewError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            val observer = object: Observer<Unit> {
                override fun onSubscribe(d: Disposable) {
                    binding.registerEditTextUsernameLayout.visibility = View.GONE
                    binding.registerEditTextPasswordLayout.visibility = View.GONE
                    binding.registerEditTextPasswordRepeatLayout.visibility = View.GONE
                    binding.buttonRegister.visibility = View.GONE
                    binding.registerTextViewError.visibility = View.GONE
                    binding.registerProgressBar.visibility = View.VISIBLE
                }

                override fun onNext(t: Unit) {}

                override fun onError(e: Throwable) {
                    if(e is io.grpc.StatusRuntimeException) {
                        binding.registerTextViewError.text = e.status.description
                    } else {
                        binding.registerTextViewError.text = e.message
                    }
                    binding.registerTextViewError.visibility = View.VISIBLE
                    binding.registerEditTextUsernameLayout.visibility = View.VISIBLE
                    binding.registerEditTextPasswordLayout.visibility = View.VISIBLE
                    binding.registerEditTextPasswordRepeatLayout.visibility = View.VISIBLE
                    binding.buttonRegister.visibility = View.VISIBLE
                    binding.registerProgressBar.visibility = View.GONE
                }

                override fun onComplete() {
                    findNavController().navigate(R.id.action_RegisterFragment_to_MainScrollFragment)
                }
            }
            (activity as MainActivity).service.registerRequest(observer, username, password)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}