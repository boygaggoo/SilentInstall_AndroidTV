package com.changhong.appcontrol.analysis;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * json�汾�ŷ���������
 * @author yangtong
 *
 */
public class VersionAnalysis {

	private SharedPreferences versionPref;
	public static final String PREF_VERSION = "control_version";
	
	public VersionAnalysis(Context context){
		this.versionPref = context.getSharedPreferences(PREF_VERSION,Context.MODE_PRIVATE);
	}
	
	/**
	 * ��ȡ��һ�α���İ汾�š�
	 * @return
	 */
	public String getLastVersion(){
		
		return versionPref.getString("version", null);
	}
	
	/**
	 * �汾���Ƿ����ı�
	 * @param curVersion �˴ν��������İ汾��
	 * @return
	 */
	public boolean isVersionChanged(String curVersion){
		if(curVersion.equals(getLastVersion())){
			return false;
		}else {
			return true;
		}
	}
	
	/**
	 * ����ǰ�汾�ű��浽SharedPreferences
	 * @param curVersion
	 * @return
	 */
	public boolean saveCurVersion(String curVersion){
		SharedPreferences.Editor editor = versionPref.edit();
		editor.putString("version", curVersion);
		editor.commit();
		Log.i("yangtong","Save Cur Version "+curVersion);
		return true;
	}
	
}
