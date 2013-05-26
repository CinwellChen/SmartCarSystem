package com.guet.SmartCarMovie;



import java.util.ArrayList;
import java.util.List; 

import com.guet.SmartCarSystem.R;

import android.content.Context;  
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.BaseAdapter;
import android.widget.TextView;  

public class movieListAdapter extends BaseAdapter  
{  
	  private static final String TAG = "Car";
	  private LayoutInflater mInflater;  
	 
	  private List<String> movieNames = new ArrayList<String>();;
	  private List<String> movieTimes = new ArrayList<String>();;	  
	  
	  public String selectItemName="";		//长击选中的选项名称
	  public String clickItemName="";		//单击选中的选项名称
	  
	  public movieListAdapter(Context context,List<String> nam ,List<String> tim)  
	  {
	    mInflater = LayoutInflater.from(context); 
	   
	    movieNames = nam;
	    movieTimes = tim;	    
	  } 
	  
	  @Override  
	  public int getCount()  
	  { 
	    return movieNames.size();  
	  }  
	 
	  @Override  
	  public Object getItem(int position)  
	  {
	    return movieNames.get(position);  
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
			  convertView = mInflater.inflate(R.layout.movielistitem, null); 
			  holder = new ViewHolder(); 
			  
			  holder.movieNameTxt = (TextView) convertView.findViewById(R.id.movieName);
			  holder.movieTimeTxt = (TextView) convertView.findViewById(R.id.movieTime);
					    
			  convertView.setTag(holder);
		}  
		else  
		{  
		  holder = (ViewHolder) convertView.getTag();		 
		}  
		
		String namStr = movieNames.get(position);
		String timStr = movieTimes.get(position);
		
		Log.d(TAG, "position=" + position);
		Log.d( TAG,"movieNames=" + namStr );
		Log.d( TAG,"movieTimes=" + timStr );
		
		holder.movieNameTxt.setText(namStr);
		holder.movieTimeTxt.setText(timStr);	
		
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
			holder.movieNameTxt.setTextColor(Color.BLUE);
			holder.movieTimeTxt.setTextColor(Color.BLUE);
		            
		}else{ 		
			
			
			holder.movieNameTxt.setTextColor(Color.WHITE);
			holder.movieTimeTxt.setTextColor(Color.WHITE);
		} 

	    return convertView;  
	  } 
	  
	  private class ViewHolder
	  {			
			TextView movieNameTxt;
			TextView movieTimeTxt;
	  }
} 