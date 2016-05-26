package test.alien.com.phototest;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import test.alien.com.phototest.cache.BitmapCach;

/**
 * Created by alien on 16/5/19.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ItemViewHolder> {

    private List<String> picList  = new ArrayList<>();
    private Context context;
    private Bitmap bitmap;
    private BitmapCach bitmapCach;

    public ImageAdapter(Context context){
        this.context = context;
        bitmapCach = new BitmapCach();
        bitmapCach.initialMemoryCatch();
        bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_launcher);
    }


    public  void  refershData(List<String> picList){
        this.picList =null;
        this.picList = picList;
        this.notifyDataSetChanged();
    }


    public void addItem(String picUrl) {
        if(!picList.contains(picUrl)){
            picList.add(picUrl);
            this.notifyItemInserted(picList.size()-1);
        }

    }

    @Override
    public int getItemCount() {
        return picList.size();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item, viewGroup, false);
        return new ItemViewHolder(view);
    }
    @Override
    public void onBindViewHolder(final ItemViewHolder viewHolder, final int position) {

        Bitmap cacheBitmap = bitmapCach.getBitmapFromMemCache(picList.get(position));
        if (cacheBitmap != null) {
            viewHolder.img.setImageBitmap(cacheBitmap);
            return;
        }
        if(cancelPotentialWork(picList.get(position), viewHolder.img)) {
            final BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(viewHolder,
                    BitmapHelper.dp2px(context, 60), BitmapHelper.dp2px(context, 100), new BitmapWorkerTask.AsyncTaskListener() {
                @Override
                public void getResult(String pathUrl, Bitmap bitmap) {
                    bitmapCach.addBitmapToMemoryCache(pathUrl, bitmap);
                }
            });

            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(context.getResources(), bitmap, bitmapWorkerTask);
            viewHolder.img.setImageDrawable(asyncDrawable);
            bitmapWorkerTask.execute(picList.get(position));
        }

    }

    public boolean cancelPotentialWork(String picUrl, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            String bitmapUrl= bitmapWorkerTask.getPathUrl();
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapUrl == null || !bitmapUrl.equals(picUrl)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private  BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }


    public  static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView img;

        public ItemViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.img);
            }
    }
}

