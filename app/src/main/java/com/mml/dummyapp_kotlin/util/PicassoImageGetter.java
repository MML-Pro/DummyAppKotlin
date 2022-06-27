package com.mml.dummyapp_kotlin.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.widget.TextView;

import com.mml.dummyapp_kotlin.R;
import com.squareup.picasso.Picasso;

public class PicassoImageGetter implements Html.ImageGetter {

    private TextView textView = null;
    Context mContext;

    public PicassoImageGetter(TextView target, Context context) {
        textView = target;
        mContext = context;
    }

    @Override
    public Drawable getDrawable(String source) {
        BitmapDrawablePlaceHolder drawable = new BitmapDrawablePlaceHolder();
//        Picasso.get().load(source).into(drawable);
        PicassoCache.getPicassoInstance(mContext)
                .load(source)
                .error(R.drawable.no_thumbnail)
                .into(drawable);
        return drawable;

    }

    @SuppressWarnings("deprecation")
    private class BitmapDrawablePlaceHolder extends BitmapDrawable implements com.squareup.picasso.Target {
        protected Drawable drawable;
        @Override
        public void draw(final Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }

        public void setDrawable(Drawable drawable) {
                this.drawable = drawable;
                int width = drawable.getIntrinsicWidth();
                int height = drawable.getIntrinsicHeight();
                drawable.setBounds(0, 0, width, height);
                setBounds(0, 0, width, height);
            if (textView != null) {
                textView.setText(textView.getText());
            }
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            setDrawable(new BitmapDrawable(mContext.getResources(), bitmap));
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            setDrawable(errorDrawable);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }

    }
}