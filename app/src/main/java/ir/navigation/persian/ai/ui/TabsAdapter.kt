package ir.navigation.persian.ai.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabsAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    
    override fun getItemCount(): Int = 5
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MapFragment()
            1 -> SavedFragment()
            2 -> SearchFragment()
            3 -> SimpleTabFragment.newInstance("🤖 چت AI\n\nچت با هوش مصنوعی\n(بزودی)")
            else -> SettingsFragment()
        }
    }
}
