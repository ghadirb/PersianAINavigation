# Sherpa ONNX Persian TTS Model

## مدل فارسی آفلاین با کیفیت بالا

### اطلاعات مدل:
- **نام**: vits-mimic3-fa-haaniye
- **نوع**: ONNX Runtime
- **زبان**: فارسی
- **کیفیت**: بالا (صدای طبیعی)
- **حجم**: ~60 MB
- **آفلاین**: ✅ بدون نیاز به اینترنت

### فایل‌های موجود:

#### مدل اصلی:
```
vits-mimic3-fa-haaniye_low/
├── fa-haaniye_low.onnx (60 MB) - مدل اصلی
├── fa-haaniye_low.onnx.json - تنظیمات مدل
├── tokens.txt - توکن‌های فارسی
└── espeak-ng-data/ - داده‌های تلفظ
    ├── fa_dict - دیکشنری فارسی
    └── phondata - داده‌های آوایی
```

#### کتابخانه‌های Native (jniLibs):
```
jniLibs/arm64-v8a/
├── libsherpa-onnx-core.so
├── libsherpa-onnx-jni.so
├── libonnxruntime.so
├── libespeak-ng.so
└── ... (سایر کتابخانه‌ها)
```

### نحوه استفاده:

```kotlin
// در VoiceAlertManager
val voiceAlert = VoiceAlertManager(context)
voiceAlert.initialize()

// تغییر به حالت ONNX
voiceAlert.switchMode(TTSMode.OFFLINE_ONNX)

// استفاده
voiceAlert.alertSpeedCamera(distance, speedLimit, cameraType)
```

### مزایا:
✅ کیفیت صدای بسیار بالا
✅ صدای طبیعی و روان
✅ بدون نیاز به اینترنت
✅ سرعت مناسب
✅ پشتیبانی کامل از فارسی

### منبع:
- Sherpa ONNX: https://github.com/k2-fsa/sherpa-onnx
- مدل: vits-mimic3-fa-haaniye
- نسخه: 1.9.11
