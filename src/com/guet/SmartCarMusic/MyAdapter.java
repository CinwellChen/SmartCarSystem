package com.guet.SmartCarMusic;



import java.io.File; 
import java.util.List;

import com.guet.SmartCarSystem.R;

import android.content.Context;  
import android.graphics.Bitmap;  
import android.graphics.BitmapFactory;  
import android.graphics.Color;
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.BaseAdapter;  
import android.widget.ImageView;  
import android.widget.TextView;  

public class MyAdapter extends BaseAdapter  
{  
	  private static final String TAG = "bb";
	  private LayoutInflater mInflater;  
	  private Bitmap mIcon3;  
	  private Bitmap mIcon4;  
	  private List<String> items; 
	  public List<Long> itemsSelectTag;	  
	  private List<String> paths;
	  
	  public MyAdapter(Context context,List<String> it,List<String> pa)  
	  {
	    mInflater = LayoutInflater.from(context);  
	    items = it;  
	    paths = pa;
	    
		mIcon3 = BitmapFactory.decodeResource(context.getResources(),R.drawable.folders);  
		//��ǰ���й�����Ƶ�ļ��������Դ˴�����ʾ���ļ���ͳ������Ƶͼ��
	    mIcon4 = BitmapFactory.decodeResource(context.getResources(),R.drawable.audiofile);
	  }  
	    
	  @Override  
	  public int getCount()  
	  { 
	    return items.size();  
	  }  
	  @Override  
	  public Object getItem(int position)  
	  {
	    return items.get(position);  
	  }  
	    
	  @Override  
	  public long getItemId(int position)  
	  { 
	    return position;  
	  }  
	    
	  @Override  
	  public View getView(int position,View convertView,ViewGroup parent)  
	  {
	    ViewHolder holder;
	    
	    if(convertView == null)  
	    { 
			  convertView = mInflater.inflate(R.layout.file_row2, null); 
			  holder = new ViewHolder();  
			  holder.text = (TextView) convertView.findViewById(R.id.text);  
			  holder.icon = (ImageView) convertView.findViewById(R.id.icon);  
			    
			  convertView.setTag(holder);
		}  
		else  
		{  
		  holder = (ViewHolder) convertView.getTag();		 
		} 
		    
		File f=new File(paths.get(position).toString());
	    holder.text.setText(f.getName()); 
	    
	    //�ļ����ļ���ͼ����ʾ����
	    if(f.isDirectory())  
	    {  
	    	holder.icon.setImageBitmap(mIcon3);  
	    }  
	    else  
	    {  
	        holder.icon.setImageBitmap(mIcon4);  
	    }
	    
	    //����ѡ���ɫ����
		if( this.itemsSelectTag.get(position) == (long)position)
		{ 	               
			convertView.setBackgroundColor(Color.YELLOW);			
		            
		}else{			
			
			convertView.setBackgroundColor(Color.TRANSPARENT);		           
		} 

	    return convertView;  
	  } 
	  
	  private class ViewHolder  
	  { 		
	    TextView text;  
	    ImageView icon;  
	  }
	  
	  public void setSelectItem(int selectItem) 
	  {
		  this.itemsSelectTag.set(selectItem, (long)selectItem);
		  
//		  int size = this.itemsSelectTag.size();
//		  Log.d(TAG,"size="+size);		  
//		  for( int j=0; j < size; j++ )
//		  {
//			  Log.d(TAG,j + "=" + this.itemsSelectTag.get(j));
//		  }		 
	  } 
} 