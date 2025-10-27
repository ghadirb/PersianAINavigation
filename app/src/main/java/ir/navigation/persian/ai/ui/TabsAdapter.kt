package ir.navigation.persian.ai.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabsAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    
    override fun getItemCount(): Int = 5
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SimpleTabFragment.newInstance("🗺️ نقشه\n\nنقشه بزودی فعال می‌شود")
            1 -> SimpleTabFragment.newInstance("📍 مکان‌های ذخیره شده\n\nبزودی")
            2 -> SimpleTabFragment.newInstance("🔍 جستجو\n\nبزودی")
            3 -> SimpleTabFragment.newInstance("🤖 چت AI\n\nبزودی")
            else -> SimpleTabFragment.newInstance("⚙️ تنظیمات\n\nبزودی")
        }
    }
}
