package com.mipt.TvdFileManager;

interface IEventHandlerCallbacks 
{
	void playThumbVideo(String path);
	void showVideoMassage(String path);
	void releaseMediaPlayerAsync();
	void releaseMediaPlayerSync();
	void stopPlaying();
	boolean hasReleaseMedia();
}