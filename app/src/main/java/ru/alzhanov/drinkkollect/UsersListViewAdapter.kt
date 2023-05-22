package ru.alzhanov.drinkkollect

import android.app.Activity
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import ru.alzhanov.drinkkollect.databinding.UserInListLayoutBinding

class UsersViewHolder(
    inflate: UserInListLayoutBinding,
    private val showSendRequest: Boolean,
    private val showRemoveFromFriends: Boolean
) : RecyclerView.ViewHolder(inflate.root) {
    val binding = inflate
    val rootView: View = itemView.rootView
    fun bind(user: String) {
        binding.user.text = user
        val curUser = (itemView.context as MainActivity).service.getUsername()
        if (showSendRequest) {
            binding.buttonsLayout.visibility = View.GONE
            binding.chipSendFriendRequest.visibility = View.VISIBLE
            binding.chipSendFriendRequest.setOnClickListener {
                if (curUser == null) {
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
                        user
                    )
                }
            }
            manipulate()
        } else {
            binding.buttonsLayout.visibility = View.VISIBLE
            if (showRemoveFromFriends) {
                binding.buttonAcceptRequest.visibility = View.GONE
                binding.buttonRejectRequest.visibility = View.GONE
//                binding.buttonRejectRequest.setOnClickListener {
//                    val observer = object : Observer<Unit> {
//                        override fun onSubscribe(d: Disposable) {
//                        }
//
//                        override fun onError(e: Throwable) {
//                            Toast.makeText(
//                                (itemView.context as MainActivity),
//                                "Something went wrong. Try again",
//                                Toast.LENGTH_SHORT
//                            )
//                                .show()
//                        }
//
//                        override fun onComplete() {
//                            binding.buttonRejectRequest.icon = null
//                            binding.buttonRejectRequest.width = 200
//                            binding.buttonRejectRequest.text =
//                                (itemView.context as MainActivity).getString(R.string.removed)
//                        }
//
//                        override fun onNext(t: Unit) {
//                        }
//
//                    }
//                    (itemView.context as MainActivity).service.removeFriendRequest(
//                        observer,
//                        user
//                    )
//                }
            } else {
                binding.buttonAcceptRequest.visibility = View.VISIBLE
                binding.buttonRejectRequest.setOnClickListener {
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
                            binding.buttonsLayout.visibility = View.GONE
                        }

                        override fun onNext(t: Unit) {
                        }

                    }
                    (itemView.context as MainActivity).service.rejectFriendRequest(
                        observer,
                        user
                    )
                }
            }
            binding.chipSendFriendRequest.visibility = View.GONE
            if (curUser == null) {
                UnauthDialog((itemView.context as MainActivity).getString(R.string.log_in_to_accept_friend_requests)).show(
                    (itemView.context as MainActivity).supportFragmentManager,
                    "LoginDialog"
                )
            } else {
                binding.buttonAcceptRequest.setOnClickListener {
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
                            binding.buttonsLayout.visibility = View.GONE
                        }

                        override fun onNext(t: Unit) {
                        }

                    }
                    (itemView.context as MainActivity).service.acceptFriendRequest(
                        observer,
                        user
                    )
                }

            }
        }

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
}

class UsersListViewAdapter(
    private val context: Activity,
    private val valuesList: MutableList<String>,
    private val showSendRequest: Boolean,
    private val showRemoveFromFriends: Boolean = false
) :
    RecyclerView.Adapter<UsersViewHolder>() {
    private var onItemClickListener: ((String) -> Unit)? = null
    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val binding = UserInListLayoutBinding.inflate(
            context.layoutInflater,
            parent,
            false
        )

        return UsersViewHolder(
            binding, showSendRequest, showRemoveFromFriends
        )
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        holder.rootView.setOnClickListener {
            onItemClickListener?.let {
                it(valuesList[position])
            }
        }
        holder.bind(valuesList[position])
    }

    override fun getItemCount(): Int {
        return valuesList.size
    }
}


