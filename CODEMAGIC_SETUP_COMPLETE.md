# راهنمای کامل تنظیم CodeMagic (گام‌به‌گام)

## 📋 پیش‌نیاز: اطلاعاتی که نیاز دارید

قبل از شروع، این اطلاعات را آماده کنید:
- [ ] توکن GitHub (اگر ندارید، در مرحله 1 می‌سازیم)
- [ ] ایمیل خود برای دریافت نوتیفیکیشن

---

## 🔑 مرحله 1: ساخت توکن GitHub (اگر ندارید)

### 1.1 ورود به GitHub
1. به https://github.com بروید
2. وارد حساب خود شوید
3. روی عکس پروفایل (گوشه بالا راست) کلیک کنید
4. **Settings** را انتخاب کنید

### 1.2 ساخت Personal Access Token
1. در منوی چپ، پایین‌ترین گزینه: **Developer settings**
2. **Personal access tokens** > **Tokens (classic)**
3. **Generate new token** > **Generate new token (classic)**

### 1.3 تنظیمات Token
```
Note: CodeMagic Access for Persian AI Navigation
Expiration: 90 days (یا No expiration)
```

### 1.4 انتخاب Scopes (دقیقاً این‌ها را تیک بزنید):
```
✅ repo (تمام زیرمجموعه‌ها)
   ✅ repo:status
   ✅ repo_deployment
   ✅ public_repo
   ✅ repo:invite
   ✅ security_events

✅ workflow

✅ read:org (اختیاری)
```

### 1.5 ذخیره Token
1. **Generate token** را بزنید
2. ⚠️ **مهم**: Token را کپی کنید (فقط یک بار نمایش داده می‌شود)
3. در فایل `TOKENS.txt` ذخیره کنید:

```
GitHub Token: ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

---

## 🚀 مرحله 2: اتصال Repository به CodeMagic

### 2.1 ورود به CodeMagic
1. به https://codemagic.io/start بروید
2. **Log in with GitHub** را بزنید
3. اجازه دسترسی را بدهید

### 2.2 افزودن Repository
1. روی **Add application** کلیک کنید
2. **GitHub** را انتخاب کنید
3. اگر repository را نمی‌بینید:
   - **Install GitHub App** را بزنید
   - مجوزها را تایید کنید
4. **PersianAINavigation** را پیدا و انتخاب کنید
5. **Set up build** را بزنید

### 2.3 انتخاب Workflow
1. **Detect workflows from file** را انتخاب کنید
2. فایل `codemagic.yaml` به صورت خودکار شناسایی می‌شود
3. Workflow **android-debug-workflow** را انتخاب کنید
4. **Finish** را بزنید

---

## ⚙️ مرحله 3: تنظیمات Environment Variables

### 3.1 رفتن به تنظیمات
1. از داشبورد، روی پروژه کلیک کنید
2. تب **Environment variables** را انتخاب کنید

### 3.2 افزودن Variable برای ایمیل
```
Variable name: NOTIFICATION_EMAIL
Value: [ایمیل شما]
Secure: ❌ (خیر)
```

**مثال**:
```
NOTIFICATION_EMAIL: myemail@gmail.com
```

---

## 📧 مرحله 4: تنظیم Email Notifications

### 4.1 ویرایش codemagic.yaml (در GitHub)
1. به repository خود بروید
2. فایل `codemagic.yaml` را باز کنید
3. خط 23 را پیدا کنید:
```yaml
- user@example.com
```

4. آن را با ایمیل خود جایگزین کنید:
```yaml
- myemail@gmail.com
```

5. **Commit changes** را بزنید

---

## 🔥 مرحله 5: اولین Build

### 5.1 شروع Build
1. به داشبورد CodeMagic بروید
2. روی **Start new build** کلیک کنید
3. Workflow را انتخاب کنید: **android-debug-workflow**
4. **Start build** را بزنید

### 5.2 مشاهده لاگ
- پیشرفت build را ببینید
- اگر خطا بود، لاگ را بررسی کنید
- زمان تقریبی: 5-10 دقیقه

### 5.3 دانلود APK
1. Build تمام شد؟ ✅
2. به بخش **Artifacts** بروید
3. `app-debug.apk` را دانلود کنید
4. روی گوشی Android نصب کنید

---

## 🎯 تست موفقیت

### چک‌لیست:
- [ ] Repository به CodeMagic متصل شد
- [ ] اولین build موفق بود
- [ ] APK دانلود شد
- [ ] ایمیل نوتیفیکیشن دریافت شد
- [ ] APK روی گوشی نصب شد

---

## 🔐 (اختیاری) مرحله 6: آماده‌سازی Keystore برای Release

**این مرحله فعلاً لازم نیست!** فقط برای آینده:

### 6.1 ساخت Keystore
```powershell
cd C:\Users\Admin\CascadeProjects\PersianAINavigation
.\create-keystore.ps1
```

### 6.2 یادداشت اطلاعات
اسکریپت این اطلاعات را به شما می‌دهد:
```
Keystore File: persian-navigation.jks
Key Alias: persian-nav
Password: [رمز شما]
```

### 6.3 آپلود به CodeMagic
1. **Team settings** > **Code signing identities**
2. **Android** را انتخاب کنید
3. **Upload** را بزنید
4. فایل `.jks` را انتخاب کنید
5. فرم را پر کنید:
   - **Reference name**: `keystore_reference`
   - **Keystore password**: [رمزی که در مرحله 6.1 وارد کردید]
   - **Key alias**: `persian-nav`
   - **Key password**: [همان رمز]
6. **Save** را بزنید

---

## 📝 یادداشت‌های مهم

### ✅ انجام شده:
- ✅ Repository در GitHub موجود است
- ✅ فایل `codemagic.yaml` آماده است
- ✅ Workflow Debug تنظیم شده
- ✅ .gitignore به‌روز است

### ⏳ باید انجام شود:
- [ ] اتصال repository به CodeMagic
- [ ] تنظیم ایمیل نوتیفیکیشن
- [ ] اولین build

### 🔮 آینده (اختیاری):
- [ ] ساخت Keystore
- [ ] آپلود Keystore به CodeMagic
- [ ] Build نسخه Release

---

## 🆘 عیب‌یابی

### خطا: "Build failed"
**راه حل**:
1. لاگ را بررسی کنید
2. به خط قرمز دقت کنید
3. معمولاً به دلیل:
   - نبود فایل gradle-wrapper.jar
   - مشکل در dependencies

**راه حل سریع**:
```yaml
# در codemagic.yaml، قبل از gradlew اضافه کنید:
- name: Download Gradle Wrapper
  script: |
    gradle wrapper
```

### خطا: "Repository not found"
**راه حل**:
1. مطمئن شوید repository عمومی است
2. یا GitHub App را دوباره نصب کنید
3. مجوزهای لازم را بدهید

### خطا: "Workflow not found"
**راه حل**:
1. مطمئن شوید فایل `codemagic.yaml` در ریشه repository است
2. Syntax YAML را چک کنید: https://yamlchecker.com/

---

## 📞 راه‌های ارتباطی

### پشتیبانی CodeMagic:
- https://docs.codemagic.io/
- support@codemagic.io

### مستندات:
- Android builds: https://docs.codemagic.io/yaml-quick-start/building-a-native-android-app/
- Environment variables: https://docs.codemagic.io/yaml-basic-configuration/configuring-environment-variables/

---

## ✨ نکات طلایی

1. 🎯 **Debug برای تست، Release برای انتشار**
2. 🔐 **هرگز** رمزها را در کد قرار ندهید
3. 💾 **همیشه** Keystore را backup کنید
4. 📧 **حتماً** ایمیل نوتیفیکیشن تنظیم کنید
5. 🏷️ **از tag** برای Release استفاده کنید

---

## 🎉 پایان

پس از انجام این مراحل، شما:
- ✅ APK آماده برای تست دارید
- ✅ از CodeMagic برای build خودکار استفاده می‌کنید
- ✅ ایمیل نوتیفیکیشن دریافت می‌کنید
- ✅ برای Release آماده هستید

---

**⏱️ زمان تخمینی**: 15-20 دقیقه

**🚀 شروع کنید!**
