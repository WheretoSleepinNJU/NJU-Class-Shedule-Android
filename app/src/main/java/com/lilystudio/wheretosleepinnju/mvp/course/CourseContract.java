package com.lilystudio.wheretosleepinnju.mvp.course;


import com.lilystudio.wheretosleepinnju.BasePresenter;
import com.lilystudio.wheretosleepinnju.BaseView;
import com.lilystudio.wheretosleepinnju.data.beanv2.CourseV2;

import java.util.List;

/**
 * Created by mnnyang on 17-10-3.
 */

public interface CourseContract {
    interface Presenter extends BasePresenter {
        void updateCourseViewData(long csNameId);
        void deleteCourse(long courseId);
    }

    interface View extends BaseView<Presenter> {
        void initFirstStart();
        void setCourseData(List<CourseV2> courses);
        void updateCoursePreference();
    }


}
