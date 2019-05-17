package com.lilystudio.wheretosleepinnju.mvp.about;

import com.lilystudio.wheretosleepinnju.BasePresenter;
import com.lilystudio.wheretosleepinnju.BaseView;
import com.lilystudio.wheretosleepinnju.data.beanv2.VersionWrapper;

/**
 * Created by mnnyang on 17-11-3.
 */

public interface AboutContract {
    interface Presenter extends BasePresenter {
        void checkUpdate();
    }

    interface View extends BaseView<AboutContract.Presenter> {
    }
}
