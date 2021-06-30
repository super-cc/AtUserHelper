package love.isuper.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by guoshichao on 2021/6/30
 */
public class ToastUtils {

    private static Object iNotificationManagerObj;

    /**
     * @param context
     * @param message
     */
    public static void show(Context context, String message) {
        show(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
    }

    /**
     * @param context
     * @param message
     */
    public static void show(Context context, String message, int duration) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        //后setText 兼容小米默认会显示app名称的问题
        Toast toast = Toast.makeText(context, null, duration);
        toast.setText(message);
        if (isNotificationEnabled(context)) {
            toast.show();
        } else {
            showSystemToast(toast);
        }
    }

    /**
     * 显示系统Toast
     */
    private static void showSystemToast(Toast toast) {
        try {
            @SuppressLint("SoonBlockedPrivateApi")
            Method getServiceMethod = Toast.class.getDeclaredMethod("getService");
            getServiceMethod.setAccessible(true);
            //hook INotificationManager
            if (iNotificationManagerObj == null) {
                iNotificationManagerObj = getServiceMethod.invoke(null);

                Class iNotificationManagerCls = Class.forName("android.app.INotificationManager");
                Object iNotificationManagerProxy = Proxy.newProxyInstance(toast.getClass().getClassLoader(), new Class[]{iNotificationManagerCls}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        //强制使用系统Toast
                        if ("enqueueToast".equals(method.getName())
                                || "enqueueToastEx".equals(method.getName())) {  //华为p20 pro上为enqueueToastEx
                            args[0] = "android";
                        }
                        return method.invoke(iNotificationManagerObj, args);
                    }
                });
                Field sServiceFiled = Toast.class.getDeclaredField("sService");
                sServiceFiled.setAccessible(true);
                sServiceFiled.set(null, iNotificationManagerProxy);
            }
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 消息通知是否开启
     *
     * @return
     */
    private static boolean isNotificationEnabled(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        boolean areNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled();
        return areNotificationsEnabled;
    }
}
