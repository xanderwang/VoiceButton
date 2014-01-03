package com.example.voicetest;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;

public class RecordThread extends Thread {
	
	//用于抛出实时声音检测结果
	private Handler mHandler = null;
	
	//用于读取音频缓冲区的数据
	private AudioRecord mAudioRecoder;
	//读取的缓冲区的数据大小
	private int mBuffer;
	
	//线程开关
	private boolean isRunning = false;
	
	//上次声音达到要求的时间
	private long lastTime = 0;
	//本次声音达到要求的时间
	private long nowTime = 0;
	
	//声音采样频率
	private static int SAMPLE_RATE_IN_HZ = 44100;

	public RecordThread() {
		super();	
		//获取缓冲区的大小
		mBuffer = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		
		mAudioRecoder = new AudioRecord(MediaRecorder.AudioSource.MIC, 
				SAMPLE_RATE_IN_HZ,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, mBuffer);
		//条件准备好，可以在线程分析声
		isRunning = true;
	}
	
	public static final int VOICE_TEST_RESULT = 0;

	private static final int VOICE_ACTION_NONE = 10;
	private static final int VOICE_ACTION_START = 11;
	private static final int VOICE_ACTION_CONTINUE = 12;
	private static final int VOICE_ACTION_END = 13;
	
	public static enum VOICEACTION {
			NONE,START,CONTINUE,END
	};
	
	private int mLastVoiceAction = VOICE_ACTION_NONE;
	
	private boolean isVoiceAction = false;
	private long startVoiceTime = 0;
	private long endVoiceTime = 0;
	//声音的检测结过
	private int values = 0;
	
	public void run() {
		super.run();
		mAudioRecoder.startRecording();
		while (isRunning) {
			// soundBuffer 用于读取缓存区的声音
			byte[] soundBuffer = new byte[mBuffer];
			int readResult = mAudioRecoder.read(soundBuffer, 0, mBuffer);
			
			if(AudioRecord.ERROR_INVALID_OPERATION == readResult ){//硬件初始化失
				isRunning = false;
			}
			else if( AudioRecord.ERROR_BAD_VALUE == readResult  ){//参数不能解析成有效的数据或索
				isRunning = false;
			}
			
			values = 0;
			// soundBuffer 内容取出，进行平方和运算
			for (int i = 0; i < soundBuffer.length; i++) {
				// 这里没有做运算的优化，为了更加清晰的展示代码
				values += soundBuffer[i] * soundBuffer[i];
			}
			// 平方和除以数据长度，得到音量大小可以获取白噪声，然后对实际采样进行标准化
			values /= readResult;
			// 如果想利用这个数值进行操作，建议sendMessage 将其抛出，在 Handler 里进行处理
			
			if(values >= 3000){
				if( !isVoiceAction ){//吹气
					mLastVoiceAction = VOICE_ACTION_START;
					startVoiceTime = System.currentTimeMillis();//记录时间
				}
				else if(isVoiceAction){//吹气
					mLastVoiceAction = VOICE_ACTION_CONTINUE;
				}
				isVoiceAction = true;//处于吹气状
			}
			else {
				isVoiceAction = false;//处于未吹气状			
				if(VOICE_ACTION_CONTINUE == mLastVoiceAction){//吹气结束
					mLastVoiceAction = VOICE_ACTION_END;
					endVoiceTime = System.currentTimeMillis();//记录结束时间
				}
				else if( VOICE_ACTION_END == mLastVoiceAction ){
					mLastVoiceAction = VOICE_ACTION_NONE;
				}
			}
			
			if( null != mHandler && VOICE_ACTION_NONE != mLastVoiceAction ){
				mHandler.sendMessage( Message.obtain(mHandler, VOICE_TEST_RESULT, mLastVoiceAction) );
			}
			
		}
		mAudioRecoder.stop();
	}
	
	public void stopThread() {
		// 在调用本线程Activity onPause 里调用，以便 Activity 暂停时释放麦克风
		isRunning = false;
	}

	public void startThread() {
		// 在调用本线程Activity onResume 里调用，以便 Activity 恢复后继续获取麦克风输入音量
		isRunning = true;
		super.start();
	}

	public Handler getHandler() {
		return mHandler;
	}

	public void setHandler(Handler handler) {
		this.mHandler = handler;
	}
	
}