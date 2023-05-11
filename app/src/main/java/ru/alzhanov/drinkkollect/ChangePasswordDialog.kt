package ru.alzhanov.drinkkollect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import ru.alzhanov.drinkkollect.databinding.ChangePasswordDialogBinding

class ChangePasswordDialog : DialogFragment() {
    private lateinit var binding: ChangePasswordDialogBinding
    private val DIALOG_WIDTH_PERCENTAGE = 0.85

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.default_dialog_bg);
        binding = ChangePasswordDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.changePasswordBack.setOnClickListener {
            dialog!!.dismiss()
        }

        binding.changePasswordChange.setOnClickListener {
            val oldPassword = binding.changePasswordOldPassword.text.toString()
            val newPassword = binding.changePasswordNewPassword.text.toString()
            val newPasswordRepeat = binding.changePasswordRepeat.text.toString()
            if (newPassword != newPasswordRepeat) {
                binding.changePasswordRepeat.error = resources.getString(R.string.passwords_mismatch)
                return@setOnClickListener
            }
            val observer = object: Observer<Unit> {
                override fun onSubscribe(d: Disposable) {}

                override fun onNext(t: Unit) {}

                override fun onError(e: Throwable) {
                    binding.changePasswordRepeat.error = e.message
                }

                override fun onComplete() {
                    dialog!!.dismiss()
                }
            }
            (activity as MainActivity).service.changePasswordRequest(observer, oldPassword, newPassword)
        }
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * DIALOG_WIDTH_PERCENTAGE).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}