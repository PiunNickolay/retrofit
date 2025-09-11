package ru.netology.learningandtrying.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.learningandtrying.R
import ru.netology.learningandtrying.activity.NewPostFragment.Companion.textArg
import ru.netology.learningandtrying.adapter.OnInteractionListener
import ru.netology.learningandtrying.adapter.PostsAdapter
import ru.netology.learningandtrying.databinding.FragmentFeedBinding
import ru.netology.learningandtrying.dto.Post
import ru.netology.learningandtrying.viewmodel.PostViewModel


class FeedFragment : Fragment() {

    @SuppressLint("StringFormatInvalid")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)
        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)
        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val sharedIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(sharedIntent)
            }

            override fun onPost(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_postFragment,
                    Bundle().apply {
                        putLong("postId", post.id)
                    }
                )
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply { textArg = post.content })
            }

            override fun onImage(post: Post) {
                val imageUrl = "http://10.0.2.2:9999/media/${post.attachment?.url}"
                findNavController().navigate(
                    R.id.action_feedFragment_to_imageFragment,
                    Bundle().apply { putString("url", imageUrl) }
                )
            }

        })
        binding.list.adapter = adapter
        viewModel.data.observe(viewLifecycleOwner) { data ->
            adapter.submitList(data.posts)
            binding.empty.isVisible = data.empty

        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.loading.isVisible = state.loading
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) {
                        viewModel.load()
                    }
                    .show()
                binding.swipeRefreshLayout.isRefreshing = state.refreshing
            }
        }
        viewModel.errorEvent.observe(viewLifecycleOwner) { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry) {
                    viewModel.load()
                }
                .show()
        }

        viewModel.newerCount.observe(viewLifecycleOwner) { count ->
            binding.newPostsButton.isVisible = count > 0
            if (count > 0) {
                binding.newPostsButton.text = getString(R.string.new_posts, count)
            }
        }

        binding.newPostsButton.setOnClickListener {
            viewModel.showNewPosts()
            binding.list.smoothScrollToPosition(0)
            binding.newPostsButton.isVisible = false
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.load()
        }

        binding.fab.setOnClickListener {
            viewModel.cancelEdit()
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }
        return binding.root
    }
}