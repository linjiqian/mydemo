package cn.linjiqian.downuprefreshlistview_library.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * @author jiqian
 * @创建时间 2015-7-4 下午8:40:06
 * @描述	SharedPreferences保存参数读取参数的工具类
 *
 * @svn 提交者: $Author: super $
 * @提交时间 $Date: 2015-07-10 20:42:37 +0800 (Fri, 10 Jul 2015) $
 * @当前版本 $Rev: 3 $
 * 
 */
public class SpUtils {
	
	
	public static void saveString(Context context,String key,String value){
		SharedPreferences sp = context.getSharedPreferences("refresh_date",0);
		Editor edit = sp.edit();
		edit.putString(key, value);
		edit.commit();
	}
	
	public static String readString(Context context,String key){
		SharedPreferences sp = context.getSharedPreferences("refresh_date",0);
		String value = sp.getString(key,"");
		return value;
	}
	
}
