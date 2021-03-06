package com.yhy.easyrouter;

import android.app.Application;

import com.google.gson.Gson;
import com.yhy.easyrouter.entity.User;
import com.yhy.easyrouter.utils.ToastUtils;
import com.yhy.erouter.ERouter;
import com.yhy.erouter.common.EJsonParser;

import java.lang.reflect.Type;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-19 17:56
 * version: 1.0.0
 * desc   :
 */
public class App extends Application {
    private static App instance;

    private User mUser;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        ToastUtils.init(this);

        // 初始化
        ERouter.getInstance()
                .init(this)
                .debug(BuildConfig.DEBUG)
                .jsonParser(new EJsonParser() {
                    Gson gson = new Gson();

                    @Override
                    public <T> T fromJson(String json, Type type) {
                        return gson.fromJson(json, type);
                    }

                    @Override
                    public <T> String toJson(T obj) {
                        return gson.toJson(obj);
                    }
                });
    }

    public static App getInstance() {
        return instance;
    }

    public void setUser(User user) {
        mUser = user;
    }

    public User getUser() {
        return mUser;
    }
}
