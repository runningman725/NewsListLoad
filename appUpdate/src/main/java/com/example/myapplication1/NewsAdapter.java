package com.example.myapplication1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by naver on 7/5/2017.
 */

public class NewsAdapter extends BaseAdapter implements AbsListView.OnScrollListener{

    private List<NewsBean> mList;
    private LayoutInflater minflator;
    private ImageLoader mImageLoader;
    private int mStart,mEnd;
    public static String[] URLS;//用来保存start到end中间的url项
    private boolean mFirstIn;

    public NewsAdapter(Context context, List<NewsBean> data, ListView listView){
        mList=data;
        minflator= LayoutInflater.from(context);
        mImageLoader=new ImageLoader(listView);
        URLS=new String[data.size()];
        for(int i=0;i<data.size();i++){
            URLS[i]=data.get(i).newsIconUrl;
        }
        mFirstIn=true;

        //一定要注册对应的事件
        listView.setOnScrollListener(this);

    }


    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView==null){
            viewHolder=new ViewHolder();
            convertView=minflator.inflate(R.layout.item_layout,null);
            viewHolder.ivIcon= (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.tvTitle= (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_content);
            convertView.setTag(viewHolder);
        }else{
            viewHolder= (ViewHolder) convertView.getTag();

        }
        viewHolder.ivIcon.setImageResource(R.mipmap.ic_launcher);
        String url=mList.get(position).newsIconUrl;
        viewHolder.ivIcon.setTag(url);
//        new ImageLoader().showImageByThread(url,viewHolder.ivIcon);
        mImageLoader.showImageByAsyncTask(url,viewHolder.ivIcon);
        viewHolder.tvTitle.setText(mList.get(position).newsTitle);
        viewHolder.tvContent.setText(mList.get(position).newsContent);
        return convertView;

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(scrollState==SCROLL_STATE_IDLE){
            //加载图片
            mImageLoader.loadImages(mStart,mEnd);
        }else {
            //停止任务
            mImageLoader.cancelAllTasks();
        }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mStart=firstVisibleItem;
        mEnd=firstVisibleItem+visibleItemCount;
        if(mFirstIn && visibleItemCount>0){
            mImageLoader.loadImages(mStart,mEnd);
            mFirstIn=false;
        }
    }

    class ViewHolder{
        public TextView tvTitle,tvContent;
        public ImageView ivIcon;
    }

}
