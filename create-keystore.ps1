# اسکریپت ساخت Keystore برای Android

param(
    [string]$Password = "",
    [string]$CompanyName = "YourCompany",
    [string]$KeystoreName = "persian-navigation.jks",
    [string]$KeyAlias = "persian-nav"
)

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "  ساخت Keystore برای Android  " -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# بررسی وجود keytool
try {
    $null = keytool -help 2>&1
} catch {
    Write-Host "خطا: keytool یافت نشد!" -ForegroundColor Red
    Write-Host "لطفا Java JDK را نصب کنید: https://adoptium.net/" -ForegroundColor Yellow
    exit 1
}

# دریافت پسورد
if ($Password -eq "") {
    $SecurePassword = Read-Host "رمز عبور Keystore را وارد کنید (حداقل 6 کاراکتر)" -AsSecureString
    $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($SecurePassword)
    $Password = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
}

if ($Password.Length -lt 6) {
    Write-Host "خطا: رمز عبور باید حداقل 6 کاراکتر باشد!" -ForegroundColor Red
    exit 1
}

# دریافت نام شرکت
if ($CompanyName -eq "YourCompany") {
    $CompanyName = Read-Host "نام شرکت یا نام خود را وارد کنید"
    if ($CompanyName -eq "") {
        $CompanyName = "Persian Navigation"
    }
}

Write-Host ""
Write-Host "در حال ساخت Keystore..." -ForegroundColor Yellow

# ساخت Keystore
$dname = "CN=Persian AI Navigation, OU=Navigation, O=$CompanyName, L=Tehran, ST=Tehran, C=IR"

try {
    keytool -genkey -v `
        -keystore $KeystoreName `
        -alias $KeyAlias `
        -keyalg RSA `
        -keysize 2048 `
        -validity 10000 `
        -storepass $Password `
        -keypass $Password `
        -dname $dname

    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "✅ Keystore با موفقیت ساخته شد!" -ForegroundColor Green
        Write-Host ""
        Write-Host "📁 فایل: $KeystoreName" -ForegroundColor Cyan
        Write-Host "🔑 Alias: $KeyAlias" -ForegroundColor Cyan
        Write-Host ""
        
        # ایجاد فایل keystore.properties
        $propertiesContent = @"
storeFile=$KeystoreName
storePassword=$Password
keyAlias=$KeyAlias
keyPassword=$Password
"@
        
        $propertiesFile = "keystore.properties"
        $propertiesContent | Out-File -FilePath $propertiesFile -Encoding UTF8
        
        Write-Host "✅ فایل $propertiesFile ایجاد شد" -ForegroundColor Green
        Write-Host ""
        
        # بررسی .gitignore
        if (Test-Path ".gitignore") {
            $gitignoreContent = Get-Content ".gitignore" -Raw
            if ($gitignoreContent -notlike "*keystore.properties*") {
                Write-Host "⚠️  توجه: keystore.properties را به .gitignore اضافه کنید!" -ForegroundColor Yellow
                
                $addToGitignore = Read-Host "آیا می‌خواهید الان اضافه شود? (y/n)"
                if ($addToGitignore -eq "y") {
                    @"

# Keystore files
keystore.properties
*.jks
*.keystore
"@ | Out-File -FilePath ".gitignore" -Append -Encoding UTF8
                    Write-Host "✅ به .gitignore اضافه شد" -ForegroundColor Green
                }
            }
        }
        
        Write-Host ""
        Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
        Write-Host "  اطلاعات مهم (یادداشت کنید!)  " -ForegroundColor Cyan
        Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
        Write-Host "Keystore File: $KeystoreName" -ForegroundColor White
        Write-Host "Key Alias: $KeyAlias" -ForegroundColor White
        Write-Host "Password: [محرمانه - همان رمزی که وارد کردید]" -ForegroundColor White
        Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "⚠️  این اطلاعات را در مکان امن ذخیره کنید!" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "مراحل بعدی:" -ForegroundColor Green
        Write-Host "1. فایل $KeystoreName را Backup کنید" -ForegroundColor White
        Write-Host "2. برای CodeMagic: این keystore را در Team Settings > Code Signing آپلود کنید" -ForegroundColor White
        Write-Host "3. Reference name را 'keystore_reference' بگذارید" -ForegroundColor White
        Write-Host "4. Build Release: gradlew assembleRelease" -ForegroundColor White
        Write-Host ""
        
    } else {
        throw "خطا در ساخت Keystore"
    }
    
} catch {
    Write-Host ""
    Write-Host "❌ خطا در ساخت Keystore: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "راهنمایی:" -ForegroundColor Yellow
    Write-Host "- مطمئن شوید Java JDK نصب است" -ForegroundColor White
    Write-Host "- رمز حداقل 6 کاراکتر باشد" -ForegroundColor White
    Write-Host "- مسیر نوشتنی باشد" -ForegroundColor White
    exit 1
}

Write-Host ""
Write-Host "✨ تمام!" -ForegroundColor Green
