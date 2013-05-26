package com.guet.SmartCarSMS;


import java.util.ArrayList;
import java.util.List;

import com.guet.SmartCarSystem.R;

import android.content.Context;  
import android.graphics.Color;
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;  

public class smsListAdapter extends BaseAdapter  
{  
	  private LayoutInflater mInflater;	 
	  private List<String> smsPersons = new ArrayList<String>();
	  private List<String> smsIds = new ArrayList<String>();	  
	  public String clickItemId="";		//单击选中的选项ID号
	  
	  public smsListAdapter(Context context, List<String> person, List<String> ids)  
	  {
	    mInflater = LayoutInflater.from(context);  
	    smsPersons = person;
	    smsIds = ids;
	  } 
	  
	  @Override  
	  public int getCount()  
	  { 
	    return smsPersons.size();  
	  }  
	 
	  @Override  
	  public Object getItem(int position)  
	  {
	    return smsPersons.get(position);  
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
			  convertView = mInflater.inflate(R.layout.smslistitem, null); 
			  holder = new ViewHolder(); 
			  
			  holder.smsImageView = (ImageView) convertView.findViewById(R.id.smsImage);
			  holder.smsPersonTxt = (TextView) convertView.findViewById(R.id.smsPerson);
			  convertView.setTag(holder);
		}  
		else  
		{  
		  holder = (ViewHolder) convertView.getTag();		 
		} 
	    
		String personStr = smsPersons.get(position);
		String idStr = smsIds.get(position);		
		holder.smsPersonTxt.setText(personStr);
		
		//针对单击选中的选项的处理
		if( idStr.equals(clickItemId) )
		{
			holder.smsPersonTxt.setTextColor(Color.YELLOW);
			holder.smsPersonTxt.setBackgroundColor(Color.BLUE);
		            
		}else{ 			
			
			holder.smsPersonTxt.setTextColor(Color.BLACK);
			holder.smsPersonTxt.setBackgroundColor(Color.TRANSPARENT);			
		} 
		
		//图标显示
		holder.smsImageView.setImageResource(R.drawable.sms3);
		
	    return convertView;  
	  } 
	  
	  private class ViewHolder
	  {
			TextView smsPersonTxt;
			ImageView smsImageView;
	  }
} 