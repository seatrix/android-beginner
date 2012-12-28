package com.explorer.common;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

/**/
 // send ISO mount command
 // @author qian_wei

public class SocketClient {

	// server name
	private static final String SOCKET_NAME = "configserver";

	//Whether the command to the end of the logo
	// CNcomment:命令是否执行结束标识
	private boolean running = false;

	// Socket
	private LocalSocket s = null;

	// Socket address
	private LocalSocketAddress l;

	// input stream
	private InputStream is;

	// output stream
	private OutputStream os;

	// data out/in stream
	private DataOutputStream dos;

	// To receive command execution return value
	private String rec_data = null;

	// Page associated with
	private CommonActivity comActivity = null;

	private boolean cmdFlag = false;

	/**/
	 // Constructor
	 //@param comActivity  Page associated with

	public SocketClient(CommonActivity comActivity) {
		running = true;
		connect();
		new Thread(local_receive).start();
		this.comActivity = comActivity;

	}

	// begin add by qian_wei/zhou_yong 2011/10/20
	// for chmod the file
	/**/
	// * Constructor , chmod file
	// * @param comActivity   // Constructor
	// * @param isStartThread //whether  or not to open the thread to read the results

    public SocketClient(CommonActivity comActivity, boolean isStartThread) {
        running = true;
        connect();
        if(isStartThread) {
            new Thread(local_receive).start();
        }
        this.comActivity = comActivity;
    }
    // end add by qian_wei/zhou_yong 2011/10/20

	// begin by yuejun 20120829 SATA differs from USB

	public SocketClient() {

		running = true;
		connect();
	}

	// end by yuejun 20120829 SATA differs from USB

	public void connect() {
		try {
			s = new LocalSocket();
			l = new LocalSocketAddress(SOCKET_NAME,
					LocalSocketAddress.Namespace.RESERVED);
			s.connect(l);
			is = s.getInputStream();
			os = s.getOutputStream();
			System.out.println(os);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	  //send ISO mount command
	 // @param s //ISO mount command

	public void writeMess(String s) {
		try {
			Log.e("SocketClient", s);
			if(s.startsWith("mountiso")) {
			    cmdFlag = false;
			} else {
			    cmdFlag = true;
			}
			dos = new DataOutputStream(os);
			int strLen = s.getBytes().length;
			System.out.println(strLen);
			byte[] sendLen = intToBytes2(strLen);
			byte[] allLen = new byte[s.getBytes().length + 4];

			byte[] srcLen = s.getBytes();

			for (int i = 0; i < (s.getBytes().length + 4); i++) {
				if (i < 4) {
					allLen[i] = sendLen[i];
					System.out.println(i);
				} else {
					System.out.println("=" + i);
					allLen[i] = srcLen[i - 4];
				}
			}
			dos.write(allLen);
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	 /**/
	 //* int Converted to byte
	 //* @param n int
	 // * @return byte

	private byte[] intToBytes2(int n) {
		byte[] b = new byte[4];
		for (int i = 3; i >= 0; i--) {
			b[i] = (byte) (n >> (i * 8));
		}
		return b;
	}


	// begin modify by qian_wei/zhou_yong 2011/10/20
	// for chmod the file

	/*Asynchronous command execution results*/
	/*CNcomment: 异步获得命令执行结果*/

	public void readNetResponseAsyn() {
	    try {
	        InputStream m_Rece = s.getInputStream();
	        byte[] data;
	        int receiveLen = 0;
	        while (running) {
	            receiveLen = m_Rece.available();
	            data = new byte[receiveLen];
	            if (receiveLen != 0) {
	                m_Rece.read(data);
	                rec_data = new String(data);
	                Log.w("TAG", rec_data);
	                //  successful
	                if (rec_data.contains("execute ok")) {
	                    if(cmdFlag) {
	                        comActivity.getHandler().sendEmptyMessage(10);
	                    } else {
	                        comActivity.getHandler().sendEmptyMessage(5);
	                    }
	                    running = false;
	                }
	                // fail
	                else if (rec_data.contains("failed execute")) {
	                    if(cmdFlag) {
	                        comActivity.getHandler().sendEmptyMessage(10);
	                    } else {
	                        comActivity.getHandler().sendEmptyMessage(8);
	                    }
	                    running = false;
	                }
	            }
	            // Infinite loop, to prevent excessive use of resources
	            try {
	                Thread.sleep(50);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }
	        m_Rece.close();
	        close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

     /*Synchronous command execution results*/
     /*CNcomment: 同步获得命令执行结果*/

    public void readNetResponseSync() {
        try {
            InputStream m_Rece = s.getInputStream();
            byte[] data;
            int receiveLen = 0;
            while (running) {
                receiveLen = m_Rece.available();
                data = new byte[receiveLen];
                if (receiveLen != 0) {
                    m_Rece.read(data);
                    rec_data = new String(data);
                    Log.w("TAG", rec_data);
                    // successful
                    if (rec_data.contains("execute ok")) {
                        running = false;
                    }
                    // fail
                    else if (rec_data.contains("failed execute")) {
                        running = false;
                    }
                }
                // Infinite loop, to prevent excessive use of resources
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            m_Rece.close();
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	Thread local_receive = new Thread() {
		public void run() {
//		    try {
//	            InputStream m_Rece = s.getInputStream();
//	            byte[] data;
//	            int receiveLen = 0;
//	            while (running) {
//	                receiveLen = m_Rece.available();
//	                data = new byte[receiveLen];
//	                if (receiveLen != 0) {
//	                    m_Rece.read(data);
//	                    rec_data = new String(data);
//	                    Log.w("TAG", rec_data);
//	                    // 执行成功
//	                    if (rec_data.contains("execute ok")) {
//	                        if(cmdFlag) {
//	                            comActivity.getHandler().sendEmptyMessage(10);
//	                        } else {
//	                            comActivity.getHandler().sendEmptyMessage(5);
//	                        }
//	                        running = false;
//	                    }
//	                    // 执行失败
//	                    else if (rec_data.contains("failed execute")) {
//	                        if(cmdFlag) {
//	                            comActivity.getHandler().sendEmptyMessage(10);
//	                        } else {
//	                            comActivity.getHandler().sendEmptyMessage(8);
//	                        }
//	                        running = false;
//	                    }
//	                }
//	                // 死循环，防止资源使用过度
//	                try {
//	                    Thread.sleep(50);
//	                } catch (InterruptedException e) {
//	                    e.printStackTrace();
//	                }
//	            }
//	            m_Rece.close();
//	            close();
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	        }
			readNetResponseAsyn();
		}
	};
	// end modify by qian_wei/zhou_yong 2011/10/20

	/**
	 * disconnect Socket
	 */
	public void close() {
		try {
			dos.close();
			is.close();
			os.close();
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
