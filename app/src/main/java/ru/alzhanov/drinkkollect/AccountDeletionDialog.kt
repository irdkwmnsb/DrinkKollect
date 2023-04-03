package ru.alzhanov.drinkkollect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import ru.alzhanov.drinkkollect.databinding.AccountDeletionDialogBinding

class AccountDeletionDialog : DialogFragment() {
    private lateinit var binding: AccountDeletionDialogBinding
    private val DIALOG_WIDTH_PERCENTAGE = 0.85

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.default_dialog_bg);
        binding = AccountDeletionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.accountDeletionBack.setOnClickListener {
            dialog!!.dismiss()
        }

        binding.accountDeletionDelete.setOnClickListener {
            //TODO: delete account
            findNavController().navigate(R.id.action_ProfileFragment_to_LoginFragment)
            dialog!!.dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * DIALOG_WIDTH_PERCENTAGE).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}