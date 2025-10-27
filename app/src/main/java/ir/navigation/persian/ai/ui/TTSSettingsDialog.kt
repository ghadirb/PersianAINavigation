package ir.navigation.persian.ai.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.lifecycle.lifecycleScope
import ir.navigation.persian.ai.tts.TTSMode
import ir.navigation.persian.ai.tts.VoiceAlertManager
import kotlinx.coroutines.launch

/**
 * دیالوگ تنظیمات TTS
 * انتخاب حالت: آفلاین Android، آفلاین ONNX، آنلاین API
 */
class TTSSettingsDialog(
    context: Context,
    private val voiceAlert: VoiceAlertManager,
    private val onModeChanged: (TTSMode) -> Unit
) : Dialog(context) {
    
    private lateinit var radioGroup: RadioGroup
    private lateinit var rbAndroid: RadioButton
    private lateinit var rbONNX: RadioButton
    private lateinit var rbOnline: RadioButton
    private lateinit var tvDescription: TextView
    private lateinit var btnApply: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(48, 48, 48, 48)
        
        // عنوان
        val title = TextView(context)
        title.text = "⚙️ تنظیمات صدای هشدار"
        title.textSize = 20f
        title.setTextColor(0xFF000000.toInt())
        layout.addView(title)
        
        // فاصله
        val spacer1 = View(context)
        spacer1.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 32
        )
        layout.addView(spacer1)
        
        // RadioGroup
        radioGroup = RadioGroup(context)
        
        // آفلاین Android
        rbAndroid = RadioButton(context)
        rbAndroid.text = "📱 آفلاین (Android TTS)"
        rbAndroid.id = 1
        rbAndroid.setOnClickListener { updateDescription(TTSMode.OFFLINE_ANDROID) }
        radioGroup.addView(rbAndroid)
        
        // آفلاین ONNX
        rbONNX = RadioButton(context)
        rbONNX.text = "🎙️ آفلاین (کیفیت بالا - ONNX)"
        rbONNX.id = 2
        rbONNX.setOnClickListener { updateDescription(TTSMode.OFFLINE_ONNX) }
        radioGroup.addView(rbONNX)
        
        // آنلاین
        rbOnline = RadioButton(context)
        rbOnline.text = "🌐 آنلاین (کیفیت بالاتر)"
        rbOnline.id = 3
        rbOnline.setOnClickListener { updateDescription(TTSMode.ONLINE_API) }
        radioGroup.addView(rbOnline)
        
        layout.addView(radioGroup)
        
        // فاصله
        val spacer2 = View(context)
        spacer2.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 24
        )
        layout.addView(spacer2)
        
        // توضیحات
        tvDescription = TextView(context)
        tvDescription.textSize = 14f
        tvDescription.setTextColor(0xFF666666.toInt())
        layout.addView(tvDescription)
        
        // فاصله
        val spacer3 = View(context)
        spacer3.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 32
        )
        layout.addView(spacer3)
        
        // ProgressBar
        progressBar = ProgressBar(context)
        progressBar.visibility = View.GONE
        layout.addView(progressBar)
        
        // دکمه‌ها
        val buttonLayout = LinearLayout(context)
        buttonLayout.orientation = LinearLayout.HORIZONTAL
        
        btnCancel = Button(context)
        btnCancel.text = "انصراف"
        btnCancel.setOnClickListener { dismiss() }
        buttonLayout.addView(btnCancel)
        
        btnApply = Button(context)
        btnApply.text = "اعمال"
        btnApply.setOnClickListener { applySettings() }
        buttonLayout.addView(btnApply)
        
        layout.addView(buttonLayout)
        
        setContentView(layout)
        
        // تنظیم حالت فعلی
        when (voiceAlert.getCurrentMode()) {
            TTSMode.OFFLINE_ANDROID -> rbAndroid.isChecked = true
            TTSMode.OFFLINE_ONNX -> rbONNX.isChecked = true
            TTSMode.ONLINE_API -> rbOnline.isChecked = true
        }
        updateDescription(voiceAlert.getCurrentMode())
    }
    
    /**
     * به‌روزرسانی توضیحات
     */
    private fun updateDescription(mode: TTSMode) {
        tvDescription.text = when (mode) {
            TTSMode.OFFLINE_ANDROID -> """
                ✅ بدون نیاز به اینترنت
                ✅ سریع و پایدار
                ⚠️ کیفیت صدا متوسط
                💾 حجم کم
            """.trimIndent()
            
            TTSMode.OFFLINE_ONNX -> """
                ✅ بدون نیاز به اینترنت
                ✅ کیفیت صدای بالا
                ✅ صدای طبیعی‌تر
                ⚠️ نیاز به دانلود مدل (60MB)
                💾 حجم متوسط
            """.trimIndent()
            
            TTSMode.ONLINE_API -> """
                ✅ بهترین کیفیت صدا
                ✅ صدای کاملاً طبیعی
                ⚠️ نیاز به اینترنت
                ⚠️ مصرف داده
                🔄 تبدیل خودکار به آفلاین در صورت قطع اینترنت
            """.trimIndent()
        }
    }
    
    /**
     * اعمال تنظیمات
     */
    private fun applySettings() {
        val selectedMode = when (radioGroup.checkedRadioButtonId) {
            1 -> TTSMode.OFFLINE_ANDROID
            2 -> TTSMode.OFFLINE_ONNX
            3 -> TTSMode.ONLINE_API
            else -> TTSMode.OFFLINE_ANDROID
        }
        
        // نمایش ProgressBar
        progressBar.visibility = View.VISIBLE
        btnApply.isEnabled = false
        btnCancel.isEnabled = false
        
        // تغییر حالت
        (context as? androidx.appcompat.app.AppCompatActivity)?.lifecycleScope?.launch {
            val success = voiceAlert.switchMode(selectedMode)
            
            progressBar.visibility = View.GONE
            btnApply.isEnabled = true
            btnCancel.isEnabled = true
            
            if (success) {
                Toast.makeText(context, "✅ حالت TTS تغییر کرد", Toast.LENGTH_SHORT).show()
                onModeChanged(selectedMode)
                dismiss()
            } else {
                Toast.makeText(context, "❌ خطا در تغییر حالت", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
