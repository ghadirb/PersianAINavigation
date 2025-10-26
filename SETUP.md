# راهنمای نصب و راه‌اندازی

## پیش‌نیازها

### 1. مدل‌های AI
برای عملکرد کامل برنامه، نیاز به دو مدل AI دارید:

#### الف) مدل TTS فارسی (persian_tts_model.onnx)
از پوشه `omid-test-9-main` استفاده کنید:
```bash
cd C:\Users\Admin\Downloads\Compressed\omid-test-9-main\model
bash download_and_export.sh
```
فایل خروجی را به `app/src/main/assets/persian_tts_model.onnx` کپی کنید.

#### ب) مدل پیش‌بینی مسیر (route_predictor_model.tflite)
این مدل باید با داده‌های واقعی مسیر آموزش داده شود. 
یک مدل پایه در `app/src/main/assets/` قرار دهید یا از TensorFlow Lite Model Maker استفاده کنید.

### 2. تنظیمات Google Drive API
1. به [Google Cloud Console](https://console.cloud.google.com/) بروید
2. پروژه جدید ایجاد کنید یا موجود را انتخاب کنید
3. Google Drive API را فعال کنید
4. OAuth 2.0 Client ID بسازید (Android)
5. فایل `google-services.json` را دانلود و در `app/` قرار دهید

### 3. کامپایل پروژه

#### روش 1: استفاده از Android Studio
1. پروژه را در Android Studio باز کنید
2. Gradle Sync انجام دهید
3. Build > Build Bundle(s) / APK(s) > Build APK(s)

#### روش 2: استفاده از Command Line
```bash
cd C:\Users\Admin\CascadeProjects\PersianAINavigation
gradlew assembleDebug
```

APK خروجی در `app/build/outputs/apk/debug/` خواهد بود.

## راه‌اندازی در CodeMagic

### 1. متصل کردن GitHub Repository
1. به [codemagic.app](https://codemagic.app/) بروید
2. با GitHub وارد شوید
3. Repository `ghadirb/PersianAINavigation` را اضافه کنید

### 2. تنظیمات Build
فایل `codemagic.yaml` از قبل آماده است، اما:
1. در تنظیمات CodeMagic، Environment Variables را اضافه کنید:
   - `GCLOUD_SERVICE_ACCOUNT_CREDENTIALS`
2. در صورت نیاز به Keystore برای Release Build:
   - Keystore file را آپلود کنید
   - Password ها را تنظیم کنید

### 3. اجرای Build
- هر Push به GitHub به طور خودکار Build را آغاز می‌کند
- یا از داشبورد CodeMagic به صورت دستی Start Build کنید

## استفاده از برنامه

### مجوزها
اولین بار که برنامه را باز می‌کنید، مجوزهای زیر را درخواست می‌کند:
- دسترسی به موقعیت مکانی (الزامی)
- دسترسی به موقعیت در پس‌زمینه (برای مسیریابی)
- ضبط صدا (برای TTS اختیاری)
- دسترسی به اینترنت و اعلان‌ها

### شروع مسیریابی
1. ورود به Google Drive (اختیاری اما توصیه می‌شود)
2. همگام‌سازی داده‌های دوربین و یادگیری
3. وارد کردن مختصات مبدا و مقصد
4. کلیک روی "شروع مسیریابی"

### افزودن دوربین/سرعت‌گیر
- روی دکمه "افزودن دوربین/سرعت‌گیر" کلیک کنید
- مختصات و اطلاعات را وارد کنید
- داده‌ها به صورت خودکار با Google Drive همگام می‌شوند

## مشکلات رایج

### Build Errors
- مطمئن شوید Gradle Wrapper به درستی دانلود شده است
- Java 17 را نصب کنید
- `./gradlew clean` را اجرا کنید

### مشکل مدل‌های AI
- مدل‌ها باید در `app/src/main/assets/` باشند
- فرمت فایل‌ها صحیح باشد (.onnx و .tflite)

### مشکل Google Drive
- مطمئن شوید Google Drive API فعال است
- Client ID در `google-services.json` صحیح باشد
- دسترسی‌های OAuth تنظیم شده باشد

## توسعه بیشتر

### اضافه کردن زبان‌های جدید
در `PersianTTSEngine.kt` نقشه کاراکترها را گسترش دهید.

### بهبود مدل یادگیری
داده‌های بیشتر را از Google Drive بخوانید و مدل را دوباره آموزش دهید.

### اضافه کردن منابع نقشه جدید
در `NavigationActivity.kt` آدرس tile server را تغییر دهید.

## پشتیبانی
برای مشکلات و سوالات در GitHub Issues پست بگذارید:
https://github.com/ghadirb/PersianAINavigation/issues
