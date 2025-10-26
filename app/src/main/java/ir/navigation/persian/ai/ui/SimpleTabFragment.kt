package ir.navigation.persian.ai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class SimpleTabFragment : Fragment() {
    
    companion object {
        private const val ARG_TEXT = "text"
        
        fun newInstance(text: String): SimpleTabFragment {
            val fragment = SimpleTabFragment()
            val args = Bundle()
            args.putString(ARG_TEXT, text)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val textView = TextView(requireContext())
        textView.text = arguments?.getString(ARG_TEXT) ?: ""
        textView.textSize = 24f
        textView.setPadding(32, 32, 32, 32)
        return textView
    }
}
