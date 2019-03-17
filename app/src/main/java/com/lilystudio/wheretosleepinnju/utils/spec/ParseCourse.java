package com.lilystudio.wheretosleepinnju.utils.spec;

import android.util.Log;

import com.lilystudio.wheretosleepinnju.app.Constant;
import com.lilystudio.wheretosleepinnju.app.Url;
import com.lilystudio.wheretosleepinnju.data.bean.Course;
import com.lilystudio.wheretosleepinnju.data.bean.CourseTime;
import com.lilystudio.wheretosleepinnju.data.db.CoursesPsc;
import com.lilystudio.wheretosleepinnju.utils.LogUtil;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NJU课程解析
 * Created by mnnyang on 17-10-19.
 * Changed by idealclover on 18-07-07
 */

public class ParseCourse {
    private static final Pattern pattern1 = Pattern.compile("第\\d{1,2}-\\d{1,2}节");
    private static final Pattern pattern2 = Pattern.compile("\\d{1,2}-\\d{1,2}周");
    private static final Pattern pattern3 = Pattern.compile("第\\d{1,2}周");

    public static String parseViewStateCode(String html) {
        String code = "";
        Document doc = org.jsoup.Jsoup.parse(html);
        Elements inputs = doc.getElementsByAttributeValue("name", Url.__VIEWSTATE);
        if (inputs.size() > 0) {
            code = inputs.get(0).attr("value");
            LogUtil.d(CoursesPsc.class, "finded __VIEWSTATE code=" + code);
        } else {
            LogUtil.d(CoursesPsc.class, "Not find __VIEWSTATE code");
        }

        return code;
    }

    /**
     * @param html
     * @return 解析失敗返回空
     */
    public static CourseTime parseTime(String html) {
        Document doc = org.jsoup.Jsoup.parse(html);
        CourseTime courseTime = new CourseTime();

        Elements elements = doc.getElementsMatchingOwnText("^20[0-9][0-9]-20[0-9][0-9]学年第(一|二)学期$?");
        if(elements == null){
            LogUtil.e(ParseCourse.class, "get the course time failed ");
            return null;
        }

        for(Element x: elements){
            String year = x.text().substring(0, 9);
            String term = "第" +  x.text().substring(12, 13) + "学期";
            courseTime.years.add(year);
            courseTime.terms.add(term );
            courseTime.selectYear = year;
            courseTime.selectTerm = term;
        }
        return courseTime;
    }

    /**
     * @param html
     * @return 解析失败返回空
     */
    public static ArrayList<Course> parse(String html) {

        Document doc = org.jsoup.Jsoup.parse(html);

        Elements table1 = doc.select("tr.TABLE_TR_01");
        table1.addAll(doc.select("tr.TABLE_TR_02"));
        ArrayList<Course> courses = new ArrayList<>();

        int node = 0;
        for(Element tr: table1){
            ArrayList<Course> temp = new ArrayList<>();
            String timeAndPlace = tr.child(5).html();
            timeAndPlace = timeAndPlace.replaceAll("<br>", "\n");
            // Deal with the case that the time and place is empty
            if(timeAndPlace.isEmpty()){
                continue;
            }
            parseTimeAndClassroom(temp, timeAndPlace, node);
            for(Course course: temp) {
                Log.d("nameandteacher", tr.child(2).text()+"\n"+tr.child(4).text());
                course.setName(tr.child(2).text());
                course.setTeacher(tr.child(4).text());
            }
            courses.addAll(temp);
        }
        return courses;
    }
    private static void specialParseTimeAndClassroom(ArrayList<Course> courses,String classInfo){
        String infos [] =classInfo.split("\n");
        boolean existTempCourse=false;//魔幻标签，为了最后add 1-4周的课程
        Course tempCourse=new Course();
        for(String info:infos){
            Course course=new Course();
            String str [] = info.split(" ");
            //week pattern "周一"
            if (info.charAt(0) == '周') {
                String weekStr = info.substring(0, 2);
                int week = getIntWeek(weekStr);
                course.setWeek(week);
            }
            //节数 pattern "3-5节"
            Matcher matcher = pattern1.matcher(info);
            if (matcher.find()) {
                String nodeInfo = matcher.group(0);
                String[] nodes = nodeInfo.substring(1, nodeInfo.length() - 1).split("-");
                course.setNodes(nodes);
            }
            //周数 pattern "1-17周"
            boolean flag=false;
            matcher = pattern2.matcher(info);
            if (matcher.find()) {
                flag=true;
                String weekInfo = matcher.group(0);//第2-16周
                if (weekInfo.length() < 2) {
                    return;
                }
                String[] weeks = weekInfo.substring(0, weekInfo.length() - 1).split("-");

                if (weeks.length > 0) {
                    int startWeek = Integer.decode(weeks[0]);
                    course.setStartWeek(startWeek);
                }
                if (weeks.length > 1) {
                    int endWeek = Integer.decode(weeks[1]);
                    course.setEndWeek(endWeek);
                }
            }

            //从第5周开始
            int beginweek=info.indexOf("从第");
            if(beginweek==-1){
                //单双周 pattern "单周" "双周"
                if (info.contains("单周")) {
                    course.setWeekType(Course.WEEK_SINGLE);
                    course.setStartWeek(1);
                    course.setEndWeek(17);
                } else if (info.contains("双周")) {
                    course.setWeekType(Course.WEEK_DOUBLE);
                    course.setStartWeek(1);
                    course.setEndWeek(17);
                }
                course.setClassRoom(str[str.length - 1]);
                courses.add(course);
                continue;
            }
            int beginWeek=info.charAt(info.indexOf("从第")+2)-'0';
            if(flag){//先封装前面的连续课程
                course.setClassRoom(str[str.length - 1]);
                //courses.add(course);
                tempCourse.setEndWeek(course.getEndWeek());
                tempCourse.setStartWeek(course.getStartWeek());
                tempCourse.setWeekType(course.getWeekType());
                tempCourse.setWeek(course.getWeek());
                tempCourse.setClassRoom(course.getClassRoom());
                existTempCourse=true;

                Course temp=course;//然后将一些信息复制一下
                course=new Course();
                course.setWeek(temp.getWeek());
                List<Integer> nodes=temp.getNodes();
                int nodesarray[]=new int[nodes.size()];
                int i=0;
                for(Integer node:nodes){
                    nodesarray[i++]=node.intValue();
                }
                tempCourse.setNodes(nodesarray);
                course.setNodes(nodesarray);//吐槽：setter和getter方法不匹配是什么神仙操作？？居然还要手动转换
            }
            course.setStartWeek(beginWeek);
            if(info.contains("单周")) {
                course.setWeekType(Course.WEEK_SINGLE);
            }else if(info.contains("双周")){
                course.setWeekType(Course.WEEK_DOUBLE);
            }
            course.setClassRoom(str[str.length - 1]);
            course.setEndWeek(17);//似乎是默认17周
            courses.add(course);
            //TODO 找到显示课程的地方 解决时间相同 日期不同的冲突问题：检查覆盖
            //TODO 上述问题已修复：把1-4周的课程 最后add 相当于覆盖吧
        }
        if(existTempCourse)
            courses.add(tempCourse);
    }
    private static void parseTimeAndClassroom(ArrayList<Course> courses, String time, int htmlNode) {
        Log.d("CLASSINFO", time);
        //修复bug：“从XX开始：”形式的时间无法解析正确结果
        if(time.contains("开始")){
            //Log.d("here","hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
            specialParseTimeAndClassroom(courses,time);
            return;
        }
        String infos [] = time.split("\n");
        for(String info: infos){

            Course course = new Course();
            String str [] = info.split(" ");
            //week pattern "周一"
            if (info.charAt(0) == '周') {
                String weekStr = info.substring(0, 2);
                int week = getIntWeek(weekStr);
                course.setWeek(week);
            }
            //节数 pattern "3-5节"
            Matcher matcher = pattern1.matcher(info);
            if (matcher.find()) {
                String nodeInfo = matcher.group(0);
                String[] nodes = nodeInfo.substring(1, nodeInfo.length() - 1).split("-");
                course.setNodes(nodes);
            }
            //单双周 pattern "单周" "双周"
            if (info.contains("单周")) {
                course.setWeekType(Course.WEEK_SINGLE);
                course.setStartWeek(1);
                course.setEndWeek(17);
            } else if (info.contains("双周")) {
                course.setWeekType(Course.WEEK_DOUBLE);
                course.setStartWeek(1);
                course.setEndWeek(17);
            }
            //周数 pattern "1-17周"
            matcher = pattern2.matcher(info);
            if (matcher.find()) {
                String weekInfo = matcher.group(0);//第2-16周
                if (weekInfo.length() < 2) {
                    return;
                }
                String[] weeks = weekInfo.substring(0, weekInfo.length() - 1).split("-");

                if (weeks.length > 0) {
                    int startWeek = Integer.decode(weeks[0]);
                    course.setStartWeek(startWeek);
                }
                if (weeks.length > 1) {
                    int endWeek = Integer.decode(weeks[1]);
                    course.setEndWeek(endWeek);
                }
            }
            //TMD的坑爹教务系统 针对“第2周 第4周 第6周”的特殊修改
            matcher = pattern3.matcher(info);
            if (matcher.find()) {
                String startweek = matcher.group(0);//第2周
                startweek = startweek.substring(1, startweek.length() - 1); //提取
                int startWeek = Integer.decode(startweek);
                String endweek = matcher.group(0);
                int endWeek;
                if(!matcher.find()){
                    //“从第3周开始 单周
                    endWeek = 17;
                }else {
                    int classes=1;  //这里修了一个bug，这个bug之前导致第五周 第七周 第九周 第十一周的课程weektype==WEEK_ALL
                    while (matcher.find()) {
                        endweek = matcher.group(0);
                        classes++;
                        //Log.i("xxxxxxxxxxxxxxxxxx", endweek+"\n"+classes);
                    }
                    endweek = endweek.substring(1, endweek.length() - 1); //提取
                    endWeek = Integer.decode(endweek);

                    if (endWeek - startWeek == classes * 2) {
                        if (startWeek % 2 == 1) {
                            course.setWeekType(Course.WEEK_SINGLE);
                        } else {
                            course.setWeekType(Course.WEEK_DOUBLE);
                        }
                    }
                }
                course.setStartWeek(startWeek);
                course.setEndWeek(endWeek);
            }
            //地点
            course.setClassRoom(str[str.length - 1]);
            courses.add(course);
        }
    }

    /**
     * 汉字转换int
     */
    private static int getIntWeek(String chinaWeek) {
        for (int i = 0; i < Constant.WEEK.length; i++) {
            if (Constant.WEEK[i].equals(chinaWeek)) {
                return i;
            }
        }
        return 0;
    }
}
