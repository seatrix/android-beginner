package com.slieer;

import java.net.InetAddress;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SID;
import jcifs.smb.SmbSession;
 
public class simpleJavaAuthenticationTest {
    
    public static void main(String[] args) {
        try {
            InetAddress ip = InetAddress.getByName(Constants.IP);
            UniAddress myDomain = new UniAddress(ip);
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("workgroup","slieer","slieer");
            SmbSession.logon(myDomain, auth);
            System.out.println("Hostname: "+ myDomain.getHostName());
            jcifs.smb.SID mySid = jcifs.smb.SID.getServerSid(myDomain.getHostName(), auth);
            System.out.println("Sid Domain Name: "+ auth.getName());
            System.out.println("Sid Domain SID: " +mySid.getDomainSid());
            SID[] groupMembers = mySid.getGroupMemberSids(auth.getName(), auth, SID.SID_TYPE_USER);
            System.out.println("Total SIDs = "+groupMembers.length);
            for (int i = 0; i < groupMembers.length; i++ ) {
                System.out.println("Sid["+i+"] GroupMembersSids: " +mySid.getAccountName());
            }
        } catch (Exception e) {
            System.out.println("Exception : "+e.toString());
        }
    }
    
}
