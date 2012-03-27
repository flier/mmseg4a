Introduction
===========

mmseg4a is an Android porting of the [LibMMSeg](http://www.coreseek.cn/opensource/mmseg/) library, which base on the [MMSEG ](http://technology.chtsai.org/mmseg/) algorithm.

Usage
===========
You should load the dictionary first with the [SegmenterManager](https://github.com/flier/mmseg4a/blob/master/src/lu/flier/mmseg4a/SegmenterManager.java) object, and create a [Segmenter](https://github.com/flier/mmseg4a/blob/master/src/lu/flier/mmseg4a/Segmenter.java) object with createSegmenter method, call it's segment method to take the tokens.

~~~~~ java
SegmenterManager mgr = new DictionaryLoader(DemoActivity.this).load();
String tokens = mgr.createSegmenter(true).segment("这是一段需要分词的中文").getTokens();
~~~~~

Please check the [DemoActivity](https://github.com/flier/mmseg4a/blob/master/demo/src/lu/flier/mmseg4a/DemoActivity.java) class for more details.


Performance
===========
You could run the performance tests in the [demo](https://github.com/flier/mmseg4a/tree/master/demo) folder. 

    loaded dictionary in 567ms
    loaded a sample file with 31458 lines/2761.38 KBs in 5135ms
    found 1110124 tokens in 29765ms (92.77KB/s, 37296.29 tokens/s)

(HTC DHD with 1G CPU and 768M memory)