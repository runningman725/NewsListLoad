package com.example.naver.myapplication1;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {


    private ListView mListView;
    private static String URL = "http://www.imooc.com/api/teacher?type=4&&num=30";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.lv_main);
        new NewsAsyncTask().execute(URL);

    }

    /**
     * 将url对应的JSON格式数据转化为我们所封装的NewsBean对象
     * @param url
     * @return
     */
    private List<NewsBean> getJsonData(String url) {
        List<NewsBean> newsBeanList=new ArrayList<>();
        try {
            String jsonString=readStream(new URL(url).openStream());//此句功能与url.openConnection().getInputStream()相同
            JSONObject jsonObject;
            NewsBean newsBean;
            try {
                jsonObject=new JSONObject(jsonString);
                JSONArray jsonArray=jsonObject.getJSONArray("data");
                for(int i=0;i<jsonArray.length();i++){
                    jsonObject=jsonArray.getJSONObject(i);
                    newsBean=new NewsBean();
                    newsBean.newsIconUrl=jsonObject.getString("picSmall");
                    newsBean.newsTitle=jsonObject.getString("name");
                    newsBean.newsContent=jsonObject.getString("description");
                    newsBeanList.add(newsBean);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return newsBeanList;
    }

    /**
     * 通过inpustStream解析网页返回的数据
     * @param is
     * @return
     */
    private  String readStream(InputStream is){
        InputStreamReader isr;
        String result="";
        try {
            String line="";
            isr=new InputStreamReader(is,"utf-8");//字节流转化为字符流
            BufferedReader br=new BufferedReader(isr);//将字符流以buffer的形式读取出来
            while((line=br.readLine())!=null){
                result+=line;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }

    /**
     * 实现网络的异步访问
     */
    class NewsAsyncTask extends AsyncTask<String, Void, List<NewsBean>> {

        @Override
        protected List<NewsBean> doInBackground(String... params) {

            return getJsonData(params[0]);//请求网址
        }

        @Override
        protected void onPostExecute(List<NewsBean> newsBeans) {
            super.onPostExecute(newsBeans);
            NewsAdapter adapter=new NewsAdapter(MainActivity.this,newsBeans);
            mListView.setAdapter(adapter);
        }
    }

}
