package com.transportation.kotline.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.transportation.kotline.databinding.FragmentOnboardingThreeBinding
import com.transportation.kotline.other.Global.SHARED_PREFS
import com.transportation.kotline.other.Global.STARTED_PREFS
import com.transportation.kotline.other.OptionActivity

class OnBoardingThreeFragment : Fragment() {

    private var _binding: FragmentOnboardingThreeBinding? = null
    private val binding get() = _binding as FragmentOnboardingThreeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentOnboardingThreeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnGetStarted.setOnClickListener {
            // put shared preferences if button started is clicked
            val sharedPreference =
                activity?.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
            val editor = sharedPreference?.edit()
            editor?.putBoolean(STARTED_PREFS, true)
            editor?.apply()
            Intent(activity, OptionActivity::class.java).apply {
                startActivity(this)
                activity?.finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}