
package com.exam.slieer.ui.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.exam.slieer.R;
import com.exam.slieer.data.DummyContent;

/**
 */
public class DummyItemDetailFragment extends Fragment {
    private static final String TAG = "DummyItemDetailFragment";
    public static final String ARG_ITEM_ID = "item_id";
    private DummyContent.DummyItem mItem;
    private Integer currItemid;
    //private String url = "http://bbs.21nowart.com/data/attachment/forum/month_0902/20090201_8a248b643e0de56fbadeqoxxkz6dsWAz.jpg.thumb.jpg";
    private String url = "http://www.google.com.hk/images/nav_logo114.png";
    
    public DummyItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate......");
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            currItemid = Integer.valueOf(getArguments().getString(ARG_ITEM_ID));
            mItem = DummyContent.ITEM_MAP.get(currItemid);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView......");
        View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);

        switch (currItemid) {
            case DummyContent.DOWNLOAD_IMG:
                ImageView img = (ImageView)rootView.findViewById(R.id.imageView1);
                Log.i(TAG, img.toString());
                new GetImageTask(img).execute(url);
                break;

            default:
                break;
        }
        if (mItem != null) {
            ((TextView)rootView.findViewById(R.id.item_detail)).setText(mItem.content);
        }
        return rootView;
    }

    static class GetImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView image;
        
        GetImageTask(ImageView imageView){
            this.image = imageView;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "pre Execute...");
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            URL myFileUrl = null;
            Bitmap bitmap = null;
            InputStream is = null;
            HttpURLConnection conn = null;
            try {
                Log.i(TAG, params[0]);
                myFileUrl = new URL(params[0]);
            } catch (MalformedURLException e) {
                Log.i(TAG, e.getMessage(), e);
            }
            try {
                conn = (HttpURLConnection)myFileUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);

            } catch (IOException e) {
                Log.i(TAG, e.getMessage(), e);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                } catch (IOException e) {
                    Log.i(TAG, e.getMessage(), e);
                }
            }
            return bitmap;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            Log.i(TAG, "----------" + result.toString());
            image.setVisibility(View.VISIBLE);
            image.setImageBitmap(result);
            // 只更新稍比图片大一些的区域
            image.postInvalidate(0, 0, result.getWidth(), result.getHeight() + 30);
            super.onPostExecute(result);
        }

    }
}
