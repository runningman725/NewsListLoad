package com.example.naver.myapplication1;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageLoader {

    private ImageView mImageView;
    private String mUrl;
    //创建Cache
    private LruCache<String,Bitmap> mCaches;

    public ImageLoader(){
        //获取最大可用内存
        int maxMemory= (int) Runtime.getRuntime().maxMemory();
        int cacheSize=maxMemory/4;
        mCaches=new LruCache<String,Bitmap>(cacheSize){//使用匿名内部类的方式重写方法
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //在每次存入缓存的时候调用
                return value.getByteCount();
            }
        };

    }

    //把bitmap增加到缓存
    public void addBitmapToCache(String url,Bitmap bitmap){
        if(getBitmapFromCache(url)==null){
            mCaches.put(url,bitmap);
        }
    }

    //缓存中获取bitmap数据
    public Bitmap getBitmapFromCache(String url){
        return mCaches.get(url);
    }

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mImageView.getTag().equals(mUrl)) {
                mImageView.setImageBitmap((Bitmap) msg.obj);
            }
        }
    };

    public void showImageByThread(ImageView imageView, final String url){
        mImageView=imageView;
        mUrl=url;
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Bitmap bitmap=getBitmapFromURL(url);
                    //不能将bitmap直接传达给UI线程，需要handler在中间做调动，将bitmap利用传给主线程的handler，再UI主线程中进行设置
                    Message message=Message.obtain();
                    message.obj=bitmap;
                    mHandler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //从url中获得bitmap对象
    //不管是使用多线程模式加载还是AsyncTask加载都需要使用该方法
    public Bitmap getBitmapFromURL(String urlString) throws IOException {
        Bitmap bitmap;
        InputStream is = null;
        try {
            URL url=new URL(urlString);
            HttpURLConnection connection= (HttpURLConnection) url.openConnection();
            is=connection.getInputStream();
            bitmap= BitmapFactory.decodeStream(is);
            connection.disconnect();
//            Thread.sleep(1000);
            return bitmap;
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {
            is.close();
        }
        return null;
    }

    public void showImageByAsyncTask(ImageView imageView,String url) throws IOException {
        //从缓存中下载对应的图片
        Bitmap bitmap=getBitmapFromCache(url);
        if(bitmap==null){
            new NewsAsyncTask(imageView, url).execute(url);
        }else{
            imageView.setImageBitmap(bitmap);
        }

    }

    private class NewsAsyncTask extends AsyncTask<String,Void,Bitmap>{

        private ImageView mImageView;
        private String mUrl;

        public NewsAsyncTask(ImageView imageView,String url) {
            mImageView=imageView;
            mUrl=url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                String url=params[0];
                //从网络上获取图片
                Bitmap bitmap=getBitmapFromURL(url);
                if(bitmap!=null){
                    //将不在缓存的图片加入到缓存
                    addBitmapToCache(url,bitmap);
                }
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(mImageView.getTag().equals(mUrl)){

                mImageView.setImageBitmap(bitmap);
            }
        }
    }
}


















