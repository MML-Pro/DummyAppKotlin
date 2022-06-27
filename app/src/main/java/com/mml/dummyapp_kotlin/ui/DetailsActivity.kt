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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ShareActionProvider
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.mml.dummyapp_kotlin.R
import com.mml.dummyapp_kotlin.data.database.FavoritesEntity
import com.mml.dummyapp_kotlin.databinding.ActivityDetailsBinding
import com.mml.dummyapp_kotlin.models.Item
import com.mml.dummyapp_kotlin.util.CustomTabsHelper
import com.mml.dummyapp_kotlin.util.PicassoImageGetter
import com.mml.dummyapp_kotlin.viewmodels.PostViewModel
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import org.jsoup.Jsoup
import java.util.*

@AndroidEntryPoint
class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private lateinit var url: String
    private lateinit var title: String
    private lateinit var content: String
    var youtubeThumbnailImageSrc: String? = null
    var youTubeLink: String? = null
    var youtubeThumbnailImageSetVisibility = 0
    private lateinit var postViewModel: PostViewModel
    private lateinit var postItem: Item
    private var postFavoritesSaved = false
    private var postFavoritesSavedId = 0
    private var menuItem: MenuItem? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.progressBar.visibility = View.VISIBLE
        postViewModel = ViewModelProvider(this)[PostViewModel::class.java]
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


        val document = Jsoup.parse(postItem.content)
        //                    final Elements elements = document.select("img");
        val element = document.body()
        for (e in element.getElementsByClass("YOUTUBE-iframe-video")) {
            youtubeThumbnailImageSrc = e.attr("data-thumbnail-src")
            youTubeLink = e.attr("src")
            Log.e("YouTube thumbnail", youtubeThumbnailImageSrc!!)
            Log.e("Youtube link", youTubeLink!!)
        }
        if (youtubeThumbnailImageSrc == null) {
            youtubeThumbnailImageSetVisibility = 8
        }
        url = postItem.url
        title = postItem.title
        content = postItem.content


        //        blogImage = (ImageView) findViewById(R.id.blogImage);
        binding.apply {
            titleTextView.text = title
            blogContent.movementMethod = LinkMovementMethod.getInstance()
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            binding.fab.bringToFront()
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
        binding.youtubeThumbnailImage.visibility = youtubeThumbnailImageSetVisibility
        binding.youtubeThumbnailImage.adjustViewBounds = true
        Picasso.get().load(youtubeThumbnailImageSrc).into(binding.youtubeThumbnailImage)
        binding.youtubeThumbnailImage.setOnClickListener { view1 ->
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

    private fun checkSavedFavoritesItems(menuItem: MenuItem?) {
        postViewModel.readFavoritePosts.observe(this) { favoritesEntity ->
            try {
                for (savedPost in favoritesEntity) {
                    if (savedPost.item.id == postItem.id) {
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
        if (menuItem.itemId == R.id.action_add_to_favorites && !postFavoritesSaved) {
            saveTogFavorites(menuItem)
        } else if (menuItem.itemId == R.id.action_add_to_favorites && postFavoritesSaved) {
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
        val favoritesEntity = FavoritesEntity(0, postItem)
        postViewModel.insertFavorites(favoritesEntity)
        menuItem.setIcon(R.drawable.ic_favorite)
        Snackbar.make(binding.root, "Saved", Snackbar.LENGTH_LONG).show()
        postFavoritesSaved = true
    }

    private fun removePostFromFavorites(menuItem: MenuItem) {
        val favoritesEntity = FavoritesEntity(postFavoritesSavedId, postItem)
        Log.d(
            TAG,
            "checkSavedFavoritesItems: $postFavoritesSavedId"
        )
        postViewModel.deleteFavoritePost(favoritesEntity)
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