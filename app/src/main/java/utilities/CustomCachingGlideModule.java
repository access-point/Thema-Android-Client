package utilities;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.module.GlideModule;

/**
 * Created by Sergios on 24/10/2016.
 */

public class CustomCachingGlideModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // set size & external vs. internal
        int cacheSize500MegaBytes = 524288000;

        builder.setDiskCache(
                new InternalCacheDiskCacheFactory(context, cacheSize500MegaBytes)
        );


        //builder.setDiskCache(
        //new ExternalCacheDiskCacheFactory(context, cacheSize100MegaBytes));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        // nothing to do here
    }
}