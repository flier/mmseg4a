package lu.flier.mmseg4a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
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
	
	class StringTokenIterator implements Iterator<String>
	{
		private long _tokens;
		
		StringTokenIterator(String text)
		{
			_tokens = MMSegApi.SegmenterSegment(_seg, text);
		}

		public void dispose()
		{
			if (_tokens != 0)
			{
				MMSegApi.TokensDestroy(_tokens);
				
				_tokens = 0;
			}
		}
		
		@Override
		protected void finalize() throws Throwable 
		{
			dispose();
		}
		
		@Override
		public boolean hasNext() {
			return !MMSegApi.SegmenterIsEnd(_seg);
		}

		@Override
		public String next() {
			if (MMSegApi.SegmenterIsEnd(_seg)) 
				throw new NoSuchElementException();
			
			String token = MMSegApi.SegmenterNext(_seg);
			
			if (token == null) throw new NoSuchElementException();
			
			return token;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();	
		}
	}
	
	class StreamTokenIterator implements Iterator<String>
	{
		private final BufferedReader _reader;
		private long _tokens;
		
		StreamTokenIterator(BufferedReader reader)
		{
			_reader = reader;
		}
		
		public void dispose()
		{
			if (_tokens != 0)
			{
				MMSegApi.TokensDestroy(_tokens);
				
				_tokens = 0;
			}
		}
		
		@Override
		protected void finalize() throws Throwable 
		{
			dispose();
		}
		
		@Override
		public boolean hasNext() {
			try {
				return !MMSegApi.SegmenterIsEnd(_seg) || _reader.ready();
			} catch (IOException e) {
				return false;
			}
		}

		@Override
		public String next() {
			if (MMSegApi.SegmenterIsEnd(_seg)) 
			{
				dispose();
				
				try 
				{
					String line = _reader.readLine();
					
					if (line == null) throw new NoSuchElementException();
					
					_tokens = MMSegApi.SegmenterSegment(_seg, line);
				} 
				catch (IOException e) 
				{
					throw new NoSuchElementException();
				}
			}
			
			String token = MMSegApi.SegmenterNext(_seg);
			
			if (token == null) throw new NoSuchElementException();
			
			return token;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}
	}
	
	public interface Tokens extends Iterable<String>
	{
		String getTokens() throws IOException;
	}
	
	public class StringTokens implements Tokens
	{
		private final String _text;
		
		StringTokens(String text)
		{
			_text = text;
		}

		@Override
		public Iterator<String> iterator() {
			return null;
		}

		@Override
		public String getTokens() throws IOException {
			return MMSegApi.SegmenterTokens(_seg, _text, '\t');
		}
		
	}
	
	public class StreamTokens implements Tokens
	{
		private final BufferedReader _reader;
		
		StreamTokens(Reader reader)
		{
			_reader = new BufferedReader(reader);
		}
		
		@Override
		public Iterator<String> iterator() {
			return new StreamTokenIterator(_reader);
		}		
		
		@Override
		public String getTokens() throws IOException
		{
			StringBuffer sb = new StringBuffer();
			
			do
			{
				String line = _reader.readLine();
				
				if (line == null) break;
				
				sb.append(MMSegApi.SegmenterTokens(_seg, line, '\t')).append('\t');
			} while (_reader.ready());
			
			return sb.toString();
		}
	}
	
	public Tokens segment(String text)
	{
		return new StringTokens(text);
	}
	
	public Tokens segment(InputStream is)
	{
		return segment(is, "UTF-16");
	}
	
	public Tokens segment(InputStream is, String encoding)
	{
		return new StreamTokens(new InputStreamReader(is, Charset.forName(encoding)));
	}
}
