package lu.flier.mmseg4a;

import java.io.File;

public class SegmenterManager 
{
	private long _mgr;
	
	public SegmenterManager(File path) 
	{
		_mgr = MMSegApi.SegmenterManagerCreate(path.getAbsolutePath());
	}
	
	public void dispose()
	{
		if (_mgr != 0)
		{
			MMSegApi.SegmenterManagerDestroy(_mgr);
			
			_mgr = 0;
		}
	}

	@Override
	protected void finalize() throws Throwable 
	{
		dispose();
	}
	
	public void clear()
	{
		MMSegApi.SegmenterManagerClear(_mgr);
	}
	
	public Segmenter createSegmenter(boolean pooled)
	{
		return new Segmenter(this, MMSegApi.SegmenterManagerGetSegmenter(_mgr, pooled));
	}
}
