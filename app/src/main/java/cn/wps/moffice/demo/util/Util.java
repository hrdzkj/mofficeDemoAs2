package cn.wps.moffice.demo.util;

import java.io.File;

import cn.wps.moffice.demo.fileManager.ListFileActivity;
import cn.wps.moffice.demo.floatingview.service.FloatServiceTest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class Util
{
	private static SettingPreference settingPreference;

  	// 检测该包名所对应的应用是否存在
  	public static boolean checkPackage(Context context, String packageName) 
  	{  
	    if (packageName == null || "".equals(packageName))  
	        return false;  
	    
	    try 
	    {  
	    	context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);  
	        return true;  
	    } 
	    catch (NameNotFoundException e) 
	    {  
	        return false;  
	    }  
	}  
  	
  	public static void showToast(Context context, String content)
  	{
  		showToast(context, content, Toast.LENGTH_LONG);
  	}
  	
  	public static void showToast(Context context, String content, int length_short)
  	{
  		Toast.makeText(context, content, length_short).show();
  	}

  	//获得文档打开需要设置的参数，参数都是由settingPreference存放的
  	public static Intent getOpenIntent(Context context, String path, boolean isAIDL)
  	{
  		 settingPreference = new SettingPreference(context);
  		
  		 //获得上次打开的文件信息
  		String 	closeFilePath 		= settingPreference.getSettingParam(Define.CLOSE_FILE, "null");  
  		String 	packageName   		= settingPreference.getSettingParam(Define.THIRD_PACKAGE, context.getPackageName());
  		float 		ViewProgress 	= settingPreference.getSettingParam(Define.VIEW_PROGRESS, (float)0.0);
  	    float 		ViewScale 		= settingPreference.getSettingParam(Define.VIEW_SCALE, (float)1.0);
  	    int 		ViewScrollX 	= settingPreference.getSettingParam(Define.VIEW_SCROLL_X, 0);
  	    int 		ViewScrollY 	= settingPreference.getSettingParam(Define.VIEW_SCROLL_Y ,0);
  		String 	userName        	= settingPreference.getSettingParam(Define.USER_NAME, "newUser");
  		
  		 //获取用户设置的参数信息
	    String		OpenMode		= settingPreference.getSettingParam(Define.OPEN_MODE, null);
	    boolean   	SendSaveBroad   = settingPreference.getSettingParam(Define.SEND_SAVE_BROAD, false);
	    boolean   	SendCloseBroad  = settingPreference.getSettingParam(Define.SEND_CLOSE_BROAD, false);
	    boolean   	IsIsClearBuffer = settingPreference.getSettingParam(Define.IS_CLEAR_BUFFER, false);
	    boolean   	IsClearTrace 	= settingPreference.getSettingParam(Define.IS_CLEAR_TRACE, false);
	    boolean   	IsClearFile 	= settingPreference.getSettingParam(Define.IS_CLEAR_FILE, false);
	    boolean   	IsViewScale     = settingPreference.getSettingParam(Define.IS_VIEW_SCALE ,false);
	    boolean   	AutoJump		= settingPreference.getSettingParam(Define.AUTO_JUMP, false);
	    boolean   	EnterReviseMode = settingPreference.getSettingParam(Define.ENTER_REVISE_MODE, false);
	    boolean   	CacheFileInvisible = settingPreference.getSettingParam(Define.CACHE_FILE_INVISIBLE, false);

		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString(Define.OPEN_MODE, OpenMode);			     //打开模式
		bundle.putBoolean(Define.SEND_SAVE_BROAD, SendSaveBroad);    //保存文件的广播
		bundle.putBoolean(Define.SEND_CLOSE_BROAD, SendCloseBroad);	 //关闭文件的广播
		bundle.putBoolean(Define.CLEAR_BUFFER, IsIsClearBuffer);	 //清除临时文件
		bundle.putBoolean(Define.CLEAR_TRACE, IsClearTrace);		 //清除使用记录	
		bundle.putBoolean(Define.CLEAR_FILE, IsClearFile);           //删除打开文件
		bundle.putBoolean(Define.AUTO_JUMP, AutoJump);				//自动跳转，包括页数和xy坐标
		bundle.putBoolean(Define.CACHE_FILE_INVISIBLE, CacheFileInvisible);    //
		bundle.putBoolean(Define.ENTER_REVISE_MODE, EnterReviseMode);    //
		bundle.putString(Define.USER_NAME, userName);    //
		bundle.putString(Define.THIRD_PACKAGE, packageName);    //
		
		if (path.equals(closeFilePath))						       //如果打开的文档时上次关闭的
		{
			if (IsViewScale)
				bundle.putFloat(Define.VIEW_SCALE, ViewScale);				//视图比例
			if (AutoJump)
			{
				bundle.putFloat(Define.VIEW_PROGRESS, ViewProgress);		//阅读进度
				bundle.putInt(Define.VIEW_SCROLL_X, ViewScrollX);			//x
				bundle.putInt(Define.VIEW_SCROLL_Y, ViewScrollY);			//y
			}
		}
		
		if (isAIDL)
			bundle.putBoolean(Define.SEND_CLOSE_BROAD, true);	 //aidl打开需要发送关闭广播，这样关闭文档时候才能把浮窗关闭
		else
		{
			//第三方打开情况下，默认设置wps发送back和home广播
			bundle.putBoolean(Define.BACK_KEY_DOWN, true);
			bundle.putBoolean(Define.HOME_KEY_DOWN, true);
		}
		
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		if (checkPackage(context, Define.PACKAGENAME_ENT))
		{
			intent.setClassName(Define.PACKAGENAME_ENT, Define.CLASSNAME);
		}
		else if (checkPackage(context, Define.PACKAGENAME))
		{
			intent.setClassName(Define.PACKAGENAME, Define.CLASSNAME);
		}
		else if (checkPackage(context, Define.PACKAGENAME_ENG))
		{
			intent.setClassName(Define.PACKAGENAME_ENG, Define.CLASSNAME);
		}
		else if (checkPackage(context, Define.PACKAGENAME_KING_ENT))
		{
			intent.setClassName(Define.PACKAGENAME_KING_ENT, Define.CLASSNAME);
		}
		else if (checkPackage(context, Define.PACKAGENAME_KING_PRO))
		{
			intent.setClassName(Define.PACKAGENAME_KING_PRO, Define.CLASSNAME);
		}
		else if (checkPackage(context, Define.PACKAGENAME_KING_PRO_HW))
		{
			intent.setClassName(Define.PACKAGENAME_KING_PRO_HW, Define.CLASSNAME);
		}
		else
		{
			showToast(context, "文件打开失败，移动wps可能未安装");
			return null;
		}
		
		File file = new File(path);
		if (file == null || !file.exists())
		{			
			showToast(context, "打开失败，文件不存在！");
			return null;
		}
		
		Uri uri = Uri.fromFile(file);
		intent.setData(uri);
		intent.putExtras(bundle);
  		
  		settingPreference = null;
  		return intent;
  	}
  	
  	public static Intent getPDFOpenIntent(Context context, String path, boolean isAIDL)
  	{
  		Intent intent = new Intent();
		Bundle bundle = new Bundle();

		settingPreference = new SettingPreference(context);
		
	    String 	packageName   	= settingPreference.getSettingParam(Define.THIRD_PACKAGE,  context.getPackageName());
	    boolean FairCopy        = settingPreference.getSettingParam(Define.FAIR_COPY, true);
	    String userName         = settingPreference.getSettingParam(Define.USER_NAME, "");
	
	    bundle.putString(Define.USER_NAME, userName);	
		bundle.putBoolean(Define.SEND_CLOSE_BROAD, true);       //关闭文件的广播,由于demo的浮窗需要根据关闭广播来关闭，请设置该值为true
		bundle.putBoolean(Define.FAIR_COPY, FairCopy);
		bundle.putString(Define.USER_NAME,userName);
		bundle.putString(Define.THIRD_PACKAGE, packageName);
		bundle.putBoolean(Define.BACK_KEY_DOWN, settingPreference.getSettingParam(Define.BACK_KEY_DOWN, true));
		bundle.putBoolean(Define.HOME_KEY_DOWN, settingPreference.getSettingParam(Define.HOME_KEY_DOWN, true));
		bundle.putBoolean(Define.CACHE_FILE_INVISIBLE, false);    //

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		
		if (checkPackage(context, Define.PACKAGENAME_ENT))
		{
			intent.setClassName(Define.PACKAGENAME_ENT, Define.CLASSNAME);
		}
		else if (checkPackage(context, Define.PACKAGENAME))
		{
			intent.setClassName(Define.PACKAGENAME, Define.CLASSNAME);
		}
		else if (checkPackage(context, Define.PACKAGENAME_ENG))
		{
			intent.setClassName(Define.PACKAGENAME_ENG, Define.CLASSNAME);
		}
		else if (checkPackage(context, Define.PACKAGENAME_KING_ENT))
		{
			intent.setClassName(Define.PACKAGENAME_KING_ENT, Define.CLASSNAME);
		}
		else if (checkPackage(context, Define.PACKAGENAME_KING_PRO))
		{
			intent.setClassName(Define.PACKAGENAME_KING_PRO, Define.CLASSNAME);
		}
		else if (checkPackage(context, Define.PACKAGENAME_KING_PRO_HW))
		{
			intent.setClassName(Define.PACKAGENAME_KING_PRO_HW, Define.CLASSNAME);
		}
		else
		{
			showToast(context, "文件打开失败，移动wps可能未安装");
			return null;
		}
		
		File file = new File(path);
		if (file == null || !file.exists())
		{			
			showToast(context, "打开失败，文件不存在！");
			return null;
		}
		
		Uri uri = Uri.fromFile(file);
		intent.setData(uri);
		intent.putExtras(bundle);
  		
  		if (isAIDL)
			bundle.putBoolean(Define.SEND_CLOSE_BROAD, true);	 //aidl打开需要发送关闭广播，这样关闭文档时候才能把浮窗关闭
  		
  		return intent;
  	}
  	
    public static String getMIMEType(File f)
    {  
        String end = f.getName().substring(f.getName().lastIndexOf(".") + 1,  
                		f.getName().length()).toLowerCase();  
        String type = "";  
        if (end.equals("mp3") || end.equals("aac") || end.equals("aac") 
        		|| end.equals("amr") || end.equals("mpeg") || end.equals("mp4"))  
        {  
          type = "audio";  
        }
        else if (end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals("jpeg"))  
        {  
          type = "image";  
        } 
        else if (end.equals("doc") || end.equals("docx") || end.equals("pdf")
        		|| end.equals("txt"))
        {  
          type = "application/msword";  
          return type;
        }  
        else
        {
        	type = "*";
        }
        type += "/*";  
        return type;  
    } 
    
	/**
  	 * 判断是否是wps能打开的文件
  	 * @param file
  	 * @return
  	 */
  	public static boolean IsWPSFile(File file)
  	{
  		String end = file.getName().substring(file.getName().lastIndexOf(".") + 1,  
        		file.getName().length()).toLowerCase();  
  		if (end.equals("doc") || end.equals("docx") || end.equals("wps") 
			|| end.equals("dot") || end.equals("wpt") 
			|| end.equals("xls") || end.equals("xlsx") || end.equals("et") 
			|| end.equals("ppt") || end.equals("pptx") || end.equals("dps") 
			|| end.equals("txt") || end.equals("pdf"))
  			return true;
  		
  		return false;
  	}
  	
  	public static boolean isPDFFile(String filePath)
  	{
  		String path = filePath.toLowerCase();
		return path.endsWith(".pdf");
  	}
  	
  	public static boolean isPptFile(String filePath) {
  		filePath = filePath.toLowerCase();
  		return filePath.endsWith(".ppt")
  				|| filePath.endsWith("pptx");
  	}
  	
  	public static boolean isExcelFile(String filePath) {
  		filePath = filePath.toLowerCase();
  		return filePath.endsWith(".xls")
  				|| filePath.endsWith(".xlsx")
  				||filePath.endsWith(".et");
  	}
}
