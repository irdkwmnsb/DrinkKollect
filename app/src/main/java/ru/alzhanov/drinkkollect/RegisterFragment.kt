package ru.alzhanov.drinkkollect

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
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
        binding.buttonRegister.setOnClickListener {
            val username = binding.registerEditTextUsername.text.toString()
            val password = binding.registerEditTextPassword.text.toString()
            val passwordRepeat = binding.registerEditTextPasswordRepeat.text.toString()
            if (password != passwordRepeat) {
                binding.registerEditTextPasswordRepeatLayout.error = resources.getString(R.string.passwords_mismatch_ru)
                return@setOnClickListener
            }
            try {
                (activity as MainActivity).service.registerRequest(username, password)
            } catch (e: Exception) {
                binding.registerEditTextPasswordRepeatLayout.error = e.message
                return@setOnClickListener
            }
            findNavController().navigate(R.id.action_RegisterFragment_to_MainScrollFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}