#include <string.h>
#include <jni.h>

#include <android/log.h>

#define  LOG_TAG    "mmseg4a"
#define  LOG_DEBUG(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOG_INFO(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOG_WARN(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOG_ERROR(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOG_FATAL(...)  __android_log_print(ANDROID_LOG_FATAL,LOG_TAG,__VA_ARGS__)

#include <memory>

#include "mmseg4a.h"

#include "SegmenterManager.h"
#include "Segmenter.h"

jint throwIOException(JNIEnv *pEnv, const char *msg)
{
	static jclass clsIOException = pEnv->FindClass("java/io/IOException");

	return pEnv->ThrowNew(clsIOException, msg);
}

jint throwNullPointerException(JNIEnv *pEnv)
{
	static jclass clsIOException = pEnv->FindClass("java/lang/NullPointerException");

	return pEnv->ThrowNew(clsIOException, "invalid parameter");
}

jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
	LOG_INFO("module %s loaded", LOG_TAG);

	return JNI_VERSION_1_2;
}

void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved)
{
	LOG_INFO("module %s unloaded", LOG_TAG);
}

jlong JNICALL Java_lu_flier_mmseg4a_MMSegApi_SegmenterManagerCreate
  (JNIEnv *pEnv, jclass, jstring path)
{
	std::auto_ptr<css::SegmenterManager> mgr(new css::SegmenterManager());

	const char *str = (const char *) pEnv->GetStringUTFChars(path, NULL);

	LOG_INFO("loading dictionary from %s", str);

	int ret = mgr->init(str);

	pEnv->ReleaseStringUTFChars(path, str);

	if (0 == ret) return (jlong) mgr.release();

	LOG_ERROR("fail to load dictionary, err=%d", ret);

	throwIOException(pEnv, "fail to load dictionary");

	return 0;
}

void JNICALL Java_lu_flier_mmseg4a_MMSegApi_SegmenterManagerDestroy
  (JNIEnv *pEnv, jclass, jlong mgr)
{
	if (0 == mgr) {
		throwNullPointerException(pEnv);
	} else {
		LOG_DEBUG("destroy segmenter manager @ %x", mgr);

		delete reinterpret_cast<css::SegmenterManager *>(mgr);
	}
}

void JNICALL Java_lu_flier_mmseg4a_MMSegApi_SegmenterManagerClear
  (JNIEnv *pEnv, jclass, jlong mgr)
{
	if (0 == mgr) {
		throwNullPointerException(pEnv);
	} else {
		LOG_DEBUG("clear segmenter manager @ %x", mgr);

		reinterpret_cast<css::SegmenterManager *>(mgr)->clear();
	}
}

jlong JNICALL Java_lu_flier_mmseg4a_MMSegApi_SegmenterManagerGetSegmenter
  (JNIEnv *pEnv, jclass, jlong mgr, jboolean pooled)
{
	if (0 == mgr) {
		throwNullPointerException(pEnv);

		return 0;
	} else {
		return (jlong) reinterpret_cast<css::SegmenterManager *>(mgr)->getSegmenter(JNI_TRUE == pooled);
	}
}

void JNICALL Java_lu_flier_mmseg4a_MMSegApi_SegmenterDestroy
  (JNIEnv *pEnv, jclass, jlong seg)
{
	if (0 == seg) {
		throwNullPointerException(pEnv);
	} else {
		LOG_DEBUG("destroy segmenter @ %x", seg);

		delete reinterpret_cast<css::Segmenter *>(seg);
	}
}

jboolean JNICALL Java_lu_flier_mmseg4a_MMSegApi_SegmenterIsEnd
  (JNIEnv *pEnv, jclass, jlong seg)
{
	if (0 == seg) {
		throwNullPointerException(pEnv);

		return false;
	} else {
		return reinterpret_cast<css::Segmenter *>(seg)->isSentenceEnd() ? JNI_TRUE : JNI_FALSE;
	}
}

jstring JNICALL Java_lu_flier_mmseg4a_MMSegApi_SegmenterNext
  (JNIEnv *pEnv, jclass, jlong seg)
{
	if (0 == seg) {
		throwNullPointerException(pEnv);
	} else {
		u2 len = 0, symlen = 0;

		const char *tok = (const char *) reinterpret_cast<css::Segmenter *>(seg)->peekToken(len, symlen);

		if (tok && len && symlen) {
			std::string str(tok, len);

			return pEnv->NewStringUTF(str.c_str());
		}
	}

	return NULL;
}

typedef std::pair<jobject, const char *> tokens_t;

jlong JNICALL Java_lu_flier_mmseg4a_MMSegApi_SegmenterSegment
  (JNIEnv *pEnv, jclass, jlong seg, jstring str)
{
	if (0 == seg) {
		throwNullPointerException(pEnv);
	} else {
		const char *p = pEnv->GetStringUTFChars(str, NULL);
		jsize len = pEnv->GetStringUTFLength(str);

		LOG_DEBUG("segment text %p:%d", p, len);

		reinterpret_cast<css::Segmenter *>(seg)->setBuffer((u1 *) p, (u4) len);

		return (jlong) new tokens_t(pEnv->NewGlobalRef(str), p);
	}

	return 0;
}

void JNICALL Java_lu_flier_mmseg4a_MMSegApi_TokensDestroy
  (JNIEnv *pEnv, jclass, jlong p)
{
	if (0 == p) {
		throwNullPointerException(pEnv);
	} else {
		LOG_DEBUG("destroy tokens @ %x", p);

		std::auto_ptr<tokens_t> tokens(reinterpret_cast<tokens_t *>(p));

		pEnv->ReleaseStringUTFChars((jstring) tokens->first, tokens->second);
		pEnv->DeleteGlobalRef(tokens->first);
	}
}

jobject JNICALL Java_lu_flier_mmseg4a_MMSegApi_SegmenterTokens
  (JNIEnv *pEnv, jclass, jlong obj, jstring text)
{
	if (0 == obj) {
		throwNullPointerException(pEnv);
	} else {
		const char *p = pEnv->GetStringUTFChars(text, NULL);
		jsize len = pEnv->GetStringUTFLength(text);

		LOG_DEBUG("segmenting text %d bytes", p, len);

		css::Segmenter *seg = reinterpret_cast<css::Segmenter *>(obj);

		seg->setBuffer((u1 *) p, (u4) len);

		static jclass clsArrayList = pEnv->FindClass("java/util/ArrayList");
		static jmethodID ctor = pEnv->GetMethodID(clsArrayList, "<init>", "()V");
		static jmethodID add = pEnv->GetMethodID(clsArrayList, "add", "(Ljava/lang/Object;)Z");
		jobject tokens = pEnv->NewObject(clsArrayList, ctor);

		while (true) {
			u2 len = 0, symlen = 0;
			char* tok = (char*)seg->peekToken(len,symlen);

			if(!tok || !*tok || !len) break;

			std::string str(tok, len);

			pEnv->CallBooleanMethod(tokens, add, pEnv->NewStringUTF(str.c_str()));

			seg->popToken(len);
		}

		return tokens;
	}

	return NULL;
}
