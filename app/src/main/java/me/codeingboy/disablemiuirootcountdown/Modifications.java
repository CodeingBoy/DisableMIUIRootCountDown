package me.codeingboy.disablemiuirootcountdown;

import android.app.Activity;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * 由 CodeingBoy 在 2015-06-14 0014 创建
 */
public class Modifications implements IXposedHookInitPackageResources, IXposedHookLoadPackage,
        IXposedHookZygoteInit {

    public static Button accept;
    public static TextView WarningText;
    private static XSharedPreferences prefs;
    private final static boolean ALLDISABLE = true;
    public static Activity activity;

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        /*
        prefs = new XSharedPreferences("me.codeingboy.disablemiuirootcountdown");
        prefs.makeWorldReadable();
        XposedBridge.log("DMRCD:" + String.valueOf(prefs.getAll().size()));
        */
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam
                                                       resparam)
            throws Throwable {
        if (!resparam.packageName.equals("com.miui.securitycenter"))
            return;

        // hook 界面
        resparam.res.hookLayout("com.miui.securitycenter", "layout", "pm_activity_root_apply",
                new XC_LayoutInflated() {
                    @Override
                    public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                        /*if (ALLDISABLE) { // 整个界面都不要了
                            Context context = liparam.view.getContext(); // 获取 Context 以获得界面
                            // Activity 实例
                            if (context instanceof Activity) {
                                activity = (Activity) context;
                                return;
                            }
                        }*/
                        // 获取控件
                        accept = (Button) liparam.view.findViewById(
                                liparam.res.getIdentifier("accept", "id", "com.miui" +
                                        ".securitycenter"));
                        WarningText = (TextView) liparam.view.findViewById(
                                liparam.res.getIdentifier("warning_info", "id", "com.miui" +
                                        ".securitycenter"));
                        if (WarningText != null)
                            WarningText.setLines(6); // 设置行数以免无法显示内容，设为 5 或 6 都可以
                    }
                });
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.miui.securitycenter")) return;

        // hook OnCreate，这是处理界面的最佳时机
        findAndHookMethod("com.miui.permcenter.root.RootApplyActivity", lpparam.classLoader,
                "onCreate", android.os.Bundle.class, new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (accept == null) return;  // 没有获取到按钮
                        for (int i = 0; i < 4; i++) {
                            accept.performClick(); // 发送点击事件以略过4次警告界面
                        }
                        if (ALLDISABLE){
                            accept.performClick();
                            return; // 界面看不到，后面的事情不需要做了
                        }
                /*
                reloadPref();
                XposedBridge.log(String.valueOf(prefs.getAll().size()));
                XposedBridge.log(prefs.getString("policy", "-1"));
                if (prefs.getInt("policy", -1) == 1) {
                    // 完全跳过提示界面
                    accept.performClick();
                    return; // 界面看不到，后面的事情不需要做了
                }
                */
                            accept.setEnabled(true);
                        accept.setText("允许");
                        //accept.setText(String.valueOf(prefs.getAll().size()));
                        if (WarningText != null) {
                            // 设置警告文字
                            String temp = "确定允许此应用请求系统最高管理权限吗？\n" +
                                    "允许后可能会产生系统不稳定、隐私数据（如帐号密码、短信）泄露等不良后果，\n" +
                                    "甚至造成不可挽回的硬件损坏！\n" +
                                    "建议您慎重使用！\n" +
                                    ((String) WarningText.getText()).split("！")[1];
                            WarningText.setText(temp);
                        }
                    }

                });


        // 这个类应该是一个 Handler，用来处理倒计时
        findAndHookMethod("com.miui.permcenter.root.c", lpparam.classLoader, "handleMessage",
                android.os.Message.class, new XC_MethodReplacement() {

                    @Override
                    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws
                            Throwable {
                        return null;
                    }
                });
    }

    // 重载设置
    public void reloadPref() {
        prefs.reload();
    }

}
