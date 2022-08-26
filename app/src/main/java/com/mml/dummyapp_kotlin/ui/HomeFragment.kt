package com.mml.dummyapp_kotlin.ui

import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.opengl.Visibility
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.AbsListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.*
import com.google.android.material.snackbar.Snackbar
import com.mml.dummyapp_kotlin.R
import com.mml.dummyapp_kotlin.adapters.PostAdapter
import com.mml.dummyapp_kotlin.databinding.FragmentHomeBinding
import com.mml.dummyapp_kotlin.models.Item
import com.mml.dummyapp_kotlin.util.*
import com.mml.dummyapp_kotlin.util.Constants.callAndBuildAdRequest
import com.mml.dummyapp_kotlin.viewmodels.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


private const val TAG = "HomeFragment"

@AndroidEntryPoint
class HomeFragment : Fragment(), TitleAndGridLayout, MenuProvider {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var postsAPiFlag = false

//    private var itemArrayList = arrayListOf<Item>()

    private var searchItemList = arrayListOf<Item>()

    private val postViewModel: PostViewModel by viewModels()

    private val titleLayoutManager: GridLayoutManager by lazy {
        GridLayoutManager(requireContext(), 2)
    }
    private val gridLayoutManager: GridLayoutManager by lazy {
        GridLayoutManager(requireContext(), 3)
    }

    private var linearLayoutManager: LinearLayoutManager? = null


    private lateinit var networkListener: NetworkListener

    private var _adapter: PostAdapter? = null
    private val adapter get() = _adapter!!

    private var isScrolling = false
    var currentItems = 0
    var totalItems: Int = 0
    var scrollOutItems: Int = 0

    private val KEY_RECYCLER_STATE = "recycler_state"
    private val mBundleRecyclerViewState by lazy { Bundle() }

    //    private var keyword: String? = null
    private lateinit var adView: AdView
    private lateinit var adRequest: AdRequest

    private val adSize: AdSize
        get() {
            val display = activity?.windowManager?.defaultDisplay
            val outMetrics = DisplayMetrics()
            display?.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.root.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                requireContext(),
                adWidth
            )
        }

//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        binding.adViewContainer.removeAllViews()
//
//        adView = AdView(requireContext())
//        binding.adViewContainer.addView(adView)
//        requestHomeBanner()
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        layoutManager = WrapContentLinearLayoutManager(
//            context,
//            wrapContentLinearLayoutManager.VERTICAL, false
//        )
        networkListener = NetworkListener()

    }


    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        adRequest = callAndBuildAdRequest(null)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val menuHost: MenuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.CREATED)


//
//        titleLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
//            override fun getSpanSize(position: Int): Int {
//                return when (position) {
//                    adapter.VIEW_TYPE_AD_GRID_LAYOUT -> 2
//                    else -> 1
//                }
//            }
//        }

//        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
//            override fun getSpanSize(position: Int): Int {
//                return when (position) {
//                    adapter.VIEW_TYPE_AD_GRID_LAYOUT -> 3
//                    else -> 1
//                }
//            }
//        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _adapter = PostAdapter(this)
        postViewModel.finalURL.postValue("${Constants.BASE_URL}?key=${Constants.API_KEY}")

//        adapter.setHasStableIds(true)
        adView = AdView(requireContext())
        binding.adViewContainer.addView(adView)
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"
        adView.setAdSize(adSize)

        binding.homeRecyclerView.apply {
            linearLayoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = this@HomeFragment.adapter
            layoutManager = this@HomeFragment.linearLayoutManager
            setHasFixedSize(true)
            this@HomeFragment.adapter.viewType = 0

        }

//        this.adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
//            override fun onChanged() {
//                super.onChanged()
//                binding.homeRecyclerView.scrollToPosition(0)
//            }
//
//        })


        postViewModel.readBackOnline.observe(viewLifecycleOwner) {
            postViewModel.backOnline = it
        }

        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY


//        binding.homeRecyclerView.recycledViewPool.setMaxRecycledViews(
//            2, 0
//        )


        lifecycleScope.launchWhenStarted {
            networkListener.checkNetworkAvailability(requireContext()).collect { stats ->
                Log.d(TAG, "networkListener: $stats")
                postViewModel.networkStats = stats
                postViewModel.showNetworkStats()
                if (stats && savedInstanceState == null) {
                    requestApiData()
                    requestHomeBanner()

                } else {
//
                    getPostsFromDB()
                }
            }
        }


        postViewModel.recyclerViewLayout.observe(viewLifecycleOwner) { layout ->

            linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//            Log.w(TAG, "getSavedLayout called")
            when (layout) {
                "cardLayout" -> {
//                    binding.loadMoreBtn.visibility = View.VISIBLE
//                    wrapContentLinearLayoutManager =
//
                    adapter.viewType = 0
                    binding.homeRecyclerView.layoutManager = linearLayoutManager
                    binding.homeRecyclerView.adapter = adapter

                }
                "cardMagazineLayout" -> {
//                    binding.loadMoreBtn.visibility = View.VISIBLE
                    binding.homeRecyclerView.layoutManager = linearLayoutManager
                    adapter.viewType = 1
                    binding.homeRecyclerView.adapter = adapter
                }
                "titleLayout" -> {
//                    binding.loadMoreBtn.visibility = View.GONE
                    binding.homeRecyclerView.layoutManager = titleLayoutManager
                    adapter.viewType = 2
                    binding.homeRecyclerView.adapter = adapter
                }
                "gridLayout" -> {
//                    binding.loadMoreBtn.visibility = View.GONE
                    binding.homeRecyclerView.layoutManager = gridLayoutManager
                    adapter.viewType = 3
                    binding.homeRecyclerView.adapter = adapter

                }
            }
        }

        binding.homeRecyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        isScrolling = true
                    }
                    binding.progressBar.visibility = View.GONE


                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (dy > 0) {
                        currentItems = linearLayoutManager!!.childCount
                        totalItems = adapter.itemCount
                        scrollOutItems = linearLayoutManager!!.findFirstVisibleItemPosition()
                        if (isScrolling && currentItems + scrollOutItems >= totalItems && postsAPiFlag) {
                            hideShimmerEffect()
                            postViewModel.getPosts()
                            isScrolling = false
//                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            })

        postViewModel.errorCode.observe(viewLifecycleOwner)
        { errorCode ->
            if (errorCode == 400) {
//                Snackbar.make(requireView(), R.string.lastPost, Snackbar.LENGTH_LONG).show()
                Toast.makeText(requireContext(), R.string.lastPost, Toast.LENGTH_LONG).show()
                binding.progressBar.visibility = View.GONE
            }
        }

    }


    override fun onResume() {
        super.onResume()
        val listState = mBundleRecyclerViewState.getParcelable<Parcelable>(KEY_RECYCLER_STATE)
        binding.homeRecyclerView.layoutManager?.onRestoreInstanceState(listState)
        adView.resume()
    }

    private fun getPostsFromDB() {

        showShimmerEffect()
        postViewModel.readAllPosts.observe(viewLifecycleOwner) { items ->
            if (items.isNotEmpty()) {
                hideShimmerEffect()
//                binding.loadMoreBtn.visibility = View.GONE

                adapter.submitList(items)

//                adapter.submitList(items)
                //adapter.notifyDataSetChanged()
            } else {
                noInternetConnectionLayout()
            }
        }
    }

    override fun onStop() {
        super.onStop()
//        mBundleRecyclerViewState = Bundle()
        val listState: Parcelable? = binding.homeRecyclerView.layoutManager?.onSaveInstanceState()
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState)
    }


    private fun requestApiData() {
//        showShimmerEffect()
//        Log.d(TAG, "requestApiData: called")
        postViewModel.getPosts()
        postViewModel.postsResponse.observe(viewLifecycleOwner) { response ->

            postsAPiFlag = true
            when (response) {
                is NetworkResult.Success -> {

                    response.data?.let {
//                        adapter.differ.currentList.addAll(it.items)
//                        adapter.differ.submitList(null)

//                        itemArrayList.addAll(it.items)
                        binding.progressBar.visibility = View.GONE

                        Log.d(TAG, "requestApiData: ${it.items.get(0).title}")

                        adapter.submitList(it.items.toList())
                        hideShimmerEffect()

                    }

                }

                is NetworkResult.Error -> {
                    hideShimmerEffect()
                    binding.progressBar.visibility = View.GONE
                    //                    loadDataFromCache()
//                    Toast.makeText(
//                        requireContext(),
//                        response.message.toString(),
//                        Toast.LENGTH_LONG
//                    ).show()

                    Log.e(TAG, "Error response ${response.message.toString()}")
//                    Log.e(TAG, response.message.toString())
                }

                is NetworkResult.Loading -> {
//                    if (postViewModel.recyclerViewLayout.value == "titleLayout" ||
//                        postViewModel.recyclerViewLayout.value == "gridLayout"
//                    ) {
//                        hideShimmerEffect()
//                    } else {
//                        showShimmerEffect()


//                    }
//                    if (binding.homeRecyclerView.canScrollVertically(-1)) {
//                        hideShimmerEffect()
//                        lifecycleScope.launch {
//                            delay(1000)
//                        }
//                        binding.progressBar.visibility = View.VISIBLE
//                    } else {
//                        showShimmerEffect()
//                    }

//                    showShimmerEffect()
//
//                   lifecycleScope.launch {
//                       delay(1000)
//                   }

                    binding.progressBar.visibility = View.VISIBLE
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
                        adapter.viewType = 0
                        binding.homeRecyclerView.layoutManager = linearLayoutManager
                        binding.homeRecyclerView.adapter = adapter
                        postViewModel.saveRecyclerViewLayout("cardLayout")
                    }
                    1 -> {
                        adapter.viewType = 1
                        binding.homeRecyclerView.layoutManager = linearLayoutManager
                        binding.homeRecyclerView.adapter = adapter
                        postViewModel.saveRecyclerViewLayout("cardMagazineLayout")

                    }
                    2 -> {
                        adapter.viewType = 2
                        binding.homeRecyclerView.layoutManager = titleLayoutManager
                        binding.homeRecyclerView.adapter = adapter
                        postViewModel.saveRecyclerViewLayout("titleLayout")
                    }
                    3 -> {
                        adapter.viewType = 3
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

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.main, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//
//
//    }


    private fun requestHomeBanner() {

        adView.adListener = object : AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "onAdFailedToLoad: ${adError.cause.toString()}")
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }
        // Create an ad request.


        adView.loadAd(adRequest)

        // Start loading the ad in the background.


    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.change_layout) {
//            changeAndSaveLayout()
//            return true
//        }
//        return super.onOptionsItemSelected(item)
//    }


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
//        _adapter?.isDestroyed = true
        adView.destroy()
        binding.adViewContainer.removeAllViews()
        this.linearLayoutManager = null
        binding.homeRecyclerView.removeAllViews()
        _adapter = null
        _binding = null

    }

//    override fun onDetach() {
//        super.onDetach()
//
//    }


    /** Called when leaving the activity */
    override fun onPause() {
        adView.pause()
        super.onPause()
    }


    override fun tellFragmentToGetItems() {
        if (postViewModel.recyclerViewLayout.value.equals("titleLayout")
            || postViewModel.recyclerViewLayout.value.equals("gridLayout")
        ) {
            hideShimmerEffect()
            postViewModel.getPosts()
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main, menu)
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
//                this@HomeFragment.keyword = keyword
                requestSearchApi(keyword)

                return false

            }


            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
        searchView.setOnCloseListener {
            showShimmerEffect()
            if (Utils.hasInternetConnection(requireContext())) {
//                if (keyword.isNullOrEmpty()) {
//                    return@setOnCloseListener false
//                } else {
//                Log.d(TAG, "setOnCloseListener: called $keyword")
//                    keyword?.let { postViewModel.getItemsBySearch(it).cancel() }
//                postViewModel.getItemsBySearch().cancel()
//                    adapter.differ.currentList.toMutableList().clear()
//                adapter.differ.submitList(null)
                searchItemList.clear()
                adapter.clearList()
//                    adapter.clearDifferList()
//                    postsAPiFlag = false
                postViewModel.finalURL.postValue(null)
//                postViewModel.token.value = null

//                binding.progressBar.visibility = View.GONE

                requestApiData()
                binding.progressBar.visibility = View.GONE
//


                Log.d(TAG, "onCreateMenu: ${postViewModel.finalURL.value.toString()}")
//
//                    adapter.notifyDataSetChanged()
//                }
            } else {
                Log.d(TAG, "setOnCloseListener: called")
                lifecycleScope.launch {
//                    adapter.differ.currentList.clear()
                }
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
    }

    private fun requestSearchApi(keyword: String) {
        if (Utils.hasInternetConnection(requireContext())) {

//            postViewModel.getPosts().cancel()
            showShimmerEffect()

            postViewModel.getItemsBySearch()
            postViewModel.searchedPostsResponse.observe(this) { response ->
                when (response) {
                    is NetworkResult.Success -> {
                        //                                adapter.differ.currentList.clear()

                        searchItemList.clear()
                        binding.progressBar.visibility = View.GONE


                        searchItemList.addAll(response.data?.items?.filter {
                            it.title.contains(keyword) || it.content.contains(keyword)
                        } as ArrayList<Item>)



                        Log.d(TAG, "requestSearchApi: test size ${searchItemList.size}")
//                        Log.d(
//                            TAG,
//                            "requestSearchApi: test size ${binding.homeRecyclerView.layoutManager?.itemCount}"
//                        )


                        if (searchItemList.isEmpty()) {
//                                adapter.differ.submitList(null)
                            Toast.makeText(
                                requireContext(),
                                "The search word was not found in any post",
                                Toast.LENGTH_SHORT
                            ).show()
                            hideShimmerEffect()

                        } else {
                            postsAPiFlag = false
                            adapter.clearList()
//                            itemArrayList.clear()

//                            searchItemList.clear()
//                            searchItemList.addAll(test)
                            hideShimmerEffect()
                            Log.d(
                                TAG, "requestSearchApi: searchItemList ${searchItemList[0].title}"
                            )
                            adapter.submitList(searchItemList)
//                            binding.homeRecyclerView.scrollToPosition(0)
                        }

                        //


                    }

                    is NetworkResult.Error -> {
                        hideShimmerEffect()
                        binding.progressBar.visibility = View.GONE
                        //                    loadDataFromCache()
                        //                                Toast.makeText(
                        //                                    requireContext(),
                        //                                    response.toString(),
                        //                                    Toast.LENGTH_LONG
                        //                                ).show()
                        Toast.makeText(
                            requireContext(),
                            response.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(TAG, "onQueryTextSubmit: $response")

                    }

                    is NetworkResult.Loading -> {
                        showShimmerEffect()
                        binding.progressBar.visibility = View.VISIBLE

                    }
                }
            }
        } else {
            postsAPiFlag = false
            postViewModel.getItemsBySearchInDB(keyword)
            postViewModel.postsBySearchInDB.observe(viewLifecycleOwner) { items ->
                if (items.isNotEmpty()) {
                    hideShimmerEffect()
                    binding.progressBar.visibility = View.GONE

                    adapter.submitList(items)
                    //                            adapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return if (menuItem.itemId == R.id.change_layout) {
            changeAndSaveLayout()
            true
        } else false
    }


}