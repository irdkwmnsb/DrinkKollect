package ru.alzhanov.drinkkollect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

        binding.buttonRegister.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_RegisterFragment)
        }

        binding.buttonLogIn.setOnClickListener {
            try {
                (activity as MainActivity).service.loginRequest(
                    binding.loginEditTextUsername.text.toString(),
                    binding.loginEditTextPassword.text.toString()
                )
            } catch (e: Exception) {
                binding.loginEditTextPasswordLayout.error = e.message
                return@setOnClickListener
            }
            findNavController().navigate(R.id.action_LoginFragment_to_MainScrollFragment)
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