package com.changhong.appcontrol.operation;

public interface OperationCallBack {

	
	/**
	 * 
	 * @param result ��װ�Ƿ�ɹ�
	 */
	public void installFinish(boolean result);
	
	/**
	 * 
	 * @param result �����Ƿ�ɹ�
	 */
	public void updateFinish(boolean result);
	
	
	/**
	 * 
	 * @param result ж���Ƿ�ɹ�
	 */
	public void uninstallFinish(boolean result);
}
