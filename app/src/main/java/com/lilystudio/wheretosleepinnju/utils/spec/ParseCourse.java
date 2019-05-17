package com.lilystudio.wheretosleepinnju.utils.spec;

import android.support.annotation.NonNull;
import android.util.Log;

import com.lilystudio.wheretosleepinnju.app.AppUtils;
import com.lilystudio.wheretosleepinnju.app.Constant;
import com.lilystudio.wheretosleepinnju.app.Url;
import com.lilystudio.wheretosleepinnju.custom.course.CourseAncestor;
import com.lilystudio.wheretosleepinnju.custom.util.Utils;
import com.lilystudio.wheretosleepinnju.data.bean.Course;
import com.lilystudio.wheretosleepinnju.data.bean.CourseTime;
import com.lilystudio.wheretosleepinnju.data.beanv2.CourseV2;
import com.lilystudio.wheretosleepinnju.data.db.CoursesPsc;
import com.lilystudio.wheretosleepinnju.utils.LogUtil;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import com.lilystudio.wheretosleepinnju.data.bean.Course;

/**
 * NJU课程解析
 * Created by mnnyang on 17-10-19.
 */

public class ParseCourse {
    private static final String pattern = "第.*节";
    private static final String[] other = {"时间", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六",
            "星期日", "早晨", "上午", "下午", "晚上"};
    private static final Pattern pattern1 = Pattern.compile("第\\d{1,2}.*节");
//    private static final Pattern pattern2 = Pattern.compile("\\{第\\d{1,2}[-]*\\d*周");
    private static final Pattern pattern2 = Pattern.compile("\\d{1,2}-\\d{1,2}周");
    private static final Pattern pattern3 = Pattern.compile("第\\d{1,2}周");

//    public static String parseViewStateCode(String html) {
//        String code = "";
//        Document doc = org.jsoup.Jsoup.parse(html);
//        Elements inputs = doc.getElementsByAttributeValue("name", Url.__VIEWSTATE);
//        if (inputs.size() > 0) {
//            code = inputs.get(0).attr("value");
//            LogUtil.d(CoursesPsc.class, "finded __VIEWSTATE code=" + code);
//        } else {
//            LogUtil.d(CoursesPsc.class, "Not find __VIEWSTATE code");
//        }
//
//        return code;
//    }

    /**
     * @param html
     * @return 解析失敗返回空
     */
    public static CourseTime parseTime(String html) {
//        String SELECTED = "selected";
//        String OPTION = "option";
//
//        Document doc = org.jsoup.Jsoup.parse(html);
//        CourseTime courseTime = new CourseTime();
//
//        Elements selects = doc.getElementsByTag("select");
//        if (selects == null || selects.size() < 2) {
//            LogUtil.e(ParseCourse.class, "select < 2 ");
//            return null;
//        }
//
//        Elements options = selects.get(0).getElementsByTag(OPTION);
//
//        for (Element o : options) {
//            String year = o.text().trim();
//            courseTime.years.add(year);
//            if (o.attr(SELECTED).equals(SELECTED)) {
//                courseTime.selectYear = year;
//            }
//        }
//
//        options = selects.get(1).getElementsByTag(OPTION);
//        for (Element o : options) {
//            String term = o.text().trim();
//            courseTime.terms.add(term);
//            if (o.attr(SELECTED).equals(SELECTED)) {
//                courseTime.selectTerm = term;
//            }
//        }
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
    public static ArrayList<CourseV2> parse(String html) {

        Document doc = org.jsoup.Jsoup.parse(html);

        Elements table1 = doc.select("tr.TABLE_TR_01");
        table1.addAll(doc.select("tr.TABLE_TR_02"));

//        Element table1 = doc.getElementById("Table1");
//        Elements trs = table1.getElementsByTag("tr");

        ArrayList<CourseV2> courses = new ArrayList<>();

        int node = 0;
        for (Element tr : table1) {
//            Elements tds = tr.getElementsByTag("td");
//            for (Element td : tds) {
//                String courseSource = td.text().trim();
//                if (courseSource.length() <= 6) {
//                    //null data
//                    continue;
//                }
//
//                if (Pattern.matches(pattern, courseSource)) {
//                    //node number
//                    try {
//                        node = Integer.decode(courseSource.substring(1, courseSource.length() - 1));
//                    } catch (Exception e) {
//                        node = 0;
//                        e.printStackTrace();
//                    }
//                    continue;
//                }
//
//                if (inArray(other, courseSource)) {
//                    //other data
//                    continue;
//                }
//                courses.addAll(ParseCourse.parseTextInfo(courseSource, node));
//            }
            ArrayList<CourseV2> temp = new ArrayList<>();
            String timeAndPlace = tr.child(5).html();
            timeAndPlace = timeAndPlace.replaceAll("<br>", "\n");
            // Deal with the case that the time and place is empty
            if(timeAndPlace.isEmpty()){
                continue;
            }
            parseTimeAndClassroom(temp, timeAndPlace, node);
//            parseTextInfo(timeAndPlace, node);
//            parseTimeAndClassroom(temp, timeAndPlace, node);
            for(CourseV2 course: temp) {
                Log.d("nameandteacher", tr.child(2).text()+"\n"+tr.child(4).text());
                course.setCouName(tr.child(2).text());
                course.setCouTeacher(tr.child(4).text());
            }
            courses.addAll(temp);
        }
//        return mergeSameClass(courses);
        return courses;
    }
//
//    /**
//     * 合并同一门课
//     */
//    private static ArrayList<CourseV2> mergeSameClass(ArrayList<CourseV2> courses) throws NumberFormatException {
//        ArrayList<CourseV2> result = new ArrayList<>();
//        int i = 0;
//        for (CourseV2 cours : courses) {
//            boolean find = false;
//            for (CourseV2 courseV2 : result) {
//                //同一门课
//                if (cours.isSameClass(courseV2)) {
//                    find = true;
//                    String oneAllWeek = courseV2.getCouAllWeek();
//                    String twoAllWeek = cours.getCouAllWeek();
//                    if (oneAllWeek.length() > 0 && twoAllWeek.length() > 0) {
//                        if (Integer.decode(oneAllWeek.substring(0, 1))
//                                < Integer.decode(twoAllWeek.substring(0, 1))) {
//                            courseV2.setCouAllWeek(oneAllWeek + "," + twoAllWeek);
//                        } else {
//                            courseV2.setCouAllWeek(twoAllWeek + "," + oneAllWeek);
//                        }
//                    } else if (twoAllWeek.length() > 0) {
//                        courseV2.setCouAllWeek(twoAllWeek);
//                    }
//                }
//            }
//
//            if (!find) {
//                result.add(cours);
//            }
//        }
//
//        for (int i1 = 0; i1 < result.size(); i1++) {
//            CourseV2 left = result.get(i1);
//            if (left.getCouColor() == null) {
//                int color = Utils.getRandomColor();
//                left.setCouColor(color);
//                for (CourseV2 courseV2 : result) {
//                    if(courseV2.isSameClassWithoutLocation(left)){
//                        courseV2.setCouColor(color);
//                    }
//                }
//            }
//        }
//        return result;
//    }

//    public static void main(String[] args) {
//        //String s = readToString("/home/xxyangyoulin/Android/project/ClassSchedule/app/src/main/java/com/mnnyang/njuclassschedule/impt/demo.html");
//        String s = "<table id=\"Table1\" class=\"blacktab\" bordercolor=\"Black\" border=\"0\" width=\"100%\">\n" +
//                "    <tbody>\n" +
//                "    <tr>\n" +
//                "        <td colspan=\"2\" rowspan=\"1\" width=\"2%\">时间</td>\n" +
//                "        <td align=\"Center\" width=\"14%\">星期一</td>\n" +
//                "        <td align=\"Center\" width=\"14%\">星期二</td>\n" +
//                "        <td align=\"Center\" width=\"14%\">星期三</td>\n" +
//                "        <td align=\"Center\" width=\"14%\">星期四</td>\n" +
//                "        <td align=\"Center\" width=\"14%\">星期五</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\" width=\"14%\">星期六</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\" width=\"14%\">星期日</td>\n" +
//                "    </tr>\n" +
//                "    <tr>\n" +
//                "        <td colspan=\"2\">早晨</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "    </tr>\n" +
//                "    <tr>\n" +
//                "        <td rowspan=\"5\" width=\"1%\">上午</td>\n" +
//                "        <td width=\"1%\">第1节</td>\n" +
//                "        <td align=\"Center\" rowspan=\"2\" width=\"7%\">船舶货运专题A<br>任选<br>周一第1,2节{第6-8周}<br>王庸凯<br>獐210<br><br>船舶货运专题A<br>任选<br>周一第1,2节{第2-4周}<br>王庸凯<br>獐210\n" +
//                "        </td>\n" +
//                "        <td align=\"Center\" width=\"7%\">&nbsp;</td>\n" +
//                "        <td align=\"Center\" width=\"7%\">&nbsp;</td>\n" +
//                "        <td align=\"Center\" width=\"7%\">&nbsp;</td>\n" +
//                "        <td align=\"Center\" rowspan=\"2\" width=\"7%\">远洋运输业务与海商法<br>必修<br>周五第1,2节{第6-17周}<br>姚智慧<br>獐332<br><br>远洋运输业务与海商法<br>必修<br>周五第1,2节{第1-4周}<br>姚智慧<br>獐332\n" +
//                "        </td>\n" +
//                "        <td class=\"noprint\" align=\"Center\" width=\"7%\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\" width=\"7%\">&nbsp;</td>\n" +
//                "    </tr>\n" +
//                "    <tr>\n" +
//                "        <td>第2节</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "    </tr>\n" +
//                "    <tr>\n" +
//                "        <td>第3节</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\" rowspan=\"2\">远洋运输业务与海商法<br>必修<br>周三第3,4节{第6-17周}<br>姚智慧<br>獐131<br><br>远洋运输业务与海商法<br>必修<br>周三第3,4节{第1-4周}<br>姚智慧<br>獐131\n" +
//                "        </td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "    </tr>\n" +
//                "    <tr>\n" +
//                "        <td>第4节</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "    </tr>\n" +
//                "    <tr>\n" +
//                "        <td>第5节</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "    </tr>\n" +
//                "    <tr>\n" +
//                "        <td rowspan=\"5\">下午</td>\n" +
//                "        <td>第6节</td>\n" +
//                "        <td align=\"Center\" rowspan=\"2\">航海气象与海洋学专题<br>任选<br>周一第6,7节{第7-18周}<br>张飞成<br>獐422</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\" rowspan=\"2\">船舶货运专题A<br>任选<br>周五第6,7节{第2-4周}<br>王庸凯<br>獐410<br><br>船舶货运专题A<br>任选<br>周五第6,7节{第6-8周}<br>王庸凯<br>獐410\n" +
//                "        </td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "    </tr>\n" +
//                "    <tr>\n" +
//                "        <td>第7节</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "    </tr>\n" +
//                "    <tr>\n" +
//                "        <td>第8节</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\" rowspan=\"2\">大学生职业发展与就业指导<br>课外<br>周二第8,9节{第12-12周|双周}<br>王超鹏<br>獐103</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "    </tr>\n" +
//                "    <tr>\n" +
//                "        <td>第9节</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "    </tr>\n" +
//                "    <tr>\n" +
//                "        <td>第10节</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "    </tr>\n" +
//                "    <tr>\n" +
//                "        <td rowspan=\"4\" width=\"1%\">晚上</td>\n" +
//                "        <td>第11节</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "    </tr>\n" +
//                "    <tr>\n" +
//                "        <td>第12节</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "    </tr>\n" +
//                "    <tr>\n" +
//                "        <td>第13节</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "    </tr>\n" +
//                "    <tr>\n" +
//                "        <td>第14节</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "        <td class=\"noprint\" align=\"Center\">&nbsp;</td>\n" +
//                "    </tr>\n" +
//                "    </tbody>\n" +
//                "</table>";
//
//        ArrayList<CourseV2> courses = ParseCourse.parse(s);
//        courses = mergeSameClass(courses);
//        for (CourseV2 course : courses) {
//            System.out.println("TEST  " + course.getCouName() + "--" + course.getCouColor());
//        }
//    }
//
//    public static String readToString(String fileName) {
//        String encoding = "UTF-8";
//        File file = new File(fileName);
//        Long filelength = file.length();
//        byte[] filecontent = new byte[filelength.intValue()];
//        try {
//            FileInputStream in = new FileInputStream(file);
//            in.read(filecontent);
//            in.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            return new String(filecontent, encoding);
//        } catch (UnsupportedEncodingException e) {
//            System.err.println("The OS does not support " + encoding);
//            e.printStackTrace();
//            return null;
//        }
//    }

//    private static ArrayList<CourseV2> parseTextInfo(String source, int node) {
//
//        System.out.println("---->" + source + "");
//
//        ArrayList<CourseV2> courses = new ArrayList<>();
//        String[] split = source.split(" ");
//        if (split.length / 4 > 0 && split.length % 4 == 0) {
//            for (int i = 0; i < split.length / 4; i++) {
//                CourseV2 course = new CourseV2().setCouOnlyId(AppUtils.createUUID());
//                courses.add(course);
//
//                //course.setSource(source);
//                //course.setName(split[i * 4]);
//                course.setCouName(split[i * 4]);
//                String time = split[1 + i * 4];
//                parseTime(course, time, node);
//                //course.setTeacher(split[2 + i * 4]);
//                //course.setClassRoom(split[3 + i * 4]);
//                course.setCouTeacher(split[2 + i * 4]);
//                course.setCouLocation(split[3 + i * 4]);
//            }
//
//        } else if (split.length / 5 > 0 && split.length % 5 == 0) {
//            for (int i = 0; i < split.length / 5; i++) {
//                CourseV2 course = new CourseV2().setCouOnlyId(AppUtils.createUUID());
//                courses.add(course);
//
//                course.setCouName(split[i * 5]);
//                String time = split[2 + i * 5];
//                parseTime(course, time, node);
//
//                course.setCouTeacher(split[3 + i * 5]);
//                course.setCouLocation(split[4 + i * 5]);
//            }
//
//        } else if (split.length > 2) {
//            CourseV2 course = new CourseV2().setCouOnlyId(AppUtils.createUUID());
//
//            courses.add(course);
//
//            //course.setSource(source);
//            //course.setName(split[0]);
//            course.setCouName(split[0]);
//            String time = split[1];
//            parseTime(course, time, node);
//            course.setCouTeacher(split[2]);
//            if (split.length > 3) {
//                course.setCouLocation(split[3]);
//            }
//        } else {
//            //TODO other type
//            LogUtil.e("ParseCourse", "解析爆炸:" + source);
//        }
//
//        return courses;
//    }
//
//    /**
//     * 周一第1,2节{第2-16周|双周}
//     * {第1-15周|2节/单周}
//     * <p>
//     * TODO 周一第1,2节{第1-15周|2节/周} //次格式未解决 数据不足
//     */
//    private static void parseTime(CourseV2 course, String time, int htmlNode) {
//        //week
//        if (time.charAt(0) == '周') {
//            String weekStr = time.substring(0, 2);
//            int week = getIntWeek(weekStr);
//            course.setCouWeek(week);
//        }
//
//        //单双周
//        if (time.contains("|单周")) {
//            course.setShowType(Course.SHOW_SINGLE);
//        } else if (time.contains("|双周")) {
//            course.setShowType(Course.SHOW_DOUBLE);
//
//        }
//
//        //节数
//        Matcher matcher = pattern1.matcher(time);
//
//        if (matcher.find()) {
//            String nodeInfo = matcher.group(0);
//            String[] nodes = nodeInfo.substring(1, nodeInfo.length() - 1).split(",");
//
//            //course.setNodes(nodes);
//            int[] intNodes = getIntNodes(nodes);
//            if (intNodes.length > 0) {
//                course.setCouStartNode(intNodes[0]);
//                course.setCouNodeCount(intNodes.length);
//            }
//        } else if (htmlNode != 0) {
//            //course.addNode(htmlNode);
//            course.setCouStartNode(htmlNode).setCouNodeCount(1);
//            System.out.println("use htmlNode");
//        } else {
//            //周一第1,2节{第1-15周|2节/周}
//            //TODO 上传无法解析的数据
//            System.out.println("无法解析的时间：" + time);
//        }
//
//        //周数
//        matcher = pattern2.matcher(time);
//        if (matcher.find()) {
//            String weekInfo = matcher.group(0);//第2-16周
//            if (weekInfo.length() < 2) {
//                return;
//            }
//            String[] weeks = weekInfo.substring(2, weekInfo.length() - 1).split("-");
//
//            if (weeks.length > 0) {
//                int startWeek = Integer.decode(weeks[0]);
//                //course.setStartWeek(startWeek);
//                course.setStartIndex(startWeek);
//            }
//            if (weeks.length > 1) {
//                int endWeek = Integer.decode(weeks[1]);
//                //course.setEndWeek(endWeek);
//                course.setEndIndex(endWeek);
//            }
//
//            String allWeek = getStringTypeOfAllWeek(course);
//
//            course.setCouAllWeek(allWeek);
//        }
//    }


    private static void specialParseTimeAndClassroom(ArrayList<CourseV2> courses,String classInfo){
        String infos [] =classInfo.split("\n");
        boolean existTempCourse=false;//魔幻标签，为了最后add 1-4周的课程
        CourseV2 tempCourse=new CourseV2();
        for(String info:infos){
            CourseV2 course=new CourseV2();
            String str [] = info.split(" ");
            //week pattern "周一"
            if (info.charAt(0) == '周') {
                String weekStr = info.substring(0, 2);
                int week = getIntWeek(weekStr);
                course.setCouWeek(week);
            }
            //节数 pattern "3-5节"
            Matcher matcher = pattern1.matcher(info);
            if (matcher.find()) {
                String nodeInfo = matcher.group(0);
                String[] nodes = nodeInfo.substring(1, nodeInfo.length() - 1).split("-");
//                course.setNodes(nodes);
                int[] intNodes = getIntNodes(nodes);
                if (intNodes.length > 0) {
                    course.setCouStartNode(intNodes[0]);
                    course.setCouNodeCount(intNodes.length);
                }
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
                    course.setStartIndex(startWeek);
                }
                if (weeks.length > 1) {
                    int endWeek = Integer.decode(weeks[1]);
                    course.setEndIndex(endWeek);
                }
            }

            //从第5周开始
            int beginweek=info.indexOf("从第");
            if(beginweek==-1){
                //单双周 pattern "单周" "双周"
                if (info.contains("单周")) {
                    course.setShowType(Course.SHOW_SINGLE);
                    course.setStartIndex(1);
                    course.setEndIndex(16);
                } else if (info.contains("双周")) {
                    course.setShowType(Course.SHOW_DOUBLE);
                    course.setStartIndex(1);
                    course.setEndIndex(16);
                }
                course.setCouLocation(str[str.length - 1]);
                String allWeek = getStringTypeOfAllWeek(course);
                course.setCouAllWeek(allWeek);
                courses.add(course);
                continue;
            }
            int beginWeek=info.charAt(info.indexOf("从第")+2)-'0';
            if(flag){//先封装前面的连续课程
                course.setCouLocation(str[str.length - 1]);
                //courses.add(course);
                tempCourse.setEndIndex(course.getEndIndex());
                tempCourse.setStartIndex(course.getStartIndex());
                tempCourse.setShowType(course.getShowType());
                tempCourse.setCouWeek(course.getCouWeek());
                tempCourse.setCouLocation(course.getCouLocation());
                existTempCourse=true;

                CourseV2 temp=course;//然后将一些信息复制一下
                course=new CourseV2();
                course.setCouWeek(temp.getCouWeek());
//                List<Integer> nodes=temp.getNodes();
//                int nodesarray[]=new int[nodes.size()];
//                int i=0;
//                for(Integer node:nodes){
//                    nodesarray[i++]=node.intValue();
//                }
//                tempCourse.setNodes(nodesarray);
//                course.setNodes(nodesarray);//吐槽：setter和getter方法不匹配是什么神仙操作？？居然还要手动转换
                course.setCouStartNode(temp.getCouStartNode());
                course.setCouNodeCount(temp.getCouNodeCount());
//                int[] intNodes = getIntNodes(nodes);
//                if (intNodes.length > 0) {
//                    course.setCouStartNode(intNodes[0]);
//                    course.setCouNodeCount(intNodes.length);
//                }
            }
            course.setStartIndex(beginWeek);
            if(info.contains("单周")) {
                course.setShowType(Course.SHOW_SINGLE);
            }else if(info.contains("双周")){
                course.setShowType(Course.SHOW_DOUBLE);
            }
            course.setCouLocation(str[str.length - 1]);
            course.setEndIndex(16);//似乎是默认17周
            String allWeek = getStringTypeOfAllWeek(course);
            course.setCouAllWeek(allWeek);
            courses.add(course);
            //TODO 找到显示课程的地方 解决时间相同 日期不同的冲突问题：检查覆盖
            //TODO 上述问题已修复：把1-4周的课程 最后add 相当于覆盖吧
        }
        if(existTempCourse)
            courses.add(tempCourse);
    }
    private static void parseTimeAndClassroom(ArrayList<CourseV2> courses, String time, int htmlNode) {
        Log.d("CLASSINFO", time);
        //修复bug：“从XX开始：”形式的时间无法解析正确结果
        if(time.contains("开始")){
            //Log.d("here","hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
            specialParseTimeAndClassroom(courses,time);
            return;
        }
        String infos [] = time.split("\n");
        for(String info: infos){

            CourseV2 course = new CourseV2();
            String str [] = info.split(" ");
            //week pattern "周一"
            if (info.charAt(0) == '周') {
                String weekStr = info.substring(0, 2);
                int week = getIntWeek(weekStr);
                course.setCouWeek(week);
            }
            //节数 pattern "3-5节"
            Matcher matcher = pattern1.matcher(info);
            if (matcher.find()) {
                String nodeInfo = matcher.group(0);
                String[] nodes = nodeInfo.substring(1, nodeInfo.length() - 1).split("-");
                int[] intNodes = getIntNodes(nodes);
                if (intNodes.length > 0) {
                    course.setCouStartNode(intNodes[0]);
                    course.setCouNodeCount(intNodes.length);
                }
            }
            //单双周 pattern "单周" "双周"
            if (info.contains("单周")) {
                course.setShowType(Course.SHOW_SINGLE);
                course.setStartIndex(1);
                course.setEndIndex(17);
            } else if (info.contains("双周")) {
                course.setShowType(Course.SHOW_DOUBLE);
                course.setStartIndex(1);
                course.setEndIndex(17);
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
                    course.setStartIndex(startWeek);
                }
                if (weeks.length > 1) {
                    int endWeek = Integer.decode(weeks[1]);
                    course.setEndIndex(endWeek);
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
                            course.setShowType(Course.SHOW_SINGLE);
                        } else {
                            course.setShowType(Course.SHOW_DOUBLE);
                        }
                    }
                }
                course.setStartIndex(startWeek);
                course.setEndIndex(endWeek);
            }
            //地点
            course.setCouLocation(str[str.length - 1]);
            String allWeek = getStringTypeOfAllWeek(course);
            course.setCouAllWeek(allWeek);
            courses.add(course);
        }
    }

    @NonNull
    private static String getStringTypeOfAllWeek(CourseV2 course) {
        StringBuilder builder = new StringBuilder();
        for (int i = course.getStartIndex(); i <= course.getEndIndex(); i++) {
            if (course.getShowType() == CourseAncestor.SHOW_SINGLE) {
                if (i % 2 == 1) {
                    builder.append(i).append(",");
                }
            } else if (course.getShowType() == CourseAncestor.SHOW_DOUBLE) {
                if (i % 2 == 0) {
                    builder.append(i).append(",");
                }
            } else if (course.getShowType() == CourseAncestor.SHOW_ALL) {
                builder.append(i).append(",");
            }
        }

        String allWeek = builder.toString();
        if (allWeek.length() > 0) {
            allWeek = allWeek.substring(0, allWeek.length() - 1);
        }
        return allWeek;
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

    private static boolean inArray(String[] arr, String targetValue) {
        for (String s : arr) {
            if (s.equals(targetValue))
                return true;
        }
        return false;
    }

    /**
     * 设置课程节数
     * *必须升序排列 例如:3 4 节课
     * *错位部分将会被抛弃 例如:2 4节课程将会抛弃4
     */
    public static int[] getIntNodes(String[] nodes) {
        int intNodes[] = new int[nodes.length];

        try {
            for (int i = 0; i < nodes.length; i++) {
                intNodes[i] = Integer.decode(nodes[i]);
            }
            return intNodes;
        } catch (Exception e) {
            System.out.println("setNodes(String[] nodes) Integer.decode(nodes[i]); err");
            e.printStackTrace();
        }
        return intNodes;
    }
}
