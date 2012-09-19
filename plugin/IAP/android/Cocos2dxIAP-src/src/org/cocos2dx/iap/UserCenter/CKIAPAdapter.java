package org.cocos2dx.iap.UserCenter;

import org.cocos2dx.iap.IAPProducts;
import org.cocos2dx.iap.IAPWrapper;
import org.cocos2dx.iap.R;
import org.cocos2dx.iap.Wrapper;

import com.chukong.usercenter.InitHelper;
import com.chukong.usercenter.ProductInfo;
import com.chukong.usercenter.ResultFlag;
import com.chukong.usercenter.SingleGamePay;

import android.os.Handler;
import android.widget.Toast;

public class CKIAPAdapter implements org.cocos2dx.iap.IAPWrapper.IAPAdapter {
	
	private static CKIAPAdapter mAdapter = null;

    private static void LogD(String msg) {
		Wrapper.LogD("UserCenter-IAPAdapter", msg);
	}
    
    private static String mProductIdentifier = null;
    
    private static SingleGamePay singleGamePay = null;

    private static Handler initResultHandler = null;
    private static Handler payResultHandler = null;
    
    static {
		initResultHandler = new Handler() {
	    	public void handleMessage(android.os.Message msg) {
	    		switch (msg.what) {
	    		case ResultFlag.INIT_SUCCESS:
	    			singleGamePay = new SingleGamePay(Wrapper.getActivity());
	    			break;
	    		case ResultFlag.INIT_FAILED:
	    		default:
	    			break;
	    		}
	    	}
	    };
	    
	    payResultHandler = new Handler() {
	    	public void handleMessage(android.os.Message msg) {
	    		switch (msg.what) {
	    		case ResultFlag.RQF_PAY_SUCCEED:
	    			IAPWrapper.didCompleteTransaction(mProductIdentifier);
	    			break;
	    		case ResultFlag.RQF_PAY_CANCLE:
	    		case ResultFlag.RQF_PAY_FAILED:
	    		case ResultFlag.RQF_PAY_KEEP:
	    		default:
	    			IAPWrapper.didFailedTransaction(mProductIdentifier);
	    			break;
	    		}
	    	}
	    };
    }

	public static void initialize(String appKey, String secretKey) {
		mAdapter = new CKIAPAdapter();
		IAPWrapper.setOtherAdapter(mAdapter);

		// ��ʼ��SDK 
		InitHelper init = new InitHelper(Wrapper.getActivity());
		init.initSDK(appKey, secretKey, initResultHandler);
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
		Wrapper.postEventToMainThread(new Runnable() {
            @Override
            public void run() {
            	String tip = Wrapper.getActivity().getResources().getString(R.string.ccxiap_strNetworkUnReachable);
            	String imsi = Wrapper.getImsiNumber();
            	if (null != imsi && imsi.startsWith("46001")) {
            		// ���й���ͨ�û� 
            		tip += Wrapper.getActivity().getResources().getString(R.string.ccxiap_strUnicomTip);
            	}
            	Toast.makeText(Wrapper.getActivity(), tip, Toast.LENGTH_SHORT).show();
        	}
        });
	}

	public void notifyIAPToExit() {
		IAPWrapper.nativeNotifyGameExit();
	}
	
	@Override
	public void requestProductData(String product) {
		LogD("requestProductData : " + product);

		final String productId = product;
		Wrapper.postEventToGLThread(new Runnable() {
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

		final float fPrice = IAPProducts.getProductPrice(mProductIdentifier);
		if (0.0f == fPrice) {
			IAPWrapper.didFailedTransaction(productIdentifier);
			return;
		}
		
		// ����֧���ӿ� 

		Wrapper.postEventToMainThread(new Runnable() {
            @Override
            public void run() {
				//ProductInfo productInfo = new ProductInfo("600", "0.01", "600���");
            	ProductInfo productInfo = new ProductInfo("123"/*""+IAPProducts.getProductCoinNum(mProductIdentifier)*/,
				""+fPrice, IAPProducts.getProductName(mProductIdentifier));
            	singleGamePay.startNologinPay(productInfo, payResultHandler);
            }
		});
	}

	@Override
	public boolean networkReachable() {
		// �ж��Ƿ�ɷ�����	
		boolean ret = false;
		do {
			// ���IMSI
			if (null == singleGamePay)
				break;
			ret = true;
		} while (false);
		return ret;
	}

	@Override
	public String getAdapterName() {
		return "ChuKong";
	}
}


