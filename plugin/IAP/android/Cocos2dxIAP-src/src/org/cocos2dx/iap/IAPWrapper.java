package org.cocos2dx.iap;

import org.cocos2dx.iap.ChinaMobile.CMGCBillingAdapter;
import org.cocos2dx.iap.ChinaTelecom.DXIAPAdapter;

import android.widget.Toast;

public class IAPWrapper {	

	//cjh should read it from config file.
	public static final String PRODUCT_ACTIVATE = "ACTIVATE_PRODUCT";
	
	private static void LogD(String msg) {
		Wrapper.LogD("IAPWrapper", msg);
	}
	
	public interface IAPAdapter
	{		
		public boolean isLogin();
		
		public void loginAsync();

		public void networkUnReachableNotify();
		
		public void requestProductData(String product);

		public void addPayment(String productIdentifier);

		public boolean networkReachable();
		
		public String getAdapterName();
	}
	
	////////////////////////////////////////////////////////////////////////
	// IAPWrapper
	public static native boolean nativeIAPEnabled();
	public static native void nativeDidLoginFailed();
	public static native void nativeDidLoginSuccess();
	public static native void nativeDidReceivedProducts(String products);
	public static native void nativeDidFailedTransaction(String productIdentifier);
	public static native void nativeDidCompleteTransaction(String productIdentifier);
	
	////////////////////////////////////////////////////////////////////////
	// IAPProducts
	public static native String nativeGetProductInfo(String productId);

	private static String getPrepareParam(String productIdentifier) {
		return "";
//		return 	Wrapper.getAppID() + ","
//				+ NativeWrapper.getChannalIDString() + ","
//				+ mCurrentAdapter.getAdapterName() + ","
//				+ Wrapper.getUid() + ","
//				+ Wrapper.getLocalMacAddress() + ","
//				+ Wrapper.getPhoneNumber() + ","
//				+ IAPProducts.getProductCoinNum(productIdentifier) + ","
//				+ IAPProducts.getProductPrice(productIdentifier) + ","
//				+ Build.MANUFACTURER + "-" + Build.MODEL + ","
////cjh				+ DataStat.getCurDay() + "%20" + DataStat.getCurTime() + ","
//				+ Wrapper.getVersionName() + "-" + Wrapper.getVersionCode() + ","
//				+ IAPProducts.getProductGid(productIdentifier) + ","
//                + Wrapper.getImsiNumber();
	}
	
	private static String getConfirmParam(String productIdentifier){
		return "";
//		return  Wrapper.getAppID() + ","
//			    + NativeWrapper.getChannalIDString() + ","
//			    + mCurrentAdapter.getAdapterName() + ","
//			    + Wrapper.getUid() + ","
//			    + Wrapper.getLocalMacAddress() + ","
//				+ Wrapper.getPhoneNumber() + ","
//				+ IAPProducts.getProductCoinNum(productIdentifier) + ","
//				+ IAPProducts.getProductPrice(productIdentifier) + ","
//				+ Build.MANUFACTURER + "-" + Build.MODEL + ","
//				+ IAPWrapper.getOid() + "," 	// ֧���������صĶ�����
//				+ IAPWrapper.getSoid() + ","	// ���ط��صĶ�����
////cjh				+ DataStat.getCurDay() + "%20" + DataStat.getCurTime() + ","
//				+ "" + "," // Remark
//				+ Wrapper.getVersionName() + "-" + Wrapper.getVersionCode() + ","
//				+ IAPProducts.getProductGid(productIdentifier) + ","
//                + Wrapper.getImsiNumber();
	}

	public static boolean enabled() {
		boolean ret = IAPWrapper.nativeIAPEnabled();
		if (false == ret) LogD("nativeEnabled return false!");
		return ret;
	}

	private static IAPAdapter mCurrentAdapter = null;
	private static IAPAdapter mOtherAdapter = null;

	public static void setOtherAdapter(IAPAdapter adapter) {
		mOtherAdapter = adapter;
	}
	
	public static void addPayment(String productIdentifier) {
		LogD("addPayment:" + productIdentifier);
		if (null != mCurrentAdapter) {
			if (enabled()) {
				mCurrentAdapter.addPayment(productIdentifier);
//cjh				DataStat.preparePay(getPrepareParam(productIdentifier));
			}
		}
	}

	public static boolean networkReachable() {
		LogD("networkReachable");
		if (null != mCurrentAdapter) {
			if (enabled()) {
				return mCurrentAdapter.networkReachable();
			}
		}
		return false;
	}
	
	public static void networkUnReachableNotify() {
		LogD("networkUnReachableNotify");
		if (null != mCurrentAdapter){
			if (enabled()) mCurrentAdapter.networkUnReachableNotify();
		}
	}
	
	private static String mProductWanted = null;
	public static void requestProductData(String product, int payMode) {
		
		// ѡ��һ��֧����ʽ
		switch (payMode) {
		case 1:
			// ����֧����ʽ 
			mCurrentAdapter = mOtherAdapter;
			break;
		case 2:
			{
				// ����֧����ʽ����Ҫ���ݲ�ͬ�� sim ��ѡ��ͬ�Ķ���֧�� sdk 
				mCurrentAdapter = getSMSAdapter();
			}
			break;
		case 3:
			{
				// �Զ�ѡ��֧����ʽ���ȼ�����֧�� 
				mCurrentAdapter = getSMSAdapter();
				if (null == mCurrentAdapter) {
					mCurrentAdapter = mOtherAdapter;
				}
			}
			break;
		}
		
		LogD("requestProductData" + product);
		
		if (null == mCurrentAdapter) {
			// ֻ��ѡ��ʹ�ö��Ÿ��Ѳ��п��ܳ����Ҳ������� adapter �����
			Wrapper.postEventToMainThread(new Runnable() {
	            @Override
	            public void run() {
	            	Toast.makeText(Wrapper.getActivity(), R.string.strSimUnavailable, Toast.LENGTH_SHORT).show();
	        	}
	        });
			didFailedTransaction(product);
			return;
		}
		
		if (false == enabled()) return;
		
		if (false == networkReachable()) {
			networkUnReachableNotify();
			didFailedTransaction(product);
			return;
		}
		
		if (false == mCurrentAdapter.isLogin()) {
			mProductWanted = product;
			mCurrentAdapter.loginAsync();
			return;
		}
		mCurrentAdapter.requestProductData(product);
	}

	private static IAPAdapter getSMSAdapter() {
		IAPAdapter ret = null;

		String imsi = Wrapper.getImsiNumber();
		do {
			if (null == imsi) break;
			if(imsi.startsWith("46000") || imsi.startsWith("46002") || imsi.startsWith("46007")){
				//��Ϊ�ƶ�������46000�µ�IMSI�Ѿ����꣬����������һ��46002��ţ�134/159�Ŷ�ʹ���˴˱��  
		        //�й��ƶ�  
				ret = CMGCBillingAdapter.getInstance();
			}else if(imsi.startsWith("46001")){  
		        //�й���ͨ  
		    }else if(imsi.startsWith("46003")){  
		        //�й�����  
		    	ret = DXIAPAdapter.getInstance();
		    }
		} while(false);

		return ret;
	}
	
	public static void afterLogin() {
		LogD("AfterLogin");
		if (null == mProductWanted || null ==mCurrentAdapter) {
			return;
		}
		if (enabled()) {
			String product = mProductWanted;
			mProductWanted = null;
			mCurrentAdapter.requestProductData(product);
		}
	}
	
	public static void didLoginFailed() {
		LogD("didLoginFailed");
		if (null == mCurrentAdapter) return;
		if (false == enabled()) return;
		Wrapper.postEventToGLThread(new Runnable() {
   	            @Override
   	            public void run() {
   	            	IAPWrapper.nativeDidLoginFailed();
   	            }
		});
	}
	
	public static void didLoginSuccess() {
		LogD("didLoginSuccess");
		if (null == mCurrentAdapter) return;
		if (false == enabled()) return;
		Wrapper.postEventToGLThread(new Runnable() {
   	            @Override
   	            public void run() {
   	            	IAPWrapper.nativeDidLoginSuccess();
   	            }
		});
	}
	
	public static void didReceivedProducts(final String products) {
		LogD("didReceivedProducts:" + products);
		if (null == mCurrentAdapter) return;
		if (false == enabled()) return;
		Wrapper.postEventToGLThread(new Runnable() {
   	            @Override
   	            public void run() {
   	            	IAPWrapper.nativeDidReceivedProducts(products);
   	            }
		});
	}
	
	public static void didFailedTransaction(final String productIdentifier) {
		LogD("didFailedTransaction:" + productIdentifier);
		if (false == enabled()) return;
		Wrapper.postEventToGLThread(new Runnable() {
   	            @Override
   	            public void run() {
   	            	IAPWrapper.nativeDidFailedTransaction(productIdentifier);
   	            }
		});
	}
	
	public static void didCompleteTransaction(final String productIdentifier) {
		LogD("didCompleteTransaction:" + productIdentifier);
		if (null == mCurrentAdapter) return;
		if (false == enabled()) return;
		Wrapper.postEventToGLThread(new Runnable() {
   	            @Override
   	            public void run() {
   	            	IAPWrapper.nativeDidCompleteTransaction(productIdentifier);
   	            }
		});
		
//cjh		DataStat.confirmPay(getConfirmParam(productIdentifier));
	}
}
