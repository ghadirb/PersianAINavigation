# خلاصه پروژه مسیریاب هوشمند فارسی

## ✅ وضعیت پروژه: تکمیل شده و آپلود شده

### 📍 لینک‌های مهم
- **GitHub Repository**: https://github.com/ghadirb/PersianAINavigation.git
- **Google Drive (یادگیری جمعی)**: https://drive.google.com/drive/folders/1bp1Ay9kmK_bjWq_PznRfkPvhhjdhSye1

## 🎯 ویژگی‌های پیاده‌سازی شده

### 1. سیستم مسیریابی 🗺️
- ✅ **GraphHopper**: موتور مسیریابی قدرتمند
- ✅ **MapLibre**: نمایش نقشه با قابلیت آفلاین
- ✅ **ردیابی موقعیت**: GPS tracking در پس‌زمینه
- ✅ **محاسبه مسیر**: مسیریابی real-time

### 2. هشدارهای صوتی فارسی 🔊
- ✅ **PersianTTSEngine**: تبدیل متن به گفتار فارسی با ONNX Runtime
- ✅ **هشدار دوربین**: اعلام دوربین‌های کنترل سرعت
- ✅ **هشدار سرعت‌گیر**: اطلاع از سرعت‌گیرهای پیش رو
- ✅ **هشدار محدودیت سرعت**: اخطار تخطی از سرعت مجاز
- ✅ **دستورات پیچ**: راهنمای صوتی برای پیچ‌ها

### 3. سیستم یادگیری مسیر 🧠
- ✅ **RouteLearningEngine**: ML با TensorFlow Lite
- ✅ **پیش‌بینی مسیر**: پیشنهاد بهترین مسیر بر اساس:
  - تاریخچه مسیرهای قبلی
  - زمان روز و روز هفته
  - شرایط ترافیک
  - انتخاب‌های کاربران دیگر
- ✅ **یادگیری تطبیقی**: بهبود با هر مسیر جدید

### 4. اشتراک‌گذاری داده‌ها ☁️
- ✅ **GoogleDriveManager**: همگام‌سازی با Google Drive
- ✅ **یادگیری جمعی**: اشتراک داده‌های مسیر بین کاربران
- ✅ **به‌روزرسانی دوربین‌ها**: دریافت و ارسال مکان دوربین‌ها
- ✅ **ادغام هوشمند**: merge کردن داده‌های محلی و سرور

### 5. مدیریت دوربین و سرعت‌گیر 📹
- ✅ انواع دوربین:
  - دوربین ثابت
  - دوربین سیار
  - سرعت‌گیر
  - چراغ راهنمایی با دوربین
  - دوربین میانگین سرعت
- ✅ افزودن دستی دوربین‌ها
- ✅ ذخیره و همگام‌سازی خودکار

## 📂 ساختار پروژه

```
PersianAINavigation/
├── app/
│   ├── src/main/
│   │   ├── java/ir/navigation/persian/ai/
│   │   │   ├── model/           # مدل‌های داده
│   │   │   │   ├── SpeedCamera.kt
│   │   │   │   └── RouteData.kt
│   │   │   ├── tts/             # موتور صوتی
│   │   │   │   └── PersianTTSEngine.kt
│   │   │   ├── ml/              # یادگیری ماشین
│   │   │   │   └── RouteLearningEngine.kt
│   │   │   ├── drive/           # Google Drive
│   │   │   │   └── GoogleDriveManager.kt
│   │   │   ├── service/         # سرویس‌ها
│   │   │   │   └── NavigationService.kt
│   │   │   └── ui/              # رابط کاربری
│   │   │       ├── MainActivity.kt
│   │   │       └── NavigationActivity.kt
│   │   ├── res/                 # منابع
│   │   └── assets/              # مدل‌های AI
│   ├── build.gradle.kts
│   └── google-services.json
├── README.md
├── SETUP.md
├── CONTRIBUTING.md
├── codemagic.yaml
└── settings.gradle.kts
```

## 🚀 مراحل استفاده

### برای شما (توسعه‌دهنده):
1. ✅ پروژه در GitHub آپلود شده است
2. ⚠️ **مدل‌های AI را اضافه کنید**:
   - `persian_tts_model.onnx` به `app/src/main/assets/`
   - `route_predictor_model.tflite` به `app/src/main/assets/`
3. 🔧 **تنظیم Google Services**:
   - فایل `google-services.json` واقعی را جایگزین کنید
4. 📱 **بیلد در CodeMagic**:
   - به codemagic.app بروید
   - Repository را متصل کنید
   - Build را شروع کنید

### برای کاربران نهایی:
1. نصب APK از CodeMagic یا GitHub Releases
2. اعطای مجوزهای لازم
3. ورود به Google Drive (اختیاری)
4. شروع مسیریابی

## 📋 وابستگی‌های کلیدی

```kotlin
// Navigation & Maps
implementation("org.maplibre.gl:android-sdk:11.0.0")
implementation("org.maplibre.navigation:navigation-core:5.0.0-pre10")
implementation("com.graphhopper:graphhopper-core:8.0")

// AI & ML
implementation("com.microsoft.onnxruntime:onnxruntime-android:1.17.0")
implementation("org.tensorflow:tensorflow-lite:2.14.0")

// Google Services
implementation("com.google.apis:google-api-services-drive:v3-rev20231027-2.0.0")
implementation("com.google.android.gms:play-services-location:21.1.0")
```

## ⚡ قابلیت‌های پیشرفته

### الگوریتم‌های ML:
- **Feature Extraction**: 12 ویژگی از مسیرها
- **Haversine Distance**: محاسبه دقیق فاصله
- **Route Similarity**: تشخیص مسیرهای مشابه
- **Traffic Analysis**: تحلیل سطح ترافیک

### بهینه‌سازی:
- **ONNX Quantization**: مدل TTS بهینه‌شده
- **TFLite Optimization**: استفاده از NNAPI
- **Coroutines**: عملیات async بدون block
- **Memory Management**: مدیریت حافظه برای مدل‌های بزرگ

## 🔮 توسعه‌های آینده

### Priority High:
- [ ] آموزش مدل TTS با داده‌های بیشتر
- [ ] پشتیبانی از نقشه‌های آفلاین OSM
- [ ] بهینه‌سازی مصرف باتری

### Priority Medium:
- [ ] حالت شب
- [ ] نمایش 3D مسیر
- [ ] گزارش تصادفات و ترافیک

## 📊 آماری از کد

- **خطوط کد**: ~2,800 خط
- **فایل‌های Kotlin**: 8 فایل اصلی
- **فایل‌های Layout**: 3 layout
- **سرویس‌های پس‌زمینه**: 1 foreground service
- **مدل‌های ML**: 2 مدل (TTS + Route Prediction)

## 💡 نکات مهم برای Build

### مدل‌های AI:
از پوشه `omid-test-9-main` برای تولید مدل TTS استفاده کنید:
```bash
cd C:\Users\Admin\Downloads\Compressed\omid-test-9-main\model
bash download_and_export.sh
```

### Google Services:
یک پروژه Firebase/Google Cloud ایجاد کنید و:
1. Google Drive API را فعال کنید
2. OAuth 2.0 Client ID بسازید
3. `google-services.json` را دانلود کنید

### CodeMagic Build:
فایل `codemagic.yaml` آماده است، فقط:
- Environment variables را تنظیم کنید
- Keystore برای release build (اختیاری)

## 🎓 تکنولوژی‌های استفاده شده

- **Kotlin**: زبان اصلی
- **Android SDK 24-34**: حداقل Android 7.0
- **Material Design 3**: UI/UX مدرن
- **Coroutines**: برنامه‌نویسی async
- **MVVM Pattern**: معماری پروژه
- **ONNX Runtime**: اجرای مدل TTS
- **TensorFlow Lite**: یادگیری مسیر
- **Google Drive API**: ذخیره ابری
- **FusedLocationProvider**: ردیابی GPS

## 📞 پشتیبانی

- **GitHub Issues**: https://github.com/ghadirb/PersianAINavigation/issues
- **Email**: از طریق GitHub profile

## 📄 لایسنس
این پروژه تحت لایسنس MIT منتشر شده است.

---

**✨ پروژه با موفقیت تکمیل و به GitHub آپلود شد!**

**🔗 Repository**: https://github.com/ghadirb/PersianAINavigation

**📌 آخرین Commit**: c053c09 - "Add SETUP and CONTRIBUTING documentation"

**🎯 وضعیت**: آماده برای Build در CodeMagic
