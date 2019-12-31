package com.lilystudio.wheretosleepinnju.add;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lilystudio.wheretosleepinnju.BaseActivity;
import com.lilystudio.wheretosleepinnju.R;
import com.lilystudio.wheretosleepinnju.app.Constant;
import com.lilystudio.wheretosleepinnju.custom.AutoCompleteTextViewLayout;
import com.lilystudio.wheretosleepinnju.custom.EditTextLayout;
import com.lilystudio.wheretosleepinnju.data.bean.Course;
import com.lilystudio.wheretosleepinnju.data.db.CourseDbDao;
import com.lilystudio.wheretosleepinnju.utils.DialogHelper;
import com.lilystudio.wheretosleepinnju.utils.DialogListener;
import com.lilystudio.wheretosleepinnju.utils.LogUtil;
import com.lilystudio.wheretosleepinnju.utils.Preferences;
import com.lilystudio.wheretosleepinnju.utils.spec.PopupWindowDialog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AddActivity extends BaseActivity implements AddContract.View, View.OnClickListener {

    private AddContract.Presenter mPresenter;

    private EditText mEtName;
    private AutoCompleteTextViewLayout mAtCompTVClassroom;
    private EditTextLayout mEtlTeacher;
    private LinearLayout mLLTime;
    private Button mBtnAddTimeSlot;
    private EditTextLayout mEtlTime;
    private EditTextLayout mEtlWeekRange;

    private int mSelectedWeek = 1;
    private int mSelectedNodeStart = 1;
    private int mSelectedNodeEnd = 2;
    private int mSelectedStartWeek = 1;
    private int mSelectedEndWeek = 16;

    private int mWeekType = Course.WEEK_ALL;

    /**
     * 编辑模式
     */
    private boolean isEditMode;
    private Course mCourse;

    private int mCourseId;
    private Button mBtnRemove;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        initBackToolbar(getString(R.string.add_course));

        initView();

        initDefaultValues();

        mPresenter = new AddPresenter(this);
    }

    private void initDefaultValues() {
        Intent intent = getIntent();
        Course course = (Course) intent.getSerializableExtra(Constant.INTENT_COURSE);
        if (course != null) {
            //set toolbar title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getString(R.string.edit_course));
            }

            mCourse = course;
            mCourseId = course.getCourseId();
            LogUtil.i(TAG, "id====" + mCourseId);

            mEtName.setText(course.getName());
            mAtCompTVClassroom.setText(course.getClassRoom());
            mEtlTeacher.setText(course.getTeacher());

            mSelectedWeek = course.getWeek();
            mSelectedStartWeek = course.getStartWeek();
            mSelectedEndWeek = course.getEndWeek();
            mSelectedNodeStart = course.getNodes().get(0);
            mSelectedNodeEnd = course.getNodes().get(course.getNodes().size() - 1);
            mWeekType = course.getWeekType();


            mBtnRemove.setVisibility(View.VISIBLE);
            isEditMode = true;
        }

        updateWeekNode();
        updateWeekRange();
    }

    private void initView() {
        mEtName = findViewById(R.id.et_course_name);
        mAtCompTVClassroom =  findViewById(R.id.etl_classroom);
        String[] classrooms=getResources().getStringArray(R.array.classrooms);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,classrooms);
        mAtCompTVClassroom.setAdapter(adapter);
        mAtCompTVClassroom.setDropDownVerticalOffset(2);

        mEtlTeacher = findViewById(R.id.etl_teacher);
        mLLTime=findViewById(R.id.ll_time);
        addTimeSlot();
//        mEtlTime = findViewById(R.id.etl_time);
        mEtlWeekRange = findViewById(R.id.etl_week_range);
        mBtnAddTimeSlot =findViewById(R.id.btn_add_time_slot);

        mBtnRemove = findViewById(R.id.btn_remove);

//        mEtlTime.setOnClickListener(this);
        mEtlWeekRange.setOnClickListener(this);
        mBtnAddTimeSlot.setOnClickListener(this);

        mBtnRemove.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_add:
                addAll();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addAll(){
        for(int i=0;i<mLLTime.getChildCount();i++){
            parseStr(((EditTextLayout)mLLTime.getChildAt(i).findViewById(R.id.etl_time)).getText());
            add();
            isEditMode=false;//防止多次添加
        }
        finish();
    }

    private void parseStr(String str){
        switch (str.charAt(1)){
            case '一':
                mSelectedWeek=1;
                break;
            case '二':
                mSelectedWeek=2;
                break;
            case '三':
                mSelectedWeek=3;
                break;
            case '四':
                mSelectedWeek=4;
                break;
            case '五':
                mSelectedWeek=5;
                break;
            case '六':
                mSelectedWeek=6;
                break;
            case '日':
                mSelectedWeek=7;
                break;
        }

        Pattern p=Pattern.compile("[0-9]+");
        Matcher matcher=p.matcher(str);
        matcher.find();
        mSelectedNodeStart=Integer.parseInt(matcher.group());
        mSelectedNodeEnd= matcher.find()? Integer.parseInt(matcher.group()):mSelectedNodeStart;

    }

    private void add() {
        Course course = new Course();

        int currentCsNameId = Preferences.getInt(
                getString(R.string.app_preference_current_cs_name_id), 0);

        String csName = CourseDbDao.newInstance().getCsName(currentCsNameId);

        LogUtil.i(this, "当前课表-->"+currentCsNameId);

        course.setName(mEtName.getText().toString().trim())
                .setCsName(csName)
                .setCsNameId(currentCsNameId)
                .setClassRoom(mAtCompTVClassroom.getText().trim())
                .setTeacher(mEtlTeacher.getText().trim())

                .setStartWeek(mSelectedStartWeek)
                .setEndWeek(mSelectedEndWeek)
                .setWeekType(mWeekType)
                .setWeek(mSelectedWeek);

        for (int i = mSelectedNodeStart; i <= mSelectedNodeEnd; i++) {
            course.addNode(i);
        }

        if (isEditMode) {
            course.setCourseId(mCourseId);
            mPresenter.updateCourse(course);
            return;
        }

        mPresenter.addCourse(course);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.etl_time:
                mEtlTime=v.findViewById(R.id.etl_time);
                timeAction();
                break;
            case R.id.iv_clear:
                mLLTime.removeViewAt((int)v.getTag());
                fresh();
                break;
            case R.id.etl_week_range:
                rangeAction();
                break;
            case R.id.btn_add_time_slot:
                addTimeSlot();
                break;
            case R.id.btn_remove:
                remove();
                break;
        }
    }

    private void addTimeSlot() {
        View timeIntervalView=View.inflate(this,R.layout.layout_time_slot,null);
        mEtlTime=timeIntervalView.findViewById(R.id.etl_time);
        mEtlTime.setOnClickListener(this);
        if(mLLTime.getChildCount()!=0){
            mEtlTime.getIvClear().setVisibility(View.VISIBLE);
            mEtlTime.getIvClear().setTag(mLLTime.getChildCount());
            mEtlTime.getIvClear().setOnClickListener(this);
        }
        mSelectedWeek=1;
        mSelectedNodeStart=1;
        mSelectedNodeEnd=2;
        updateWeekNode();
        mLLTime.addView(timeIntervalView);
    }

    private void fresh(){
        for(int i=0;i<mLLTime.getChildCount();i++){
            ImageView ivClear=((EditTextLayout)mLLTime.getChildAt(i).findViewById(R.id.etl_time)).getIvClear();
            if(i==0){
                ivClear.setVisibility(View.INVISIBLE);
            }
            else{
                ivClear.setVisibility(View.VISIBLE);
                ivClear.setTag(i);
                ivClear.setOnClickListener(this);
            }
        }
    }

    private void remove() {
        if (!isEditMode) {
            return;
        }

        new DialogHelper().showNormalDialog(this, getString(R.string.confirm_to_delete),
                "课程 【" + mCourse.getName() + "】" + Constant.WEEK[mCourse.getWeek()]
                        + "第" + mCourse.getNodes().get(0) + "节 " + "",
                new DialogListener() {
                    @Override
                    public void onPositive(DialogInterface dialog, int which) {
                        super.onPositive(dialog, which);
                        mPresenter.removeCourse(mCourseId);
                    }
                });
    }

    private void rangeAction() {
        new PopupWindowDialog().showWeekRangeDialog(this,
                mSelectedStartWeek, mSelectedEndWeek, mWeekType,
                new PopupWindowDialog.WeekRangeCallback() {
                    @Override
                    public void onSelected(int start, int end, int type) {
                        mSelectedStartWeek = start;
                        mSelectedEndWeek = end;
                        mWeekType = type;
                        updateWeekRange();
                    }
                });
    }

    private void timeAction() {
        new PopupWindowDialog().showSelectTimeDialog(this,
                mSelectedWeek, mSelectedNodeStart, mSelectedNodeEnd,
                new PopupWindowDialog.SelectTimeCallback() {
                    @Override
                    public void onSelected(int week, int nodeStart, int endStart) {
                        mSelectedWeek = week;
                        mSelectedNodeStart = nodeStart;
                        mSelectedNodeEnd = endStart;
                        updateWeekNode();
                    }
                });
    }

    private void updateWeekNode() {
        String string;
        if (mSelectedNodeStart == mSelectedNodeEnd) {
            string = getString(R.string.string_course_time_2,
                    Constant.WEEK[mSelectedWeek], mSelectedNodeStart);
        } else {
            string = getString(R.string.string_course_time,
                    Constant.WEEK[mSelectedWeek],
                    mSelectedNodeStart, mSelectedNodeEnd);
        }
        mEtlTime.setText(string);
    }

    private void updateWeekRange() {
        mEtlWeekRange.setText(getString(R.string.string_week_range,
                mSelectedStartWeek, mSelectedEndWeek));
    }

    @Override
    public void showAddFail(String msg) {
        toast(msg);
    }


    @Override
    public void onAddSucceed(Course course) {
        toast("【" + course.getName() + "】" + getString(R.string.add_succeed));
        notifiUpdateMainPage(Constant.INTENT_UPDATE_TYPE_COURSE);
//        finish();
    }

    @Override
    public void onDelSucceed() {
        toast(getString(R.string.delete_succeed));
        notifiUpdateMainPage(Constant.INTENT_UPDATE_TYPE_COURSE);
        finish();
    }

    @Override
    public void onUpdateSucceed(Course course) {
        toast("【" + course.getName() + "】" + getString(R.string.update_succeed));
        notifiUpdateMainPage(Constant.INTENT_UPDATE_TYPE_COURSE);
        finish();
    }
}
