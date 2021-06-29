package com.example.myapplication.base;

import android.app.Activity;

import java.util.Iterator;
import java.util.Stack;

public class AppManager {
    private static Stack<Activity> activityStack;
    private static AppManager instance;

    private AppManager() {
    }

    public static AppManager getAppManager() {
        if (instance == null) {
            instance = new AppManager();
        }
        return instance;
    }

    public static Stack<Activity> getActivityStack() {
        return activityStack;
    }

    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack();
        }
        activityStack.add(activity);
    }

    public void removeActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
        }
    }

    public boolean isActivity() {
        if (activityStack != null) {
            return !activityStack.isEmpty();
        } else {
            return false;
        }
    }

    public Activity currentActivity() {
        Activity activity = (Activity) activityStack.lastElement();
        return activity;
    }

    public void finishActivity() {
        Activity activity = (Activity) activityStack.lastElement();
        this.finishActivity(activity);
    }

    public void finishActivity(Activity activity) {
        if (activity != null && !activity.isFinishing()) {
            activity.finish();
        }
    }

    public void finishActivity(Class<?> cls) {
        Iterator var2 = activityStack.iterator();

        while (var2.hasNext()) {
            Activity activity = (Activity) var2.next();
            if (activity.getClass().equals(cls)) {
                this.finishActivity(activity);
                break;
            }
        }
    }

    public void finishAllActivity() {
        int i = 0;
        for (int size = activityStack.size(); i < size; ++i) {
            if (null != activityStack.get(i)) {
                this.finishActivity((Activity) activityStack.get(i));
            }
        }
        activityStack.clear();
    }

    public Activity getActivity(Class<?> cls) {
        if (activityStack != null) {
            Iterator var2 = activityStack.iterator();

            while (var2.hasNext()) {
                Activity activity = (Activity) var2.next();
                if (activity.getClass().equals(cls)) {
                    return activity;
                }
            }
        }
        return null;
    }

    public void appExit() {
        try {
            finishAllActivity();
            // 杀死该应用进程
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } catch (Exception var2) {
            activityStack.clear();
            var2.printStackTrace();
        }
    }
}
