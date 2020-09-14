#include "rtx2080ti.h"
RTX2080TITEMP temp={0};
int fd=0;
int ParseTemperature(PRtx2080ti_readoutdata data)
{
	float AMB=0;
	float pixel=0;
	int i=-1;
	if(data->Header.Delimeter!=0x2||data->Header.Stop!=0x3) return -1;
	AMB=(data->Ambient.AMB_H*256+data->Ambient.AMB_L-27315)/100.0;
	temp.AMB=AMB;
	while(i++<255)
	{
		pixel=(data->Pixel[i].OBJ_H*256+data->Pixel[i].OBJ_L-27315)/100.0;
		temp.Pixel[i]=pixel;
	}	
	return 0;
}
int ReadOutModuleData(PRtx2080ti_readoutdata data)
{
	
	int ret=-1;
	if(fd<=0) fd=open(DEVICE_NAME,O_RDWR);
	if(fd<0) return -1;
	ret=ioctl(fd,THERMOPILE__GET_FRANEDATA,data);
	if(ret < 0)
	{	
		printf("Get Data from MOdule Failed");
		return -1;
	}
	return 0;
}

int SetModuleFramerate(int framerate)
{
	int ret=0;
	if(fd<=0) return -1;
	ret=ioctl(fd,THERMOPILE__SET_FRAMERATE,(void *)&framerate);
	if(ret==0) return 0;
	else return -1;
}

int OpenModule()
{
	if(fd<=0) fd=open(DEVICE_NAME,O_RDWR);
	if(fd<0) return -1;
	return 0;
}
int ReleaseModule()
{
	if(fd>0) close(fd);
	return 0;
}

