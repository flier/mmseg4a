package lu.flier.mmseg4a;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Segmenter 
{
	private final SegmenterManager _mgr;
	private long _seg;
	
	Segmenter(SegmenterManager mgr, long seg)
	{
		_mgr = mgr;
		_seg = seg;
	}
	
	public void dispose()
	{
		if (_seg != 0) 
		{
			MMSegApi.SegmenterDestroy(_seg);
			
			_seg = 0;
		}		
	}

	@Override
	protected void finalize() throws Throwable 
	{
		dispose();
	}
	
	public SegmenterManager getManager()
	{
		return _mgr;
	}
	
	class TokenIterator implements Iterator<String>
	{
		private long _tokens;
		
		TokenIterator(long tokens)
		{
			_tokens = tokens;
		}
		
		@Override
		protected void finalize() throws Throwable 
		{
			MMSegApi.TokensDestroy(_tokens);
			
			_tokens = 0;
		}
		
		@Override
		public boolean hasNext() {
			return !MMSegApi.SegmenterIsEnd(_seg);
		}

		@Override
		public String next() {
			String token = MMSegApi.SegmenterNext(_seg);
			
			if (token == null) throw new NoSuchElementException();
			
			return token;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}
	}
	
	class Tokens implements Iterable<String>
	{
		private final long _tokens;
		
		Tokens(long tokens)
		{
			_tokens = tokens;
		}
		
		@Override
		public Iterator<String> iterator() {
			return new TokenIterator(_tokens);
		}		
	}
	
	public Iterable<String> segment(String text)
	{
		return new Tokens(MMSegApi.SegmenterSegment(_seg, text));
	}
}
