package com.changhong.appcontrol.operation;

import com.changhong.appcontrol.model.InstallApp;
import android.R.integer;
import android.os.AsyncTask;

/**
 * �½��߳��������ز���װĳ��Ӧ��
 * @author yangtong
 *
 */
public class InstallTask extends AsyncTask<InstallApp, integer, Boolean> {

	OperationCallBack callBack;
	
	public InstallTask(OperationCallBack callBack){
		this.callBack = callBack;
	}	
	
	@Override
	protected Boolean doInBackground(InstallApp... params) {	
		return OperationUtils.installPackage(params[0].installType, params[0].appUrl);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		callBack.installFinish(result);
	}

	
	
}
