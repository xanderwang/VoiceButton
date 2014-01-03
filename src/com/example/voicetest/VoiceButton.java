package com.example.voicetest;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class VoiceButton extends Activity implements View.OnClickListener{
	
	private String TAG = "VoiceTest";
	
	private Button mStart = null;
	private Button mStop = null;
	private TextView mResult = null;
	private RecordThread mRecordThread = null;
	
	private static final int START_VOICE_TEST = 10;
	private static final int STOP_VOICE_TEST = 11;
	
	public static final int VOICE_TEST_RESULT = 1000;
	public static final int VOICE_TEST_TING = 1001;
	public static final int VOICE_TEST_NO = 10002;
	
	private String mStartTip = "";
	private String mStopTip = "";
	private String mResultTip = "";
	private String mReadyTip = "";
	
	private Handler mHandle = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int what = msg.what;
			String result = mResult.getText().toString() + "\n";
			switch (what) {
			case RecordThread.VOICE_TEST_RESULT:
			case VOICE_TEST_NO:
			case VOICE_TEST_TING:
				result += mResultTip + msg.obj.toString();
				break;
			case START_VOICE_TEST:
				result += mStartTip;
				break;
			case STOP_VOICE_TEST:
				result += mStopTip;
				break;
				
			default:
				break;
			}
			mResult.setText(result);
		}
	};
	
	
	private static int SAMPLE_RATE_IN_HZ = 44100;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_voice_test);
		
		mStart = (Button)findViewById(R.id.start);
		mStart.setOnClickListener(this);
		mStop = (Button)findViewById(R.id.stop);
		mStop.setOnClickListener(this);
		mResult = (TextView)findViewById(R.id.result);
		
		Resources mResources = getResources();
		mStartTip = mResources.getString(R.string.startTest);
		mStopTip = mResources.getString(R.string.stopTest);
		mResultTip = mResources.getString(R.string.testResult);
		mReadyTip = mResources.getString(R.string.ready_to_start);
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch (id) {
		case R.id.start:
			String mResultString = mResult.getText().toString();
			if(!mResultString.equals(mReadyTip)){
				mResult.setText(mReadyTip);
			}
			mRecordThread = new RecordThread();
			mRecordThread.setHandler(mHandle);
			mRecordThread.startThread();
			mHandle.sendEmptyMessage(START_VOICE_TEST);
			break;
		case R.id.stop:
			if( null != mRecordThread ){
				mRecordThread.stopThread();
			}
			break;
		default:
			break;
		}
	}
}
