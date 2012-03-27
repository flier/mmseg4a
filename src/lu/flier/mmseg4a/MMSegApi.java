package lu.flier.mmseg4a;


public class MMSegApi {
	static {
        System.loadLibrary("mmseg4a");
    }

	public static native long SegmenterManagerCreate(String path); 
	
	public static native void SegmenterManagerDestroy(long mgr);
	
	public static native void SegmenterManagerClear(long mgr);
	
	public static native long SegmenterManagerGetSegmenter(long mgr, boolean pooled);
	
	public static native void SegmenterDestroy(long seg);

	public static native boolean SegmenterIsEnd(long seg);

	public static native String SegmenterNext(long seg);

	public static native long SegmenterSegment(long seg, String text);
	
	public static native void TokensDestroy(long tokens);

	public static native String SegmenterTokens(long seg, String text, char sep);
}
