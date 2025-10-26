# ✅ چک‌لیست تنظیم CodeMagic

این چک‌لیست را چاپ کنید یا کنار خود نگه دارید.

---

## 📝 قبل از شروع

- [ ] حساب GitHub دارم
- [ ] Repository در GitHub است: https://github.com/ghadirb/PersianAINavigation
- [ ] ایمیل خود را می‌دانم
- [ ] 15-20 دقیقه وقت دارم

---

## 🔑 بخش 1: GitHub Token (5 دقیقه)

### اگر توکن ندارید:

- [ ] به GitHub.com رفتم و وارد شدم
- [ ] Settings > Developer settings > Personal access tokens
- [ ] Generate new token (classic) را زدم
- [ ] Note: `CodeMagic Access` نوشتم
- [ ] Expiration: `90 days` یا `No expiration`
- [ ] Scopes انتخاب کردم:
  - [ ] ✅ repo (همه)
  - [ ] ✅ workflow
- [ ] Generate token را زدم
- [ ] Token را کپی و ذخیره کردم

**Token من**:
```
ghp_________________________________
```

---

## 🚀 بخش 2: اتصال به CodeMagic (5 دقیقه)

- [ ] به https://codemagic.io/start رفتم
- [ ] Log in with GitHub را زدم
- [ ] اجازه دسترسی دادم
- [ ] Add application را زدم
- [ ] PersianAINavigation را پیدا کردم
- [ ] Set up build را زدم
- [ ] Detect workflows from file را انتخاب کردم
- [ ] android-debug-workflow را انتخاب کردم
- [ ] Finish را زدم

---

## 📧 بخش 3: تنظیم ایمیل (2 دقیقه)

### در GitHub:
- [ ] به repository رفتم
- [ ] فایل `codemagic.yaml` را باز کردم
- [ ] Edit را زدم
- [ ] خط 23: `user@example.com` را پیدا کردم
- [ ] با ایمیل خودم جایگزین کردم: `__________________`
- [ ] Commit changes را زدم

---

## 🔥 بخش 4: اولین Build (10 دقیقه)

- [ ] به داشبورد CodeMagic برگشتم
- [ ] Start new build را زدم
- [ ] android-debug-workflow را انتخاب کردم
- [ ] Start build را زدم
- [ ] منتظر ماندم تا تمام شود ☕
- [ ] Build موفق بود ✅
- [ ] به Artifacts رفتم
- [ ] `app-debug.apk` را دانلود کردم
- [ ] ایمیل نوتیفیکیشن دریافت کردم

---

## 📱 بخش 5: تست روی گوشی (5 دقیقه)

- [ ] فایل APK را به گوشی منتقل کردم
- [ ] گزینه "Install from Unknown Sources" را فعال کردم
- [ ] APK را نصب کردم
- [ ] برنامه باز شد ✅
- [ ] رمز `12345` را وارد کردم
- [ ] قفل باز شد ✅
- [ ] برنامه کار می‌کند 🎉

---

## ✅ همه چیز تمام شد!

- [ ] ✅ CodeMagic تنظیم شد
- [ ] ✅ اولین build موفق بود
- [ ] ✅ APK دانلود و تست شد
- [ ] ✅ نوتیفیکیشن کار می‌کند

---

## 🔐 (اختیاری) بخش 6: Keystore برای Release

### فقط وقتی می‌خواهید نسخه نهایی منتشر کنید:

- [ ] PowerShell را باز کردم
- [ ] `cd C:\Users\Admin\CascadeProjects\PersianAINavigation`
- [ ] `.\create-keystore.ps1` را اجرا کردم
- [ ] رمز وارد کردم: `__________________`
- [ ] نام شرکت: `__________________`
- [ ] فایل `persian-navigation.jks` ساخته شد
- [ ] اطلاعات را یادداشت کردم:
  ```
  Keystore: persian-navigation.jks
  Alias: persian-nav
  Password: __________________
  ```

### آپلود به CodeMagic:
- [ ] Team settings > Code signing identities
- [ ] Add identity > Android
- [ ] فایل `.jks` را آپلود کردم
- [ ] Reference name: `keystore_reference`
- [ ] اطلاعات را وارد کردم
- [ ] Save کردم

### Build Release:
- [ ] `git tag v1.0.0` زدم
- [ ] `git push origin v1.0.0` زدم
- [ ] Workflow Release خودکار شروع شد
- [ ] APK release دانلود شد

---

## 📊 نتیجه نهایی

### دارم ✅:
- [x] Repository در GitHub
- [x] CodeMagic متصل
- [x] Debug APK
- [x] Email notifications

### ندارم (فعلاً نیازی نیست):
- [ ] Keystore
- [ ] Release APK
- [ ] Google Play listing

---

## 🎯 مرحله بعدی

اکنون می‌توانید:
1. ✅ برنامه را تست کنید
2. ✅ تغییرات بدهید و push کنید
3. ✅ Build خودکار دریافت کنید

برای انتشار رسمی:
1. ⏳ Keystore بسازید
2. ⏳ Release build بگیرید
3. ⏳ در Google Play منتشر کنید

---

**📅 تاریخ انجام**: ___/___/___

**✍️ یادداشت‌ها**:
```
[فضای خالی برای یادداشت]





```

---

**🎉 تبریک! همه چیز تنظیم شد.**

**📞 نیاز به کمک؟** فایل `CODEMAGIC_SETUP_COMPLETE.md` را ببینید.
