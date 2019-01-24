package cn.wps.moffice.demo.client;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.annotation.SuppressLint;
import android.os.RemoteException;
import android.util.Log;
import cn.wps.moffice.client.ActionType;
import cn.wps.moffice.client.AllowChangeCallBack;
import cn.wps.moffice.client.OfficeEventListener;
import cn.wps.moffice.client.OfficeInputStream;
import cn.wps.moffice.client.OfficeOutputStream;
import cn.wps.moffice.demo.floatingview.service.FloatServiceTest;
import cn.wps.moffice.demo.util.Define;
import cn.wps.moffice.demo.util.EncryptClass;
import cn.wps.moffice.demo.util.FileUtil;
import cn.wps.moffice.demo.util.SettingPreference;
import cn.wps.moffice.demo.util.ToastUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class OfficeEventListenerImpl extends OfficeEventListener.Stub 
{
	protected MOfficeClientService service = null;
	
	private final static String ZIP_ENTRY_FILE_NAME = "inside_file";
	
	private boolean mIsValidPackage = true;

	public OfficeEventListenerImpl( MOfficeClientService service )
	{
		this.service = service;
	}
	
	@Override
	public int onOpenFile( String path, OfficeOutputStream output )
			throws RemoteException 
	{
        Log.d("OfficeEventListener", "onOpenFile ：" + path);
        if (FileUtil.mTempStream!=null && output!=null){
            byte[] tempByte= FileUtil.mTempStream.toByteArray();
            output.write(tempByte,0,tempByte.length);
            return 0;
        }else{
            return -1;
        }

      /*
      if (!mIsValidPackage)
			return -1;

		SettingPreference settingPreference;
		settingPreference 	= 	new SettingPreference(this.service.getApplicationContext());
		boolean isEncrypt 	= settingPreference.getSettingParam(Define.ENCRYPT_FILE, false);
		
		if (isEncrypt)
			return EncryptClass.encryptOpenFile(path, output);
		else
			return EncryptClass.normalOpenFile(path, output);
			*/

	}
	
	@SuppressLint("CheckResult")
	@Override
	public int onSaveFile(OfficeInputStream input, String path)throws RemoteException 
	{
		Log.d("OfficeEventListener", "onSaveFile : " + path);

		FileUtil.updateFile(input).
				subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
				.subscribe(aBoolean -> {
                     ToastUtil.showShort("不落地保存结果:"+aBoolean.booleanValue());

				}, throwable -> {
					throwable.printStackTrace();
					ToastUtil.showShort("上传文件时网络异常");
				});

		return 0; // 没有上传完就返回0，可能有bug
		/*
		SettingPreference settingPreference;
		settingPreference 	= 	new SettingPreference(this.service.getApplicationContext());
		boolean isEncrypt 	= settingPreference.getSettingParam(Define.ENCRYPT_FILE, false);
		
		if (isEncrypt)
			return EncryptClass.encryptSaveFile(input, path);
		else
			return EncryptClass.normalSaveFile(input, path);
        */

	}

	@Override
	public int onCloseFile() throws RemoteException 
	{
		Log.d("OfficeEventListener", "onCloseFile");
		return 0;
	}
	
	@Override
	public boolean isActionAllowed(String path, ActionType type) throws RemoteException 
	{
		// Log.d("OfficeEventListener", "isActionAllowed ： " + type.toString() + "  Path : " + path);
		/*
		SettingPreference settingPreference;
		settingPreference 	= 	new SettingPreference(this.service.getApplicationContext());

		//光标输入模式，进行特殊处理
		if (type.equals(ActionType.AT_CURSOR_MODEL) 
		    && settingPreference.getSettingParam(type.toString(), true))
		{
			return isCursorMode(path, type);
		}
		
		if (type.equals(ActionType.AT_EDIT_REVISION)) 	//如果是接受或拒绝某条修订的事件,做特殊处理
		{
			return isRevisionMode(path, type, settingPreference);
		}
		
		boolean	 typeAT 	= 	settingPreference.getSettingParam(type.toString(), true);
		String	 pathAT 	= 	settingPreference.getSettingParam(Define.AT_PATH, "/");
		boolean  isExist 	= 	path.startsWith(pathAT) || path.equals("");  //有部分事件传过来路径为"",
		if (!typeAT && isExist)
			return false;
		*/
		return true;
	}
	
	//是否可以操作他人修订（作者名不同的修订）
	private boolean isRevisionMode(String path, ActionType type, SettingPreference settingPreference)
	{
		String docUserName  = settingPreference.getSettingParam(Define.USER_NAME, "");
		boolean	 typeAT 	= 	settingPreference.getSettingParam(type.toString(), true);
		boolean isSameOne 	= docUserName.equals(path);	//在此事件中，path中存放是是作者批注名
		if (!typeAT && !isSameOne)
		{
			return false;
		}
		
		return true;
	}
	
	//中广核特殊需求
	private boolean isCursorMode(String path, ActionType type) throws RemoteException
	{
		
		boolean flag = null != FloatServiceTest.getDocument() && FloatServiceTest.getDocument().getSelection().getStart() == FloatServiceTest.getDocument().getSelection().getEnd();
		
		if (!flag)
			return false;

		if (FloatServiceTest.getDocument().isProtectOn())
			return false;
		
		FloatServiceTest.getDocument().getSelection().getFont().setBold(true);
		FloatServiceTest.getDocument().getSelection().getFont().setItalic(true);
		FloatServiceTest.getDocument().getSelection().getFont().setName("宋体");
		FloatServiceTest.getDocument().getSelection().getFont().setStrikeThrough();
		FloatServiceTest.getDocument().getSelection().getFont().setSize((float)15.0);
		FloatServiceTest.getDocument().getSelection().getFont().setTextColor(0x00ff00);
		
		return true;
	}
	
	/**
	 * 实现多个可变包名的验证
	 * originalPackage是最原始的第三方包名，华为渠道为“com.huawei.svn.hiwork”
	 * thirdPackage为可变动的应用包名，具体有企业资金定制
	 */
	@Override
	public boolean isValidPackage(String originalPackage, String thirdPackage)
			throws RemoteException {
//此处是某些企业的特殊需求，可以忽略
//		mIsValidPackage = false;
//		if (originalPackage.equals(service.getPackageName()) && thirdPackage.equals("cn.wps.moffice"))
//		{
//			mIsValidPackage = true;
//			return true;
//		}
		return false;
	}

	@Override
	public void setAllowChangeCallBack(AllowChangeCallBack allowChangeCallBack)	throws RemoteException
	{
		FloatServiceTest.setAllowCallBack(allowChangeCallBack);
	}
	
	@Override
	public boolean isEncryptFile(String path)
	{
//		if (path == null || new File(path).exists() == false)
//			return false;
//		FileInputStream is = null;
//		try
//		{
//			is = new FileInputStream(path);
//		}
//		catch (FileNotFoundException e)
//		{
//			e.printStackTrace();
//			return false;
//		}
//		
//		final ZipInputStream zis = new ZipInputStream(is);
//		ZipEntry ze = null;
//        try {
//            ze = zis.getNextEntry();
//        }
//        catch (IOException e)
//        {
//        	e.printStackTrace();
//        	closeInputStream(zis);
//        	closeInputStream(is);
//        	return false;
//        }
//        if (null == ze || !ze.getName().equals(ZIP_ENTRY_FILE_NAME))
//        {
//        	closeInputStream(zis);
//        	closeInputStream(is);
//        	return false;
//        }
//        try 
//        {
//			zis.close();
//		}
//        catch (IOException e)
//        {
//			e.printStackTrace();
//			return false;
//		}
		return EncryptClass.isEncryptFile(path);
	}
	
	private static int closeInputStream(InputStream is)
	{
		try
		{
			is.close();
        }
		catch (IOException e)
		{
        	e.printStackTrace();
        	return -1;
        }
		return 0;
	}
}
