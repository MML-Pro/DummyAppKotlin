package com.mml.dummyapp_kotlin.adapters

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.mml.dummyapp_kotlin.R
import com.mml.dummyapp_kotlin.databinding.CardLayoutBinding
import com.mml.dummyapp_kotlin.databinding.CardMagazineBinding
import com.mml.dummyapp_kotlin.models.Item
import com.mml.dummyapp_kotlin.ui.DetailsActivity
import com.mml.dummyapp_kotlin.util.MyImageview
import com.mml.dummyapp_kotlin.util.TitleAndGridLayout
import org.jsoup.Jsoup
import org.ocpsoft.prettytime.PrettyTime
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private val context: Context,
    items: List<Item>, private val titleAndGridLayout: TitleAndGridLayout
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items: List<Item>
    private var viewType = 0
//    val fragment: Fragment
//    private val postViewModel: PostViewModel

    fun setViewType(viewType: Int) {
        this.viewType = viewType
        notifyDataSetChanged()
    }

    private fun getViewType(): Int {
        return viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
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
        val itemType = getViewType()
        val item: Item = items[position]
        val document = Jsoup.parse(item.content)
        val elements = document.select("img")
        val intent = Intent(context, DetailsActivity::class.java)
        when (itemType) {
            CARD -> if (holder is CardViewHolder) {
                holder.bind(item)
                holder.itemView.setOnClickListener { view: View ->
                    intent.putExtra("postItem", item)
                    view.context.startActivity(intent)
                }
            }
            CARD_MAGAZINE -> if (holder is CardMagazineViewHolder) {
                holder.bind(item)
                holder.itemView.setOnClickListener { view: View ->
                    intent.putExtra("postItem", item)
                    view.context.startActivity(intent)
                }
            }
            TITLE -> if (holder is TitleViewHolder) {
                holder.postTitle.text = item.title
//                Log.d("TITLE", "title layout called")
                try {
//                    Log.e("IMAGE", elements[0].attr("src"))
                    Glide.with(context).load(elements[0].attr("src"))
                        .transition(DrawableTransitionOptions.withCrossFade(600))
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.no_image)
                        .into(holder.postImage)
                } catch (e: IndexOutOfBoundsException) {
                    holder.postImage.setImageResource(R.drawable.no_image)
//                    Log.e(TAG, e.toString())
                }
                if (position == itemCount - 1)
                    titleAndGridLayout.tellFragmentToGetItems("titleLayout")
//                } else {
//                    postViewModel.getPostListByLabel()
//                }
                holder.itemView.setOnClickListener { view: View ->
                    intent.putExtra("postItem", item)
                    view.context.startActivity(intent)
                }
            }
            GRID -> if (holder is GridViewHolder) {
                holder.postTitle.text = item.title
                try {
//                    Log.e("IMAGE", elements[0].attr("src"))
                    Glide.with(context).load(elements[0].attr("src"))
                        .transition(DrawableTransitionOptions.withCrossFade(600))
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.no_image)
                        .into(holder.postImage)
                } catch (e: IndexOutOfBoundsException) {
                    holder.postImage.setImageResource(R.drawable.no_image)
//                    Log.e(TAG, e.toString())
                }
                if (position == itemCount - 1) {
                    titleAndGridLayout.tellFragmentToGetItems("gridLayout")
                }
                holder.itemView.setOnClickListener { view: View ->
                    intent.putExtra("postItem", item)
                    view.context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class CardViewHolder(binding: CardLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val cardLayoutBinding: CardLayoutBinding = binding
        private val context: Context = cardLayoutBinding.root.context
        fun bind(item: Item) {
            val document = Jsoup.parse(item.content)
            val elements = document.select("img")

//        Log.e("IMAGE", document.getAllElements().select("img").get(0).attr("src"));
            var date: Date? = Date()
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
            cardLayoutBinding.postTitle.text = item.title
            try {
//                Log.e("IMAGE", elements[0].attr("src"))
                Glide.with(context).load(elements[0].attr("src"))
                    .transition(DrawableTransitionOptions.withCrossFade(600))
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.no_image)
                    .into(cardLayoutBinding.postImage)
            } catch (e: IndexOutOfBoundsException) {
                cardLayoutBinding.postImage.setImageResource(R.drawable.no_image)
//                Log.e(TAG, e.toString())
            }
            cardLayoutBinding.postDescription.text = document.text()
            try {
                date = format.parse(item.published)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            val prettyTime = PrettyTime()
            cardLayoutBinding.postDate.text = prettyTime.format(date)
        }

    }

    class CardMagazineViewHolder(binding: CardMagazineBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val cardMagazineBinding: CardMagazineBinding
        val context: Context
        fun bind(item: Item) {
            val document = Jsoup.parse(item.content)
            val elements = document.select("img")
            var date: Date? = Date()
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())


//        Log.e("IMAGE", document.getAllElements().select("img").get(0).attr("src"));
            cardMagazineBinding.postTitle.text = item.title
            try {
//                Log.e("IMAGE", elements[0].attr("src"))
                Glide.with(context).load(elements[0].attr("src"))
                    .transition(DrawableTransitionOptions.withCrossFade(600))
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.no_image)
                    .into(cardMagazineBinding.postImage)
            } catch (e: IndexOutOfBoundsException) {
                cardMagazineBinding.postImage.setImageResource(R.drawable.no_image)
//                Log.e(TAG, e.toString())
            }
            try {
                date = format.parse(item.published)
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
        var postTitle: TextView = itemView.findViewById(R.id.postTitle)
        var postImage: MyImageview = itemView.findViewById(R.id.postImage)

    }

    class GridViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postTitle: TextView = itemView.findViewById(R.id.postTitle)
        val postImage: MyImageview = itemView.findViewById(R.id.postImage)

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
        this.items = items
//        this.fragment = fragment
//        this.postViewModel = postViewModel
    }
}