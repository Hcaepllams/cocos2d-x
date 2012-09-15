package org.cocos2dx.iap.ChinaMobile;

import java.util.Hashtable;

import org.cocos2dx.helloiap.R;
import org.cocos2dx.iap.IAPProducts;
import org.cocos2dx.iap.IAPWrapper;
import org.cocos2dx.iap.IAPWrapper.IAPAdapter;
import org.cocos2dx.iap.Wrapper;

import cn.emagsoftware.gamebilling.api.GameInterface;
import cn.emagsoftware.gamebilling.view.BillingView;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
//import android.telephony.PhoneStateListener;
//import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

//class SignalStrengthListener extends PhoneStateListener{  
//    @Override  
//    public void onSignalStrengthsChanged(SignalStrength signalStrength){  
//    	CMGCBillingAdapter.setSingleStrength(signalStrength.getGsmSignalStrength());
//    	super.onSignalStrengthsChanged(signalStrength);  
//    }  
//}

public class CMGCBillingAdapter implements IAPAdapter{

    
	private static CMGCBillingAdapter mAdapter = null;
	
//	private static SignalStrengthListener signalStrengthListener = null;
//	private static int singleStrength = 0;
	
//	public static void setSingleStrength(int singleStrengthValue){
//		singleStrength = singleStrengthValue;
//	}
	
	private static final boolean mDebug = false;
	private static void LogD(String msg) {
		if (mDebug) Log.d("CMGCBillingAdapter", msg);
	}
	
	private static String mProductIdentifier;
	public static String getCurrentProductId() { return mProductIdentifier; }
	
	public static CMGCBillingAdapter getInstance() {
    	return mAdapter;
    }
	
	public static void initialize() {
		mAdapter = new CMGCBillingAdapter();
        
		// cjh replace company name
		try {
			GameInterface.initializeApp(Wrapper.getActivity(), Wrapper.getActivity().getResources().getString(R.string.app_name), "Please replace me", "000-0000000");
		}catch (Exception e) {
			e.printStackTrace();
		}
//		signalStrengthListener = new SignalStrengthListener();
//		TelephonyManager phoneMgr = (TelephonyManager)Wrapper.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
//		phoneMgr.listen(signalStrengthListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}

	@Override
	public boolean isLogin() {
		// ���ڻ�����ҪLogin
		return true;
		// ��ȡ���ؼ����־�������ܵķ�ֹ���棬��IMEI
		//TelephonyManager tm = (TelephonyManager)Wrapper.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
		
		//final String key = "activateflag"+tm.getDeviceId();
		//final SharedPreferences sp = Wrapper.getActivity().getPreferences(Context.MODE_PRIVATE);
		//boolean activate = sp.getBoolean(key, false);
		//return activate;
	}

	@Override
	public void loginAsync() {
		LogD("loginAsync needn't be called!!!");
	}

	@Override
	public void networkUnReachableNotify() {
		if(null == Wrapper.getUIHandler()) return;
		
		Wrapper.getCocos2dxGLSurfaceView().post(new Runnable() {
            @Override
            public void run() {
            	Toast.makeText(Wrapper.getActivity(), R.string.strSimUnavailable, Toast.LENGTH_SHORT).show();
        	}
        });
    }

	@Override
	public void requestProductData(String product) {
		IAPWrapper.didReceivedProducts(product);
	}

	@Override
	public void addPayment(String productIdentifier) {
		LogD("addPayment" + productIdentifier);
		if (null == productIdentifier || null == Wrapper.getActivity()) {
			IAPWrapper.didFailedTransaction(productIdentifier);
			return;
		}
		mProductIdentifier = productIdentifier;
		Wrapper.getCocos2dxGLSurfaceView().post(new Runnable() {
            @Override
            public void run() {
            	Wrapper.getActivity().startActivity(new Intent(Wrapper.getActivity(), CMGCBillingActivity.class));
        	}
        });
		
//		Wrapper.getActivity().startActivity(new Intent(Wrapper.getActivity(), CMGCBillingActivity.class));
		
		LogD("addPayment " + productIdentifier);
	}

	@Override
	public boolean networkReachable() {
		boolean ret = true;
		do {
			if (null == Wrapper.getActivity()) break;
			
			// �����Ź����Ƿ����
			TelephonyManager telephonyManager=(TelephonyManager) Wrapper.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
			String imsi = telephonyManager.getSubscriberId();
			if (null != imsi && imsi.length() > 10) break;

			// �������ã�����false
			ret = false;
		} while (false);
		return ret;
	}

	///////////////////////////////////////////////////////////////////////////
	// ʵ��  �� CMGCBillingActivity �ص��Ľӿ�
	///////////////////////////////////////////////////////////////////////////

	static public boolean bHandled = false;
	static public void onBillingFinish() {
		//�Ʒ����̽���
		// mProductIdentifier = null;
		LogD("onBillingFinish" + mProductIdentifier);
	}

	static public void onBillingSuccess() {
		//�Ʒѳɹ�
		if (null == mProductIdentifier) return;
		LogD("onBillingSuccess" + mProductIdentifier);
		IAPWrapper.didCompleteTransaction(mProductIdentifier);
		// ����ɹ�����Flurry��¼һ��
		Hashtable<String, String> param = new Hashtable<String, String>();
		param.put(mProductIdentifier, "" + IAPProducts.getProductPrice(mProductIdentifier));
	//cjh	FlurryAPIWrapper.logEvent("Payment From CMGC", param);
		mProductIdentifier = null;
		bHandled = true;
	}

	static public void onUserOperCancel() {
		//�û�ȡ���Ʒ�����
		if (null == mProductIdentifier) return;
		LogD("onUserOperCancel" + mProductIdentifier);
		IAPWrapper.didFailedTransaction(mProductIdentifier);
		mProductIdentifier = null;
		bHandled = true;
	}

	static public void onUserOperError(int errCode) {
		Hashtable<String, String> param = new Hashtable<String, String>();
		param.put(mProductIdentifier, "" + IAPProducts.getProductPrice(mProductIdentifier));

		//��Ҫʵ�ִ���׽������errCode��ο��ĵ�
		switch(errCode){
		case BillingView.ERROR_SMS_SEND_FAILURE:
			bHandled = false;
			break;
		case BillingView.ERROR_WEB_NETWORK_ERROR:
			bHandled = true;
			IAPWrapper.didFailedTransaction(mProductIdentifier);
			mProductIdentifier = null;
			Toast.makeText(Wrapper.getActivity(), R.string.strNetworkUnReachable, Toast.LENGTH_SHORT).show();
			
			param.put("reason", "ERROR_WEB_NETWORK_ERROR");
			break;
		case BillingView.ERROR_BILLING_FAILURE:
			bHandled = true;
			IAPWrapper.didFailedTransaction(mProductIdentifier);
			mProductIdentifier = null;
			Toast.makeText(Wrapper.getActivity(), R.string.strSendConfirmSMSFailed, Toast.LENGTH_SHORT).show();
			
			param.put("reason", "ERROR_BILLING_FAILURE");
			break;
		default:
			bHandled = false;
			
			param.put("reason", "default case");
			break;
		}
		
		LogD("onUserOperError" + mProductIdentifier + errCode);
	//cjh	FlurryAPIWrapper.logEvent("onUserOperError", param);
	}
	
	static public void onActivityDestroy() {
		LogD("onActivityDestroy" + bHandled + CMGCBillingActivity.okBtnClicked);
		if (! bHandled){
			if (CMGCBillingActivity.okBtnClicked) {
				handleCheck();
			} else
			if (mProductIdentifier != null)
			{
				IAPWrapper.didFailedTransaction(mProductIdentifier);
			}
		}
		
		// reset
		bHandled = false;
	}
	
	static public void handleCheck() {
		if (mProductIdentifier == null) {
			return;
		}
		
		// ���Ͷ���ʧ�ܣ����û�����Ե��������ɣ������ɹ�
		ContentResolver cr = Wrapper.getActivity().getContentResolver();
		if ((! mProductIdentifier.equals(IAPWrapper.PRODUCT_ACTIVATE)) ||
			Settings.System.getInt(cr, Settings.System.AIRPLANE_MODE_ON, 0) == 1/* ||
			singleStrength > 40*/) {
			Toast.makeText(Wrapper.getActivity(), R.string.strSendSMSFailed, Toast.LENGTH_SHORT).show();
			IAPWrapper.didFailedTransaction(mProductIdentifier);
			mProductIdentifier = null;
		}
		else
		{
			LogD("handleCheck" + "bill Success!");
			onBillingSuccess();
		
			// ������Ҫ��¼һ�£��Ա��ѯ
			Hashtable<String, String> param = new Hashtable<String, String>();
			param.put(mProductIdentifier, "" + IAPProducts.getProductPrice(mProductIdentifier));
//			param.put("singleStrength", "" + singleStrength);
//cjh			FlurryAPIWrapper.logEvent("payment sucessfull though sms failed", param);
		}
		
		return;
	}

	@Override
	public String getAdapterName() {
		return "CMGC";
	}
}
