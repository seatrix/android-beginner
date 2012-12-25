package com.slieer;
import java.net.InetAddress;
import java.util.*;

import jcifs.UniAddress;
import jcifs.smb.*;

public class GetGroupMemberSids {

    public static void main( String[] av ) throws Exception {
        
        InetAddress ip = InetAddress.getByName("192.168.51.8");
        UniAddress myDomain = new UniAddress(ip);
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("workgroup","slieer","slieer");
        SmbSession.logon(myDomain, auth);

        jcifs.smb.SID sid = jcifs.smb.SID.getServerSid(myDomain.getHostName(), auth);

        System.out.println("sid AccountName:" + sid.getAccountName());
        System.out.println("sid domainName:" + sid.getDomainName());
        //System.out.println("sid rid:" + sid.getRid());
        System.out.println("sid=" + sid.getTypeText());
        

        SID[] mems = sid.getGroupMemberSids("workgroup\\slieer", auth, SID.SID_TYPE_USER);

        for (int mi = 0; mi < mems.length; mi++) {
            SID mem = mems[mi];
            System.out.println(mem.getType() + " " + mem.toDisplayString());
        }
    }
}
