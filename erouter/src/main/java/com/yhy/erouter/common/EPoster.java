package com.yhy.erouter.common;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.View;

import com.yhy.erouter.ERouter;
import com.yhy.erouter.callback.Callback;
import com.yhy.erouter.expt.IllegalOperationException;
import com.yhy.erouter.expt.UrlMatchException;
import com.yhy.erouter.interceptor.EInterceptor;
import com.yhy.erouter.mapper.EInterceptorMapper;
import com.yhy.erouter.mapper.ERouterGroupMapper;
import com.yhy.erouter.utils.EUtils;
import com.yhy.erouter.utils.LogUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 19:46
 * version: 1.0.0
 * desc   : 路由转发器
 */
@SuppressWarnings("unchecked")
public class EPoster {
    private final String TAG = getClass().getSimpleName();
    // 当前环境
    private Activity mActivity;
    private Fragment mFragmentV4;
    private android.app.Fragment mFragment;
    private Service mService;
    // 分组
    private String mGroup;
    // 路径
    private String mUrl;
    // 保存url和路由数据的集合
    private Map<String, RouterMeta> mMetaMap;
    // 目标Activity的Uri
    private Uri mUri;
    // Activity跳转时的Action
    private String mAction;
    // 路由参数
    private Bundle mParams;
    // 拦截器名称集合
    private List<String> mInterList;
    // 拦截器名称和实例映射集合
    private Map<String, EInterceptor> mInterMap;
    // 请求码，只有startActivityForResult时使用
    private int mRequestCode;
    // 路由回调
    private Callback mCallback;
    // Activity切换动画
    private int mTransEnter;
    private int mTransExit;
    // Activity共享元素动画
    private Pair<View, String>[] mAnimArr;
    private ActivityOptionsCompat mOptions;

    // Intent Flag List
    private List<Integer> mFlagList;

    /**
     * 构造函数
     *
     * @param activity 当前Activity
     */
    public EPoster(Activity activity) {
        this(activity, null, null, null);
    }

    /**
     * 构造函数
     *
     * @param fragmentV4 当前Fragment
     */
    public EPoster(Fragment fragmentV4) {
        this(null, fragmentV4, null, null);
    }

    /**
     * 构造函数
     *
     * @param fragment 当前Fragment
     */
    public EPoster(android.app.Fragment fragment) {
        this(null, null, fragment, null);
    }

    /**
     * 构造函数
     *
     * @param service 当前Service
     */
    public EPoster(Service service) {
        this(null, null, null, service);
    }

    /**
     * 构造函数
     *
     * @param activity   当前Activity
     * @param fragmentV4 当前Fragment
     * @param fragment   当前Fragment
     * @param service    当前Service
     */
    private EPoster(Activity activity, Fragment fragmentV4, android.app.Fragment fragment, Service service) {
        mActivity = activity;
        mFragmentV4 = fragmentV4;
        mFragment = fragment;
        mService = service;

        // 初始化
        mRequestCode = -1;
        mMetaMap = new HashMap<>();
        mParams = new Bundle();
        mInterList = new ArrayList<>();
        mInterMap = new HashMap<>();
        mFlagList = new ArrayList<>();
    }

    /**
     * 设置目标路径
     *
     * @param url 目标路径
     * @return 当前构造器
     */
    public EPoster to(String url) {
        return to(EUtils.getGroupFromUrl(url), url);
    }

    /**
     * 设置目标路径
     *
     * @param group 分组名称
     * @param url   目标路径
     * @return 当前构造器
     */
    public EPoster to(String group, String url) {
        mGroup = TextUtils.isEmpty(group) ? EUtils.getGroupFromUrl(url) : group;
        mUrl = url;
        LogUtils.i(TAG, "Set url as '" + mUrl + "'.");
        return this;
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, int value) {
        return setParam(TypeKind.INT.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, byte value) {
        return setParam(TypeKind.BYTE.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, short value) {
        return setParam(TypeKind.SHORT.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, boolean value) {
        return setParam(TypeKind.BOOLEAN.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, long value) {
        return setParam(TypeKind.LONG.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, float value) {
        return setParam(TypeKind.FLOAT.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, double value) {
        return setParam(TypeKind.DOUBLE.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, String value) {
        return setParam(TypeKind.STRING.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, Serializable value) {
        return setParam(TypeKind.SERIALIZABLE.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, Parcelable value) {
        return setParam(TypeKind.PARCELABLE.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, Object value) {
        return setParam(TypeKind.OBJECT.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param bundle bundle参数
     * @return 当前对象
     */
    public EPoster param(Bundle bundle) {
        if (null == mParams) {
            mParams = bundle;
        } else {
            mParams.putAll(bundle);
        }
        return this;
    }

    /**
     * 添加拦截器
     *
     * @param name 拦截器名称
     * @return 当前对象
     */
    public EPoster interceptor(String name) {
        if (!mInterList.contains(name)) {
            mInterList.add(name);
            LogUtils.i(TAG, "Add interceptor '" + name + "' successfully.");
        }
        return this;
    }

    /**
     * Activity切换动画
     *
     * @param enter 进入动画
     * @param exit  退出动画
     * @return 当前对象
     */
    public EPoster transition(int enter, int exit) {
        mTransEnter = enter;
        mTransExit = exit;
        LogUtils.i(TAG, "Set animation of enter and exit are '" + enter + "' and '" + exit + "' successfully.");
        return this;
    }

    /**
     * 添加Activity共享元素动画
     * <p>
     * API 16+ 有效
     *
     * @param name 共享名称
     * @param view 共享控件
     * @return 当前对象
     */
    @SuppressWarnings("unchecked")
    public EPoster animate(String name, View view) {
        // 需要动态控制数组大小，不能直接使用List或者Vector的toArray()方法（类型强制转换失败）
        if (null == mAnimArr) {
            mAnimArr = new Pair[]{Pair.create(view, name)};
        } else {
            Pair<View, String>[] temp = mAnimArr;
            // 扩容 +1
            mAnimArr = new Pair[mAnimArr.length + 1];
            // 拷贝数组
            System.arraycopy(temp, 0, mAnimArr, 0, temp.length);
            mAnimArr[mAnimArr.length - 1] = Pair.create(view, name);
        }
        LogUtils.i(TAG, "Add shared animation '" + name + "' on '" + view + "' successfully.");
        return this;
    }

    /**
     * 添加Intent flag
     *
     * @param flag Intent flag
     * @return 当前对象
     */
    public EPoster flag(int flag) {
        mFlagList.add(flag);
        LogUtils.i(TAG, "Add flag '" + flag + "' successfully.");
        return this;
    }

    /**
     * 设置目标Activity的Uri
     *
     * @param uri 目标Activity的Uri
     * @return 当前对象
     */
    public EPoster uri(Uri uri) {
        mUri = uri;
        LogUtils.i(TAG, "Set uri as '" + uri + "'.");
        return this;
    }

    /**
     * 设置Activity跳转的Action
     *
     * @param action Activity跳转的Action
     * @return 当前对象
     */
    public EPoster action(String action) {
        mAction = action;
        LogUtils.i(TAG, "Set action as '" + action + "'.");
        return this;
    }

    /**
     * 获取目标
     *
     * @param <T> 目标对象类型
     * @return 目标对象
     * <p>
     * 值：
     * Activity :: XxxxActivity.class
     * Fragment :: new XxxxFragment()
     * Service  :: XxxxService.class
     */
    public <T> T get() {
        // 先尝试从缓存中获取
        RouterMeta meta = mMetaMap.get(mUrl);
        if (null != meta) {
            return parseResult(meta);
        }

        // 缓存中没有再从路由映射器中获取
        Map<String, RouterMeta> metaMap = getMetaMap();
        if (null != metaMap) {
            return parseResult(metaMap.get(mUrl));
        }
        return null;
    }

    /**
     * 转发路由
     *
     * @param <T> 目标对象类型
     * @return 目标对象
     * <p>
     * 值：
     * Activity :: XxxxActivity.class
     * Fragment :: new XxxxFragment()
     * Service  :: XxxxService.class
     */
    public <T> T go() {
        return go(mRequestCode, null);
    }

    /**
     * 转发路由
     *
     * @param requestCode 请求码
     * @param <T>         目标对象类型
     * @return 目标对象
     * <p>
     * 值：
     * Activity :: XxxxActivity.class
     * Fragment :: new XxxxFragment()
     * Service  :: XxxxService.class
     */
    public <T> T go(int requestCode) {
        return go(requestCode, null);
    }

    /**
     * 转发路由
     *
     * @param callback 回调
     * @param <T>      目标对象类型
     * @return 目标对象
     * <p>
     * 值：
     * Activity :: XxxxActivity.class
     * Fragment :: new XxxxFragment()
     * Service  :: XxxxService.class
     */
    public <T> T go(Callback callback) {
        return go(mRequestCode, callback);
    }

    /**
     * 转发路由
     *
     * @param requestCode 请求码
     * @param callback    回调
     * @param <T>         目标对象类型
     * @return 目标对象
     * <p>
     * 值：
     * Activity :: XxxxActivity.class
     * Fragment :: new XxxxFragment()
     * Service  :: XxxxService.class
     */
    public <T> T go(int requestCode, Callback callback) {
        mRequestCode = requestCode;
        mCallback = callback;

        LogUtils.i(TAG, "Post to '" + mUrl + "' start.");

        // 优先判断Uri跳转，Uri跳转的目标只有Activity
        if (null != mUri) {
            return (T) postActivity(null);
        }

        // 执行路由，先从缓存中获取，不存在再加载
        RouterMeta meta = mMetaMap.get(mUrl);
        if (null != meta) {
            return post(meta);
        }

        // 缓存中没有，加载路由
        Map<String, RouterMeta> metaMap = getMetaMap();
        if (null != metaMap) {
            return post(metaMap.get(mUrl));
        }
        return null;
    }

    /**
     * 获取当前路由上下文
     *
     * @return 当前路由上下文
     */
    public Context getContext() {
        return null != mActivity ? mActivity : null != mFragmentV4 ? mFragmentV4.getActivity() : mService;
    }

    /**
     * 路由转发
     *
     * @param meta 路由数据
     * @param <T>  目标对象类型
     * @return 目标对象
     */
    @SuppressWarnings("unchecked")
    private <T> T post(RouterMeta meta) {
        if (null != meta) {
            // 先执行拦截器
            if (null != mInterList && !mInterList.isEmpty()) {
                loadInterceptors();
                createCurrentInterceptors();

                // 执行拦截器队列
                EInterceptor interceptor;
                for (String name : mInterList) {
                    interceptor = mInterMap.get(name);
                    LogUtils.i(TAG, "Execute interceptor named '" + name + "' that '" + interceptor + "'.");
                    if (interceptor.execute(this)) {
                        // 中断路由
                        LogUtils.i(TAG, "The interceptor named '" + name + "' that '" + interceptor + "' interrupted current router.");
                        return null;
                    }
                }
            }

            // 所有拦截器都通过后
            // 针对不同的路由类型，选择对应的路由转发
            switch (meta.getType()) {
                case ACTIVITY: {
                    Intent acInte = postActivity(meta);
                    return null == acInte ? null : (T) acInte;
                }
                case SERVICE: {
                    Intent svInte = postService(meta);
                    return null == svInte ? null : (T) svInte;
                }
                case FRAGMENT_V4: {
                    Fragment fm = postFragmentV4(meta);
                    return null == fm ? null : (T) fm;
                }
                case FRAGMENT: {
                    android.app.Fragment fm = postFragment(meta);
                    return null == fm ? null : (T) fm;
                }
                case UNKNOWN:
                default: {
                    break;
                }
            }
        }
        throw new UrlMatchException("Not found router which " + mUrl);
    }

    /**
     * 根据设置的拦截器名称列表，创建对应的拦截器对象，并保存到Map集合中
     */
    private void createCurrentInterceptors() {
        Class<? extends EInterceptor> clazz;
        EInterceptor interceptor;
        for (String name : mInterList) {
            // 先从映射器缓存中获取到映射器
            clazz = EInterMapCache.getInstance().get(name);
            if (null != clazz) {
                try {
                    interceptor = clazz.newInstance();
                    mInterMap.put(name, interceptor);
                    LogUtils.i(TAG, "Load interceptor '" + name + "' that '" + interceptor + "'.");
                } catch (InstantiationException e) {
                    if (null != mCallback) {
                        mCallback.onError(this, e);
                    }
                    LogUtils.e(e);
                } catch (IllegalAccessException e) {
                    if (null != mCallback) {
                        mCallback.onError(this, e);
                    }
                    LogUtils.e(e);
                }
            }
        }
    }

    /**
     * 加载拦截器映射器，并保存到拦截器映射器缓存中
     */
    @SuppressWarnings("unchecked")
    private void loadInterceptors() {
        if (EInterMapCache.getInstance().get().isEmpty()) {
            try {
                // 加载映射器
                Class<? extends EInterceptorMapper> clazz = (Class<? extends EInterceptorMapper>) Class.forName(EConsts.INTERCEPTOR_PACKAGE + "." + EInterceptorMapper.class.getSimpleName() + EConsts.SUFFIX_INTERCEPTOR_CLASS);
                EInterceptorMapper interMapper = clazz.newInstance();
                // 定义接收拦截器映射关系的集合
                Map<String, Class<? extends EInterceptor>> interMap = new HashMap<>();
                // 执行映射器的加载方法
                interMapper.load(interMap);
                // 将映射关系集合保存到缓存中
                EInterMapCache.getInstance().putAll(interMap);
            } catch (ClassNotFoundException e) {
                if (null != mCallback) {
                    mCallback.onError(this, e);
                }
                LogUtils.e(e);
            } catch (InstantiationException e) {
                if (null != mCallback) {
                    mCallback.onError(this, e);
                }
                LogUtils.e(e);
            } catch (IllegalAccessException e) {
                if (null != mCallback) {
                    mCallback.onError(this, e);
                }
                LogUtils.e(e);
            }
        }
    }

    /**
     * 转发Fragment路由，创建目标Fragment实例
     *
     * @param meta 路由数据
     * @return 目标Fragment实例
     */
    private Fragment postFragmentV4(RouterMeta meta) {
        try {
            Fragment fm = (Fragment) meta.getDest().newInstance();
            fm.setArguments(mParams);
            if (null != mCallback) {
                mCallback.onPosted(this);
            }
            LogUtils.i(TAG, "Post fragment v4.");
            return fm;
        } catch (InstantiationException e) {
            if (null != mCallback) {
                mCallback.onError(this, e);
            }
            LogUtils.e(e);
        } catch (IllegalAccessException e) {
            if (null != mCallback) {
                mCallback.onError(this, e);
            }
            LogUtils.e(e);
        }
        return null;
    }

    /**
     * 转发Fragment路由，创建目标Fragment实例
     *
     * @param meta 路由数据
     * @return 目标Fragment实例
     */
    private android.app.Fragment postFragment(RouterMeta meta) {
        try {
            android.app.Fragment fm = (android.app.Fragment) meta.getDest().newInstance();
            fm.setArguments(mParams);
            if (null != mCallback) {
                mCallback.onPosted(this);
            }
            LogUtils.i(TAG, "Post fragment.");
            return fm;
        } catch (InstantiationException e) {
            if (null != mCallback) {
                mCallback.onError(this, e);
            }
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            if (null != mCallback) {
                mCallback.onError(this, e);
            }
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 转发Service路由，创建并启动目标服务
     *
     * @param meta 路由数据
     * @return 目标Service.class
     */
    private Intent postService(RouterMeta meta) {
        Intent intent = null;
        try {
            if (null != mActivity) {
                // Activity中创建服务
                intent = new Intent(mActivity, meta.getDest());
                addFlags(intent);
                intent.putExtras(mParams);
                mActivity.startService(intent);
                LogUtils.i(TAG, "Post to '" + mUrl + "' from '" + mActivity + "'.");
            } else if (null != mFragmentV4) {
                // Fragment中创建服务
                intent = new Intent(mFragmentV4.getActivity(), meta.getDest());
                addFlags(intent);
                intent.putExtras(mParams);
                mFragmentV4.getActivity().startService(intent);
                LogUtils.i(TAG, "Post to '" + mUrl + "' from '" + mFragmentV4 + "'.");
            } else if (null != mFragment) {
                // Fragment中创建服务
                intent = new Intent(mFragment.getActivity(), meta.getDest());
                addFlags(intent);
                intent.putExtras(mParams);
                mFragment.getActivity().startService(intent);
                LogUtils.i(TAG, "Post to '" + mUrl + "' from '" + mFragment + "'.");
            } else if (null != mService) {
                // Service中创建服务
                intent = new Intent(mService, meta.getDest());
                addFlags(intent);
                intent.putExtras(mParams);
                mService.startService(intent);
                LogUtils.i(TAG, "Post to '" + mUrl + "' from '" + mService + "'.");
            }
            // 成功转发回调
            if (null != mCallback) {
                mCallback.onPosted(this);
            }
        } catch (Exception e) {
            if (null != mCallback) {
                mCallback.onError(this, e);
            }
            LogUtils.e(e);
        }
        return intent;
    }

    /**
     * 转发Activity路由，跳转到目标Activity
     *
     * @param meta 路由数据
     * @return 目标Activity.class
     */
    private Intent postActivity(RouterMeta meta) {
        if (null == meta && null == mUri) {
            throw new UrlMatchException("Either 'url' or 'uri' is not null, but both of them are null.");
        }
        Intent intent = null;
        try {
            if (null != mActivity) {
                // Activity中跳转Activity
                if (null == mUri) {
                    intent = new Intent(mActivity, meta.getDest());
                } else {
                    intent = new Intent(mAction, mUri);
                    LogUtils.i(TAG, "Post to uri '" + mUri + "' from '" + mActivity + "' with action '" + mAction + "'.");
                }
                addFlags(intent);
                intent.putExtras(mParams);
                // 设置共享元素动画
                makeAnimate(mActivity);
                if (mRequestCode == -1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mActivity.startActivity(intent, null == mOptions ? null : mOptions.toBundle());
                    } else {
                        mActivity.startActivity(intent);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mActivity.startActivityForResult(intent, mRequestCode, null == mOptions ? null : mOptions.toBundle());
                    } else {
                        mActivity.startActivityForResult(intent, mRequestCode);
                    }
                }
                // 设置切换动画
                overrideTransition(mActivity);
            } else if (null != mFragmentV4) {
                // Fragment中跳转Activity
                if (null == mUri) {
                    intent = new Intent(mFragmentV4.getActivity(), meta.getDest());
                } else {
                    intent = new Intent(mAction, mUri);
                    LogUtils.i(TAG, "Post to '" + mUri.getPath() + "' from '" + mFragmentV4 + "' with action '" + mAction + "'.");
                }
                addFlags(intent);
                intent.putExtras(mParams);
                makeAnimate(mFragmentV4.getActivity());
                if (mRequestCode == -1) {
                    mFragmentV4.startActivity(intent, null == mOptions ? null : mOptions.toBundle());
                } else {
                    mFragmentV4.startActivityForResult(intent, mRequestCode, null == mOptions ? null : mOptions.toBundle());
                }
                overrideTransition(mFragmentV4.getActivity());
            } else if (null != mFragment) {
                // Fragment中跳转Activity
                if (null == mUri) {
                    intent = new Intent(mFragment.getActivity(), meta.getDest());
                } else {
                    intent = new Intent(mAction, mUri);
                    LogUtils.i(TAG, "Post to '" + mUri.getPath() + "' from '" + mFragment + "' with action '" + mAction + "'.");
                }
                addFlags(intent);
                intent.putExtras(mParams);
                makeAnimate(mFragment.getActivity());
                if (mRequestCode == -1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mFragment.startActivity(intent, null == mOptions ? null : mOptions.toBundle());
                    } else {
                        mFragment.startActivity(intent);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mFragment.startActivityForResult(intent, mRequestCode, null == mOptions ? null : mOptions.toBundle());
                    } else {
                        mFragment.startActivityForResult(intent, mRequestCode);
                    }
                }
                overrideTransition(mFragment.getActivity());
            } else if (null != mService) {
                // Service中跳转Activity页面
                if (null == mUri) {
                    intent = new Intent(mService, meta.getDest());
                } else {
                    intent = new Intent(mAction, mUri);
                    LogUtils.i(TAG, "Post to '" + mUri.getPath() + "' from '" + mService + "' with action '" + mAction + "'.");
                }
                addFlags(intent);
                intent.putExtras(mParams);
                mService.startActivity(intent);
            }
            // 成功转发回调
            if (null != mCallback) {
                mCallback.onPosted(this);
            }
        } catch (Exception e) {
            if (null != mCallback) {
                mCallback.onError(this, e);
            }
        }
        return intent;
    }

    /**
     * Activity切换动画
     *
     * @param activity 当前Activity
     */
    private void overrideTransition(Activity activity) {
        if (null != activity && mTransEnter > 0 && mTransExit > 0) {
            activity.overridePendingTransition(mTransEnter, mTransExit);
        }
    }

    /**
     * Activity共享元素动画
     *
     * @param activity 当前Activity
     */
    private void makeAnimate(Activity activity) {
        if (null != activity && null != mAnimArr && mAnimArr.length > 0) {
            mOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, mAnimArr);
        }
    }

    /**
     * 向Intent中添加Flag
     *
     * @param intent Intent
     */
    private void addFlags(Intent intent) {
        if (null != intent && null != mFlagList && !mFlagList.isEmpty()) {
            for (int flag : mFlagList) {
                intent.addFlags(flag);
            }
        }
    }

    /**
     * 设置参数
     *
     * @param type  参数类型
     * @param name  参数名称
     * @param value 参数值
     * @param <T>   具体参数类型
     * @return 当前对象
     */
    private <T> EPoster setParam(Integer type, String name, T value) {
        if (null == type || TextUtils.isEmpty(name) || null == value) {
            return this;
        }

        if (type == TypeKind.SERIALIZABLE.ordinal()) {
            // Serializable 无法从字符串解析
            mParams.putSerializable(name, (Serializable) value);
        } else if (type == TypeKind.PARCELABLE.ordinal()) {
            // Parcelable 无法从字符串解析
            mParams.putParcelable(name, (Parcelable) value);
        } else if (type == TypeKind.OBJECT.ordinal()) {
            // 将对象转换为Json传递
            EJsonParser jsonParser = ERouter.getInstance().getJsonParser();
            if (null == jsonParser) {
                throw new IllegalOperationException("If you want to use EJsonParser, must set EJsonParser in initialization of ERouter!");
            }
            mParams.putString(name, jsonParser.toJson(value));
        } else {
            String strVal = (String) value;

            if (type == TypeKind.INT.ordinal()) {
                mParams.putInt(name, Integer.valueOf(strVal));
            } else if (type == TypeKind.BYTE.ordinal()) {
                mParams.putByte(name, Byte.valueOf(strVal));
            } else if (type == TypeKind.SHORT.ordinal()) {
                mParams.putShort(name, Short.valueOf(strVal));
            } else if (type == TypeKind.INT.ordinal()) {
                mParams.putBoolean(name, Boolean.valueOf(strVal));
            } else if (type == TypeKind.LONG.ordinal()) {
                mParams.putLong(name, Long.valueOf(strVal));
            } else if (type == TypeKind.FLOAT.ordinal()) {
                mParams.putFloat(name, Float.valueOf(strVal));
            } else if (type == TypeKind.DOUBLE.ordinal()) {
                mParams.putDouble(name, Double.valueOf(strVal));
            } else if (type == TypeKind.STRING.ordinal()) {
                mParams.putString(name, strVal);
            } else {
                // 默认传入字符串
                mParams.putString(name, strVal);
            }
        }
        LogUtils.i(TAG, "Add arg '" + name + "' successfully, value is '" + value + "'.");
        return this;
    }

    /**
     * 根据路由类型，获取目标对象
     *
     * @param meta 路由数据
     * @param <T>  目标对象类型
     * @return 目标对象
     * <p>
     * 值：
     * Activity :: XxxxActivity.class
     * Fragment :: new XxxxFragment()
     * Service  :: XxxxService.class
     */
    @SuppressWarnings("unchecked")
    private <T> T parseResult(RouterMeta meta) {
        if (null != meta) {
            switch (meta.getType()) {
                case ACTIVITY:
                case SERVICE: {
                    // Activity和Service都返回Xxxx.class
                    return (T) meta.getDest();
                }
                case FRAGMENT_V4:
                case FRAGMENT: {
                    // Fragment返回new XxxxFragment()
                    try {
                        return (T) meta.getDest().newInstance();
                    } catch (InstantiationException e) {
                        LogUtils.e(e);
                    } catch (IllegalAccessException e) {
                        LogUtils.e(e);
                    }
                }
                case UNKNOWN:
                default: {
                    break;
                }
            }
        }
        throw new UrlMatchException("Not found router which " + mUrl);
    }

    /**
     * 从路由映射器中获取当前分组下的路由映射集合，并将其保存到缓存中
     *
     * @return 一个分组下的路由映射集合
     */
    private Map<String, RouterMeta> getMetaMap() {
        // 先尝试从缓存中获取
        Map<String, RouterMeta> metaMap = EGroupMapCache.getInstance().get(mGroup);
        if (null != metaMap) {
            return metaMap;
        }

        // 缓存中不存在时再从路由映射器中获取
        metaMap = new HashMap<>();
        try {
            // 加载当前分组对应的java类
            Class<?> clazz = Class.forName(EConsts.GROUP_PACKAGE + "." + EConsts.PREFIX_OF_GROUP + EUtils.upCaseFirst(mGroup));
            // 获取到加载路由的方法
            Method loadGroup = clazz.getDeclaredMethod(EConsts.METHOD_ROUTER_LOAD, Map.class);
            // 创建当前分组的路由映射器对象
            ERouterGroupMapper erg = (ERouterGroupMapper) clazz.newInstance();
            // 执行映射器的加载路由方法
            loadGroup.invoke(erg, metaMap);
        } catch (ClassNotFoundException e) {
            LogUtils.e(e);
        } catch (NoSuchMethodException e) {
            LogUtils.e(e);
        } catch (IllegalAccessException e) {
            LogUtils.e(e);
        } catch (InstantiationException e) {
            LogUtils.e(e);
        } catch (InvocationTargetException e) {
            LogUtils.e(e);
        }
        // 存放到缓存中
        EGroupMapCache.getInstance().put(mGroup, metaMap);
        return metaMap;
    }
}
