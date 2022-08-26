package com.mml.dummyapp_kotlin.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ShareActionProvider
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.MenuItemCompat
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.mml.dummyapp_kotlin.R
import com.mml.dummyapp_kotlin.data.database.FavoritesEntity
import com.mml.dummyapp_kotlin.databinding.ActivityDetailsBinding
import com.mml.dummyapp_kotlin.models.Item
import com.mml.dummyapp_kotlin.util.Constants
import com.mml.dummyapp_kotlin.util.CustomTabsHelper
import com.mml.dummyapp_kotlin.util.PicassoImageGetter
import com.mml.dummyapp_kotlin.viewmodels.PostViewModel
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import org.jsoup.Jsoup

@AndroidEntryPoint
class DetailsActivity : AppCompatActivity() {
    private var _binding: ActivityDetailsBinding? = null
    private val binding get() = _binding!!
    private var url: String? = null
    private var title: String? = null
    private var content: String? = null
    private var youtubeThumbnailImageSrc: String? = null
    private var youTubeLink: String? = null
    private var youtubeThumbnailImageSetVisibility: Int? = null
    private val postViewModel: PostViewModel by viewModels()
    private var postItem: Item? = null
    private var postFavoritesSaved: Boolean? = null
    private var postFavoritesSavedId: Int? = null
    private var menuItem: MenuItem? = null


    private lateinit var adRequest: AdRequest


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.progressBar.visibility = View.VISIBLE
//        postViewModel = ViewModelProvider(this)[PostViewModel::class.java]
        Log.d(
            TAG,
            "onCreate checkSavedFavoritesItems: $postFavoritesSavedId"
        )






        postItem = if (intent.extras!!.containsKey("postItem")) {
            intent.getParcelableExtra("postItem")!!
        } else {
            val favoriteItem: FavoritesEntity = intent.getParcelableExtra("favoriteItem")!!
            favoriteItem.item
        }


        val document = postItem?.content?.let { Jsoup.parse(it) }
        //                    final Elements elements = document.select("img");
        val element = document?.body()
        for (e in element?.getElementsByClass("YOUTUBE-iframe-video")!!) {
            youtubeThumbnailImageSrc = e.attr("data-thumbnail-src")
            youTubeLink = e.attr("src")
            Log.e("YouTube thumbnail", youtubeThumbnailImageSrc!!)
            Log.e("Youtube link", youTubeLink!!)
        }
        if (youtubeThumbnailImageSrc == null) {
            youtubeThumbnailImageSetVisibility = 8
        }
        url = postItem?.url
        title = postItem?.title
        content = postItem?.content


        //        blogImage = (ImageView) findViewById(R.id.blogImage);
        binding.apply {
            titleTextView.text = title
            blogContent.movementMethod = LinkMovementMethod.getInstance()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                //   Log.d("ScrollView","scrollX_"+scrollX+"_scrollY_"+scrollY+"_oldScrollX_"+oldScrollX+"_oldScrollY_"+oldScrollY);
                if (scrollY > 0 && binding.fab.isShown) {
                    binding.fab.hide()
                } else if (scrollY < 22) {
                    binding.fab.show()
                }
            }
        } else {
            binding.scrollView.viewTreeObserver.addOnScrollChangedListener {
                val mScrollY: Int = binding.scrollView.scrollY
                if (mScrollY > 0 && binding.fab.isShown) {
                    binding.fab.hide()
                } else if (mScrollY < 22) {
                    binding.fab.show()
                }
            }
        }
        binding.fab.setOnClickListener {
            val shareContent = """
                $title
                $url
                """.trimIndent()
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            try {
                startActivity(Intent.createChooser(shareIntent, title))
            } catch (exception: Exception) {
                Log.e(TAG, "onCreate: $exception")
            }
        }


        //       String imageSrc = getIntent().getStringExtra("blogImage");
        //       Glide.with(getApplicationContext()).load(imageSrc).into(blogImage);
        binding.youtubeThumbnailImage.visibility = youtubeThumbnailImageSetVisibility!!
        binding.youtubeThumbnailImage.adjustViewBounds = true
        Picasso.get().load(youtubeThumbnailImageSrc).into(binding.youtubeThumbnailImage)
        binding.youtubeThumbnailImage.setOnClickListener {
            val youTube = Intent(Intent.ACTION_VIEW, Uri.parse(youTubeLink))
            startActivity(youTube)
        }
        val imageGetter = PicassoImageGetter(binding.blogContent, this)
        val html: Spannable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY, imageGetter, null) as Spannable
        } else {
            Html.fromHtml(content, imageGetter, null) as Spannable
        }
        binding.blogContent.text = html
        binding.visitSite.setOnClickListener {
            openCustomTab(
                this,
                Uri.parse(url)
            )
        }
        binding.progressBar.visibility = View.GONE


    }

    fun clearObjects() {
        url = null
        title = null
        content = null
        youtubeThumbnailImageSrc = null
        youTubeLink = null
        youtubeThumbnailImageSetVisibility = null
        postItem = null
        postFavoritesSaved = null
        postFavoritesSavedId = null
        menuItem = null
    }

    override fun onBackPressed() {
        super.onBackPressed()
        clearObjects()
        this.finish()
    }

    private fun loadAndShowInterstitial() {

        adRequest = Constants.callAndBuildAdRequest(null)

        if (clickNumber == 3) {

            InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        adError.toString().let { Log.d(TAG, it) }
//                        loadAndShowInterstitial(adRequest)
//                        loadAndShowInterstitial()
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d(TAG, "Ad was loaded.")


                        interstitialAd.show(this@DetailsActivity)

                        interstitialAd.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdClicked() {
                                    // Called when a click is recorded for an ad.
                                    Log.d(TAG, "Ad was clicked.")
                                    clickNumber = 0
                                }

                                override fun onAdDismissedFullScreenContent() {
                                    // Called when ad is dismissed.
                                    Log.d(TAG, "Ad dismissed fullscreen content.")
                                    clickNumber = 0
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    // Called when ad fails to show.
                                    Log.e(TAG, "Ad failed to show fullscreen content.")
                                    clickNumber = 0

                                }

                                override fun onAdImpression() {
                                    // Called when an impression is recorded for an ad.
                                    Log.d(TAG, "Ad recorded an impression.")
                                    clickNumber = 0
                                }

                                override fun onAdShowedFullScreenContent() {
                                    // Called when ad is shown.
                                    Log.d(TAG, "Ad showed fullscreen content.")
                                    clickNumber = 0
                                }
                            }


                    }
                })

        } else {
            clickNumber = clickNumber.plus(1)
            Log.d(TAG, "The interstitial ad wasn't ready yet. $clickNumber")
        }


    }

    override fun onStart() {
        super.onStart()
        loadAndShowInterstitial()

    }


//    override fun onPause() {
//        super.onPause()
//        this@DetailsActivity.finish()
//    }

//    protected fun finalize() {
//        // finalization logic
//        clickNumber = 0
//    }

    //
//    override fun onStop() {
//        super.onStop()
    override fun onDestroy() {
        super.onDestroy()
//        adRequest = null
        _binding = null
    }
//        this@DetailsActivity.finish()
//    }

    private fun checkSavedFavoritesItems(menuItem: MenuItem?) {
        postViewModel.readFavoritePosts.observe(this) { favoritesEntity ->
            try {
                for (savedPost in favoritesEntity) {
                    if (savedPost.item.id == postItem?.id) {
                        menuItem?.setIcon(R.drawable.ic_favorite)
                        postFavoritesSavedId = savedPost.id
                        Log.d(
                            TAG,
                            "checkSavedFavoritesItems: $postFavoritesSavedId"
                        )
                        postFavoritesSaved = true
                    }
                }
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "checkSavedFavoritesItems: " + exception.message
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.details_menu, menu)
        menuItem = menu.findItem(R.id.action_add_to_favorites)
        checkSavedFavoritesItems(menuItem)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.action_add_to_favorites && !postFavoritesSaved!!) {
            saveTogFavorites(menuItem)
        } else if (menuItem.itemId == R.id.action_add_to_favorites && postFavoritesSaved == true) {
            removePostFromFavorites(menuItem)
        } else if (menuItem.itemId == R.id.action_share) {
            val shareActionProvider =
                MenuItemCompat.getActionProvider(menuItem) as ShareActionProvider
            shareActionProvider.setShareIntent(createShareIntent())
            return true
        } else if (menuItem.itemId == R.id.copyTheLink) {
            val clipboardManager = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("link", url)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this, getString(R.string.linkCopied), Toast.LENGTH_LONG).show()
            return true
        }
        return super.onOptionsItemSelected(menuItem)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun saveTogFavorites(menuItem: MenuItem) {
        val favoritesEntity = postItem?.let { FavoritesEntity(0, it) }
        if (favoritesEntity != null) {
            postViewModel.insertFavorites(favoritesEntity)
        }
        menuItem.setIcon(R.drawable.ic_favorite)
        Snackbar.make(binding.root, "Saved", Snackbar.LENGTH_LONG).show()
        postFavoritesSaved = true
    }

    private fun removePostFromFavorites(menuItem: MenuItem) {
        val favoritesEntity = postFavoritesSavedId?.let {
            postItem?.let { it1 ->
                FavoritesEntity(
                    it,
                    it1
                )
            }
        }
        Log.d(
            TAG,
            "checkSavedFavoritesItems: $postFavoritesSavedId"
        )
        if (favoritesEntity != null) {
            postViewModel.deleteFavoritePost(favoritesEntity)
        }
        menuItem.setIcon(R.drawable.ic_favorite_border)
        Snackbar.make(
            binding.root,
            "Post deleted from favorites", Snackbar.LENGTH_LONG
        ).show()
        postFavoritesSaved = false
    }

    private fun createShareIntent(): Intent {
        val shareContent = """
            $title
            $url
            """.trimIndent()
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent)
        // shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return shareIntent
    }

    companion object {
        @JvmStatic
        private var clickNumber: Int = 0

        private const val TAG = "DetailsActivity"
        fun openCustomTab(
            context: Context?,
            uri: Uri?
        ) {
            // Here is a method that returns the chrome package name
            val packageName: String? = context?.let { CustomTabsHelper.getPackageNameToUse(it) }
            val builder = CustomTabsIntent.Builder()
            val mCustomTabsIntent: CustomTabsIntent = builder
                .setShowTitle(true)
                .build()
            // builder.setToolbarColor(ContextCompat.getColor(appCompatActivity, R.color.colorPrimary));
            if (packageName != null) {
                mCustomTabsIntent.intent.setPackage(packageName)
            }
            mCustomTabsIntent.launchUrl(context!!, uri!!)
        }
    }
}