package org.cocos2dx.iap.ChinaTelecom;

import org.cocos2dx.iap.IAPProducts;
import org.cocos2dx.iap.IAPWrapper;
import org.cocos2dx.iap.Wrapper;
import cn.game189.sms.SMSListener;
import com.egamefei.sdk.control.AiDouListener;
import com.egamefei.sdk.control.EGameFei;

public class ChinaTelecomIAPAdapter implements org.cocos2dx.iap.IAPAdapter {
	
	private static ChinaTelecomIAPAdapter mAdapter = null;
	
	private static String mIMSI;
	private static String mProductIdentifier;

    private static void LogD(String msg) {
		Wrapper.LogD("ChinaTelecom-IAPAdapter", msg);
	}
    
    public static ChinaTelecomIAPAdapter getInstance() {
    	return mAdapter;
    }

    public static boolean initialize(String cFromer) {
    	boolean ret = false;
    	final String strFromer = cFromer;
    	Wrapper.postEventToMainThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
		    	try {
					mAdapter = new ChinaTelecomIAPAdapter();
					
					// ��ʼ��SDK���ڶ��������ǵ����ṩ��������Դ 
					EGameFei.init(Wrapper.getActivity(), strFromer);
					
					// ����֧���Ļص�
					EGameFei.setAidouListener(new AiDouListener() {
						
						// resultCode������� 0�ɹ�,1ʧ��,message�����ʾ��,toolKey����id
					    @Override
						public void onResult(int resultCode, String message, String toolKey) {		    	
					    	switch (resultCode)
					    	{
					    	case 0:
					    		// �ɹ� 
					    		IAPWrapper.finishTransaction(mProductIdentifier, true, IAPWrapper.kErrorNone);
					    		break;
					    	case 1:
					    	default:
					    		// ʧ�� 
					    		IAPWrapper.finishTransaction(mProductIdentifier, false, IAPWrapper.kErrorPurchaseFailed);
					    		break;
					    	}
					    }
					});
					
					EGameFei.setSmsListener(new SMSListener() {
						@Override
						public void smsOK(String feeName, String toolKey) {
							//feeName �Ʒѵ��ʶ,toolKey ����id
							IAPWrapper.finishTransaction(mProductIdentifier, true, IAPWrapper.kErrorNone);
						}
				
						@Override
						public void smsFail(String feeName, int errorCode, String toolKey)  {
							//feeName �Ʒѵ��ʶ,errorCode ������,toolKey ����id
							IAPWrapper.finishTransaction(mProductIdentifier, false, IAPWrapper.kErrorPurchaseFailed);
						}
				
						@Override
						public void smsCancel(String feeName, int errorCode, String toolKey)  {
							//feeName �Ʒѵ��ʶ,errorCode ������,toolKey ����id
							IAPWrapper.finishTransaction(mProductIdentifier, false, IAPWrapper.kErrorUserCancelled);
						}
					});
			
					mIMSI = Wrapper.getImsiNumber();
					IAPWrapper.setIAPAdapter(mAdapter);
		    	} catch (Exception e) {
		    		e.printStackTrace();
		    	}
			}
		});

    	ret = true;
    	return ret;
	}

//	@Override
//	public void notifyServiceInvalid() {
//		LogD("notifyServiceInvaild");
//		Wrapper.postEventToMainThread(new Runnable() {
//            @Override
//            public void run() {
//            	Toast.makeText(Wrapper.getActivity(), R.string.ccxiap_strNetworkUnReachable, Toast.LENGTH_SHORT).show();
//        	}
//        });
//	}

	public void notifyIAPToExit() {
		IAPWrapper.notifyGameExit();
	}
	
	@Override
	public void loadProduct(String product) {
		LogD("loadProduct : " + product);

		final String[] productIds = {product};
		Wrapper.postEventToGLThread(new Runnable() {
            @Override
            public void run() {
        		IAPWrapper.finishLoadProducts(productIds, true, IAPWrapper.kErrorNone);
        	}
        });
	}
 

	@Override
	public void purchaseProduct(String productIdentifier) {
		LogD("purchaseProduct : " + productIdentifier);
		mProductIdentifier = productIdentifier;
		final String smsKey = IAPProducts.getProductInfoByKey(mProductIdentifier, "DXSMSKey");
		
		if (smsKey == null || smsKey.length() == 0) {
			IAPWrapper.finishTransaction(mProductIdentifier, false, IAPWrapper.kErrorSmsKeyInvalid);
			return;
		}
		
		Wrapper.postEventToMainThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				// ���õ���֧���ӿ� 
				EGameFei.pay(smsKey);
			}
		});

	}

	@Override
	public boolean isServiceValid() {
		// �ж��Ƿ�ɷ�����	
		boolean ret = false;
		do {
			// ���IMSI
			if (null == mIMSI)
				break;
			if (mIMSI.startsWith("46003"))
			{
				ret = true;
			}
		} while (false);
		return ret;
	}

	@Override
	public String getAdapterName() {
		return "ChinaTelecom";
	}

	
}


