package com.slieer.broadcat;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class DatagramBroadcast extends DataSwapListenerAdapter
    implements Runnable {
  public static final int DatagramPort = 135;
  public static final int Datagram_Length = 1;
  
  private java.net.DatagramSocket road;
  private InetAddress ia;
  DatagramPacket dp;
  private DataPacket cdp;
  private boolean ishandling = true;

  DataSwapEvent dsevent;
  /**
   * 这个构造器用于接收数据报.
   */
  public DatagramBroadcast() {
    try {
      System.out.println("recv ...");
      dsevent = new DataSwapEvent(this);
      SocketAddress sa = new InetSocketAddress(
                                               DatagramRecvSender_b.DatagramPort);
      //至少要指定数据报接收端口,这是对接收端的唯一要求.当然如果主机是多址主机,需要road = new DatagramSocket(sa);
      road = new DatagramSocket(DatagramRecvSender_b.DatagramPort);
      System.out.println(road.isBound()+"::"+road.getBroadcast());
    }
    catch (SocketException ex) {
      ex.printStackTrace();
    }
    catch (Exception ex1) {
      ex1.printStackTrace();
    }
  }
  /**
   * 这个构造器用于发送数据报.
   * @param send String
   */
  public DatagramRecvSender_b(String send) {
    try {
      System.out.println("send ...");
      //这个类可以计算本地网的定向广播地址.
      BroadcastAddr info = new BroadcastAddr();
      info.setNetmask("255.255.255.0");
      info.setNetaddr("1Array2.168.0.106");
      info.execCalc();
      ia = InetAddress.getByName(info.getNetbroadcastaddr());//"255.255.255.255"
      dsevent = new DataSwapEvent(this);
      SocketAddress sa = new InetSocketAddress(ia,
                                               DatagramRecvSender_b.DatagramPort);
      //如果需要接收数据报,就需要指定一个端口,否则不必指定.road = new DatagramSocket(DatagramRecvSender_s.DatagramPort-1);
      road = new DatagramSocket();
      //DatagramSocket类实例的getBroadcast()返回true,即该类的广播属性缺省设置是true,就是使能的. 
      //road.setBroadcast(true);可以测试一下 当执行了road.setBroadcast(false)后会出现什么状况.
      System.out.println("isBound--"+road.isBound()+":getBroadcast--"+road.getBroadcast());
    }
    catch (SocketException ex) {
      ex.printStackTrace();
    }
    catch (UnknownHostException ex1) {
      ex1.printStackTrace();
    }
  }

  public void sendFile(String file) {
    cdp = new DataPacket(file);
    System.out.println(file);
    Iterator it = cdp.getDataPackets().iterator();
    byte[] b;
    while (it.hasNext()) {
      b = ( (DataEntry) it.next()).getByte();
      DatagramPacket dp = new DatagramPacket(b, 0, b.length, ia,DatagramRecvSender_b.DatagramPort);
      try {
        Thread.sleep(100);
        if(dp==null)
          continue;
        road.send(dp);
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  
  public boolean isHandling() {
    return ishandling;
  }

  public void isHandling(boolean ishand) {
    ishandling = ishand;
  }

  public void run() {
    byte[] buffer = new byte[DataPacket.DataSwapSize];
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    DataPacket dp = new DataPacket();
    while (ishandling) {
      packet.setLength(buffer.length);
      System.out.println("wait .. ");
      try {
        road.receive(packet);
        dp.Add(packet.getData());
        if (dp.isFull()) {
          dsevent.setImage(dp.Gereratedata());
          this.processRecvFinishedEvent(dsevent);
          dp = new DataPacket();
        }
      }
      catch (IOException ex) {
        System.out.println(ex);
      }
    }
  }

  public static void main(String[] args) {
    String file[];
    ArrayList al = new ArrayList();
    //换成你机器上的图象文件夹
    String path = "E:＼＼nature＼＼"; 
    File f = new File(path);
    file = f.list();
    DatagramRecvSender_b dgrs = new DatagramRecvSender_b("");
    for(int j=0;j<1000;j++)
    for (int i = 0; i < file.length; i++) {
      try {
        Thread.sleep(2000);
      }
      catch (InterruptedException ex) {
      }
      if (file[i].endsWith("jpg") || file[i].endsWith("bmp"))
        dgrs.sendFile(path + file[i]);
    }
  }
}

