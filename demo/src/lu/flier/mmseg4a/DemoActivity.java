package lu.flier.mmseg4a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

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
        barTestingProgress.setVisibility(View.INVISIBLE);
    }
    
    class TestTask extends AsyncTask<Void, String, Void>
    {
    	private int step = 0;

		@Override
		protected void onPreExecute() {
			btnStartTest.setEnabled(false);
			
			barTestingProgress.setVisibility(View.VISIBLE);
			barTestingProgress.setMax(3);
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
	    		
	    		ts = System.currentTimeMillis() - ts;
	    		
	    		publishProgress(String.format("loaded dictionary in %dms", ts));
	    		
	    		InputStream is = DemoActivity.this.getResources().openRawResource(R.raw.bible);
	    		
	    		int size = is.available(), count = 0;
	    		StringBuffer sb = new StringBuffer();
	    		
	    		ts = System.currentTimeMillis();
	    		
	    		count = loadSample(is, count, sb);
	    		
	    		publishProgress(String.format("loaded a sample file with %d lines/%.2f KBs in %dms", 
	    				count, ((float) size) / 1024, System.currentTimeMillis() - ts));
	    		
	    		ts = System.currentTimeMillis();
	    		
	    		String tokens = mgr.createSegmenter(true).segment(sb.toString()).getTokens();
	    		
	    		ts = System.currentTimeMillis() - ts;
	    		count = 0;
	    		
	    		for (int i=0; i<tokens.length(); i++)
	    		{
	    			if (tokens.charAt(i) == '\t')
	    			{
	    				count ++;
	    			}
	    		}
	    		
	    		publishProgress(String.format("found %d tokens in %sms (%.2f KB/s, %.2f tokens/s)", 
	    				count, ts, ((float) size) * 1000 / 1024 / ts, ((float) count) * 1000 / ts));
	    	} catch (Exception e) {
	    		Log.e(TAG, "fail to load dictionary", e);
			} 
	    	
			return null;
		}

		private int loadSample(InputStream is, int count, StringBuffer sb) throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-16")));
			
			while (reader.ready())
			{
				String line = reader.readLine();
				
				if (line == null) break;
				
				sb.append(line).append("\n");
				
				count++;
			}
			return count;
		}
    	
    }
    
    public void onStartTest(View view)
    {
    	new TestTask().execute();
    }
}