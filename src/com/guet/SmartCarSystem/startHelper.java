package com.guet.SmartCarSystem;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


//车载信息系统：系统启动器
public class startHelper extends BroadcastReceiver
{
	static final String ACTION = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Auto-generated method stub
		
		if (intent.getAction().equals(ACTION))
		{
			Intent sayHelloIntent = new Intent(context, SmartCarSystem.class);
			sayHelloIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			context.startActivity(sayHelloIntent);
		}
	}

}
