
/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2019. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

#ifndef INCLUDE_CAMPOSTALGO_FEATURECONFIG_H_
#define INCLUDE_CAMPOSTALGO_FEATURECONFIG_H_
#include <binder/Parcel.h>
#include <binder/Parcelable.h>

#include <gui/Surface.h>
#include <gui/view/Surface.h>

#include "campostalgo/FeaturePipeConfig.h"
#include "campostalgo/StreamInfo.h"
#include "campostalgo/Stream.h"
#include "FeatureParam.h"

using ::com::mediatek::campostalgo::FeatureParam;

using android::sp;
using android::OK;
using android::status_t;
using android::Parcel;
using android::Parcelable;
using android::Vector;


namespace com {
namespace mediatek {
namespace campostalgo {

class FeatureConfig: public Parcelable {
public:
    FeatureConfig();
    virtual void setFeaturePipeConfig(sp<FeaturePipeConfig> config);
    virtual void setSurfaces(Vector<sp<android::Surface>>& vSurfaces);
    virtual void setStreamInfo(Vector<sp<StreamInfo>>& vSteamInfo);
    virtual const sp<FeaturePipeConfig>& getFeaturePipeConfig() const;
    virtual void addSurface(sp<android::Surface>& stream);
    virtual void addStreamInfo(sp<StreamInfo>& steamInfo);

    virtual const Vector<sp<StreamInfo>> & getStreamInfos() const;
    virtual const Vector<sp<android::Surface>>& getSurfaceList() const;
    virtual const sp<FeatureParam> getInterfaceParams() const;
    virtual ~FeatureConfig();
    virtual status_t writeToParcel(Parcel* parcel) const override;
    virtual status_t readFromParcel(const Parcel* parcel) override;
protected:
    sp<FeaturePipeConfig> mFeaturePipeConfig;
    Vector<sp<android::Surface>> mAppSurfaces;
    Vector<sp<StreamInfo>> mStreamInfos;
    sp<FeatureParam> mInterfaceParams;
};
}
}
}
#endif /* INCLUDE_CAMPOSTALGO_FEATURECONFIG_H_ */