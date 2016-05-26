package test.alien.com.phototest.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by alien on 16/5/20.
 */
public class CameraSize implements Parcelable {
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    private int width;
    private int height;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.width);
        dest.writeInt(this.height);
    }

    public CameraSize() {
    }

    protected CameraSize(Parcel in) {
        this.width = in.readInt();
        this.height = in.readInt();
    }

    public static final Parcelable.Creator<CameraSize> CREATOR = new Parcelable.Creator<CameraSize>() {
        @Override
        public CameraSize createFromParcel(Parcel source) {
            return new CameraSize(source);
        }

        @Override
        public CameraSize[] newArray(int size) {
            return new CameraSize[size];
        }
    };
}
