package ru.alzhanov.drinkkollect

import android.app.Activity
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import ru.alzhanov.drinkkollect.databinding.UserInListLayoutBinding

class UsersViewHolder(inflate: UserInListLayoutBinding) : RecyclerView.ViewHolder(inflate.root) {
    val binding = inflate
    fun bind(user: String) {
        binding.user.text = user
        binding.user.setOnClickListener {
        }
        binding.chipSendFriendRequest.setOnClickListener {
            if ((itemView.context as MainActivity).service.getUsername() == null) {
                UnauthDialog((itemView.context as MainActivity).getString(R.string.log_in_to_send_friend_requests)).show(
                    (itemView.context as MainActivity).supportFragmentManager,
                    "LoginDialog"
                )
            } else {
                val observer = object : Observer<Unit> {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(
                            (itemView.context as MainActivity),
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
                (itemView.context as MainActivity).service.sendFriendRequest(
                    observer,
                    (itemView.context as MainActivity).service.getUsername()!!
                )
            }
        }
        manipulate()
    }

    private fun manipulate() {
        //TODO add a check if the request was already sent
//        if (req not sent) {
        binding.chipSendFriendRequest.text =
            (itemView.context as MainActivity).getString(R.string.send_friend_request)
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
//                (itemView.context as MainActivity).getString(R.string.cancel_friend_request)
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

    class UsersListViewAdapter(
        private val context: Activity,
        private val valuesList: MutableList<String>
    ) :
        RecyclerView.Adapter<UsersViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
            val binding = UserInListLayoutBinding.inflate(
                context.layoutInflater,
                parent,
                false
            )

            return UsersViewHolder(
                binding
            )
        }

        override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
            holder.bind(valuesList[position])
        }

        override fun getItemCount(): Int {
            return valuesList.size
        }
    }
}