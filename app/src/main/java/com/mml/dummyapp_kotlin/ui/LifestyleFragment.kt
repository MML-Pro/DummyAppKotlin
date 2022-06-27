package com.mml.dummyapp_kotlin.ui

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mml.dummyapp_kotlin.R
import com.mml.dummyapp_kotlin.adapters.PostAdapter
import com.mml.dummyapp_kotlin.util.TitleAndGridLayout
import com.mml.dummyapp_kotlin.databinding.LifestyleFragmentBinding
import com.mml.dummyapp_kotlin.models.Item
import com.mml.dummyapp_kotlin.util.Constatns.API_KEY
import com.mml.dummyapp_kotlin.util.Constatns.BASE_URL_POSTS_BY_LABEL
import com.mml.dummyapp_kotlin.util.NetworkResult
import com.mml.dummyapp_kotlin.util.Utils
import com.mml.dummyapp_kotlin.util.WrapContentLinearLayoutManager
import com.mml.dummyapp_kotlin.viewmodels.PostViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "LifestyleFragment"

@AndroidEntryPoint
class LifestyleFragment : Fragment(), TitleAndGridLayout {
    private var _binding: LifestyleFragmentBinding? = null
    private val binding get() = _binding!!

    private var itemArrayList = mutableListOf<Item>()

    private val postViewModel: PostViewModel by viewModels()
    private val titleLayoutManager: GridLayoutManager by lazy { GridLayoutManager(context, 2) }
    private val gridLayoutManager: GridLayoutManager by lazy { GridLayoutManager(context, 3) }
    private lateinit var layoutManager: WrapContentLinearLayoutManager

    private lateinit var adapter: PostAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        itemArrayList = ArrayList()
//        postViewModel = ViewModelProvider(this)[PostViewModel::class.java]
        adapter = context?.let { PostAdapter(it, itemArrayList, this) }!!
        postViewModel.finalURL.value =
            BASE_URL_POSTS_BY_LABEL + "posts/search?q=label:Lifestyle&key=" + API_KEY

        layoutManager = WrapContentLinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL, false
        )
//        titleLayoutManager = GridLayoutManager(context, 2)
//        gridLayoutManager = GridLayoutManager(context, 3)
    }


    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LifestyleFragmentBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        postViewModel.label.value = "Lifestyle"

//        itemArrayList = ArrayList()
//        adapter = PostAdapter(context!!, itemArrayList, this, postViewModel)


        postViewModel.recyclerViewLayout.observe(viewLifecycleOwner) { layout ->
            Log.w(TAG, "getSavedLayout called")
            when (layout) {
                "cardLayout" -> {
                    binding.loadMoreBtn.visibility = View.VISIBLE
                    binding.lifestyleRecyclerView.layoutManager = layoutManager
                    adapter.setViewType(0)
                    binding.lifestyleRecyclerView.adapter = adapter
                }
                "cardMagazineLayout" -> {
                    binding.loadMoreBtn.visibility = View.VISIBLE
                    binding.lifestyleRecyclerView.layoutManager = layoutManager
                    adapter.setViewType(1)
                    binding.lifestyleRecyclerView.adapter = adapter
                }
                "titleLayout" -> {
                    binding.loadMoreBtn.visibility = View.GONE
                    binding.lifestyleRecyclerView.layoutManager = titleLayoutManager
                    adapter.setViewType(2)
                    binding.lifestyleRecyclerView.adapter = adapter
                }
                "gridLayout" -> {
                    binding.loadMoreBtn.visibility = View.GONE
                    binding.lifestyleRecyclerView.layoutManager = gridLayoutManager
                    adapter.setViewType(3)
                    binding.lifestyleRecyclerView.adapter = adapter
                }
            }
        }

        if (Utils.hasInternetConnection(requireContext())) {
            requestApiData()
        } else {
            noInternetConnectionLayout()

        }


        binding.loadMoreBtn.setOnClickListener {
            val dialog: AlertDialog =
                Utils.setProgressDialog(requireContext())
            postViewModel.postsResponse.observe(viewLifecycleOwner) {
                if (it is NetworkResult.Loading) {
                    dialog.show()
                    hideShimmerEffect()
                } else {
                    dialog.dismiss()
                }
            }
            if (Utils.hasInternetConnection(requireContext())) {
                postViewModel.getPostListByLabel()
//                Log.w(TAG, "loadMoreBtn: " + dialog.isShowing());
            } else {
//                postViewModel.isLoading.postValue(true)
//                postViewModel.getAllItemsFromDataBase.getValue()
//                postViewModel.isLoading.postValue(false)
                noInternetConnectionLayout()
            }
        }

        postViewModel.errorCode.observe(viewLifecycleOwner) { errorCode ->
            if (errorCode == 400) {
                Snackbar.make(requireView(), R.string.lastPost, Snackbar.LENGTH_LONG).show()
                binding.loadMoreBtn.visibility = View.GONE
            } else {
                noInternetConnectionLayout()
                Snackbar.make(requireView(), "error code $errorCode", Snackbar.LENGTH_LONG).show()
            }
        }

        return binding.root
    }

    private fun requestApiData() {
        Log.d(TAG, "requestApiData: called")
        postViewModel.getPostListByLabel()
        postViewModel.postsResponse.observe(viewLifecycleOwner) { response ->

            when (response) {
                is NetworkResult.Success -> {
                    hideShimmerEffect()
                    response.data?.let {
                        itemArrayList.addAll(it.items)
//                        Log.e(TAG, "requestApiData: ${itemArrayList[0]}", )
                    }
                    adapter.notifyDataSetChanged()

                }

                is NetworkResult.Error -> {
                    hideShimmerEffect()
                    //                    loadDataFromCache()
                    Toast.makeText(
                        requireContext(),
                        response.message.toString(),
                        Toast.LENGTH_LONG
                    ).show()

                    Log.e(TAG, response.data.toString())
                    Log.e(TAG, response.message.toString())
                }

                is NetworkResult.Loading -> {
                    if (postViewModel.recyclerViewLayout.value == "titleLayout" ||
                        postViewModel.recyclerViewLayout.value == "gridLayout"
                    ) {
                        hideShimmerEffect()
                    } else {
                        showShimmerEffect()
                    }
                }
            }

        }
    }

    private fun setUpRecyclerView() {
        _binding!!.apply {
            lifestyleRecyclerView.apply {
                adapter = adapter
                loadMoreBtn.visibility = View.VISIBLE
//                binding.homeRecyclerView.adapter = mAdapter
            }
        }
        showShimmerEffect()
    }


    private fun showShimmerEffect() {
        binding.apply {
            shimmerLayout.visibility = View.VISIBLE
            lifestyleRecyclerView.visibility = View.INVISIBLE
        }

    }

    private fun hideShimmerEffect() {
        binding.apply {
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            lifestyleRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun changeAndSaveLayout() {
        Log.w(TAG, "changeAndSaveLayout: called")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.choose_layout))
        val recyclerViewLayouts = resources.getStringArray(R.array.RecyclerViewLayouts)
        //        SharedPreferences.Editor editor = sharedPreferences.edit();
        builder.setItems(
            recyclerViewLayouts
        ) { _: DialogInterface?, index: Int ->
            try {
                when (index) {
                    0 -> {
                        adapter.setViewType(0)
                        binding.lifestyleRecyclerView.layoutManager = layoutManager
                        binding.lifestyleRecyclerView.adapter = adapter
                        //                    editor.putString("recyclerViewLayout", "cardLayout");
//                    editor.apply();
                        postViewModel.saveRecyclerViewLayout("cardLayout")
                    }
                    1 -> {
                        adapter.setViewType(1)
                        binding.lifestyleRecyclerView.layoutManager = layoutManager
                        binding.lifestyleRecyclerView.adapter = adapter
                        //                    editor.putString("recyclerViewLayout", "cardMagazineLayout");
//                    editor.apply();
                        postViewModel.saveRecyclerViewLayout("cardMagazineLayout")
                    }
                    2 -> {
                        adapter.setViewType(2)
                        binding.lifestyleRecyclerView.layoutManager = titleLayoutManager
                        binding.lifestyleRecyclerView.adapter = adapter
                        //                    editor.putString("recyclerViewLayout", "titleLayout");
//                    editor.apply();
                        postViewModel.saveRecyclerViewLayout("titleLayout")
                    }
                    3 -> {
                        adapter.setViewType(3)
                        binding.lifestyleRecyclerView.layoutManager = gridLayoutManager
                        binding.lifestyleRecyclerView.adapter = adapter
                        //                    editor.putString("recyclerViewLayout", "gridLayout");
//                    editor.apply();
                        postViewModel.saveRecyclerViewLayout("gridLayout")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "changeAndSaveLayout: " + e.message)
                Log.e(TAG, "changeAndSaveLayout: " + e.cause)
            }
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.change_layout) {
            changeAndSaveLayout()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun noInternetConnectionLayout() {
        binding.apply {
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            lifestyleRecyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun tellFragmentToGetItems(layout: String) {
        if (layout == "titleLayout" || layout == "gridLayout") {
            hideShimmerEffect()
            postViewModel.getPostListByLabel()
        }
    }
}