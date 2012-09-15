package org.cocos2dx.iap.ChinaTelecom;

import org.cocos2dx.helloiap.R;
import org.cocos2dx.iap.IAPProducts;
import org.cocos2dx.iap.IAPWrapper;
import org.cocos2dx.iap.Wrapper;

import cn.game189.sms.SMSListener;

import com.egamefei.sdk.control.AiDouListener;
import com.egamefei.sdk.control.EGameFei;

import android.widget.Toast;

public class DXIAPAdapter implements org.cocos2dx.iap.IAPWrapper.IAPAdapter {
	
	private static DXIAPAdapter mAdapter = null;
	
	private static String mIMSI;
	private static String mProductIdentifier;

    private static void LogD(String msg) {
		Wrapper.LogD("DXIAPAdapter", msg);
	}
    
    public static DXIAPAdapter getInstance() {
    	return mAdapter;
    }

    public static void initialize() {
		mAdapter = new DXIAPAdapter();
		
		// ��ʼ��SDK���ڶ��������ǵ����ṩ��������Դ 
		EGameFei.init(Wrapper.getActivity(), "90235529"); //cjh ������Դ�����������ļ��ж�ȡ
		
		// ����֧���Ļص�
		EGameFei.setAidouListener(new AiDouListener() {
			
			// resultCode������� 0�ɹ�,1ʧ��,message�����ʾ��,toolKey����id
		    @Override
			public void onResult(int resultCode, String message, String toolKey) {		    	
		    	switch (resultCode)
		    	{
		    	case 0:
		    		// �ɹ� 
		    		IAPWrapper.didCompleteTransaction(mProductIdentifier);
		    		break;
		    	case 1:
		    	default:
		    		// ʧ�� 
		    		IAPWrapper.didFailedTransaction(mProductIdentifier);
		    		break;
		    	}
		    }
		});
		
		EGameFei.setSmsListener(new SMSListener() {
			@Override
			public void smsOK(String feeName, String toolKey) {
				//feeName �Ʒѵ��ʶ,toolKey ����id
				IAPWrapper.didCompleteTransaction(mProductIdentifier);
			}
	
			@Override
			public void smsFail(String feeName, int errorCode, String toolKey)  {
				//feeName �Ʒѵ��ʶ,errorCode ������,toolKey ����id
				IAPWrapper.didFailedTransaction(mProductIdentifier);
			}
	
			@Override
			public void smsCancel(String feeName, int errorCode, String toolKey)  {
				//feeName �Ʒѵ��ʶ,errorCode ������,toolKey ����id
				IAPWrapper.didFailedTransaction(mProductIdentifier);
			}
		});

		mIMSI = Wrapper.getImsiNumber();
	}

	@Override
	public boolean isLogin() {
		return true;
	}

	@Override
	public void loginAsync() {
		// isLoginһֱ����true�� ��Ӧ�õ��õ�����
		IAPWrapper.didLoginFailed();
	}

	@Override
	public void networkUnReachableNotify() {
		LogD("networkUnReachableNotify");
		Wrapper.getCocos2dxGLSurfaceView().post(new Runnable() {
            @Override
            public void run() {
            	Toast.makeText(Wrapper.getActivity(), R.string.strNetworkUnReachable, Toast.LENGTH_SHORT).show();
        	}
        });
	}

	@Override
	public void requestProductData(String product) {
		LogD("requestProductData : " + product);

		final String productId = product;
		Wrapper.getCocos2dxGLSurfaceView().post(new Runnable() {
            @Override
            public void run() {
        		IAPWrapper.didReceivedProducts(productId);
        	}
        });
	}
 

	@Override
	public void addPayment(String productIdentifier) {
		LogD("addPayment : " + productIdentifier);
		mProductIdentifier = productIdentifier;
		String smsKey = IAPProducts.getProductDXSMSKey(mProductIdentifier);
		
		if (smsKey == null || smsKey.length() == 0) {
			IAPWrapper.didFailedTransaction(mProductIdentifier);
			return;
		}
		
		// ���õ���֧���ӿ� 
		EGameFei.pay(smsKey);
	}

	@Override
	public boolean networkReachable() {
		// �ж��Ƿ�ɷ�����	
		boolean ret = false;
		do {
			// ���IMSI
			if (null == mIMSI)
				break;
			if (! mIMSI.startsWith("46003"))
				break;
			ret = true;
		} while (false);
		return ret;
	}

	@Override
	public String getAdapterName() {
		return "DianXin";
	}

	
}


