package com.exam.slieer.utils.jni.corejava8jni;

import java.io.PrintWriter;

/**
 * @version 1.10 1997-07-01
 * @author Cay Horstmann
 */
public class Printf2
{
   public static native String sprint(String format, double x);

   public static native void fprint(PrintWriter out, String format, double x);

}
