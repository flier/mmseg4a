package lu.flier.mmseg4a;

import java.io.InputStream;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DemoActivity extends Activity 
{
	private static final String TAG = "mmseg4a";
	
	private Button btnStartTest;
	private TextView txtTestReport;
	private ProgressBar barTestingProgress;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        btnStartTest = ((Button) this.findViewById(R.id.btnStartTest));
        txtTestReport = ((TextView) this.findViewById(R.id.txtTestReport));
        barTestingProgress = ((ProgressBar) this.findViewById(R.id.barTestingProgress));
    }
    
    class TestTask extends AsyncTask<Void, String, Void>
    {
    	private int step = 0;

		@Override
		protected void onPreExecute() {
			btnStartTest.setEnabled(false);
			
			barTestingProgress.setVisibility(View.VISIBLE);
			barTestingProgress.setMax(5);
		}
		
		@Override
		protected void onPostExecute(Void result) {
			btnStartTest.setEnabled(true);
			
			barTestingProgress.setVisibility(View.INVISIBLE);
		}

		@Override
		protected void onProgressUpdate(String... values) {
			barTestingProgress.setProgress(step++);
			txtTestReport.append(values[0] + "\n");
		}

		@Override
		protected Void doInBackground(Void... params) {
	    	try
	    	{
	    		publishProgress("starting the test...");
	    		
	    		long ts = System.currentTimeMillis();
	    		
	    		SegmenterManager mgr = new DictionaryLoader(DemoActivity.this).load();
	    		
	    		publishProgress(String.format("loaded dictionary in %dms", System.currentTimeMillis() - ts));
	    		
	    		InputStream is = DemoActivity.this.getResources().openRawResource(R.raw.bible);
	    		
	    		int size = is.available();
	    		
	    		publishProgress(String.format("segment a sample file with %d bytes", size));
	    		
	    		ts = System.currentTimeMillis();
	    		
	    		int count = mgr.createSegmenter(true).segment(is).getTokens().size();
	    		
	    		ts = System.currentTimeMillis() - ts;
	    		
	    		publishProgress(String.format("found %d tokens in %sms (%.2fKB/s)", count, ts, ((float) size) / 1024 / ts));
	    	} catch (Exception e) {
	    		Log.e(TAG, "fail to load dictionary", e);
			} 
	    	
			return null;
		}
    	
    }
    
    public void onStartTest(View view)
    {
    	new TestTask().execute();
    }
}