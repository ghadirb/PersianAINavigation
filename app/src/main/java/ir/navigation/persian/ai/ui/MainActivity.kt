package ir.navigation.persian.ai.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import ir.navigation.persian.ai.databinding.ActivityMainTabbedBinding
import ir.navigation.persian.ai.ui.adapters.ViewPagerAdapter
import ir.navigation.persian.ai.ui.fragments.AIChatFragment
import ir.navigation.persian.ai.ui.fragments.MapFragment
import ir.navigation.persian.ai.ui.fragments.SavedPlacesFragment
import ir.navigation.persian.ai.ui.fragments.SettingsFragment

/**
 * MainActivity با TabLayout - رابط کاربری مدرن
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainTabbedBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainTabbedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupTabs()
    }
    
    private fun setupTabs() {
        // لیست Fragment ها
        val fragments = listOf<Fragment>(
            MapFragment(),
            SavedPlacesFragment(),
            AIChatFragment(),
            SettingsFragment()
        )
        
        // نام Tab ها
        val tabTitles = listOf(
            "🗺️ نقشه",
            "📍 مکان‌ها",
            "🤖 AI",
            "⚙️ تنظیمات"
        )
        
        // تنظیم ViewPager2
        val adapter = ViewPagerAdapter(this, fragments)
        binding.viewPager.adapter = adapter
        
        // اتصال TabLayout به ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
}
