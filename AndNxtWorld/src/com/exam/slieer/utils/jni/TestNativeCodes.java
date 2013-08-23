package com.exam.slieer.utils.jni;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.exam.jni.Order;
import com.exam.slieer.utils.bean.User;
import com.exam.slieer.utils.jni.corejava8jni.Printf2;
import com.exam.slieer.utils.jni.corejava8jni.Printf4;

import android.util.Log;

public class TestNativeCodes {
    public final static String TAG = "TestNativeCodes";
    static {
        System.loadLibrary("HelloNative");
    }

    public static void testHelloNative() {
        /**
         * 
         HelloNative obj = new HelloNative(); Log.i(TAG, obj.message +
         * "call before "); obj.callCppFunction(); Log.i(TAG, obj.message +
         * "call end"); for (int each : obj.arrays) Log.i(TAG, "" + each);
         * 
         * helloNextNative(); helloJavaBeanNative();
         */

        //corejava8Native();
        orderNative();
    }

    public static void helloNextNative() {
        HelloNextNative h = new HelloNextNative();
        Log.i(TAG, "getInt:" + h.getInt());
        h.setInt(1000);
        Log.i(TAG, "getInt:" + h.getInt());
    }

    public static void helloJavaBeanNative() {
        Log.i(TAG, "helloJavaBeanNative...");
        HelloJavaBeanNative h = new HelloJavaBeanNative();
        h.setUser("slieer");
        Log.i(TAG, "setUser execute finish !");

        // Log.i(TAG, h.getUser().toString());

        User user = h.getUser();
        Log.i(TAG, "userName:" + user.getUserName());
    }

    public static void corejava8Native() {
        {
            double price = 44.95;
            double tax = 7.75;
            double amountDue = price * (1 + tax / 100);

            String s = Printf2.sprint("Amount due = %8.2f", amountDue);
            System.out.println(s);
        }
        {
            double price = 44.95;
            double tax = 7.75;
            double amountDue = price * (1 + tax / 100);
            PrintWriter out = new PrintWriter(System.out);
            Printf2.fprint(out, "Amount due = %8.2f\n", amountDue);
            out.flush();
        }
        {
            double price = 44.95;
            double tax = 7.75;
            double amountDue = price * (1 + tax / 100);
            PrintWriter out = new PrintWriter(System.out);
            /* This call will throw an exception--note the %% */
            Printf4.fprint(out, "Amount due = %%8.2f\n", amountDue);
            out.flush();
        }

    }
    
    public static void orderNative(){
        List<String> list = new ArrayList<String>();
        list.add("aa");
        list.add("bb");
        list.add("cc");
        Order.sort(list);
        
        int[] ch = new int[]{1,3,4,2,1,-1,22,7,10};
        Order.sortInts(ch);
        String s="人民网北京5月21日电 （记者 杨牧）中国驻朝鲜使馆官员今晨8时15分向人民网记者确认，朝鲜已释放被扣押中国渔船及船员。昨晚，微博上一度传出“被朝方扣押中国渔船已获释”的消息。随后，又有微博辟谣称，被扣渔船一事朝鲜方面尚没有最新表态。据媒体最新报道，被朝鲜扣押的“辽普渔25222”渔船的船主于学君透露，5月21日凌晨3时50分，其接到船长电话，“朝鲜释放了我们的船只和船员，船长说他们已开始返航。”。于学君称，“感谢外交部，谢谢大家”。事件回顾：5月5日夜，一艘载有16名中国渔民，编号为“辽普渔25222”的辽宁渔船被朝鲜方面扣押。期间，朝方至少8次致电中国船东，逼交至少60万元人民币“罚款”，否则将在19日中午12时前没收渔船并遣返渔民。5月10日，船主于学君向中国驻朝鲜大使馆电话求助。中国驻朝鲜大使馆接电后立即向朝鲜外务省领事局提出交涉，要求朝方尽快放船、放人和切实保障我被扣船员生命财产安全与合法权益，并将交涉情况告知船主，敦促朝方尽快妥善处理。5月18日夜，该船船主于学君通过微博对外求助，随后得到中国媒体广泛关注。5月20日，外交部发言人洪磊就此事指出，中方就中国渔船被抓扣一事与朝鲜方面保持密切沟通，已通过相关渠道向朝方提出交涉，要求朝方尽快妥善处理，维护中方人员的生命财产安全与合法权益。5月21日凌晨3时50分，船主于学君接到船长电话，“朝鲜释放了我们的船只和船员，船长说他们已开始返航。”相关细节：“我的渔船被朝鲜武装巡逻艇扣押时绝对是在中国海域内作业!”急得住院输液的于学君在接受《环球时报》记者采访时非常肯定地说：“辽普渔25222被朝鲜巡逻艇上的武装人员登艇扣押时是在东经123度53分，北纬38度18分，完全是在中国海域内。”于学君解释说，“辽普渔25222”船上装有GPS和“北斗”两套定位系统，以保证渔船“每分每秒”都知道自己作业的准确位置。同时，辽宁省渔政管理和渔业公司也会随时监控所有在海上作业渔船的位置。“哪怕你的船不小心刚挨近朝鲜海域，渔政管理人员就会通过单边带和其他通讯方式发出警告，让中国渔船远离中朝海上交界区域。辽普渔25222今年初时曾被渔政呼叫过一次，所以根本不可能越界进入朝鲜海域。”于学君特别透露了一个细节，朝鲜武装军人登船后，很熟悉地拆除了两套定位系统，没收了所有通讯工具：“但我们船最后的定位系统在渔业公司、渔政管理系统中很容易查到，完全不存在朝鲜人说的越界问题。”于学君还特别强调，他与去年5月被朝鲜扣押的中国渔船的一名船东是好朋友，所以已经特别注意避免同类事件发生，但没想到仍然没躲过。新闻链接：3艘渔船去年5月被朝扣押此次扣押，并非朝方今年首次扣押中国渔船。有船主自认倒霉交赎金一知情者向媒体表示，今年以来仅丹东市就先后有3艘中国渔船被朝鲜扣押，其中2艘交“罚款”后释放。“辽普渔25222”船主于学君说：“很多时候，船东一看对方只要一二十万就交了，算是自认倒霉，但这反而让朝鲜方面更加积极地越界扣押中国渔船，从而让朝鲜方面尝到甜头。”去年被朝扣押船员放回去年5月，3艘中国渔船被朝鲜疑似军用舰艇抓扣又放回，扣押时间长达13天，涉事船员29人。 5月8日凌晨4时30分，辽丹渔23979船在东经123度57分、北纬38度05分被抓扣，5月8日13时，辽丹渔23528、23536船在东经123度36分，北纬38度18分被抓扣。船主在事发第一时间即向丹东、大连两地的多个相关部门报案求助，但直至21日，在经过了长达13天的苦苦等待之后，这起“渔业案件”才在中国的外交努力下得以圆满解决，全部被困船员安全返回。（据《环球时报》、《新京报》）";
        long start = System.currentTimeMillis();
        char[] cha = s.toCharArray();
        Arrays.sort(cha);
        Log.i(TAG, "java time:" + (System.currentTimeMillis() - start));
        
        start = System.currentTimeMillis();
        Order.sortStr(s);
        Log.i(TAG, "native time:" + (System.currentTimeMillis() - start));
    }
}
