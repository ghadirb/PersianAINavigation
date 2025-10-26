package ir.navigation.persian.ai.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        
        val tabLayout = TabLayout(this)
        val viewPager = ViewPager2(this)
        
        // ViewPager بالا، TabLayout پایین
        layout.addView(viewPager, android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            0, 1f
        ))
        layout.addView(tabLayout, android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ))
        
        setContentView(layout)
        
        viewPager.adapter = TabsAdapter(this)
        
        val tabs = listOf("🗺️ نقشه", "📍 ذخیره", "🔍 جستجو", "🤖 AI", "⚙️ سایر")
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()
    }
}
