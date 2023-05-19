package ru.alzhanov.drinkkollect

import android.app.Activity
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.alzhanov.drinkkollect.databinding.UserInListLayoutBinding



class UsersListViewAdapter(
    private val context: Activity,
    private val valuesList: MutableList<String>
) :
    RecyclerView.Adapter<UsersListViewAdapter.UsersViewHolder>() {
    class UsersViewHolder(inflate: UserInListLayoutBinding) : RecyclerView.ViewHolder(inflate.root) {
        val binding = inflate
        fun bind(user: String) {
            binding.user.text = user
        }
    }
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