package wesley.folz.blowme.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import wesley.folz.blowme.R;

/**
 * Created by Wesley on 12/4/2016.
 */
public class ImageAdapter extends BaseAdapter {
    /*-----------------------------------------Constructors---------------------------------------*/

    /*---------------------------------------Override Methods-------------------------------------*/

    /*---------------------------------------Public Methods---------------------------------------*/

    /*-------------------------------------Protected Methods--------------------------------------*/

    /*--------------------------------------Private Methods---------------------------------------*/

    /*--------------------------------------Getters and Setters-----------------------------------*/

    /*----------------------------------------Public Fields---------------------------------------*/

    /*--------------------------------------Protected Fields--------------------------------------*/

    /*---------------------------------------Private Fields---------------------------------------*/

    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.ic_close, R.drawable.ic_home,
            R.drawable.ic_pause, R.drawable.ic_play_arrow,
            R.drawable.ic_replay, R.drawable.ic_close, R.drawable.ic_home,
            R.drawable.ic_pause, R.drawable.ic_play_arrow,
            R.drawable.ic_replay, R.drawable.ic_close, R.drawable.ic_home,
            R.drawable.ic_pause, R.drawable.ic_play_arrow,
            R.drawable.ic_replay, R.drawable.ic_close, R.drawable.ic_home,
            R.drawable.ic_pause, R.drawable.ic_play_arrow,
            R.drawable.ic_replay, R.drawable.ic_close, R.drawable.ic_home,
            R.drawable.ic_pause, R.drawable.ic_play_arrow,
            R.drawable.ic_replay
    };
}
