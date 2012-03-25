LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LIBMMSEG_INCLUDES 	:=	$(LOCAL_PATH)/utils $(LOCAL_PATH)/css
LIBMMSEG_SOURCES	:= 	css/mmthunk.cpp \
						css/segmenter.cpp \
						css/SegmenterManager.cpp \
						css/SegmentPkg.cpp \
						css/SynonymsDict.cpp \
						css/ThesaurusDict.cpp \
						css/UnigramCorpusReader.cpp \
						css/UnigramDict.cpp \
						css/UnigramRecord.cpp \
						iniparser/dictionary.c \
						iniparser/iniparser.c \
						utils/csr_mmap.c \
						utils/csr_utils.c \
						utils/StringTokenizer.cpp \
						utils/Utf8_16.cpp 

LOCAL_MODULE    	:= mmseg4a
### Add all source file names to be included in lib separated by a whitespace
LOCAL_C_INCLUDES	:= $(LIBMMSEG_INCLUDES)
LOCAL_SRC_FILES 	:= mmseg4a.cpp $(LIBMMSEG_SOURCES)
LOCAL_CPP_FEATURES 	:= rtti
LOCAL_LDLIBS 		:= -llog -ldl

include $(BUILD_SHARED_LIBRARY)
