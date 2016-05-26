package test.alien.com.phototest;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
/**
 * Created by alien on 16/5/25.
 */
public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewReference;
    private String  pathUrl;
    private int  picWidth,picHeigh ;
    private AsyncTaskListener asyncTaskListener;

    public BitmapWorkerTask(ImageAdapter.ItemViewHolder viewHolder, int picWidth, int picHeigh) {
       this(viewHolder, picWidth, picHeigh,null);
    }

    public BitmapWorkerTask(ImageAdapter.ItemViewHolder viewHolder, int picWidth, int picHeigh,AsyncTaskListener asyncTaskListener) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(viewHolder.img);
        this.picWidth = picWidth;
        this.picHeigh = picHeigh;
        this.asyncTaskListener =asyncTaskListener;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        pathUrl = strings[0];
        Bitmap bitmap = BitmapHelper.decodeSampledBitmapFromFile(pathUrl,picWidth,picHeigh);

        if(asyncTaskListener!= null){
            asyncTaskListener.getResult(pathUrl,bitmap);
        }
        return bitmap;
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask =
                    getBitmapWorkerTask(imageView);
            if (this == bitmapWorkerTask && imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }

    }

    public String getPathUrl(){
        return pathUrl;
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

    public interface AsyncTaskListener{

        void getResult(String pathUrl,Bitmap bitmap);

    }


}
