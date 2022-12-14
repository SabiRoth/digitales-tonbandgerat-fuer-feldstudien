package de.ur.mi.audidroid.views

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import de.ur.mi.audidroid.R

/**
 * Activity for the [OnboardingFragment]
 * @author: Sabine Roth
 */

class OnboardingActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AudidroidTheme_DayNight)
        setContentView(R.layout.onboarding)
    }
}
