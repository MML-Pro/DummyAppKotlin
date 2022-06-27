package com.mml.dummyapp_kotlin.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mml.dummyapp_kotlin.R
import com.mml.dummyapp_kotlin.adapters.FavoritesPostAdapter
import com.mml.dummyapp_kotlin.databinding.FragmentFavoritesBinding
import com.mml.dummyapp_kotlin.util.WrapContentLinearLayoutManager
import com.mml.dummyapp_kotlin.viewmodels.PostViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesFragment : Fragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var favoritesPostAdapter: FavoritesPostAdapter

    private val postViewModel: PostViewModel by viewModels()
    private val titleLayoutManager: GridLayoutManager by lazy { GridLayoutManager(context,2) }
    private val gridLayoutManager: GridLayoutManager by lazy { GridLayoutManager(context,3) }
    private lateinit var layoutManager: WrapContentLinearLayoutManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        favoritesPostAdapter = FavoritesPostAdapter(
            requireActivity(),
            this, postViewModel
        )
        layoutManager = WrapContentLinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL, false
        )
        //        linearLayoutManager = new LinearLayoutManager(this);
        postViewModel.recyclerViewLayout.observe(viewLifecycleOwner) { layout ->
            Log.w(TAG, "getSavedLayout: called")
            when (layout) {
                "cardLayout" -> {
                    binding.favoritesRecyclerView.layoutManager = layoutManager
                    favoritesPostAdapter.setViewType(0)
                    binding.favoritesRecyclerView.adapter = favoritesPostAdapter
                }
                "cardMagazineLayout" -> {
                    binding.favoritesRecyclerView.layoutManager = layoutManager
                    favoritesPostAdapter.setViewType(1)
                    binding.favoritesRecyclerView.adapter = favoritesPostAdapter
                }
                "titleLayout" -> {
                    binding.favoritesRecyclerView.layoutManager = titleLayoutManager
                    favoritesPostAdapter.setViewType(2)
                    binding.favoritesRecyclerView.adapter = favoritesPostAdapter
                }
                "gridLayout" -> {
                    binding.favoritesRecyclerView.layoutManager = gridLayoutManager
                    favoritesPostAdapter.setViewType(3)
                    binding.favoritesRecyclerView.adapter = favoritesPostAdapter
                }
            }
        }
        return binding.root
    }

    companion object {
        private const val TAG = "FavoritesFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.favoritesRecyclerView.visibility = View.INVISIBLE

        postViewModel.readFavoritePosts.observe(viewLifecycleOwner) { favoritesPostList ->
            if (favoritesPostList.isEmpty()) {
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
                binding.favoritesRecyclerView.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            } else {
                favoritesPostAdapter.addData(favoritesPostList)
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
                binding.favoritesRecyclerView.visibility = View.VISIBLE
                favoritesPostAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun changeAndSaveLayout() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.choose_layout))
        val recyclerViewLayouts = resources.getStringArray(R.array.RecyclerViewLayouts)
        //        SharedPreferences.Editor editor = sharedPreferences.edit();
        builder.setItems(
            recyclerViewLayouts
        ) { _: DialogInterface?, index: Int ->
            when (index) {
                0 -> {
                    favoritesPostAdapter.setViewType(0)
                    binding.favoritesRecyclerView.layoutManager = layoutManager
                    binding.favoritesRecyclerView.adapter = favoritesPostAdapter
                    postViewModel.saveRecyclerViewLayout("cardLayout")
                }
                1 -> {
                    favoritesPostAdapter.setViewType(1)
                    binding.favoritesRecyclerView.layoutManager = layoutManager
                    binding.favoritesRecyclerView.adapter = favoritesPostAdapter
                    postViewModel.saveRecyclerViewLayout("cardMagazineLayout")
                }
                2 -> {
                    favoritesPostAdapter.setViewType(2)
                    binding.favoritesRecyclerView.layoutManager = titleLayoutManager
                    binding.favoritesRecyclerView.adapter = favoritesPostAdapter
                    postViewModel.saveRecyclerViewLayout("titleLayout")
                }
                3 -> {
                    favoritesPostAdapter.setViewType(3)
                    binding.favoritesRecyclerView.layoutManager = gridLayoutManager
                    binding.favoritesRecyclerView.adapter = favoritesPostAdapter
                    postViewModel.saveRecyclerViewLayout("gridLayout")
                }
            }
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.favorites_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.deleteAllFavoritePosts) {
            postViewModel.deleteAllFavorites()
            Snackbar.make(requireView(), "All favorites posts deleted", Snackbar.LENGTH_SHORT)
                .show()
            return true
        } else if (item.itemId == R.id.change_layout) {
            changeAndSaveLayout()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}