package com.guet.SmartCarMusic;



import java.util.ArrayList;
import java.util.List; 

import com.guet.SmartCarSystem.R;

import android.content.Context;  
import android.graphics.Color;
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.BaseAdapter;
import android.widget.TextView;  

public class musicListAdapter extends BaseAdapter  
{  
	  private static final String TAG = "Car";
	  private LayoutInflater mInflater;  
	 
	  private List<String> musicNumbers = new ArrayList<String>();
	  private List<String> musicNames = new ArrayList<String>();;
	  private List<String> musicTimes = new ArrayList<String>();;	  
	  
	  public String selectItemName="";		//长击选中的选项名称
	  public String clickItemName="";		//单击选中的选项名称
	  
	  public musicListAdapter(Context context,List<String> num,List<String> nam ,List<String> tim)  
	  {
	    mInflater = LayoutInflater.from(context);  
	    musicNumbers = num;
	    musicNames = nam;
	    musicTimes = tim;	    
	  } 
	  
	  @Override  
	  public int getCount()  
	  { 
	    return musicNumbers.size();  
	  }  
	 
	  @Override  
	  public Object getItem(int position)  
	  {
	    return musicNumbers.get(position);  
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
			  convertView = mInflater.inflate(R.layout.musiclistitem, null); 
			  holder = new ViewHolder(); 
			  
			  holder.songNumberTxt = (TextView) convertView.findViewById(R.id.songNumber);  
			  holder.songNameTxt = (TextView) convertView.findViewById(R.id.songName);
			  holder.songTimeTxt = (TextView) convertView.findViewById(R.id.songTime);
					    
			  convertView.setTag(holder);
		}  
		else  
		{  
		  holder = (ViewHolder) convertView.getTag();		 
		} 
	    
//	    Log.d(TAG,"position="+ position );
//	    Log.d(TAG,"musicNumbers.size="+ musicNumbers.size() );
//	    Log.d(TAG,"musicNames.size="+ musicNames.size() );
//	    Log.d(TAG,"musicTimes.size="+ musicTimes.size() );
		    
		String numStr = musicNumbers.get(position);
		String namStr = musicNames.get(position);
		String timStr = musicTimes.get(position);
		
		holder.songNumberTxt.setText(numStr);
		holder.songNameTxt.setText(namStr);
		holder.songTimeTxt.setText(timStr);	
		
		//方法2，根据歌曲名称
		if( namStr.equals(selectItemName) )
		{						
			convertView.setBackgroundColor(Color.YELLOW);			
		            
		}else{ 			
			
			convertView.setBackgroundColor(Color.TRANSPARENT);		           
		} 
		
		//针对单击选中的选项的处理
		if( namStr.equals(clickItemName) )
		{
			holder.songNumberTxt.setTextColor(Color.BLUE);
			holder.songNameTxt.setTextColor(Color.BLUE);
			holder.songTimeTxt.setTextColor(Color.BLUE);
		            
		}else{ 			
			
			holder.songNumberTxt.setTextColor(Color.WHITE);
			holder.songNameTxt.setTextColor(Color.WHITE);
			holder.songTimeTxt.setTextColor(Color.WHITE);
		} 

	    return convertView;  
	  } 
	  
	  private class ViewHolder
	  {
			TextView songNumberTxt;
			TextView songNameTxt;
			TextView songTimeTxt;
	  }
} 