package com.example.inkspire.adapter

import androidx.recyclerview.widget.RecyclerView
import com.example.inkspire.databinding.ItemUserBinding
import com.example.inkspire.model.UserProfileVW
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.target.Target
import com.example.inkspire.R

class UserAdapter(
    private val onUserClick: (UserProfileVW) -> Unit
) : ListAdapter<UserProfileVW, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserProfileVW) {
            binding.userUsername.text = user.username
            binding.createdChallenges.text = "${user.created_count} created"
            binding.completedChallenges.text = "${user.completed_count} completed"

            // Mostra progress bar
            binding.profilePicProgress.visibility = View.VISIBLE

            Glide.with(binding.profilePic.context)
                .load(user.profile_pic ?: R.drawable.ic_account_circle)
                .placeholder(R.drawable.ic_account_circle)
                .error(R.drawable.ic_account_circle)
                .circleCrop()
                .listener(object : com.bumptech.glide.request.RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.profilePicProgress.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.profilePicProgress.visibility = View.GONE
                        return false
                    }
                })
                .into(binding.profilePic)

            binding.root.setOnClickListener {
                onUserClick(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<UserProfileVW>() {
    override fun areItemsTheSame(oldItem: UserProfileVW, newItem: UserProfileVW) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: UserProfileVW, newItem: UserProfileVW) =
        oldItem == newItem
}