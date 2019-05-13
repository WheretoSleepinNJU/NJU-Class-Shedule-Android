package com.lilystudio.wheretosleepinnju.widget;

import android.app.job.JobParameters;
import android.app.job.JobService;

import com.lilystudio.wheretosleepinnju.app.AppUtils;
import com.lilystudio.wheretosleepinnju.utils.LogUtil;

public class UpdateJobService extends JobService {

    @Override
    public void onCreate() {
        LogUtil.e(this, "onCreate");
        super.onCreate();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        LogUtil.e(this, "UpdateJobService任务执行");
        AppUtils.updateWidget(getApplicationContext());
        jobFinished(params, false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    @Override
    public void onDestroy() {
        LogUtil.e(this, "onDestroy");

        super.onDestroy();
    }
}
