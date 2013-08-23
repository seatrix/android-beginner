#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <float.h>

#include "corejava/com_exam_slieer_utils_jni_corejava8jni_Printf2.h"

static char* find_format(const char format[]);
static char* find_format_1(const char format[]);

JNIEXPORT jstring JNICALL Java_com_exam_slieer_utils_jni_corejava8jni_Printf2_sprint
(JNIEnv* env, jclass cl, jstring format, jdouble x){
	   const char* cformat;
	   char* fmt;
	   jstring ret;

	   cformat = (*env)->GetStringUTFChars(env, format, NULL);
	   fmt = find_format(cformat);
	   if (fmt == NULL)
	      ret = format;
	   else
	   {
	      char* cret;
	      int width = atoi(fmt);
	      if (width == 0) width = DBL_DIG + 10;
	      cret = (char*)malloc(strlen(cformat) + width);
	      //sprintf(cret, cformat, x);
	      ret = (*env)->NewStringUTF(env, cret);
	      free(cret);
	   }
	   (*env)->ReleaseStringUTFChars(env, format, cformat);
	   return ret;
}


JNIEXPORT void JNICALL Java_com_exam_slieer_utils_jni_corejava8jni_Printf2_fprint
(JNIEnv* env, jclass cl, jobject out, jstring format, jdouble x){
	   const char* cformat;
	   char* fmt;
	   jstring str;
	   jclass class_PrintWriter;
	   jmethodID id_print;

	   cformat = (*env)->GetStringUTFChars(env, format, NULL);
	   fmt = find_format_1(cformat);
	   if (fmt == NULL)
	      str = format;
	   else
	   {
	      char* cstr;
	      int width = atoi(fmt);
	      if (width == 0) width = DBL_DIG + 10;
	      cstr = (char*) malloc(strlen(cformat) + width);
	      //sprintf(cstr, cformat, x);
	      str = (*env)->NewStringUTF(env, cstr);
	      free(cstr);
	   }
	   (*env)->ReleaseStringUTFChars(env, format, cformat);

	   /* now call ps.print(str) */

	   /* get the class */
	   class_PrintWriter = (*env)->GetObjectClass(env, out);

	   /* get the method ID */
	   id_print = (*env)->GetMethodID(env, class_PrintWriter, "print", "(Ljava/lang/String;)V");

	   /* call the method */
	   (*env)->CallVoidMethod(env, out, id_print, str);
}

/**
   @param format a string containing a printf format specifier
   (such as "%8.2f"). Substrings "%%" are skipped.
   @return a pointer to the format specifier (skipping the '%')
   or NULL if there wasn't a unique format specifier
*/
static char* find_format(const char format[])
{
   char* p;
   char* q;

   p = strchr(format, '%');
   while (p != NULL && *(p + 1) == '%') /* skip %% */
      p = strchr(p + 2, '%');
   if (p == NULL) return NULL;
   /* now check that % is unique */
   p++;
   q = strchr(p, '%');
   while (q != NULL && *(q + 1) == '%') /* skip %% */
      q = strchr(q + 2, '%');
   if (q != NULL) return NULL; /* % not unique */
   q = p + strspn(p, " -0+#"); /* skip past flags */
   q += strspn(q, "0123456789"); /* skip past field width */
   if (*q == '.') { q++; q += strspn(q, "0123456789"); }
      /* skip past precision */
   if (strchr("eEfFgG", *q) == NULL) return NULL;
      /* not a floating point format */
   return p;
}

/**
   @param format a string containing a printf format specifier
   (such as "%8.2f"). Substrings "%%" are skipped.
   @return a pointer to the format specifier (skipping the '%')
   or NULL if there wasn't a unique format specifier
*/
static char* find_format_1(const char format[])
{
   char* p;
   char* q;

   p = strchr(format, '%');
   while (p != NULL && *(p + 1) == '%') /* skip %% */
      p = strchr(p + 2, '%');
   if (p == NULL) return NULL;
   /* now check that % is unique */
   p++;
   q = strchr(p, '%');
   while (q != NULL && *(q + 1) == '%') /* skip %% */
      q = strchr(q + 2, '%');
   if (q != NULL) return NULL; /* % not unique */
   q = p + strspn(p, " -0+#"); /* skip past flags */
   q += strspn(q, "0123456789"); /* skip past field width */
   if (*q == '.') { q++; q += strspn(q, "0123456789"); }
      /* skip past precision */
   if (strchr("eEfFgG", *q) == NULL) return NULL;
      /* not a floating-point format */
   return p;
}
