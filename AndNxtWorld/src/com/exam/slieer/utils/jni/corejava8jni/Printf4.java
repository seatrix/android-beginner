package com.exam.slieer.utils.jni.corejava8jni;

import java.io.*;

/**
 * @version 1.10 1997-07-01
 * @author Cay Horstmann
 */
public class Printf4
{
   public static native void fprint(PrintWriter ps, String format, double x);

}
