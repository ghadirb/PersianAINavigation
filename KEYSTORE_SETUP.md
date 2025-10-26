# راهنمای ساخت Keystore برای نسخه Release

## گزینه 1: Build کردن نسخه Debug (فوری - بدون نیاز به Keystore)

این گزینه برای تست سریع مناسب است:

### در CodeMagic:
1. به پروژه خود بروید
2. Workflow را انتخاب کنید: **Persian AI Navigation (Debug)**
3. Start new build را بزنید
4. APK در artifacts قابل دانلود است

### در سیستم محلی:
```bash
cd C:\Users\Admin\CascadeProjects\PersianAINavigation
gradlew assembleDebug
```

فایل خروجی: `app/build/outputs/apk/debug/app-debug.apk`

---

## گزینه 2: ساخت Keystore برای نسخه Release

### مرحله 1: تولید Keystore

#### روش A: با دستور (خودکار)
```bash
# در PowerShell یا CMD
cd C:\Users\Admin\CascadeProjects\PersianAINavigation

keytool -genkey -v -keystore persian-navigation.jks -alias persian-nav -keyalg RSA -keysize 2048 -validity 10000 -storepass YOUR_PASSWORD -keypass YOUR_PASSWORD -dname "CN=Persian AI Navigation, OU=Navigation, O=YourCompany, L=Tehran, ST=Tehran, C=IR"
```

**جایگزین کنید:**
- `YOUR_PASSWORD`: رمز دلخواه شما (حداقل 6 کاراکتر)
- `YourCompany`: نام شرکت یا نام شما

#### روش B: با دستور تعاملی
```bash
keytool -genkey -v -keystore persian-navigation.jks -alias persian-nav -keyalg RSA -keysize 2048 -validity 10000
```

سوالات را پاسخ دهید:
```
Enter keystore password: [رمز خود]
Re-enter new password: [رمز خود]
What is your first and last name? Persian AI Navigation
What is the name of your organizational unit? Navigation
What is the name of your organization? YourCompany
What is the name of your City or Locality? Tehran
What is the name of your State or Province? Tehran
What is the two-letter country code for this unit? IR
Is CN=Persian AI Navigation, OU=Navigation... correct? yes
```

### مرحله 2: ذخیره اطلاعات Keystore

فایل `persian-navigation.jks` ساخته شد. اطلاعات زیر را یادداشت کنید:

```
Keystore Path: persian-navigation.jks
Keystore Password: [رمز شما]
Key Alias: persian-nav
Key Password: [رمز شما]
```

### مرحله 3: پیکربندی در پروژه

#### A) برای Build محلی

فایل `keystore.properties` در ریشه پروژه بسازید:
```properties
storeFile=persian-navigation.jks
storePassword=YOUR_PASSWORD
keyAlias=persian-nav
keyPassword=YOUR_PASSWORD
```

⚠️ **مهم**: این فایل را به `.gitignore` اضافه کنید!

#### B) برای CodeMagic

1. به CodeMagic بروید > Team settings > Code signing identities
2. روی **Add identity** کلیک کنید
3. **Android** را انتخاب کنید
4. فایل `persian-navigation.jks` را آپلود کنید
5. اطلاعات را وارد کنید:
   - **Reference name**: `keystore_reference`
   - **Keystore password**: [رمز شما]
   - **Key alias**: `persian-nav`
   - **Key password**: [رمز شما]

### مرحله 4: به‌روزرسانی build.gradle.kts

فایل `app/build.gradle.kts` را باز کنید و این کد را قبل از `android {` اضافه کنید:

```kotlin
// Load keystore properties
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = java.util.Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(java.io.FileInputStream(keystorePropertiesFile))
}
```

سپس در بخش `android {}` این را اضافه کنید:

```kotlin
signingConfigs {
    create("release") {
        if (keystoreProperties.containsKey("storeFile")) {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        // ... باقی تنظیمات
    }
}
```

### مرحله 5: Build کردن

```bash
# نسخه Release
gradlew assembleRelease

# خروجی
app/build/outputs/apk/release/app-release.apk
```

---

## گزینه 3: استفاده از Google Play App Signing

این روش ساده‌تر است:

1. نسخه Debug را build کنید
2. به Google Play Console بروید
3. یک App جدید بسازید
4. در بخش **Setup > App signing**, Google به صورت خودکار keystore می‌سازد
5. فایل APK یا AAB خود را آپلود کنید

---

## نکات امنیتی 🔒

1. ⚠️ **هرگز** keystore را در Git قرار ندهید
2. ⚠️ **هرگز** پسوردها را در کد قرار ندهید
3. ✅ Keystore را در مکان امن backup کنید
4. ✅ از رمز قوی استفاده کنید (حداقل 8 کاراکتر)
5. ✅ `.gitignore` را چک کنید:
   ```
   keystore.properties
   *.jks
   *.keystore
   ```

---

## عیب‌یابی

### خطا: keytool command not found
```bash
# Java را نصب کنید:
# دانلود از: https://adoptium.net/

# یا مسیر را اضافه کنید:
set PATH=%PATH%;C:\Program Files\Java\jdk-17\bin
```

### خطا: signing key not found
- مطمئن شوید keystore.properties وجود دارد
- مسیر فایل keystore را چک کنید
- رمزها را دوباره وارد کنید

### خطا در CodeMagic: keystore_reference not found
- در Team settings > Code signing identities بروید
- مطمئن شوید keystore با نام `keystore_reference` وجود دارد
- Workflow را روی **Release** تنظیم کنید

---

## مقایسه Debug vs Release

| ویژگی | Debug | Release |
|-------|-------|---------|
| نیاز به Keystore | ❌ | ✅ |
| بهینه‌سازی | ❌ | ✅ |
| Debuggable | ✅ | ❌ |
| حجم APK | بزرگتر | کوچکتر |
| سرعت | کندتر | سریعتر |
| مناسب برای | تست | انتشار |

---

## سوالات متداول

**Q: آیا می‌توانم keystore قدیمی را استفاده کنم؟**  
A: بله، اگر keystore قبلی دارید از همان استفاده کنید.

**Q: رمز keystore خودم را فراموش کردم، چه کنم؟**  
A: متاسفانه باید keystore جدید بسازید. برنامه با keystore جدید نمی‌تواند برنامه قبلی را به‌روزرسانی کند.

**Q: چند سال keystore معتبر است؟**  
A: با دستور بالا 10000 روز (~27 سال) معتبر است.

**Q: آیا می‌توانم چند کلید در یک keystore داشته باشم؟**  
A: بله، اما برای هر برنامه یک alias مجزا استفاده کنید.

---

## اسکریپت خودکار

برای ساخت سریع keystore، از اسکریپت `create-keystore.ps1` استفاده کنید:

```powershell
.\create-keystore.ps1 -Password "YOUR_PASSWORD" -CompanyName "YourCompany"
```

یا به صورت تعاملی:
```powershell
.\create-keystore.ps1
```

---

**✅ پس از انجام این مراحل، workflow Release در CodeMagic بدون خطا اجرا می‌شود!**
