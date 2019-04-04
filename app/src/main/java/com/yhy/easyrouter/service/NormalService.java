package com.yhy.easyrouter.service;

import android.content.Intent;
import android.os.IBinder;

import com.yhy.easyrouter.base.BaseService;
import com.yhy.easyrouter.utils.ToastUtils;
import com.yhy.erouter.ERouter;
import com.yhy.erouter.annotation.Router;
import com.yhy.erouter.callback.Callback;
import com.yhy.erouter.common.EPoster;

import androidx.annotation.Nullable;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-18 10:59
 * version: 1.0.0
 * desc   :
 */
@Router(url = "/service/normal")
public class NormalService extends BaseService {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtils.toast("普通服务创建成功");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ERouter.getInstance()
                .with(this)
                .to("/activity/service")
                .flag(Intent.FLAG_ACTIVITY_NEW_TASK) // Service跳转Activity时最好加上改flag
                .go(new Callback() {
                    @Override
                    public void onPosted(EPoster poster) {
                        ToastUtils.toast("服务跳转Activity成功");
                    }

                    @Override
                    public void onError(EPoster poster, Throwable e) {
                        e.printStackTrace();
                    }
                });
        return super.onStartCommand(intent, flags, startId);
    }
}
