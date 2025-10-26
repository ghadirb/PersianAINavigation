package ir.navigation.persian.ai.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabsAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    
    override fun getItemCount(): Int = 5
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SimpleTabFragment.newInstance("🗺️ نقشه\n\nنقشه اینجا نمایش داده می‌شود")
            1 -> SimpleTabFragment.newInstance("📍 مکان‌های ذخیره شده\n\nلیست مکان‌ها")
            2 -> SimpleTabFragment.newInstance("🔍 جستجو\n\nجستجوی مکان")
            3 -> SimpleTabFragment.newInstance("🤖 چت AI\n\nچت با هوش مصنوعی")
            else -> SimpleTabFragment.newInstance("⚙️ تنظیمات\n\nتنظیمات برنامه")
        }
    }
}
