#include <cstdio>
//#include <cstdlib>
#include <algorithm>
#include <string>
#include <ctime>
#include <jni.h>
#include <android/log.h>
#include "header/com_exam_jni_Order_Stu.h"
#include "header/com_exam_jni_Order.h"

#define  LOG_TAG    "OrderNative"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace std;
static void traverse_0(JNIEnv*, jintArray);
static void traverse_1(JNIEnv*, jintArray);
/*
 * Class:     com_exam_jni_Order
 * Method:    sort
 * Signature: (Ljava/util/List;)V
 */
JNIEXPORT void JNICALL Java_com_exam_jni_Order_sort
  (JNIEnv *env, jclass clas, jobject list){
	int i = 0;
	jclass cls_arraylist = env->GetObjectClass(list);

	/*get parameter List size*/
	jmethodID arraylist_size = env->GetMethodID(cls_arraylist, "size","()I");
	jint len = env->CallIntMethod(list, arraylist_size);
	//LOGI("i value is %d\n", len);

	jmethodID arraylist_get = env->GetMethodID(cls_arraylist, "get","(I)Ljava/lang/Object;");
	for(i=0;i<len;i++){
		jobject obj = env->CallObjectMethod(list, arraylist_get, i);
		//const char*str = env->GetStringUTFChars(obj, false);
		//LOGI("i value is %s\n", obj);
	}
}

/*
 * Class:     com_exam_jni_Order
 * Method:    sortInts
 * Signature: ([I)V
 */
JNIEXPORT void JNICALL Java_com_exam_jni_Order_sortInts
  (JNIEnv *env, jclass clas, jintArray intArr){
	traverse_0(env, intArr);
	traverse_1(env, intArr);
}

static void traverse_1(JNIEnv *env, jintArray intArr){
	/**获取int[] 的长度*/
	jsize length = env->GetArrayLength(intArr);
	//LOGI("i value is %d\n", length);

	/**遍历int[], 求和*/
	jint buf[length];
	jint i, sum = 0;
	env->GetIntArrayRegion(intArr, 0, length, buf);
	for (i = 0; i < length; i++) {
		sum += buf[i];
	}
	LOGI("sum value is %d\n", sum);
}

static void traverse_0(JNIEnv *env, jintArray arr){
	jint *carr;
	jint i, sum = 0;
	carr = env->GetIntArrayElements(arr, NULL);
	if (carr == NULL) {
		LOGI("exception occurred\n");
		return; /* exception occurred */
	}

	jsize length = env->GetArrayLength(arr);
	for (i=0; i < length; i++) {
		sum += carr[i];
	}
	env->ReleaseIntArrayElements(arr, carr, 0);

	LOGI("sum value is %d\n", sum);
}

int compare(const void * a, const void * b) {
	return (*(char*) a - *(char*) b);
}

/*
 * Class:     com_exam_jni_Order
 * Method:    sortStr
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_exam_jni_Order_sortStr
  (JNIEnv *env, jclass clas, jstring pstring){
	clock_t start, finish;
	double duration;

	const char *str = env->GetStringUTFChars(pstring, 0);
	std::string sss(str);
	LOGI("libstdc++ style: char* size is:%d", sss.length());
	start = clock();

	//qsort(sss, sss.length(), sizeof(int), compare);

	finish = clock();
	duration = (double)(finish - start) / CLOCKS_PER_SEC;
	printf( "%f seconds\n", duration );

	env->ReleaseStringUTFChars(pstring, str);
}

/*
 * Class:     com_exam_jni_Order
 * Method:    sortStu
 * Signature: (Ljava/util/List;)V
 */
JNIEXPORT void JNICALL Java_com_exam_jni_Order_sortStu
  (JNIEnv *env, jclass clas, jobject ojb){

}

