package org.cocos2dx.iap.ChinaMobile;

import org.cocos2dx.helloiap.HelloIAP;

import cn.emagsoftware.gamebilling.api.GameInterface.AnimationCompleteCallback;
import cn.emagsoftware.gamebilling.view.OpeningAnimation;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/** ��Ϸ��һ��չ����Ļ��ʾ��Activity���� */
public class CMGCInitialization extends Activity {

	// ��Ϸ��������View.
	private OpeningAnimation mOpeningAnimation;
   
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // չ�ֿ����������棬��Ҫ������Աʵ�ֶ�����ɺ���߼�������������Ϸ������
        // ��ѡ���Ƿ�����Ϸ��������
       
        mOpeningAnimation = new OpeningAnimation(this, new AnimationCompleteCallback() {
			@Override
			public void onAnimationCompleted(boolean isMusicEnabled) {
				if(isMusicEnabled){
					// ��Ϸ��������Ҫʵ�ֵĴ��룺��������Ϸ��������
					//cjh FishingJoyWrapper.setInitalBackgroundMusicVolume(1.0f);
				}
				else {
					//cjh FishingJoyWrapper.setInitalBackgroundMusicVolume(0);
				}
				// ��Ϸ��������Ҫ�ڴ˴���ʵ����Ϸ�������չ�ִ���
				CMGCInitialization.this.startActivity(new Intent(CMGCInitialization.this, HelloIAP.class));
				CMGCInitialization.this.finish();
			}
		});
        setContentView(mOpeningAnimation);
    }

}