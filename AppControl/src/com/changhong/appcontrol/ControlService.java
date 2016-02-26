package com.changhong.appcontrol;

import java.util.List;

import com.changhong.appcontrol.analysis.LocalAnalysis;
import com.changhong.appcontrol.analysis.ServerAnalysis;
import com.changhong.appcontrol.analysis.ServerAnalysisCallBack;
import com.changhong.appcontrol.analysis.VersionAnalysis;
import com.changhong.appcontrol.model.ControlInfo;
import com.changhong.appcontrol.model.InstallApp;
import com.changhong.appcontrol.model.LocalApp;
import com.changhong.appcontrol.model.UninsApp;
import com.changhong.appcontrol.operation.InstallTask;
import com.changhong.appcontrol.operation.OperationCallBack;
import com.changhong.appcontrol.operation.UninstallTask;
import com.changhong.appcontrol.operation.UpdateTask;
import com.changhong.appcontrol.transfer.TransferUtils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * 
 * @author yangtong
 *
 */
public class ControlService extends Service {
	//	�������Ӧ��
	public List<UninsApp> uninsApps;
	public List<InstallApp> installApps;
	public List<InstallApp> updateApps;

	//	server ���
	public String jsonString;
	public ControlInfo controlInfo;
	//public List<InstallApp> serverApps;
	public ServerAnalysis serverAnalysis;
	
	//	local���
	public List<LocalApp> localApps;
	public List<LocalApp> usrApps;
	public LocalAnalysis localAnalysis;
	
	//	�Ƿ�����Ӧ�ö�����װ/ж��/�������
	private boolean uninsFinish = false;
	private boolean installFinish = false;
	private boolean updateFinish = false;
	//	�Ƿ���δִ�гɹ���
	private boolean hasFailure = false;
	
	private VersionAnalysis versionAnalysis;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		//	Server appList��ȡ
		jsonString = intent.getStringExtra("info");
		serverAnalysis = new ServerAnalysis(jsonString);
		controlInfo = serverAnalysis.controlInfo;
		
		Log.i("yangtong","curVersion >>"+controlInfo.version);
		//	�ж�Controlnfo�汾�Ƿ�ı�
		versionAnalysis = new VersionAnalysis(ControlService.this);
		if(!versionAnalysis.isVersionChanged(controlInfo.version)){
			Log.i("yangtong","version not changed");
			TransferUtils.requestAgain(ControlService.this, TransferUtils.REQUEST_DELAY);
			//stopSelf();
			return super.onStartCommand(intent, flags, startId);
		}else {
			Log.i("yangtong","version changed");
			//versionAnalysis.saveCurVersion(controlInfo.version);
		}		
		
		//	Local appList��ȡ
		localAnalysis = new LocalAnalysis(ControlService.this);
		localAnalysis.getLocalApps();
		localApps = localAnalysis.localApps;
		usrApps = localAnalysis.usrApps;
		
		hasFailure = false;
		uninsFinish = false;
		installFinish = false;
		updateFinish = false;
		
		//	�������Ҫִ�в�����Ӧ�ã�������Ϻ����callBack
		serverAnalysis.startAnalysis(serverCallBack, usrApps);	
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	/**
	 * Ӧ�÷�����Ϻ󴥷�
	 */
	private ServerAnalysisCallBack serverCallBack = new ServerAnalysisCallBack() {
		
		@Override
		public void analysisFinish(List<InstallApp> mInstallApps,
				List<InstallApp> mUpdateApps,List<UninsApp> mUninsApps) {
			// ����upgradeApps�е�Ӧ�ã���װinstallApps�е�Ӧ��
			installApps = mInstallApps;
			updateApps = mUpdateApps;
			uninsApps = mUninsApps;
			
			if(uninsApps!=null&&uninsApps.size()>0){
				UninstallTask uninsTask = new UninstallTask(operationCallBack);
				uninsTask.execute(uninsApps.get(0));
			}else {
				uninsFinish = true;
			}
			if(installApps!=null&&installApps.size()>0){
				InstallTask installTask = new InstallTask(operationCallBack);
				installTask.execute(installApps.get(0));
				Log.i("yangtong","installApps size>>"+installApps.size());
			}else {
				installFinish = true;
			}
			if(updateApps!=null&&updateApps.size()>0){
				UpdateTask updateTask = new UpdateTask(operationCallBack);
				updateTask.execute(updateApps.get(0));
				Log.i("yangtong","updateApps size>>"+updateApps.size());
			}else {
				updateFinish = true;
			}
			
			if(uninsFinish&&installFinish&&updateFinish){
				TransferUtils.requestAgain(ControlService.this, TransferUtils.REQUEST_DELAY);
				//stopSelf();
			}
		}
	};
	
	/**
	 * ж�ء���װ������ĳ��Ӧ�ú󴥷�
	 */
	private OperationCallBack operationCallBack = new OperationCallBack() {
		
		
		int updateCount = 0;
		int updateError = 0;
		UpdateTask updateTask;
		@Override
		public void updateFinish(boolean result) {
			if(result){	//���³ɹ�������������һ��
				updateError = 0;
				updateCount++;
				if(updateCount<updateApps.size()){
					updateTask = new UpdateTask(operationCallBack);
					updateTask.execute(updateApps.get(updateCount));
				}else {
					updateFinish = true;
					updateCount = 0;
				}
			}else {	//����ʧ�ܣ��ٴγ���
				updateError++;
				if(updateError<3){
					updateTask = new UpdateTask(operationCallBack);
					updateTask.execute(updateApps.get(updateCount));
				}else {
					hasFailure = true;
					updateError = 0;
					updateCount++;
					if(updateCount<updateApps.size()){
						updateTask = new UpdateTask(operationCallBack);
						updateTask.execute(updateApps.get(updateCount));
					}else {
						updateFinish = true;
					}
				}
			}
			if(uninsFinish&&installFinish&&updateFinish){
				if(!hasFailure){
					versionAnalysis.saveCurVersion(controlInfo.version);
				}	
				TransferUtils.requestAgain(ControlService.this, TransferUtils.REQUEST_DELAY);
				//stopSelf();
			}
		}
		
		int uninsCount = 0;
		int uninsError = 0;
		UninstallTask uninstallTask;
		@Override
		public void uninstallFinish(boolean result) {
			if(result){	//	ж�سɹ�������ж����һ��
				uninsCount++;
				uninsError = 0;
				if(uninsCount<uninsApps.size()){
					uninstallTask = new UninstallTask(operationCallBack);
					uninstallTask.execute(uninsApps.get(uninsCount));
				}else {
					uninsFinish = true;
					uninsCount = 0;
				}
			}else {	//	ж�ز��ɹ�������ж�أ�ʧ�����κ�ж����һ��
				uninsError++;
				if(uninsError<3){
					uninstallTask = new UninstallTask(operationCallBack);
					uninstallTask.execute(uninsApps.get(uninsCount));
				}else {
					hasFailure = true;
					uninsCount++;
					uninsError = 0;
					if(uninsCount<uninsApps.size()){
						uninstallTask = new UninstallTask(operationCallBack);
						uninstallTask.execute(uninsApps.get(uninsCount));
					}else {
						uninsFinish = true;
					}
				}
			}
			if(uninsFinish&&installFinish&&updateFinish){
				if(!hasFailure){
					versionAnalysis.saveCurVersion(controlInfo.version);
				}	
				TransferUtils.requestAgain(ControlService.this, TransferUtils.REQUEST_DELAY);
				//stopSelf();
			}
		}
		
		int installCount = 0;
		int installError = 0;
		InstallTask installTask;
		@Override
		public void installFinish(boolean result) {
			if(result){	//	��װ�ɹ���������װ��һ��
				installCount++;
				installError = 0;
				if(installCount<installApps.size()){
					installTask = new InstallTask(operationCallBack);
					installTask.execute(installApps.get(installCount));
				}else {
					installFinish = true;
					installCount = 0;
				}
			}else {	//	��װʧ�ܣ��ٴγ��԰�װ
				installError++;
				if(installError<3){
					installTask = new InstallTask(operationCallBack);
					installTask.execute(installApps.get(installCount));
				}else {
					hasFailure = true;
					installCount++;
					installError = 0;
					if(installCount<installApps.size()){
						installTask = new InstallTask(operationCallBack);
						installTask.execute(installApps.get(installCount));
					}else {
						installFinish = true;
					}
				}
			}
			if(uninsFinish&&installFinish&&updateFinish){
				if(!hasFailure){
					versionAnalysis.saveCurVersion(controlInfo.version);
				}			
				TransferUtils.requestAgain(ControlService.this, TransferUtils.REQUEST_DELAY);
				//stopSelf();
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		
		return null;
	}
	

}
