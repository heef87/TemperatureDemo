/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <linux/watchdog.h>
#include <jni.h>
#include <android/log.h>
#include <unistd.h>
#include <string.h>

#define  LOG_TAG  "htpa3232"
#define  LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define  LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#include "htpa3232_API.h"

#define DEVICE_NAME "/dev/htpa3232"

struct htpa3232_W_DATA write_data;
struct htpa3232_R_DATA read_data;
int htpa3232_fd = -1;  

#ifndef _Included_com_ys_libhtpa3232_htpa3232
#define _Included_com_ys_libhtpa3232_htpa3232
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_ys_libhtpa3232_htpa3232_open
  (JNIEnv * env, jclass cls,int value)
{

	globe_write_data = &write_data;
	globe_read_data = &read_data;
	htpa3232_fd = open(DEVICE_NAME, O_RDWR);  
	printf("htpa3232 open htpa3232_fd=%d\n",htpa3232_fd);  	
	if(htpa3232_fd > 0){
		htpa3232_initialise();
		return htpa3232_fd;
	}
	else{
        	printf("Failed to open device %s\n", DEVICE_NAME);  
        	return -1;  		
	}	
}

JNIEXPORT void JNICALL Java_com_ys_libhtpa3232_htpa3232_close
  (JNIEnv * env, jclass cls)
{
    if (htpa3232_fd > 0){
		   fflush(stdout); 
           close(htpa3232_fd);
           htpa3232_fd = -1;
    }
    return;
}


JNIEXPORT jintArray JNICALL Java_com_ys_libhtpa3232_htpa3232_readTemperature
  (JNIEnv * env, jclass cls)
{
	jintArray intArr = (*env)->NewIntArray(env,1028);
	static int result_data[1028];
  	if (htpa3232_fd <= 0)
  		return NULL;
	else{
		htpa3232_measure(htpa3232_fd,result_data);
		(*env)->SetIntArrayRegion(env,intArr,0,1028,result_data);
	}
	return intArr;

}

#ifdef __cplusplus
}
#endif
#endif
