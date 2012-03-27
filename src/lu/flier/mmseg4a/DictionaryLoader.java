package lu.flier.mmseg4a;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class DictionaryLoader 
{
	private static final String TAG = "mmseg4a";
	
	private final Context _ctxt;

	public DictionaryLoader(Context ctxt)
	{
		_ctxt = ctxt;
	}
	
	class LoadDictionaryTask extends AsyncTask<Void, String, SegmenterManager> 
	{	
		private final ProgressDialog _dlg = new ProgressDialog(_ctxt);
		
		@Override
		protected SegmenterManager doInBackground(Void... params) 
		{
			try 
			{
				this.publishProgress(_ctxt.getString(R.string.extracting_dic));
				
				File path = extractDictionary();
				
				if (this.isCancelled()) return null;
				
				Log.d(TAG, "loading dictionary from " + path.getAbsolutePath());
				
				this.publishProgress(_ctxt.getString(R.string.loading_dic));
				
				return new SegmenterManager(path);
			} 
			catch (IOException e) 
			{
				Log.w(TAG, "fail to load dictionary", e);
			}
			
			return null;
		}

		@Override
		protected void onCancelled() 
		{
			Log.i(TAG, "load dictionary cancelled");
		}

		@Override
		protected void onProgressUpdate(String... values) 
		{
			_dlg.setMessage(values[0]);
			_dlg.show();
		}

		@Override
		protected void onPreExecute() 
		{
			_dlg.setCancelable(true);
			_dlg.setMessage(_ctxt.getString(R.string.loading_dic));
			_dlg.show();
		}
		
		@Override
		protected void onPostExecute(SegmenterManager result) 
		{
			if (_dlg != null)
			{
				try {
					_dlg.dismiss();
				} catch (Exception e) {
					
				}
			}
		}
	}
	
	public AsyncTask<Void, String, SegmenterManager> asyncLoad()
	{
		return new LoadDictionaryTask().execute();
	}
	
	public SegmenterManager load() throws IOException
	{
		File path = extractDictionary();
		
		Log.d(TAG, "loading dictionary from " + path.getAbsolutePath());
		
		return new SegmenterManager(path);
	}
	
	private int copy(InputStream is, OutputStream os) throws IOException
	{
		byte[] buf = new byte[4096];
		int size = 0;
		
		while (is.available() > 0)
		{
			int len = is.read(buf);
			
			if (len == 0) break;
			
			os.write(buf, 0, len);
			
			size += len;
		}
		
		return size;
	}
	
	private File extractDictionary() throws IOException
	{
		InputStream is = _ctxt.getResources().openRawResource(R.raw.uni);
		
		int size = is.available();
		
		try 
		{
			FileInputStream fis = _ctxt.openFileInput("uni.lib");
			
			try 
			{
				if (fis.available() == size) return _ctxt.getFilesDir();	
				
				Log.i(TAG, "extract and overwrite the expired dictionary file");
			}
			finally
			{
				fis.close();
			}
		}
		catch (FileNotFoundException e) 
		{
			Log.i(TAG, "extract the dictionary file");
		}
		
		long ts = System.currentTimeMillis();
		
		copy(is, _ctxt.openFileOutput("uni.lib", Context.MODE_PRIVATE));
		
		Log.d(TAG, String.format("extracted %d bytes to the dictionary file uni.lib in %dms", size, System.currentTimeMillis() - ts));
		
		return _ctxt.getFilesDir();
	}
}