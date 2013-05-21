#include <jni.h>
#include <stdio.h>
#include "header/com_exam_slieer_utils_jni_HelloNative.h"

void sort(jint * arr,jsize len);
JNIEXPORT void JNICALL Java_com_exam_slieer_utils_jni_HelloNative_callCppFunction
  (JNIEnv *env, jobject obj){
  	jfieldID  message_fid ;
  	jstring  msg;
  	const char* msg_new;
  	jstring  j_new_str;

	jclass clazz = (*env)->GetObjectClass(env,obj);

  	//访问int数组Field
	jfieldID  fid_array = (*env)->GetFieldID(env,clazz,"arrays","[I");
	jintArray jint_arr  = (jintArray)(*env)->GetObjectField(env,obj,fid_array);
	jint*     int_arr   = (*env)->GetIntArrayElements(env,jint_arr,NULL);

	//访问Object方法
    jmethodID methodID_len=(*env)->GetMethodID(env,clazz,"getArrayLen","()I");
    jsize  len=(*env)->CallIntMethod(env,obj,methodID_len);

    sort(int_arr,len);
    (*env)->ReleaseIntArrayElements(env,jint_arr,int_arr,0);

    //访问private field字符串
    message_fid  = (*env)->GetFieldID(env,clazz,"message","Ljava/lang/String;");
    msg    = (jstring)(*env)->GetObjectField(env,obj,message_fid);
    msg_new = (*env)->GetStringUTFChars(env,msg,NULL);
    printf(msg_new);
    (*env)->ReleaseStringUTFChars(env,msg,msg_new);

    //java中字符不可变 只能重新创建一个字符
    j_new_str=(*env)->NewStringUTF(env,"mytest ");
    (*env)->SetObjectField(env,obj,message_fid,j_new_str);
}

void sort(jint * arr,jsize len){
   	jsize i=0, j=0, t=0;
	for (i=0;i<len;i++){
        for(j=i;j<len;j++){
            if(*(arr+i)>*(arr+j)){
                t=*(arr+j);
                *(arr+j)=*(arr+i);
                *(arr+i)=t;
            }
        }
    }
}
