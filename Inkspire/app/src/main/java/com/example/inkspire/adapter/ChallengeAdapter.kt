package com.example.inkspire.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.load.engine.GlideException
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.target.Target
import com.example.inkspire.R
import com.example.inkspire.databinding.ItemChallengeBinding
import com.example.inkspire.model.ChallengeVW


class ChallengeAdapter(
    private val onChallengeClick: (ChallengeVW) -> Unit
) : ListAdapter<ChallengeVW, ChallengeAdapter.ChallengeViewHolder>(ChallengeDiffCallback()) {

    inner class ChallengeViewHolder(private val binding: ItemChallengeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(challengeUser: ChallengeVW) {
            binding.challengeTitle.text = challengeUser.title
            binding.challengeConcept.text = challengeUser.concept
            binding.challengeAuthor.text = challengeUser.username

            // Mostra progress bar
            binding.challengeImageProgress.visibility = View.VISIBLE

            Glide.with(binding.challengeImage.context)
                .load(challengeUser.result_pic ?: R.drawable.logo)
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .listener(object : com.bumptech.glide.request.RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.challengeImageProgress.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.challengeImageProgress.visibility = View.GONE
                        return false
                    }
                })
                .into(binding.challengeImage)

            binding.root.setOnClickListener {
                onChallengeClick(challengeUser)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val binding = ItemChallengeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChallengeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ChallengeDiffCallback : DiffUtil.ItemCallback<ChallengeVW>() {
    override fun areItemsTheSame(oldItem: ChallengeVW, newItem: ChallengeVW) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ChallengeVW, newItem: ChallengeVW) =
        oldItem == newItem
}



/*
class ChallengeAdapter(
    private val onChallengeClick: (ChallengeVW) -> Unit
) : ListAdapter<ChallengeVW, ChallengeAdapter.ChallengeViewHolder>(ChallengeDiffCallback()) {

    inner class ChallengeViewHolder(private val binding: ItemChallengeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(challengeUser: ChallengeVW) {
            binding.challengeTitle.text = challengeUser.title
            binding.challengeConcept.text = challengeUser.concept
            binding.challengeAuthor.text = challengeUser.username

            Glide.with(binding.challengeImage.context)
                .load(challengeUser.result_pic ?: R.drawable.logo)
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(binding.challengeImage)

            binding.root.setOnClickListener {
                onChallengeClick(challengeUser)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val binding = ItemChallengeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChallengeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ChallengeDiffCallback : DiffUtil.ItemCallback<ChallengeVW>() {
    override fun areItemsTheSame(oldItem: ChallengeVW, newItem: ChallengeVW) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ChallengeVW, newItem: ChallengeVW) =
        oldItem == newItem
}

 */