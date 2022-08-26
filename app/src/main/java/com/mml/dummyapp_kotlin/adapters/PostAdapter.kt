package com.mml.dummyapp_kotlin.adapters

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.mml.dummyapp_kotlin.R
import com.mml.dummyapp_kotlin.databinding.*
import com.mml.dummyapp_kotlin.models.Item
import com.mml.dummyapp_kotlin.ui.DetailsActivity
import com.mml.dummyapp_kotlin.util.TitleAndGridLayout
import com.mml.dummyapp_kotlin.util.Utils
import org.jsoup.Jsoup
import org.ocpsoft.prettytime.PrettyTime
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class PostAdapter(
    private val titleAndGridLayout: TitleAndGridLayout
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items = arrayListOf<Item>()

//    private var context: Context? = null

//    val VIEW_TYPE_CONTENT = 1
//    val VIEW_TYPE_AD_CARD_LAYOUT = 2
//    val VIEW_TYPE_AD_GRID_LAYOUT = 3

//    var isDestroyed = false
//
//    private var adsCnt = 3


//    fun isDetachedOrDestroyed() : Boolean {
////            nativeAd?.destroy()
//        Log.d(TAG, "isDetachedOrDestroyed: $isDestroyed")
//        return isDestroyed
//    }


    var viewType = 0
        set(value) {
            field = value
//            notifyDataSetChanged()
        }

    fun submitList(items: List<Item>) {
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun clearList() {
        this.items.clear()
        notifyDataSetChanged()
    }
//    var itemViewType = 0
//        set(value) {
//            field = value
//            notifyDataSetChanged()
//        }

//    private val differCallback = object : DiffUtil.ItemCallback<Item>() {
//        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
//            return (oldItem.id == newItem.id)
//        }
//
//        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
//          return (oldItem == newItem)
//        }
//    }
//
//    val differ = AsyncListDiffer(this, differCallback)

//}


//}


//    fun clearDifferList(){
//        differ.currentList.clear()
//        notifyDataSetChanged()
//    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

//        this@PostAdapter.context = parent.context


        when (this.viewType) {
            CARD -> {
                val cardLayoutBinding: CardLayoutBinding =
                    CardLayoutBinding.inflate(inflater, parent, false)
                return CardViewHolder(cardLayoutBinding)
            }
            CARD_MAGAZINE -> {
                val cardMagazineBinding: CardMagazineBinding =
                    CardMagazineBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return CardMagazineViewHolder(cardMagazineBinding)
            }
            TITLE -> {
                val titleLayoutBinding: TitleLayoutBinding =
                    TitleLayoutBinding.inflate(inflater, parent, false)
                return TitleViewHolder(titleLayoutBinding)
            }
            else -> {
                val gridLayoutBinding: GridLayoutBinding =
                    GridLayoutBinding.inflate(inflater, parent, false)
                return GridViewHolder(gridLayoutBinding)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item: Item = items[position]
//        val item: Item = items[getRealPosition(position)]
//        val document = Jsoup.parse(item.content)
//        val elements = document.select("img")

        val intent = Intent(holder.itemView.context, DetailsActivity::class.java)

            when (this.viewType) {
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
                    holder.bind(item)

                    if (position == itemCount - 1)
                        titleAndGridLayout.tellFragmentToGetItems()

                    holder.itemView.setOnClickListener { view: View ->
                        intent.putExtra("postItem", item)
                        view.context.startActivity(intent)
                    }
                }
                GRID -> if (holder is GridViewHolder) {
                    holder.bind(item)

                    if (position == itemCount - 1)
                        titleAndGridLayout.tellFragmentToGetItems()
//

                    holder.itemView.setOnClickListener { view: View ->
                        intent.putExtra("postItem", item)
                        view.context.startActivity(intent)
                    }
                }
            }
    }

//    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
//        return if (holder.itemViewType == VIEW_TYPE_AD_CARD_LAYOUT ||
//            holder.itemViewType == VIEW_TYPE_AD_GRID_LAYOUT) {
//            // Don't recycle the ad view, keep it around
//            true
//        } else {
//            super.onFailedToRecycleView(holder)
//        }
//    }


//    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
//        super.onViewRecycled(holder)
//        if(holder is AdViewHolder){
//            getItemViewType(holder.bindingAdapterPosition)
//
//        }
//    }

//    private fun getRealPosition(position: Int): Int {
//        return if (LIST_AD_DELTA == 0) {
//            position
//        } else {
//            position - position / LIST_AD_DELTA
//        }
//    }

    override fun getItemCount(): Int {
       return items.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    class CardViewHolder(private val cardLayoutBinding: CardLayoutBinding) :
        RecyclerView.ViewHolder(cardLayoutBinding.root) {
        fun bind(item: Item) {
            val document = Jsoup.parse(item.content)
            val elements = document.select("img")

//        Log.e("IMAGE", document.getAllElements().select("img").get(0).attr("src"));
            var date: Date? = Date()
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
            cardLayoutBinding.postTitle.text = item.title
            try {
//                Log.e("IMAGE", elements[0].attr("src"))
                Glide.with(cardLayoutBinding.root).load(elements[0].attr("src"))
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

    class CardMagazineViewHolder(private val cardMagazineBinding: CardMagazineBinding) :
        RecyclerView.ViewHolder(cardMagazineBinding.root) {

        fun bind(item: Item) {
            val document = Jsoup.parse(item.content)
            val elements = document.select("img")
            var date: Date? = Date()
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())


//        Log.e("IMAGE", document.getAllElements().select("img").get(0).attr("src"));
            cardMagazineBinding.postTitle.text = item.title
            try {
//                Log.e("IMAGE", elements[0].attr("src"))
                Glide.with(itemView.context).load(elements[0].attr("src"))
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

    }

    inner class TitleViewHolder(private val binding: TitleLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item) {
            val document = Jsoup.parse(item.content)
            val elements = document.select("img")

//        Log.e("IMAGE", document.getAllElements().select("img").get(0).attr("src"));
            binding.postTitle.text = item.title
            try {
//                Log.e("IMAGE", elements[0].attr("src"))
                Glide.with(itemView.context).load(elements[0].attr("src"))
                    .transition(DrawableTransitionOptions.withCrossFade(600))
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.no_image)
                    .into(binding.postImage)
            } catch (e: IndexOutOfBoundsException) {
                binding.postImage.setImageResource(R.drawable.no_image)
//                Log.e(TAG, e.toString())
            }
        }


    }

    inner class GridViewHolder constructor(private val binding: GridLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item) {
            val document = Jsoup.parse(item.content)
            val elements = document.select("img")

//        Log.e("IMAGE", document.getAllElements().select("img").get(0).attr("src"));
            binding.postTitle.text = item.title
            try {
//                Log.e("IMAGE", elements[0].attr("src"))
                Glide.with(itemView.context).load(elements[0].attr("src"))
                    .transition(DrawableTransitionOptions.withCrossFade(600))
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.no_image)
                    .into(binding.postImage)
            } catch (e: IndexOutOfBoundsException) {
                binding.postImage.setImageResource(R.drawable.no_image)
//                Log.e(TAG, e.toString())
            }
        }

    }


    companion object {
        private const val CARD = 0
        private const val CARD_MAGAZINE = 1
        private const val TITLE = 2
        private const val GRID = 3
        private const val TAG = "POST_ADAPTER"
//        private const val LIST_AD_DELTA = 10

    }

    init {
//        this.itemList = items
//        this.fragment = fragment
//        this.postViewModel = postViewModel
        setHasStableIds(true)
    }

    override fun getItemViewType(position: Int): Int {
        return viewType
    }

//    inner class AdViewHolder(private val binding: AdUnifiedBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        private val videoOptions = VideoOptions.Builder()
//            .setStartMuted(false)
//            .build()
//
//
//        private var adOptions = NativeAdOptions.Builder()
//            .setVideoOptions(videoOptions)
//            .build()
//
//        fun bindAdData() {
//            val builder =
//                AdLoader.Builder(binding.root.context, "ca-app-pub-3940256099942544/2247696110")
//
//            builder.forNativeAd { nativeAd ->
//                // OnUnifiedNativeAdLoadedListener implementation.
//                // If this callback occurs after the activity is destroyed, you must call
//                // destroy and return or you may get a memory leak.
//                if (isDestroyed) {
//
//                    Toast.makeText(this@PostAdapter.context, "$isDestroyed", Toast.LENGTH_SHORT)
//                        .show()
//
//                    Log.e(TAG, "bindAdData: $isDestroyed")
//
//                    nativeAd.destroy()
//
//                    Log.e(TAG, "bindAdData: ${nativeAd.body.toString()}")
//                }
//                populateNativeAdView(nativeAd, binding)
//            }
//
//            builder.withNativeAdOptions(adOptions)
//
//
//            val adLoader = builder
//                .withAdListener(
//                    object : AdListener() {
//                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//
//                            if (adsCnt > 0) {
//                                bindAdData()
//                            } else {
//                                adsCnt -= 1
//                            }
//
//                            val error =
//                                """
//           domain: ${loadAdError.domain}, code: ${loadAdError.code}, message: ${loadAdError.message}
//          """"
//                            Toast.makeText(
//                                binding.root.context,
//                                "Failed to load native ad with error $error",
//                                Toast.LENGTH_SHORT
//                            )
//                                .show()
//                        }
//                    }
//                )
//                .build()
//
//            adLoader.loadAds(AdRequest.Builder().build(), 5)
//
//
//        }
//
//        private fun populateNativeAdView(nativeAd: NativeAd, unifiedAdBinding: AdUnifiedBinding) {
//            val nativeAdView = unifiedAdBinding.root
//
//            // Set the media view.
//            nativeAdView.mediaView = unifiedAdBinding.adMedia
//
//            // Set other ad assets.
//            nativeAdView.headlineView = unifiedAdBinding.adHeadline
//            nativeAdView.bodyView = unifiedAdBinding.adBody
//            nativeAdView.callToActionView = unifiedAdBinding.adCallToAction
//            nativeAdView.iconView = unifiedAdBinding.adAppIcon
//            nativeAdView.priceView = unifiedAdBinding.adPrice
//            nativeAdView.starRatingView = unifiedAdBinding.adStars
//            nativeAdView.storeView = unifiedAdBinding.adStore
//            nativeAdView.advertiserView = unifiedAdBinding.adAdvertiser
//
//            // The headline and media content are guaranteed to be in every UnifiedNativeAd.
//            unifiedAdBinding.adHeadline.text = nativeAd.headline
//            nativeAd.mediaContent?.let { unifiedAdBinding.adMedia.setMediaContent(it) }
//
//            // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
//            // check before trying to display them.
//            if (nativeAd.body == null) {
//                unifiedAdBinding.adBody.visibility = INVISIBLE
//            } else {
//                unifiedAdBinding.adBody.visibility = View.VISIBLE
//                unifiedAdBinding.adBody.text = nativeAd.body
//            }
//
//            if (nativeAd.callToAction == null) {
//                unifiedAdBinding.adCallToAction.visibility = INVISIBLE
//            } else {
//                unifiedAdBinding.adCallToAction.visibility = View.VISIBLE
//                unifiedAdBinding.adCallToAction.text = nativeAd.callToAction
//            }
//
//            if (nativeAd.icon == null) {
//                unifiedAdBinding.adAppIcon.visibility = View.GONE
//            } else {
//                unifiedAdBinding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)
//                unifiedAdBinding.adAppIcon.visibility = View.VISIBLE
//            }
//
//            if (nativeAd.price == null) {
//                unifiedAdBinding.adPrice.visibility = INVISIBLE
//            } else {
//                unifiedAdBinding.adPrice.visibility = View.VISIBLE
//                unifiedAdBinding.adPrice.text = nativeAd.price
//            }
//
//            if (nativeAd.store == null) {
//                unifiedAdBinding.adStore.visibility = INVISIBLE
//            } else {
//                unifiedAdBinding.adStore.visibility = View.VISIBLE
//                unifiedAdBinding.adStore.text = nativeAd.store
//            }
//
//            if (nativeAd.starRating == null) {
//                unifiedAdBinding.adStars.visibility = INVISIBLE
//            } else {
//                unifiedAdBinding.adStars.rating = nativeAd.starRating!!.toFloat()
//                unifiedAdBinding.adStars.visibility = View.VISIBLE
//            }
//
//            if (nativeAd.advertiser == null) {
//                unifiedAdBinding.adAdvertiser.visibility = INVISIBLE
//            } else {
//                unifiedAdBinding.adAdvertiser.text = nativeAd.advertiser
//                unifiedAdBinding.adAdvertiser.visibility = View.VISIBLE
//            }
//
//            // This method tells the Google Mobile Ads SDK that you have finished populating your
//            // native ad view with this native ad.
//            nativeAdView.setNativeAd(nativeAd)
//
//
//        }
//    }
//
//    inner class AdViewHolderGrid(private val nativeAdRowTitleGridBinding: NativeAdRowTitleGridBinding) :
//        RecyclerView.ViewHolder(nativeAdRowTitleGridBinding.root) {
//
//        fun bindAdData() {
//            val adLoader =
//                AdLoader.Builder(
//                    nativeAdRowTitleGridBinding.root.context,
//                    "ca-app-pub-3940256099942544/2247696110"
//                )
//                    .forNativeAd { nativeAd: NativeAd ->
//
//
//                        if (isDestroyed) {
//                            nativeAd.destroy()
//                        }
//
////                        this@PostAdapter.nativeAd = nativeAd
////                        populateNativeADView(nativeAd)
//
//
//                        val styles =
//                            NativeTemplateStyle.Builder().withMainBackgroundColor(
//                                ColorDrawable(
//                                    ContextCompat.getColor(
//                                        nativeAdRowTitleGridBinding.root.context,
//                                        R.color.backgroundColor
//                                    )
//                                )
//                            ).build()
//
//                        val template: TemplateView = nativeAdRowTitleGridBinding.myTemplate
//
//                        Log.d(TAG, "bindAdData: ${nativeAd.body}")
//
//                        template.setStyles(styles)
//                        template.setNativeAd(nativeAd)
//
//
//                    }
//                    .withAdListener(object : AdListener() {
//
//                        override fun onAdClicked() {
//                            super.onAdClicked()
//                            Log.d(TAG, "onAdClicked: ")
//                        }
//
//                        override fun onAdClosed() {
//                            super.onAdClosed()
//                            Log.d(TAG, "onAdClosed: ")
//                        }
//
//                        override fun onAdLoaded() {
//                            super.onAdLoaded()
//                            Log.d(TAG, "onAdLoaded: ")
//                        }
//
//                        override fun onAdOpened() {
//                            super.onAdOpened()
//                            Log.d(TAG, "onAdOpened: ")
//                        }
//
//                        override fun onAdFailedToLoad(adError: LoadAdError) {
//                            // Handle the failure by logging, altering the UI, and so on.
//                            Toast.makeText(
//                                nativeAdRowTitleGridBinding.root.context,
//                                adError.message,
//                                Toast.LENGTH_SHORT
//                            ).show()
//
//                            Log.e(TAG, "onAdFailedToLoad: ${adError.cause.toString()}")
//                        }
//                    })
//                    .withNativeAdOptions(
//                        NativeAdOptions.Builder()
//                            // Methods in the NativeAdOptions.Builder class can be
//                            // used here to specify individual options settings.
//                            .build()
//                    ).build()
//
//            adLoader.loadAds(AdRequest.Builder().build(), 5)
//        }
//    }


}