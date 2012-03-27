package lu.flier.mmseg4a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
		private final BufferedReader _reader;
		private long _tokens;
		
		TokenIterator(BufferedReader reader)
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
	
	class Tokens implements Iterable<String>
	{
		private final BufferedReader _reader;
		
		Tokens(Reader reader)
		{
			_reader = new BufferedReader(reader);
		}
		
		@Override
		public Iterator<String> iterator() {
			return new TokenIterator(_reader);
		}		
		
		public List<String> getTokens() throws IOException
		{
			List<String> tokens = new ArrayList<String>();
			
			do
			{
				String line = _reader.readLine();
				
				if (line == null) break;
				
				tokens.addAll(MMSegApi.SegmenterTokens(_seg, line));
			} while (_reader.ready());
			
			return tokens;
		}
	}
	
	public Tokens segment(String text)
	{
		return new Tokens(new StringReader(text));
	}
	
	public Tokens segment(InputStream is)
	{
		return new Tokens(new InputStreamReader(is, Charset.forName("UTF-16")));
	}
}
