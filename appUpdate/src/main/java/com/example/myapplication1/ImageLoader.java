package com.example.myapplication1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by naver on 7/5/2017.
 */

public class ImageLoader {

    private String mUrl;
    private ImageView mImageView;
    private LruCache<String,Bitmap> mCaches;
    private ListView mListView;
    private Set<NewsAsyncTask> mTask;//创建集合来管理所有asynctask的tag

    public ImageLoader(ListView listView){
        mListView=listView;
        mTask=new HashSet<>();

        int maxMemory= (int) Runtime.getRuntime().maxMemory();
        int mCacheSize=maxMemory/4;
        mCaches=new LruCache<String, Bitmap>(mCacheSize){

            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };

    }

    public void addBitmapToCache(String url,Bitmap bitmap){
        if(getBitmapFromCache(url)==null){
            mCaches.put(url,bitmap);
        }
    }

    public Bitmap getBitmapFromCache(String url){
        return mCaches.get(url);
    }


    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mImageView.getTag().equals(mUrl)){

                mImageView.setImageBitmap((Bitmap) msg.obj);
            }
        }
    };


    public void showImageByThread(final String url, ImageView imageView){
        mUrl=url;
        mImageView=imageView;
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Bitmap bitmap=getBitmapFromURL(url);
                    Message message=Message.obtain();
                    message.obj=bitmap;
                    mHandler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private Bitmap getBitmapFromURL(String urlString) throws IOException {
        InputStream is = null;
        Bitmap bitmap;

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection= (HttpURLConnection) url.openConnection();
                is=connection.getInputStream();
                bitmap= BitmapFactory.decodeStream(is);
                connection.disconnect();
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                return bitmap;

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                is.close();
            }
              return null;
    }

    public void showImageByAsyncTask(String url,ImageView imageView){
        //从缓存中取出对应的图片
        Bitmap bitmap=getBitmapFromCache(url);
        if(bitmap==null){
//            new NewsAsyncTask(url,imageView).execute(url);
            imageView.setImageResource(R.mipmap.ic_launcher);
        }else {
            imageView.setImageBitmap(bitmap);
        }


    }

    //用来加载从start到end的所有图片
    public void loadImages(int start,int end){
        for(int i=start;i<end;i++){
            String url=NewsAdapter.URLS[i];
            //从缓存中取出对应的图片
            Bitmap bitmap=getBitmapFromCache(url);
            if(bitmap==null){
//                new NewsAsyncTask(url,imageView).execute(url);
                NewsAsyncTask task=new NewsAsyncTask(url);
                task.execute(url);
                mTask.add(task);
            }else {
                ImageView imageView= (ImageView) mListView.findViewWithTag(url);
                imageView.setImageBitmap(bitmap);
            }
        }

    }

    public void cancelAllTasks() {
        if(mTask!=null){
            for(NewsAsyncTask task:mTask){
                task.cancel(false);
            }
        }
    }


    private class NewsAsyncTask extends AsyncTask<String,Void,Bitmap>{

        private String mUrl;
//        private ImageView mImageView;

        public NewsAsyncTask(String url){
            mUrl=url;
//            mImageView=imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                Bitmap bitmap=getBitmapFromURL(mUrl);
                if(bitmap!=null){
                    addBitmapToCache(mUrl,bitmap);
                }
                return getBitmapFromURL(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
//            if(mImageView.getTag().equals(mUrl)){
//
//                mImageView.setImageBitmap(bitmap);
//            }
            ImageView imageView= (ImageView) mListView.findViewWithTag(mUrl);
            if(imageView!=null && bitmap!=null){

                imageView.setImageBitmap(bitmap);
            }
            mTask.remove(this);
        }
    }
}




















