/*
 *   Copyright Statement:
 *
 *     This software/firmware and related documentation ("MediaTek Software") are
 *     protected under relevant copyright laws. The information contained herein is
 *     confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *     the prior written permission of MediaTek inc. and/or its licensors, any
 *     reproduction, modification, use or disclosure of MediaTek Software, and
 *     information contained herein, in whole or in part, shall be strictly
 *     prohibited.
 *
 *     MediaTek Inc. (C) 2019. All rights reserved.
 *
 *     BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *    THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *     RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *     ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *     WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *     WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *     NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *     RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *     INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *     TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *     RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *     OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *     SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *     RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *     STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *     ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *     RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *     MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *     CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *     The following software/firmware and/or related documentation ("MediaTek
 *     Software") have been modified by MediaTek Inc. All revisions are subject to
 *     any receiver's applicable license agreements with MediaTek Inc.
 */
#include <android/frameworks/sensorservice/1.0/IEventQueue.h>
#include <android/frameworks/sensorservice/1.0/IEventQueueCallback.h>
#include <android/frameworks/sensorservice/1.0/ISensorManager.h>
#include <android/sensor.h>
#include <ASensorEventQueue.h>
#include <ASensorManager.h>
#include <pthread.h>
#include <stdio.h>
#include <math.h>
#include <cutils/properties.h>
#include <sensors/convert.h>
#include <system/graphics-base-v1.0.h>
#include <utils/Mutex.h>
#include <utils/std/Format.h>
#include "plugin/PipelinePluginType.h"
#include "mtk/mtk_platform_metadata_tag.h"
#include "mtk/mtk_feature_type.h"
#include "vndk/hardware_buffer.h"
#include "LogUtils.h"
#include "GLContext.h"
#include "GLUtils.h"
#include "BufferUtils.h"
#include "TaskThread.h"
#include <dlfcn.h>
#include "mtkperf_resource.h"

#include <unistd.h>

#ifndef ATRACE_TAG
#define ATRACE_TAG ATRACE_TAG_CAMERA
#endif

#ifdef __ANDROID__
#include <utils/Trace.h>
#else
#define ATRACE_BEGIN(...)
#define ATRACE_END(...)
#endif


#define LOG_TAG "AICombo"

using namespace android;
using android::frameworks::sensorservice::V1_0::IEventQueue;
using android::frameworks::sensorservice::V1_0::IEventQueueCallback;
using android::frameworks::sensorservice::V1_0::ISensorManager;
using android::frameworks::sensorservice::V1_0::Result;
using android::hardware::Return;
using android::hardware::sensors::V1_0::Event;
using android::hardware::sensors::V1_0::SensorInfo;
using android::hardware::sensors::V1_0::SensorType;

using android::hardware::hidl_handle;
using android::Mutex;
using android::OK;
using android::sp;

using NSCam::NSPipelinePlugin::eFeatureIndexMtk;
using NSCam::NSPipelinePlugin::Interceptor;
using NSCam::NSPipelinePlugin::PipelinePlugin;
using NSCam::NSPipelinePlugin::PluginRegister;
using NSCam::NSPipelinePlugin::Yuv;
using NSCam::NSPipelinePlugin::YuvPlugin;
using ::vendor::mediatek::hardware::mms::V1_0::HwCopybitParam;
using ::vendor::mediatek::hardware::mms::V1_2::IMms;

using namespace NSCam::Utils::Format;
using namespace NSCam::NSPipelinePlugin;

#define ONE_THREAD_MODE
#define DUMP_BUFFER_CAPTURE "debug.aicombo.dumpcapture.enabled"
#define DUMP_BUFFER_PREVIEW "debug.aicombo.dumppreview.enabled"
#define DUMP_BUFFER_VIDEO "debug.aicombo.dumpvideo.enabled"
static int gDumpBufferCaptureEnabled = ::property_get_int32(DUMP_BUFFER_CAPTURE, 0);
static int gDumpBufferPreviewEnabled = ::property_get_int32(DUMP_BUFFER_PREVIEW, 0);
static int gDumpBufferVideoEnabled = ::property_get_int32(DUMP_BUFFER_VIDEO, 0);

static Mutex gAcceSensorEventLock;
static int gOrientation = 0;
static int gSensorOrientation = 0;


class AICombo : public YuvPlugin::IProvider
{
public:
    typedef YuvPlugin::Property Property;
    typedef YuvPlugin::Selection Selection;
    typedef YuvPlugin::Request::Ptr RequestPtr;
    typedef YuvPlugin::RequestCallback::Ptr RequestCallbackPtr;
    typedef YuvPlugin::ConfigParam ConfigParam;
    enum AIComboType
    {
        PREVIEW,
        CAPTURE,
        VIDEO,
    };

    AICombo(AIComboType type);
    ~AICombo();
    const Property &property();
    void set(MINT32 openID1, MINT32 openID2);
    void init();
    void uninit();
    void config(const ConfigParam &param);
    MERROR negotiate(Selection &sel);
    MERROR process(RequestPtr pRequest, RequestCallbackPtr pCallback);
    void abort(std::vector<RequestPtr> &pRequests);

private:
    enum TaskType
    {
        INIT,
        PROCESS,
        UNINIT,
        EXIT,
        UNKNOWN,
    };

    class AIComboTask : public Task
    {
    private:
        TaskType type = UNKNOWN;
        AICombo *ac = nullptr;
        RequestPtr request;
        RequestCallbackPtr callback;

    public:
        AIComboTask() {}
        AIComboTask(TaskType t, AICombo *a) : type(t), ac(a) {}
        AIComboTask(TaskType t, AICombo *a, RequestPtr r) : type(t), ac(a), request(r) {}

        void run()
        {
            switch (type)
            {
            case INIT:
                ac->doInit();
                break;
            case UNINIT:
                ac->doUninit();
                break;
            case PROCESS:
                ac->doProcess(request);
                break;
            case EXIT:
            case UNKNOWN:
                break;
            }
        }
    };


    class PowerHalTask : public Task
    {
    private:
        AICombo *ac = nullptr;
        int duration;

    public:
        PowerHalTask() {}
        PowerHalTask(AICombo *a, int d) : ac(a), duration(d) {}

        void run()
        {
            ac-> boostGpu(duration);
        }
    };

    class AcceSensorListener : public IEventQueueCallback {
    public:
        Return<void> onEvent(const Event &e)
        {
            sensors_event_t sensorEvent;
            memset(&sensorEvent, 0, sizeof(sensorEvent));
            android::hardware::sensors::V1_0::implementation::convertToSensorEvent(e, &sensorEvent);

            Mutex::Autolock lock(gAcceSensorEventLock);

            switch(e.sensorType)
            {
            case SensorType::ACCELEROMETER:
            {
                int orientation = 0;
                float X = -(sensorEvent.acceleration.x);
                float Y = -(sensorEvent.acceleration.y);
                float Z = -(sensorEvent.acceleration.z);
                float magnitude = X * X + Y * Y;
                // Don't trust the angle if the magnitude is small compared to the y value
                if (magnitude * 4 >= Z * Z) {
                    float OneEightyOverPi = 57.29577957855f;
                    float angle = (float) atan2(-Y, X) * OneEightyOverPi;
                    orientation = 90 - (int) round(angle);
                    // normalize to 0 - 359 range
                    while (orientation >= 360) {
                        orientation -= 360;
                    }
                    while (orientation < 0) {
                        orientation += 360;
                    }
                }
                if (orientation != gOrientation) {
                    gOrientation = orientation;
                    if (orientation <= 45 || orientation > 315) {
                        orientation = 0;
                    }
                    if (orientation > 45 && orientation <= 135) {
                        orientation = 90;
                    }
                    if (orientation > 135 && orientation <= 225) {
                        orientation = 180;
                    }
                    if (orientation > 225) {
                        orientation = 270;
                    }
                    gSensorOrientation = orientation;
                    //MY_LOGD("onEvent gSensorOrientation %d",gSensorOrientation);
                }
            }
            break;

            default:
                MY_LOGE("unknown type(%d)",sensorEvent.type);
                break;
            }

            return android::hardware::Void();
        }
    };


    AIComboType mAIComboType;
    int mAIComboEffect = -1;
    Property mProperty;
    bool mPropertyInited = false;
    GLContext *mGLContext = nullptr;
    float mWhiteIntensity = 0.0f;
    float mSmoothIntensity = 0.0f;
    float mEyeIntensity = 0.0f;
    float mCheekIntensity = 0.0f;
    IImageBuffer *mRGBAOutBuffer = nullptr;
    bool mHasInit = false;

    const std::string gVertexShader = "attribute vec4 vPosition;\n"
                                      "attribute vec4 inputTextureCoordinate;\n"
                                      "varying vec2 yuvTexCoords;\n"
                                      "void main() {\n"
                                      "  yuvTexCoords = inputTextureCoordinate.xy;\n"
                                      "  gl_Position = vPosition;\n"
                                      "}\n";

    const std::string gFragmentShader = "#extension GL_OES_EGL_image_external : require\n"
                                        "precision mediump float;\n"
                                        "uniform samplerExternalOES yuvTexSampler;\n"
                                        "varying vec2 yuvTexCoords;\n"
                                        "void main() {\n"
                                        "  gl_FragColor = texture2D(yuvTexSampler, yuvTexCoords);\n"
                                        "}\n";

    const std::string gVertexShader2D = "attribute vec4 vPosition;\n"
                                        "attribute vec4 inputTextureCoordinate;\n"
                                        "varying vec2 texCoord;\n"
                                        "void main() {\n"
                                        "  texCoord = inputTextureCoordinate.xy;\n"
                                        "  gl_Position = vPosition;\n"
                                        "}\n";

    const std::string gFragmentShader2D = ""
                                          "precision mediump float;\n"
                                          "uniform sampler2D inputImageTexture;\n"
                                          "varying vec2 texCoord;\n"
                                          "void main() {\n"
                                          "  gl_FragColor = texture2D(inputImageTexture, texCoord);\n"
                                          "}\n";

    const std::string gVertexShaderYuv = "#version 300 es\n"
                                        "in vec4 vPosition;\n"
                                        "in vec4 inputTextureCoordinate;\n"
                                        "out vec2 texCoord;\n"
                                        "void main() {\n"
                                        "  texCoord = inputTextureCoordinate.xy;\n"
                                        "  gl_Position = vPosition;\n"
                                        "}\n";

    const std::string gFragmentShaderYuv = "#version 300 es\n"
                                        "#extension GL_OES_EGL_image_external : require\n"
                                        "#extension GL_EXT_YUV_target : require\n"
                                        "precision mediump float;\n"
                                        "uniform sampler2D inputImageTexture;\n"
                                       // "uniform __samplerExternal2DY2YEXT yuvTexSampler;\n"
                                        "in vec2 texCoord;\n"
                                        "layout (yuv) out mediump vec4 fragColor;\n"
                                        "void main() {\n"
                                        "   vec4 yuvTex;\n"
                                        "   vec4 rgbTex = texture(inputImageTexture, texCoord); \n"
                                        //"   yuvTex.xyz = rgb_2_yuv(rgbTex.xyz, itu_601); \n"
                                        "   yuvTex.xyz = rgb_2_yuv(rgbTex.xyz, itu_709); \n"
                                       // "	fragColor = texture(yuvTexSampler, yuvTexCoords); \n"
                                        "   fragColor = yuvTex; \n"
                                        "}\n";


    const float gTriangleVertices[8] = {
        -1.0f,
        -1.0f,
        1.0f,
        -1.0f,
        -1.0f,
        1.0f,
        1.0f,
        1.0f,
    };

    // turn 90 degrees counterclockwise
    const float gTextureCoordinateBack[8] = {
        0.0f,
        1.0f,
        0.0f,
        0.0f,
        1.0f,
        1.0f,
        1.0f,
        0.0f,
    };

    // turn 90 degrees clockwise
    const float gTextureCoordinateBackRevert[8] = {
        1.0f,
        0.0f,
        1.0f,
        1.0f,
        0.0f,
        0.0f,
        0.0f,
        1.0f,
    };

    // turn 90 degrees clockwise
    const float gTextureCoordinateFront[8] = {
        1.0f,
        0.0f,
        1.0f,
        1.0f,
        0.0f,
        0.0f,
        0.0f,
        1.0f,
    };

    // turn 90 degrees counterclockwise
    const float gTextureCoordinateFrontRevert[8] = {
        0.0f,
        1.0f,
        0.0f,
        0.0f,
        1.0f,
        1.0f,
        1.0f,
        0.0f,
    };


    GLint gvPositionHandle;
    GLuint gInputTextureCoordinate;
    GLuint mProgram;
    GLuint mTmpTextureID1;
    GLuint mTmpFbo1;

    GLint gvPositionHandle2D;
    GLuint gInputTextureCoordinate2D;
    GLuint mProgram2D;
    GLuint mTmpTextureID2;
    GLuint mTmpFbo2;

    GLint gvPositionHandleYuv;
    GLuint gInputTextureCoordinateYuv;
    GLuint mProgramYuv;
    GLuint mTmpFboYuv;


    bool loadPerfAPI();
    MVOID initPerfLib();
    MVOID closePerfLib();
    int enablePerf();
    int disablePerf(int handle);
    int boostGpu(int duration);

    using perfLockAcqFunc = int (*)(int, int, int[], int);
    using perfLockRelFunc = int (*)(int);

    /* function pointer to perfserv client */
    perfLockAcqFunc perfLockAcq = NULL;
    perfLockRelFunc perfLockRel = NULL;

    void *libHandle = NULL;
    bool perfLibEnabled = false;
    int mPerfHandle = -1;
    TaskThread<PowerHalTask> *mPHTaskThread = nullptr;

    //for sensor use
    sp<ISensorManager> mpSensorManager;
    sp<IEventQueue>    mpEventQueue;
    sp<AcceSensorListener> mpAcceSensorListener;
    MINT32             mAcceSensorHandle;
    int mLensFacing = 0;
    //


#ifdef ONE_THREAD_MODE
    TaskThread<AIComboTask> *mTaskThread = nullptr;
#endif

    void doInit();
    void doUninit();
    MERROR doProcess(RequestPtr pRequest);
    MERROR doProcessPreview(RequestPtr pRequest);
    MERROR doProcessCapture(RequestPtr pRequest);
    MERROR doProcessVideo(RequestPtr pRequest);
    void copyBuffer(IImageBuffer *src, IImageBuffer *dst);
    void drawFrame(RequestPtr pRequest);
    void switchEffect(RequestPtr pRequest);
    void setEffect(RequestPtr pRequest);
    void initShaderAndTexture(MSize inSize);
    void copyOESTextureTo2DTexture(MSize inSize, GLuint inTextureID);
    void copy2DTextureToOESTexture(MSize inSize, GLuint outTextureID);
    void copy2DTextureToOESYuvTexture(MSize inSize, GLuint outTextureID);
    void sensorInit();
    void sensorUninit();
    int calculateFaceOrientation();
};

class AIComboPreview : public AICombo
{
public:
    AIComboPreview() : AICombo(PREVIEW)
    {
        FUNCTION_IN;
        FUNCTION_OUT;
    }
    ~AIComboPreview()
    {
        FUNCTION_IN;
        FUNCTION_OUT;
    }
};

class AIComboCapture : public AICombo
{
public:
    AIComboCapture() : AICombo(CAPTURE)
    {
        FUNCTION_IN;
        FUNCTION_OUT;
    }
    ~AIComboCapture()
    {
        FUNCTION_IN;
        FUNCTION_OUT;
    }
};

class AIComboVideo : public AICombo
{
public:
    AIComboVideo() : AICombo(VIDEO)
    {
        FUNCTION_IN;
        FUNCTION_OUT;
    }
    ~AIComboVideo()
    {
        FUNCTION_IN;
        FUNCTION_OUT;
    }
};

REGISTER_PLUGIN_PROVIDER(Yuv, AIComboCapture);
REGISTER_PLUGIN_PROVIDER(Yuv, AIComboPreview);
REGISTER_PLUGIN_PROVIDER(Yuv, AIComboVideo);

AICombo::AICombo(AIComboType type)
{
    FUNCTION_IN;
    mAIComboType = type;
#ifdef ONE_THREAD_MODE
    mTaskThread = new TaskThread<AIComboTask>();
#endif
    mPHTaskThread = new TaskThread<PowerHalTask>();
    FUNCTION_OUT;
}

AICombo::~AICombo()
{
    FUNCTION_IN;
#ifdef ONE_THREAD_MODE
    mTaskThread->exit();
    mTaskThread->postTaskAndWaitDone(AIComboTask(EXIT, this));
    mTaskThread->join();
    delete mTaskThread;
#endif
    delete mPHTaskThread;
    FUNCTION_OUT;
}

const AICombo::Property &AICombo::property()
{
    FUNCTION_IN;

    if (!mPropertyInited)
    {
        switch (mAIComboType)
        {
        case PREVIEW:
            mProperty.mName = "AI COMBO preview";
            mProperty.mFeatures = MTK_FEATURE_AI_COMBO_PREVIEW;
            break;
        case CAPTURE:
            mProperty.mName = "AI COMBO capture";
            mProperty.mFeatures = MTK_FEATURE_AI_COMBO_CAPTURE;
            break;
        case VIDEO:
            mProperty.mName = "AI COMBO video";
            mProperty.mFeatures = MTK_FEATURE_AI_COMBO_VIDEO;
            break;
        }
        mProperty.mInPlace = MFALSE;
        mPropertyInited = true;
    }
    //MY_LOGD("mProperty.mFeatures = %lu, mAIComboType = %d", (unsigned long)mProperty.mFeatures, mAIComboType);
    FUNCTION_OUT;
    return mProperty;
}
void AICombo::set(MINT32 openID1, MINT32 openID2)
{
    FUNCTION_IN;
    //MY_LOGD("set openID1:%d openID2:%d", openID1, openID2);
    FUNCTION_OUT;
}

void AICombo::init()
{
    FUNCTION_IN;
#ifdef ONE_THREAD_MODE
    mTaskThread->postTaskAndWaitDone(AIComboTask(INIT, this));
#else
    doInit();
#endif
    FUNCTION_OUT;
}

void AICombo::doInit()
{
    FUNCTION_IN;
    sensorInit();
    initPerfLib();

    FUNCTION_OUT;
}

void AICombo::uninit()
{
    FUNCTION_IN;
#ifdef ONE_THREAD_MODE
    mTaskThread->postTaskAndWaitDone(AIComboTask(UNINIT, this));
#else
    doUninit();
#endif
    FUNCTION_OUT;
}

void AICombo::doUninit()
{
    FUNCTION_IN;
    if (mGLContext != NULL)
    {
        if (mTmpTextureID1)
        {
            GLuint texture[1];
            texture[0] = mTmpTextureID1;
            glDeleteTextures(1, texture);
            //GLUtils::checkGlError("glDeleteTextures mTmpTextureID1");
        }

        GLuint fbo[1];
        fbo[0] = mTmpFbo1;
        glDeleteFramebuffers(1, fbo);
        //GLUtils::checkGlError("glDeleteFramebuffers");
        if (mTmpTextureID2)
        {
            GLuint texture[1];
            texture[0] = mTmpTextureID2;
            glDeleteTextures(1, texture);
            //GLUtils::checkGlError("glDeleteTextures mTmpTextureID2");
        }

        GLuint fbo2[1];
        fbo2[0] = mTmpFbo2;
        glDeleteFramebuffers(1, fbo2);
        //GLUtils::checkGlError("glDeleteFramebuffers fbo2");

        if (mAIComboType == VIDEO)
        {
            GLuint fboYuv[1];
            fboYuv[0] = mTmpFboYuv;
            glDeleteFramebuffers(1, fboYuv);
            //GLUtils::checkGlError("glDeleteFramebuffers fboYuv");
        }

        if (mRGBAOutBuffer != nullptr)
        {
            BufferUtils::deallocBuffer(mRGBAOutBuffer);
            mRGBAOutBuffer = nullptr;
        }

        mGLContext->unInitialize();
        mGLContext = NULL;
        mHasInit = false;
    }
    sensorUninit();
    if (mPerfHandle != -1)
        disablePerf(mPerfHandle);
    closePerfLib();
    FUNCTION_OUT;
}

void AICombo::config(const ConfigParam &param)
{
    FUNCTION_IN;
    FUNCTION_OUT;
}

MERROR AICombo::negotiate(Selection &sel)
{
    FUNCTION_IN;
    switch (mAIComboType)
    {
    case PREVIEW:
    case CAPTURE:
        sel.mIBufferFull.setRequired(MTRUE)
            .addAcceptedFormat(eImgFmt_YV12)
            .addAcceptedSize(eImgSize_Full);
        sel.mOBufferFull.setRequired(MTRUE)
            .addAcceptedFormat(eImgFmt_YV12)
            .addAcceptedSize(eImgSize_Full);
        break;
    case VIDEO:
        sel.mIBufferFull.setRequired(MTRUE)
            .addAcceptedFormat(eImgFmt_YV12)
            .addAcceptedSize(eImgSize_Full);
        sel.mOBufferFull.setRequired(MTRUE)
            .addAcceptedFormat(eImgFmt_YV12)
            .addAcceptedSize(eImgSize_Full);
        sel.mOBufferFull0.setRequired(MTRUE)
            .addAcceptedFormat(eImgFmt_YV12)
            .addAcceptedSize(eImgSize_Full);
        break;
    }

    sel.mIMetadataApp.setRequired(MTRUE);
    sel.mOMetadataApp.setRequired(MTRUE);
    FUNCTION_OUT;
    return OK;
}

MERROR AICombo::process(RequestPtr pRequest, RequestCallbackPtr pCallback)
{
    FUNCTION_IN;
    ATRACE_BEGIN("AICombo::process");
#ifdef ONE_THREAD_MODE
    mTaskThread->postTaskAndWaitDone(AIComboTask(PROCESS, this, pRequest));
#else
    doProcess(pRequest);
#endif
    if (pCallback != nullptr)
    {
        ATRACE_BEGIN("AICombo::process_onCompleted");
        pCallback->onCompleted(pRequest, OK);
        ATRACE_END();
    }
    ATRACE_END();
    FUNCTION_OUT;
    return OK;
}

MERROR AICombo::doProcess(RequestPtr pRequest)
{
    FUNCTION_IN;
    ATRACE_BEGIN("AICombo::doProcess");
    MERROR ret;
    switch (mAIComboType)
    {
    case PREVIEW:
        ret = doProcessPreview(pRequest);
        break;
    case CAPTURE:
        ret = doProcessCapture(pRequest);
        break;
    case VIDEO:
        //+ ai_camera
        ret = doProcessVideo(pRequest);
        break;
    default:
        break;
    }
    ATRACE_END();
    FUNCTION_OUT;
    return ret;
}

template <class T>
bool tryGetMetadata(IMetadata const *pMetadata, MUINT32 tag, T &rVal)
{
    if (pMetadata == nullptr)
        return false;

    IMetadata::IEntry entry = pMetadata->entryFor(tag);

    if (!entry.isEmpty())
    {
        rVal = entry.itemAt(0, Type2Type<T>());
        return true;
    }
    return false;
}

int getLensFacing(AICombo::RequestPtr pRequest)
{
    MINT32 lensFacing = 0;
    IMetadata *pImetadata = pRequest->mIMetadataApp->acquire();
    if (pImetadata != nullptr && pImetadata->count() > 0)
    {
        tryGetMetadata<MINT32>(pImetadata, MTK_POSTALGO_LENS_FACING, lensFacing);
    }
    return lensFacing;
}

int getJpegRotation(AICombo::RequestPtr pRequest)
{
    MINT32 jpegRotation = 0;
    IMetadata *pImetadata = pRequest->mIMetadataApp->acquire();
    if (pImetadata != nullptr && pImetadata->count() > 0)
    {
        tryGetMetadata<MINT32>(pImetadata, MTK_POSTALGO_JPEG_ORIENTATION, jpegRotation);
    }
    return jpegRotation;
}

int getEffectType(AICombo::RequestPtr pRequest)
{
    MINT32 type = 0;
    IMetadata *pImetadata = pRequest->mIMetadataApp->acquire();
    if (pImetadata != nullptr && pImetadata->count() > 0)
    {
        tryGetMetadata<MINT32>(pImetadata, MTK_POSTALGO_AI_COMBO_TYPE, type);
    }
    return type;
}

float getWhiteIntensity(AICombo::RequestPtr pRequest)
{
    MINT32 value = 0;
    IMetadata *pImetadata = pRequest->mIMetadataApp->acquire();
    if (pImetadata != nullptr && pImetadata->count() > 0)
    {
        tryGetMetadata<MINT32>(pImetadata, MTK_POSTALGO_AI_COMBO_WHITE, value);
    }
    if (value == 0)
        return 0.0;
    else
        return (value / 10.0);
}

float getSmoothIntensity(AICombo::RequestPtr pRequest)
{
    MINT32 value = 0;
    IMetadata *pImetadata = pRequest->mIMetadataApp->acquire();
    if (pImetadata != nullptr && pImetadata->count() > 0)
    {
        tryGetMetadata<MINT32>(pImetadata, MTK_POSTALGO_AI_COMBO_SMOOTH, value);
    }
    if (value == 0)
        return 0.0;
    else
        return (value / 10.0);
}

float getEyeIntensity(AICombo::RequestPtr pRequest)
{
    MINT32 value = 0;
    IMetadata *pImetadata = pRequest->mIMetadataApp->acquire();
    if (pImetadata != nullptr && pImetadata->count() > 0)
    {
        tryGetMetadata<MINT32>(pImetadata, MTK_POSTALGO_AI_COMBO_BIGEYE, value);
    }
    if (value == 0)
        return 0.0;
    else
        return (value / 10.0);
}

float getCheekIntensity(AICombo::RequestPtr pRequest)
{
    MINT32 value = 0;
    IMetadata *pImetadata = pRequest->mIMetadataApp->acquire();
    if (pImetadata != nullptr && pImetadata->count() > 0)
    {
        tryGetMetadata<MINT32>(pImetadata, MTK_POSTALGO_AI_COMBO_SMALLCHEEK, value);
    }
    if (value == 0)
        return 0.0;
    else
        return (value / 10.0);
}

void AICombo::switchEffect(RequestPtr pRequest)
{
    ATRACE_BEGIN("AICombo::switchEffect");
    int effectType = getEffectType(pRequest);
    int comboEffect = 0;
    if (effectType & AI_COMBO_NO_EFFECT)
    {
        comboEffect = 0;
    }
    else if (effectType & AI_COMBO_SLIMMING)
    {
        comboEffect = 3;
    }
    else if (effectType & AI_COMBO_LEGGY)
    {
        comboEffect = 4;
    }
    else if (effectType & AI_COMBO_BOKEH)
    {
        comboEffect = 1;
    }
    else if (effectType & AI_COMBO_COLOR)
    {
        comboEffect = 2;
    }

    if (mAIComboEffect != comboEffect)
    {
            mAIComboEffect = comboEffect;
    }
    ATRACE_END();
}

void AICombo::setEffect(RequestPtr pRequest)
{
    ATRACE_BEGIN("AICombo::setEffect");
    float whiteIntensity = getWhiteIntensity(pRequest);
    float smoothIntensity = getSmoothIntensity(pRequest);
    float eyeIntensity = getEyeIntensity(pRequest);
    float cheekIntensity = getCheekIntensity(pRequest);
    /*if (fabs(mWhiteIntensity - whiteIntensity) >= 1e-6 || fabs(mSmoothIntensity - smoothIntensity) >= 1e-6)
    {
    }
    if (fabs(mEyeIntensity - eyeIntensity) >= 1e-6 || fabs(mCheekIntensity - cheekIntensity) >= 1e-6)
    {
    }*/
    mWhiteIntensity = whiteIntensity;
    mSmoothIntensity = smoothIntensity;
    mEyeIntensity = eyeIntensity;
    mCheekIntensity = cheekIntensity;
    ATRACE_END();
}

void AICombo::initShaderAndTexture(MSize inSize)
{
    ATRACE_BEGIN("AICombo::initShaderAndTexture");
    mProgram = GLUtils::createProgram(gVertexShader.c_str(), gFragmentShader.c_str());
    if (mProgram == 0)
    {
        GLUtils::checkGlError("createProgram");
    }
    gvPositionHandle = glGetAttribLocation(mProgram, "vPosition");
    //GLUtils::checkGlError("glGetAttribLocation");

    gInputTextureCoordinate = glGetAttribLocation(mProgram, "inputTextureCoordinate");
    //GLUtils::checkGlError("glGetAttribLocation");

    //create temp texture
    mTmpTextureID1 = GLUtils::generateNormalTexture(GL_LINEAR, GL_CLAMP_TO_EDGE, inSize.h, inSize.w);
    glGenFramebuffers(1, &mTmpFbo1);
    //GLUtils::checkGlError("glGenFramebuffers");

    mProgram2D = GLUtils::createProgram(gVertexShader2D.c_str(), gFragmentShader2D.c_str());
    if (mProgram2D == 0)
    {
        GLUtils::checkGlError("createProgram");
    }
    gvPositionHandle2D = glGetAttribLocation(mProgram2D, "vPosition");
    //GLUtils::checkGlError("glGetAttribLocation");

    gInputTextureCoordinate2D = glGetAttribLocation(mProgram2D, "inputTextureCoordinate");
    //GLUtils::checkGlError("glGetAttribLocation");

    mTmpTextureID2 = GLUtils::generateNormalTexture(GL_LINEAR, GL_CLAMP_TO_EDGE, inSize.h, inSize.w);
    glGenFramebuffers(1, &mTmpFbo2);
    //GLUtils::checkGlError("glGenFramebuffers mTmpFbo2");

    if (mAIComboType == VIDEO)
    {
        mProgramYuv = GLUtils::createProgram(gVertexShaderYuv.c_str(), gFragmentShaderYuv.c_str());
        if (mProgramYuv == 0)
        {
            GLUtils::checkGlError("createProgram mProgramYuv");
        }

        gvPositionHandleYuv = glGetAttribLocation(mProgramYuv, "vPosition");
        //GLUtils::checkGlError("glGetAttribLocation mProgramYuv");

        gInputTextureCoordinateYuv = glGetAttribLocation(mProgramYuv, "inputTextureCoordinate");
        //GLUtils::checkGlError("glGetAttribLocation mProgramYuv");

        glGenFramebuffers(1, &mTmpFboYuv);
        //GLUtils::checkGlError("glGenFramebuffers mTmpFboYuv");
    }
    ATRACE_END();
}

void AICombo::copyOESTextureTo2DTexture(MSize inSize, GLuint inTextureID)
{
    ATRACE_BEGIN("AICombo::copyOESTextureTo2DTexture");
    glBindFramebuffer(GL_FRAMEBUFFER, mTmpFbo1);
    //GLUtils::checkGlError("glBindFramebuffer");
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, mTmpTextureID1, 0);
    //GLUtils::checkGlError("glFramebufferTexture2D");
    glCheckFramebufferStatus(GL_FRAMEBUFFER);
    glViewport(0, 0, inSize.h, inSize.w);

    glActiveTexture(GL_TEXTURE0);

    glBindTexture(GL_TEXTURE_EXTERNAL_OES, inTextureID);
    //GLUtils::checkGlError("glBindTexture");

    glVertexAttribPointer(gvPositionHandle, 2, GL_FLOAT, GL_FALSE, 0, gTriangleVertices);
    //GLUtils::checkGlError("glVertexAttribPointer");
    glEnableVertexAttribArray(gvPositionHandle);

    if (mLensFacing == PAS_LENS_FACING_FRONT)
        glVertexAttribPointer(gInputTextureCoordinate, 2, GL_FLOAT, GL_FALSE, 0, gTextureCoordinateFront);
    else
        glVertexAttribPointer(gInputTextureCoordinate, 2, GL_FLOAT, GL_FALSE, 0, gTextureCoordinateBack);
    //GLUtils::checkGlError("glVertexAttribPointer");
    glEnableVertexAttribArray(gInputTextureCoordinate);
    //GLUtils::checkGlError("glEnableVertexAttribArray");
    glUseProgram(mProgram);
    //GLUtils::checkGlError("glUseProgram");
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    //GLUtils::checkGlError("glDrawArrays");
    glDisableVertexAttribArray(gvPositionHandle);
    glDisableVertexAttribArray(gInputTextureCoordinate);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    //GLUtils::checkGlError("glBindFramebuffer");
    ATRACE_END();
}

void AICombo::copy2DTextureToOESTexture(MSize inSize, GLuint outTextureID)
{
    ATRACE_BEGIN("AICombo::copy2DTextureToOESTexture");
    glBindFramebuffer(GL_FRAMEBUFFER, mTmpFbo2);
    //GLUtils::checkGlError("glBindFramebuffer");
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_EXTERNAL_OES, outTextureID, 0);
    //GLUtils::checkGlError("glFramebufferTexture2D");
    glCheckFramebufferStatus(GL_FRAMEBUFFER);
    glViewport(0, 0, inSize.w, inSize.h);

    glActiveTexture(GL_TEXTURE0);

    glBindTexture(GL_TEXTURE_2D, mTmpTextureID2);
    //GLUtils::checkGlError("glBindTexture");


    glVertexAttribPointer(gvPositionHandle2D, 2, GL_FLOAT, GL_FALSE, 0, gTriangleVertices);
    //GLUtils::checkGlError("glVertexAttribPointer");
    glEnableVertexAttribArray(gvPositionHandle2D);

    if (mLensFacing == PAS_LENS_FACING_FRONT)
        glVertexAttribPointer(gInputTextureCoordinate2D, 2, GL_FLOAT, GL_FALSE, 0, gTextureCoordinateFrontRevert);
    else
        glVertexAttribPointer(gInputTextureCoordinate2D, 2, GL_FLOAT, GL_FALSE, 0, gTextureCoordinateBackRevert);
    //GLUtils::checkGlError("glVertexAttribPointer");
    glEnableVertexAttribArray(gInputTextureCoordinate2D);
    //GLUtils::checkGlError("glEnableVertexAttribArray");
    glUseProgram(mProgram2D);
    //GLUtils::checkGlError("glUseProgram");
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    //GLUtils::checkGlError("glDrawArrays");
    glDisableVertexAttribArray(gvPositionHandle2D);
    glDisableVertexAttribArray(gInputTextureCoordinate2D);
    glBindTexture(GL_TEXTURE_2D, 0);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    //GLUtils::checkGlError("glBindFramebuffer");
    ATRACE_END();
}

void AICombo::copy2DTextureToOESYuvTexture(MSize inSize, GLuint outTextureID)
{
    ATRACE_BEGIN("AICombo::copy2DTextureToOESYuvTexture");
    glBindFramebuffer(GL_FRAMEBUFFER, mTmpFboYuv);
    //GLUtils::checkGlError("glBindFramebuffer yuv");
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_EXTERNAL_OES, outTextureID, 0);
    //GLUtils::checkGlError("glFramebufferTexture2D yuv");
    glCheckFramebufferStatus(GL_FRAMEBUFFER);
    glViewport(0, 0, inSize.w, inSize.h);

    glActiveTexture(GL_TEXTURE0);

    glBindTexture(GL_TEXTURE_2D, mTmpTextureID2);
    //GLUtils::checkGlError("glBindTexture");

    glVertexAttribPointer(gvPositionHandleYuv, 2, GL_FLOAT, GL_FALSE, 0, gTriangleVertices);
    //GLUtils::checkGlError("glVertexAttribPointer");
    glEnableVertexAttribArray(gvPositionHandleYuv);

    if (mLensFacing == PAS_LENS_FACING_FRONT)
        glVertexAttribPointer(gInputTextureCoordinateYuv, 2, GL_FLOAT, GL_FALSE, 0, gTextureCoordinateFrontRevert);
    else
        glVertexAttribPointer(gInputTextureCoordinateYuv, 2, GL_FLOAT, GL_FALSE, 0, gTextureCoordinateBackRevert);
    //GLUtils::checkGlError("glVertexAttribPointer yuv");
    glEnableVertexAttribArray(gInputTextureCoordinateYuv);
    //GLUtils::checkGlError("glEnableVertexAttribArray yuv");
    glUseProgram(mProgramYuv);
    //GLUtils::checkGlError("glUseProgram yuv");
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    //GLUtils::checkGlError("glDrawArrays yuv");
    glDisableVertexAttribArray(gvPositionHandleYuv);
    glDisableVertexAttribArray(gInputTextureCoordinateYuv);
    glBindTexture(GL_TEXTURE_2D, 0);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    //GLUtils::checkGlError("glBindFramebuffer");
    ATRACE_END();
}

void AICombo::sensorInit()
{
    mpSensorManager = ISensorManager::getService();
    if (mpSensorManager == NULL)
    {
        MY_LOGE("get SensorManager fail!");
        return;
    }
    mpSensorManager->getDefaultSensor(SensorType::ACCELEROMETER,
        [&](const SensorInfo& sensor, Result ret) {
            ret == Result::OK ? mAcceSensorHandle = sensor.sensorHandle : mAcceSensorHandle = -1;
        });

    if (mAcceSensorHandle == -1)
    {
        MY_LOGE("get DefaultSensor fail! mAcceSensorHandle=%d", mAcceSensorHandle);
        return;
    }
    //create SensorEventQueue and register callback
    mpAcceSensorListener = new AcceSensorListener();
    mpSensorManager->createEventQueue(mpAcceSensorListener,
        [&](const sp<IEventQueue>& queue, Result ret) {
            ret == Result::OK ? mpEventQueue = queue : mpEventQueue = NULL;
        });

    if (mpEventQueue != NULL)
    {
        ::android::hardware::Return<Result> retResult = mpEventQueue->enableSensor(mAcceSensorHandle, 20 * 1000 /* sample period */, 0 /* latency */);
        if (!retResult.isOk())
        {
            MY_LOGE("enable AccelSensor fail!");
        }
    }
    else
    {
        MY_LOGE("createEventQueue fail!");
    }
}


void AICombo::sensorUninit()
{
    if (mpEventQueue != NULL)
    {
        ::android::hardware::Return<Result> ret = mpEventQueue->disableSensor(mAcceSensorHandle);
        if (!ret.isOk())
        {
            MY_LOGE("disable accel sensor fail");
        }
        mpEventQueue = NULL;
    }
    if (mpAcceSensorListener != NULL)
    {
        mpAcceSensorListener = NULL;
    }
    if (mpSensorManager != NULL)
    {
        mpSensorManager = NULL;
    }
}

int AICombo::calculateFaceOrientation()
{
   int orientation = (mLensFacing == PAS_LENS_FACING_FRONT) ? 360 - gSensorOrientation: gSensorOrientation;
    if (orientation >= 360)
    {
        orientation -= 360;
    } else if(orientation < 0)
    {
        orientation += 360;
    }
    return orientation;
}

MERROR AICombo::doProcessPreview(RequestPtr pRequest)
{
    FUNCTION_IN;
    FPS;
    drawFrame(pRequest);

    FUNCTION_OUT;

    return OK;
}

MERROR AICombo::doProcessVideo(RequestPtr pRequest)
{
    FUNCTION_IN;
    FPS;
    drawFrame(pRequest);
    FUNCTION_OUT;

    return OK;
}

void AICombo::copyBuffer(IImageBuffer *src, IImageBuffer *dst)
{
    BufferUtils::mdpResizeAndConvert(src, dst);
}

// ai_camera TODO
void AICombo::drawFrame(RequestPtr pRequest)
{
    FUNCTION_IN;
    FPS;
    ATRACE_BEGIN("AICombo::drawFrame");

    IImageBuffer *in = NULL, *out = NULL, *out_video = NULL;

    if (pRequest->mIBufferFull != nullptr)
    {
        in = pRequest->mIBufferFull->acquire();
    }

    // preview/capture OUT
    if (pRequest->mOBufferFull != nullptr)
    {
        out = pRequest->mOBufferFull->acquire();
    }

    MSize inSize = in->getImgSize();
    MSize outSize = out->getImgSize();

    // VE SDK will use glReadPixel to read texture, boost GPU to accelerate
    mPHTaskThread->postTaskAsync(PowerHalTask(this, 25));

    /* ai_camera TODO init SDK*/
    if (mHasInit == false)
    {
        mLensFacing = getLensFacing(pRequest);
        mPerfHandle = enablePerf();
        mGLContext = new GLContext();
        mGLContext->initialize(true);
        mHasInit = true;
        mGLContext->enable();
        initShaderAndTexture(inSize);
    }

    // video OUT
    if (mAIComboType == VIDEO && pRequest->mOBufferFull0 != nullptr)
    {
        out_video = pRequest->mOBufferFull0->acquire();
        //MY_LOGD("process out_video ImageBuffer  = %p", out_video);
    }

    if (in != NULL && out != NULL)
    {

#ifdef DEBUG_DUMP_BUFFER
        std::string inbuffer = std::to_string(pRequest->mRequestNo) + "inbuffer";
        BufferUtils::dumpBuffer(in, const_cast<char *>(inbuffer.c_str()));
#endif

        //MY_LOGD("inFormat = %d, inSize.w = %d, inSize.h = %d, outFormat = %d, outSize.w = %d, outSize.h = %d, effectType = %d, mAIComboType = %d",
        //in->getImgFormat(), inSize.w, inSize.h,
        //out->getImgFormat(),outSize.w, outSize.h,
        //effectType,mAIComboType);

        // create texture in
        GLUTILS_GET_EGLIMAGE_TEXTURE_PARAMS paramsIn;
        memset(&paramsIn, 0, sizeof(GLUTILS_GET_EGLIMAGE_TEXTURE_PARAMS));
        paramsIn.eglDisplay = mGLContext->_display;
        paramsIn.isRenderTarget = false;
        paramsIn.graphicBuffer = BufferUtils::getAHWBuffer(in);

        GLUtils::getEGLImageTexture(&paramsIn);
        //MY_LOGD("create input texture ok");

        //copyOESTextureTo2DTexture(inSize, paramsIn.textureID);

        // create texture out
        GLUTILS_GET_EGLIMAGE_TEXTURE_PARAMS paramsOut;
        memset(&paramsOut, 0, sizeof(GLUTILS_GET_EGLIMAGE_TEXTURE_PARAMS));
        paramsOut.eglDisplay = mGLContext->_display;
        paramsOut.isRenderTarget = true;

        // for video path
        GLUTILS_GET_EGLIMAGE_TEXTURE_PARAMS paramsOut_video;
        if (mAIComboType == VIDEO && out_video != NULL) {
            memset(&paramsOut_video, 0, sizeof(GLUTILS_GET_EGLIMAGE_TEXTURE_PARAMS));
            paramsOut_video.eglDisplay = mGLContext->_display;
            paramsOut_video.isRenderTarget = true;
        }

        if ((mAIComboType == CAPTURE && getJpegRotation(pRequest) != 0) || out->getImgFormat() != eImgFmt_RGBA8888)
        {
            if (mRGBAOutBuffer == nullptr)
            {
                if (mAIComboType == CAPTURE && getJpegRotation(pRequest) != 0)
                {
                    mRGBAOutBuffer = BufferUtils::acquireWorkingBuffer(inSize,
                                                                       eImgFmt_RGBA8888);
                }
                else
                {
                    mRGBAOutBuffer = BufferUtils::acquireWorkingBuffer(outSize,
                                                                       eImgFmt_RGBA8888);
                }
            }
            paramsOut.graphicBuffer = BufferUtils::getAHWBuffer(mRGBAOutBuffer);
        }
        else
        {
            paramsOut.graphicBuffer = BufferUtils::getAHWBuffer(out);
        }


        if(mAIComboType == VIDEO && out_video != NULL)
        {
            paramsOut_video.graphicBuffer = BufferUtils::getAHWBuffer(out_video);
        }


        GLUtils::getEGLImageTexture(&paramsOut);

        //MY_LOGD("create output texture ok");

        EGLSyncKHR sync = mGLContext->createSyncKHR_t();
        //copy2DTextureToOESTexture(inSize, paramsOut.textureID);

        EGLSyncKHR syncVideo;
        if (mAIComboType == VIDEO && out_video != NULL)
        {
            GLUtils::getEGLImageTexture(&paramsOut_video);
            syncVideo = mGLContext->createSyncKHR_t();
            //copy2DTextureToOESYuvTexture(inSize, paramsOut_video.textureID);
        }

        if (mAIComboType == CAPTURE) {
            glFinish();
            out->acquireFenceFd = -1;
        } else {
            glFlush();
            int acquireFenceFd = mGLContext->createFenceFd_t(sync);
            out->acquireFenceFd = acquireFenceFd;
        }

        // do rotate via MDP
        if ((mAIComboType == CAPTURE && getJpegRotation(pRequest) != 0))
        {
            ATRACE_BEGIN("AICombo::Capture_mdpResizeAndConvert");
            BufferUtils::mdpResizeAndConvert(in, out, getJpegRotation(pRequest));
            //BufferUtils::mdpResizeAndConvert(mRGBAOutBuffer, out, getJpegRotation(pRequest));
            ATRACE_END();
        } else {
            // convert buffer out to YV12 format
            BufferUtils::mdpResizeAndConvert(in, out);
            /*if (out->getImgFormat() != eImgFmt_RGBA8888)
            {
                //MY_LOGE("Should not copy here, tearing issue!!!!");
                ATRACE_BEGIN("AICombo::mdpResizeAndConvert");
                BufferUtils::mdpResizeAndConvert(mRGBAOutBuffer, out);
                ATRACE_END();
            }*/
        }

        // copy out to out_video in resulthandler
       if (mAIComboType == VIDEO && out_video != NULL)
        {
            ATRACE_BEGIN("AICombo::createFenceFd_t");
            int acquireFenceFdVideo = mGLContext->createFenceFd_t(syncVideo);
            BufferUtils::mdpResizeAndConvert(out, out_video);
            //MY_LOGD("out video acquireFenceFd = %d", acquireFenceFdVideo);
            out_video->acquireFenceFd =  acquireFenceFdVideo;
            ATRACE_END();
        }

        GLUtils::releaseEGLImageTexture(&paramsIn);
        GLUtils::releaseEGLImageTexture(&paramsOut);
        GLUtils::releaseEGLImageTexture(&paramsOut_video);



#ifdef DEBUG_DUMP_BUFFER
        std::string outbuffer = std::to_string(pRequest->mRequestNo) + "fb_outbuffer";
        BufferUtils::dumpBuffer(out, const_cast<char *>(outbuffer.c_str()));
#endif
        if (mRGBAOutBuffer != nullptr)
        {
            BufferUtils::deallocBuffer(mRGBAOutBuffer);
            mRGBAOutBuffer = nullptr;
        }
    }
    ATRACE_END();
    FUNCTION_OUT;
}

MERROR AICombo::doProcessCapture(RequestPtr pRequest)
{
    FUNCTION_IN;
    drawFrame(pRequest);

    FUNCTION_OUT;
    return OK;
}

void AICombo::abort(std::vector<RequestPtr> &pRequests)
{
    FUNCTION_IN;
    FUNCTION_OUT;
}

bool AICombo::loadPerfAPI()
{
    void *func;

    const char *perfLib = "libmtkperf_client.so";

    libHandle = dlopen(perfLib, RTLD_NOW);

    if (libHandle == NULL) {
        MY_LOGW("dlopen fail: %s\n", dlerror());
        perfLibEnabled = false;
        return false;
    }

    func = dlsym(libHandle, "perf_lock_acq");
    perfLockAcq = reinterpret_cast<perfLockAcqFunc>(func);

    if (perfLockAcq == NULL) {
        MY_LOGW("perfLockAcq error: %s\n", dlerror());
        dlclose(libHandle);
        perfLibEnabled = false;
        return false;
    }

    func = dlsym(libHandle, "perf_lock_rel");
    perfLockRel = reinterpret_cast<perfLockRelFunc>(func);

    if (perfLockRel == NULL) {
        MY_LOGW("perfLockRel error: %s\n", dlerror());
        dlclose(libHandle);
        perfLibEnabled = false;
        return false;
    }

    perfLibEnabled = true;
    return true;
}


MVOID AICombo::initPerfLib()
{
    if (!perfLibEnabled || (perfLockAcq == NULL) || (perfLockRel == NULL)) {
        if (!loadPerfAPI()) {
            MY_LOGW("dlopen failed");
            return;
        }
        MY_LOGI("dlopen mtkperf_client_vendor success");
    }
    return;
}

MVOID AICombo::closePerfLib()
{
    if (libHandle != NULL) {
        perfLockAcq = NULL;
        perfLockRel = NULL;
        dlclose(libHandle);
        libHandle = NULL;
        perfLibEnabled = MFALSE;
        //MY_LOGI("dlclose mtkperf_client_vendor");
    }
    return;
}

int AICombo::enablePerf()
{
    if(!perfLibEnabled) {
        if(!loadPerfAPI()) {
            MY_LOGW("cannot reload mtkperf_client_vendor");
            return -1;
        }
    }

   // int perfLockSrc[] = {PERF_RES_CPUFREQ_MIN_CLUSTER_0, 2001000, PERF_RES_DRAM_OPP_MIN, 0};
    int perfLockSrc[] = {PERF_RES_CPUFREQ_PERF_MODE, 1, PERF_RES_DRAM_OPP_MIN, 0};
    int newHandle = perfLockAcq(0, 0, perfLockSrc, 4);
    if(newHandle == -1) {
         MY_LOGW("failed to enable boost, invalid handle:%d", newHandle);
         return -1;
    }
    //MY_LOGD("enable boost, handle:%d", newHandle);
    return newHandle;
}

int AICombo::boostGpu(int duration)
{
    if(!perfLibEnabled) {
        if(!loadPerfAPI()) {
            MY_LOGW("cannot reload mtkperf_client_vendor");
            return -1;
        }
    }

    //usleep(1000);//1ms

    int perfLockSrc[] = {PERF_RES_GPU_FREQ_MIN, 0};
    int newHandle = perfLockAcq(0, duration, perfLockSrc, 2);
    if(newHandle == -1) {
         MY_LOGW("failed to enable gpu boost, invalid handle:%d", newHandle);
         return -1;
    }
    //MY_LOGD("enable gpu boost, duration = %d, handle:%d", duration, newHandle);
    return newHandle;
}


int AICombo::disablePerf(int handle)
{
    if(!perfLibEnabled) {
        if(!loadPerfAPI()) {
            MY_LOGW("cannot reload mtkperf_client_vendor");
            return -1;
        }
    }

    perfLockRel(handle);
    //MY_LOGD("disable boost,handle:%d", handle);
    return -1;
}
