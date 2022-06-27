package com.mml.dummyapp_kotlin.adapters

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import com.mml.dummyapp_kotlin.R
import com.mml.dummyapp_kotlin.data.database.FavoritesEntity
import com.mml.dummyapp_kotlin.databinding.CardLayoutBinding
import com.mml.dummyapp_kotlin.databinding.CardMagazineBinding
import com.mml.dummyapp_kotlin.ui.DetailsActivity
import com.mml.dummyapp_kotlin.ui.HomeFragment
import com.mml.dummyapp_kotlin.util.MyImageview
import com.mml.dummyapp_kotlin.viewmodels.PostViewModel
import org.jsoup.Jsoup
import org.ocpsoft.prettytime.PrettyTime
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class FavoritesPostAdapter(
    private val fragmentActivity: FragmentActivity,
    fragment: Fragment,
    postViewModel: PostViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var favoritesList: List<FavoritesEntity>
    private var rootView: View? = null
    private var viewType = 0
    val fragment: Fragment
    private val postViewModel: PostViewModel
    private var mActionMode: ActionMode? = null
    private var multiSelection = false

    //    private int selectedPostPosition ;
    private val selectedPosts: MutableList<FavoritesEntity> = ArrayList<FavoritesEntity>()
    private val myViewHolders: MutableList<RecyclerView.ViewHolder> = ArrayList()
    fun setViewType(viewType: Int) {
        this.viewType = viewType
        notifyDataSetChanged()
    }

    fun addData(data: List<FavoritesEntity>) {
        favoritesList = data
    }

    private fun getViewType(): Int {
        return viewType
    }

    private val mActionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
            mActionMode = actionMode
            actionMode.menuInflater.inflate(R.menu.favorites_contextual_menu, menu)
            applyStatusBarColor(R.color.contextualStatusBarColor)
            return true
        }

        override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
            return true
        }

        override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
            if (menuItem.itemId == R.id.delete_favorites_post) {
                for (favoritesEntity in selectedPosts) {
                    postViewModel.deleteFavoritePost(favoritesEntity)
                }
                Log.d(TAG, "onActionItemClicked: " + favoritesList.size)
                Log.d(
                    TAG,
                    "onActionItemClicked: $itemCount"
                )
                showSnackBar(selectedPosts.size.toString() + " post/s deleted")
                multiSelection = false
                selectedPosts.clear()
                mActionMode!!.finish()
            }
            return true
        }

        override fun onDestroyActionMode(actionMode: ActionMode) {
            for (holder in myViewHolders) {
                changePostStyle(holder, R.color.cardBackgroundColor, R.color.strokeColor)
            }
            multiSelection = false
            selectedPosts.clear()
            applyStatusBarColor(R.color.statusBarColor)
        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(rootView!!, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun applyStatusBarColor(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fragmentActivity.window.statusBarColor = ContextCompat.getColor(fragmentActivity, color)
        }
    }

    private fun applySelection(
        holder: RecyclerView.ViewHolder,
        currentSelectedPost: FavoritesEntity
    ) {
        if (selectedPosts.contains(currentSelectedPost)) {
            selectedPosts.remove(currentSelectedPost)
            changePostStyle(holder, R.color.cardBackgroundColor, R.color.strokeColor)
        } else {
            selectedPosts.add(currentSelectedPost)
            val currentNightMode = (holder.itemView.resources.configuration.uiMode
                    and Configuration.UI_MODE_NIGHT_MASK)
            when (currentNightMode) {
                Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED ->                     // We don't know what mode we're in, assume notnight
                    // Night mode is not active, we're in day time
                    changePostStyle(holder, R.color.cardBackgroundLightColor, R.color.primaryColor)
                Configuration.UI_MODE_NIGHT_YES ->                     // Night mode is active, we're at night!
                    changePostStyle(holder, R.color.deepPurple, R.color.secondaryLightColor)
            }
        }
        applyActionModeTitle()
    }

    private fun changePostStyle(
        holder: RecyclerView.ViewHolder,
        backgroundColor: Int,
        strokeColor: Int
    ) {
        if (holder is CardViewHolder) {
            holder.cardLayoutBinding.secondLinearLayout.setBackgroundColor(
                ContextCompat.getColor(
                    fragmentActivity.applicationContext,
                    backgroundColor
                )
            )
            holder.cardLayoutBinding.cardView.strokeColor = ContextCompat.getColor(
                fragmentActivity.applicationContext,
                strokeColor
            )
        }
    }

    private fun applyActionModeTitle() {
        when (selectedPosts.size) {
            0 -> {
                mActionMode!!.finish()
                multiSelection = false
            }
            1 -> {
                mActionMode!!.title = selectedPosts.size.toString() + " item selected"
            }
            else -> {
                mActionMode!!.title = selectedPosts.size.toString() + " items selected"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(fragmentActivity)
        val view: View
        return when (this.viewType) {
            CARD -> {
                val cardLayoutBinding: CardLayoutBinding =
                    CardLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                CardViewHolder(cardLayoutBinding)
            }
            CARD_MAGAZINE -> {
                val cardMagazineBinding: CardMagazineBinding =
                    CardMagazineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                CardMagazineViewHolder(cardMagazineBinding)
            }
            TITLE -> {
                view = if (SDK_VERSION < Build.VERSION_CODES.LOLLIPOP) {
                    inflater.inflate(R.layout.title_layout_v15, parent, false)
                } else {
                    inflater.inflate(R.layout.title_layout, parent, false)
                }
                TitleViewHolder(view)
            }
            else -> {
                view = if (SDK_VERSION < Build.VERSION_CODES.LOLLIPOP) {
                    inflater.inflate(R.layout.grid_layout_v15, parent, false)
                } else {
                    inflater.inflate(R.layout.grid_layout, parent, false)
                }
                GridViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        myViewHolders.add(holder)
        rootView = holder.itemView.rootView
        //        selectedPostPosition = position;
        val itemType = getViewType()
        val favoriteItem: FavoritesEntity = favoritesList[position]
        val document = Jsoup.parse(favoriteItem.item.content)
        val elements = document.select("img")
        val intent = Intent(fragmentActivity.applicationContext, DetailsActivity::class.java)
        when (itemType) {
            CARD -> if (holder is CardViewHolder) {
                holder.bind(favoriteItem)
                holder.itemView.setOnClickListener { view: View ->
                    if (multiSelection) {
                        applySelection(holder, favoriteItem)
                    } else {
                        if (mActionMode != null) {
                            mActionMode!!.finish()
                        }
                        intent.putExtra("favoriteItem", favoriteItem)
                        view.context.startActivity(intent)
                    }
                }
                holder.itemView.setOnLongClickListener {
                    if (!multiSelection) {
                        multiSelection = true
                        fragmentActivity.startActionMode(mActionModeCallback)
                        applySelection(holder, favoriteItem)
                        return@setOnLongClickListener true
                    } else {
                        applySelection(holder, favoriteItem)
                        return@setOnLongClickListener true
                    }
                }
            }
            CARD_MAGAZINE -> if (holder is CardMagazineViewHolder) {
                holder.bind(favoriteItem)
                holder.itemView.setOnClickListener { view: View ->
                    if (multiSelection) {
                        applySelection(holder, favoriteItem)
                    } else {
                        if (mActionMode != null) {
                            mActionMode!!.finish()
                        }
                        intent.putExtra("favoriteItem", favoriteItem)
                        view.context.startActivity(intent)
                    }
                }
                holder.itemView.setOnLongClickListener {
                    if (!multiSelection) {
                        multiSelection = true
                        fragmentActivity.startActionMode(mActionModeCallback)
                        applySelection(holder, favoriteItem)
                        return@setOnLongClickListener true
                    } else {
                        applySelection(holder, favoriteItem)
                        return@setOnLongClickListener true
                    }
                }
            }
            TITLE -> if (holder is TitleViewHolder) {
                holder.postTitle.text = favoriteItem.item.title
                Log.d("TITLE", "title layout called")
                try {
                    Log.e("IMAGE", elements[0].attr("src"))
                    Glide.with(fragmentActivity).load(elements[0].attr("src"))
                        .transition(DrawableTransitionOptions.withCrossFade(600))
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.no_image)
                        .into(holder.postImage)
                } catch (e: IndexOutOfBoundsException) {
                    holder.postImage.setImageResource(R.drawable.no_image)
                    Log.e(TAG, e.toString())
                }
//                if (position == itemCount - 1) if (fragment is HomeFragment) {
//                    postViewModel.getPosts()
//                } else {
//                    postViewModel.getPostListByLabel()
//                }
                holder.itemView.setOnClickListener { view: View ->
                    if (multiSelection) {
                        applySelection(holder, favoriteItem)
                    } else {
                        if (mActionMode != null) {
                            mActionMode!!.finish()
                        }
                        intent.putExtra("favoriteItem", favoriteItem)
                        view.context.startActivity(intent)
                    }
                }
                holder.itemView.setOnLongClickListener {
                    if (!multiSelection) {
                        multiSelection = true
                        fragmentActivity.startActionMode(mActionModeCallback)
                        applySelection(holder, favoriteItem)
                        return@setOnLongClickListener true
                    } else {
                        applySelection(holder, favoriteItem)
                        return@setOnLongClickListener true
                    }
                }
            }
            GRID -> if (holder is GridViewHolder) {
                holder.postTitle.text = favoriteItem.item.title
                try {
                    Log.e("IMAGE", elements[0].attr("src"))
                    Glide.with(fragmentActivity).load(elements[0].attr("src"))
                        .transition(DrawableTransitionOptions.withCrossFade(600))
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.no_image)
                        .into(holder.postImage)
                } catch (e: IndexOutOfBoundsException) {
                    holder.postImage.setImageResource(R.drawable.no_image)
                    Log.e(TAG, e.toString())
                }
//                if (position == itemCount - 1) if (fragment is HomeFragment) {
//                    postViewModel.getPosts()
//                } else {
//                    postViewModel.getPostListByLabel()
//                }

                holder.itemView.setOnClickListener { view: View ->
                    if (multiSelection) {
                        applySelection(holder, favoriteItem)
                    } else {
                        if (mActionMode != null) {
                            mActionMode!!.finish()
                        }
                        intent.putExtra("favoriteItem", favoriteItem)
                        view.context.startActivity(intent)
                    }
                }
                holder.itemView.setOnLongClickListener {
                    if (!multiSelection) {
                        multiSelection = true
                        fragmentActivity.startActionMode(mActionModeCallback)
                        applySelection(holder, favoriteItem)
                        return@setOnLongClickListener true
                    } else {
                        applySelection(holder, favoriteItem)
                        return@setOnLongClickListener true
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return favoritesList.size
    }

    class CardViewHolder(binding: CardLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val cardLayoutBinding: CardLayoutBinding
        val context: Context
        fun bind(favoriteItem: FavoritesEntity) {
            val document = Jsoup.parse(favoriteItem.item.content)
            val elements = document.select("img")

//        Log.e("IMAGE", document.getAllElements().select("img").get(0).attr("src"));
            var date = Date()
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
            cardLayoutBinding.postTitle.text = favoriteItem.item.title
            try {
                Log.e("IMAGE", elements[0].attr("src"))
                Glide.with(context).load(elements[0].attr("src"))
                    .transition(DrawableTransitionOptions.withCrossFade(600))
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.no_image)
                    .into(cardLayoutBinding.postImage)
            } catch (e: IndexOutOfBoundsException) {
                cardLayoutBinding.postImage.setImageResource(R.drawable.no_image)
                Log.e(TAG, e.toString())
            }
            cardLayoutBinding.postDescription.text = document.text()
            try {
                date = format.parse(favoriteItem.item.published) as Date
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            val prettyTime = PrettyTime()
            cardLayoutBinding.postDate.text = prettyTime.format(date)
        }

        init {
            cardLayoutBinding = binding
            context = cardLayoutBinding.root.context
        }
    }

    class CardMagazineViewHolder(binding: CardMagazineBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val cardMagazineBinding: CardMagazineBinding
        val context: Context
        fun bind(favoriteItem: FavoritesEntity) {
            val document = Jsoup.parse(favoriteItem.item.content)
            val elements = document.select("img")
            var date = Date()
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())


//        Log.e("IMAGE", document.getAllElements().select("img").get(0).attr("src"));
            cardMagazineBinding.postTitle.text = favoriteItem.item.title
            try {
                Log.e("IMAGE", elements[0].attr("src"))
                Glide.with(context).load(elements[0].attr("src"))
                    .transition(DrawableTransitionOptions.withCrossFade(600))
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.no_image)
                    .into(cardMagazineBinding.postImage)
            } catch (e: IndexOutOfBoundsException) {
                cardMagazineBinding.postImage.setImageResource(R.drawable.no_image)
                Log.e(TAG, e.toString())
            }
            try {
                date = format.parse(favoriteItem.item.published) as Date
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            val prettyTime = PrettyTime()
            cardMagazineBinding.postDate.text = prettyTime.format(date)
        }

        init {
            cardMagazineBinding = binding
            context = cardMagazineBinding.root.context
        }
    }

    class TitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var postTitle: TextView
        var postImage: MyImageview

        init {
            postTitle = itemView.findViewById(R.id.postTitle)
            postImage = itemView.findViewById(R.id.postImage)
        }
    }

    class GridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var postTitle: TextView
        var postImage: MyImageview

        init {
            postTitle = itemView.findViewById(R.id.postTitle)
            postImage = itemView.findViewById(R.id.postImage)
        }
    }

    companion object {
        private const val CARD = 0
        private const val CARD_MAGAZINE = 1
        private const val TITLE = 2
        private const val GRID = 3
        private val SDK_VERSION = Build.VERSION.SDK_INT
        const val TAG = "POST ADAPTER"
    }

    init {
        favoritesList = ArrayList<FavoritesEntity>()
        this.fragment = fragment
        this.postViewModel = postViewModel
    }
}
