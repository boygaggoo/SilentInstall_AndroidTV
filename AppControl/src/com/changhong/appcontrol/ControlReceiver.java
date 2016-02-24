package com.changhong.appcontrol;

import com.changhong.appcontrol.transfer.InfoTransfer;
import com.changhong.appcontrol.transfer.TransferCallBack;
import com.changhong.appcontrol.transfer.TransferUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/**
 * AppControl���
 * @author yangtong
 *
 */
public class ControlReceiver extends BroadcastReceiver implements TransferCallBack{

	public static final String TAG = "yangtong";
	//public static final String SERVER_IP = "http://192.168.10.107";
	public static final String SERVER_IP = "http://192.168.103.211";
	//public static final String INFO_URL = "http://yangtong.me/control.json";
		
	private Context context;
	
	InfoTransfer transfer;
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "ControlReceiver onReceive");
		this.context = context;
		if(!TransferUtils.isNetworkAvaiable(context)){//	������ʱ2���Ӻ��ٴλ�ȡ
			TransferUtils.requestAgain(context, TransferUtils.REQUEST_DELAY);
			return;
		}		
		transfer = new InfoTransfer();
		transfer.GetControlInfo(SERVER_IP+"/control.json", this);
	}
	


	/**
	 * �ӷ�������ȡjson���ݽ����󴥷�
	 */
	@Override
	public void transferFinish(String controlJson) {
		// �ж���Ч�ԣ������Ч����StartService���д���
		Log.i(TAG,"controlInfo >>"+controlJson);
		if(transfer.isInfoEffective(controlJson)){
			// ��Service�����������Ĳ���
			Intent intent = new Intent(context,ControlService.class);
			intent.setAction("com.changhong.service.appcontrol");
			intent.putExtra("info", controlJson);
			context.startService(intent);
		}else {
			// ��һ��ʱ���������������
			TransferUtils.requestAgain(context, TransferUtils.REQUEST_DELAY);
		}
	}
	
}
