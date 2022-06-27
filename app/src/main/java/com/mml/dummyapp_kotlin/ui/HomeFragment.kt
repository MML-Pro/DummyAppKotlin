package com.mml.dummyapp_kotlin.ui

import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mml.dummyapp_kotlin.R
import com.mml.dummyapp_kotlin.adapters.PostAdapter
import com.mml.dummyapp_kotlin.databinding.FragmentHomeBinding
import com.mml.dummyapp_kotlin.models.Item
import com.mml.dummyapp_kotlin.util.*
import com.mml.dummyapp_kotlin.viewmodels.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

private const val TAG = "HomeFragment"

@AndroidEntryPoint
class HomeFragment : Fragment(), TitleAndGridLayout {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var itemArrayList = mutableListOf<Item>()

    private val postViewModel: PostViewModel by viewModels()
    private val titleLayoutManager: GridLayoutManager by lazy { GridLayoutManager(context, 2) }
    private val gridLayoutManager: GridLayoutManager by lazy { GridLayoutManager(context, 3) }
    private lateinit var layoutManager: WrapContentLinearLayoutManager

    private lateinit var networkListener: NetworkListener

    private lateinit var adapter: PostAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        itemArrayList = ArrayList()
//        postViewModel = ViewModelProvider(this)[PostViewModel::class.java]
        adapter = context?.let { PostAdapter(it, itemArrayList, this) }!!
        postViewModel.finalURL.value = "${Constatns.BASE_URL}?key=${Constatns.API_KEY}"
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

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

//        setUpRecyclerView()

        binding.loadMoreBtn.visibility = View.VISIBLE
        postViewModel.readBackOnline.observe(viewLifecycleOwner) {
            postViewModel.backOnline = it
        }
        networkListener = NetworkListener()

        lifecycleScope.launchWhenStarted {
            networkListener.checkNetworkAvailability(requireContext()).collect { stats ->
                Log.d(TAG, "networkListener: $stats")
                postViewModel.networkStats = stats
                postViewModel.showNetworkStats()
                if (stats) {
                    binding.loadMoreBtn.visibility = View.VISIBLE
                    requestApiData()
                } else {
                    if (binding.loadMoreBtn.visibility == View.VISIBLE) {
                        binding.loadMoreBtn.visibility = View.GONE
                    }
                    getPostsFromDB()
                }
            }
        }


        postViewModel.recyclerViewLayout.observe(viewLifecycleOwner) { layout ->
//            Log.w(TAG, "getSavedLayout called")
            when (layout) {
                "cardLayout" -> {
//                    binding.loadMoreBtn.visibility = View.VISIBLE
                    binding.homeRecyclerView.layoutManager = this.layoutManager
                    adapter.setViewType(0)
                    binding.homeRecyclerView.adapter = adapter
                }
                "cardMagazineLayout" -> {
//                    binding.loadMoreBtn.visibility = View.VISIBLE
                    binding.homeRecyclerView.layoutManager = layoutManager
                    adapter.setViewType(1)
                    binding.homeRecyclerView.adapter = adapter
                }
                "titleLayout" -> {
                    binding.loadMoreBtn.visibility = View.GONE
                    binding.homeRecyclerView.layoutManager = titleLayoutManager
                    adapter.setViewType(2)
                    binding.homeRecyclerView.adapter = adapter
                }
                "gridLayout" -> {
                    binding.loadMoreBtn.visibility = View.GONE
                    binding.homeRecyclerView.layoutManager = gridLayoutManager
                    adapter.setViewType(3)
                    binding.homeRecyclerView.adapter = adapter
                }
            }
        }

//        if (Utils.hasInternetConnection(requireContext())) {
//            requestApiData()
//        } else {
//            getPostsFromDB()
//        }


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
           postViewModel.getPosts()
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

    private fun getPostsFromDB() {

        showShimmerEffect()
        postViewModel.readAllPosts.observe(viewLifecycleOwner) { items ->
            if (items.isNotEmpty()) {
                hideShimmerEffect()
//                binding.loadMoreBtn.visibility = View.GONE
                if (itemArrayList.isNotEmpty()) itemArrayList.clear()
                itemArrayList.addAll(items)
                adapter.notifyDataSetChanged()
            } else {
                noInternetConnectionLayout()
            }
        }
    }


    private fun requestApiData() {
//        Log.d(TAG, "requestApiData: called")
//        binding.loadMoreBtn.visibility = View.VISIBLE
        postViewModel.getPosts()
        postViewModel.postsResponse.observe(viewLifecycleOwner) { response ->

            when (response) {
                is NetworkResult.Success -> {
                    hideShimmerEffect()
                    response.data?.let {
                        itemArrayList.addAll(it.items)
                        adapter.notifyDataSetChanged()
                    }

                }

                is NetworkResult.Error -> {
                    hideShimmerEffect()
                    //                    loadDataFromCache()
//                    Toast.makeText(
//                        requireContext(),
//                        response.message.toString(),
//                        Toast.LENGTH_LONG
//                    ).show()

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


    private fun showShimmerEffect() {
        binding.apply {
            shimmerLayout.visibility = View.VISIBLE
            homeRecyclerView.visibility = View.INVISIBLE
        }

    }

    private fun hideShimmerEffect() {
        binding.apply {
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            homeRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun changeAndSaveLayout() {
//        Log.w(TAG, "changeAndSaveLayout: called")
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
                        binding.homeRecyclerView.layoutManager = layoutManager
                        binding.homeRecyclerView.adapter = adapter
                        postViewModel.saveRecyclerViewLayout("cardLayout")
                    }
                    1 -> {
                        adapter.setViewType(1)
                        binding.homeRecyclerView.layoutManager = layoutManager
                        binding.homeRecyclerView.adapter = adapter
                        postViewModel.saveRecyclerViewLayout("cardMagazineLayout")

                    }
                    2 -> {
                        adapter.setViewType(2)
                        binding.homeRecyclerView.layoutManager = titleLayoutManager
                        binding.homeRecyclerView.adapter = adapter
                        postViewModel.saveRecyclerViewLayout("titleLayout")
                    }
                    3 -> {
                        adapter.setViewType(3)
                        binding.homeRecyclerView.layoutManager = gridLayoutManager
                        binding.homeRecyclerView.adapter = adapter

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
        val searchManager =
            requireContext().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.app_bar_search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        searchView.queryHint = resources.getString(R.string.searchForPosts)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(keyword: String): Boolean {
                if (keyword.isEmpty()) {
                    Snackbar.make(
                        requireView(),
                        "please enter keyword to search",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
//                itemArrayList.clear()
                if (Utils.hasInternetConnection(requireContext())) {
                    postViewModel.getItemsBySearch(keyword)
                    postViewModel.searchedPostsResponse.observe(this@HomeFragment) { response ->

                        when (response) {
                            is NetworkResult.Success -> {
                                hideShimmerEffect()
                                itemArrayList.clear()
                                binding.loadMoreBtn.visibility = View.GONE
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
                                    response.toString(),
                                    Toast.LENGTH_LONG
                                ).show()

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
                } else {
                    postViewModel.getItemsBySearchInDB(keyword)
                    postViewModel.postsBySearchInDB.observe(this@HomeFragment) { items ->
                        if (items.isNotEmpty()) {
                            hideShimmerEffect()
                            binding.loadMoreBtn.visibility = View.GONE
                            itemArrayList.clear()
                            itemArrayList.addAll(items)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }

//                } else {
//                    postViewModel.getItemsBySearchInDB(keyword)
//                    postViewModel.getItemsBySearchMT.observe(
//                        viewLifecycleOwner
//                    ) { items ->
//                        Log.d(HomeFragment.TAG, "onQueryTextSubmit database called")
//                        itemArrayList.clear()
//                        itemArrayList.addAll(items)
//                        adapter.notifyDataSetChanged()
//                    }
//                }
                return false

            }


            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
        searchView.setOnCloseListener {
            if (Utils.hasInternetConnection(requireContext())) {
                Log.d(TAG, "setOnCloseListener: called")
                itemArrayList.clear()
                requestApiData()
            } else {
                Log.d(TAG, "setOnCloseListener: called")
                itemArrayList.clear()
                getPostsFromDB()
            }
            false
        }


        postViewModel.searchError.observe(viewLifecycleOwner) { searchError ->
            if (searchError) {
                Toast.makeText(
                    requireContext(),
                    "There's no posts with this keyword", Toast.LENGTH_LONG
                ).show()
            }
        }


//        searchView.setOnCloseListener {
//            if (Utils.hasNetworkAccess(requireContext())) {
//                Log.d(TAG, "setOnCloseListener: called")
//                itemArrayList.clear()
//                binding.emptyView.visibility = View.GONE
//                binding.homeRecyclerView.visibility = View.VISIBLE
//                postViewModel.getPosts()
//                adapter.notifyDataSetChanged()
//            } else {
//                Log.d(TAG, "setOnCloseListener: called")
//                binding.emptyView.visibility = View.GONE
//                binding.homeRecyclerView.visibility = View.VISIBLE
//                postViewModel.getAllItemsFromDataBase.observe(
//                    viewLifecycleOwner
//                ) { items ->
//                    itemArrayList.addAll(items)
//                    adapter.notifyDataSetChanged()
//                }
//            }
//            false
//        }
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
            homeRecyclerView.visibility = View.GONE
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
            postViewModel.getPosts()
        }
    }
}