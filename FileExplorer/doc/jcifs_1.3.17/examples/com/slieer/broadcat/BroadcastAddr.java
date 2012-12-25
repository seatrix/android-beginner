package com.slieer.broadcat;


/**
 * 求广播地址
 * @author slieer
 *
 */
public class BroadcastAddr {
    private String netmask = "";

    private String netaddr = "";

    private String netbroadcastaddr = "";

    /**
     * @param args
     */
    public static void main(String[] args) {
        BroadcastAddr info = new BroadcastAddr();
    
        info.setNetaddr("192.168.51.8");
        info.setNetmask("255.255.255.0");
        info.execCalc();
        System.out.println(info.getNetbroadcastaddr());
    }

    public String getNetaddr() {
        return netaddr;
    }

    public void setNetaddr(String netaddr) {
        this.netaddr = netaddr;
    }

    public String getNetbroadcastaddr() {
        return netbroadcastaddr;
    }

    public void setNetbroadcastaddr(String netbroadcastaddr) {
        this.netbroadcastaddr = netbroadcastaddr;
    }

    public String getNetmask() {
        return netmask;
    }

    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }

    public void execCalc() {
        String[] tm = this.getNetmask().split("\\.");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < tm.length; i++) {
            tm[i] = String.valueOf(~(Integer.parseInt(tm[i])));
        }
        String[] tm2 = this.getNetaddr().split("\\.");
        for (int i = 0; i < tm.length; i++) {
            tm[i] = String.valueOf((Integer.parseInt(tm2[i]))
                    | (Integer.parseInt(tm[i])));
        }
        for (int i = 0; i < tm.length; i++) {
            sb.append(intTOstr(tm[i]));
            sb.append(".");
        }
        // sb.delete(sb.length()-1,sb.length());
        sb.deleteCharAt(sb.length() - 1);
        this.netbroadcastaddr = parseIp(sb.toString());
    }

    private String intTOstr(int num) {
        String tm = "";
        tm = Integer.toBinaryString(num);
        int c = 8 - tm.length();
        // 如果二进制数据少于8位,在前面补零.
        for (int i = 0; i < c; i++) {
            tm = "0" + tm;
        }
        // 1111 1111 1111 1111 1111 1111 1101 1110
        // 如果小于零,则只取最后的8位.
        if (c < 0)
            tm = tm.substring(24, 32);
        return tm;
    }

    private String intTOstr(String num) {
        return intTOstr(Integer.parseInt(num));
    }

    private String parseIp(String fbg) {
        String[] tm = fbg.split("\\.");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < tm.length; i++) {
            sb.append(strToint(tm[i]));
            sb.append(".");
        }
        // sb.delete(sb.length()-1,1);
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * 把二进制数转换为十进制.
     * 
     * @param str
     * @return
     */
    private int strToint(String str) {
        int total = 0;
        int top = str.length();
        for (int i = 0; i < str.length(); i++) {
            // System.out.println(str.charAt(i)+str.substring(i,i+1));
            String h = String.valueOf(str.charAt(i));
            // System.out.println(h+":"+top+":"+total);
            top--;
            total += ((int) Math.pow(2, top)) * (Integer.parseInt(h));
        }
        return total;
    }
}
