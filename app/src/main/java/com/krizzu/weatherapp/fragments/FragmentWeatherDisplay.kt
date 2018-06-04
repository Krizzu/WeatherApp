package com.krizzu.weatherapp.fragments

import android.animation.AnimatorInflater
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.airbnb.lottie.LottieAnimationView
import com.krizzu.weatherapp.R

class FragmentWeatherDisplay : Fragment() {

    private lateinit var weatherStatusContainer: LinearLayout
    private lateinit var weatherAnimation: LottieAnimationView
    private var isDayMode = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? = inflater.inflate(R.layout.fragment_weather_display, container!!, false)

        weatherStatusContainer = view!!.findViewById(R.id.LinearLayout_main_weatherDisplayContainer)

        weatherStatusContainer.setOnClickListener {
            animateWeatherStatus(isDayMode)
        }

        weatherAnimation = view.findViewById(R.id.Lottie_main_weatherAnim)

        return view
    }

    private fun animateWeatherStatus(nightMode: Boolean = false, duration: Long = 1800) {
        val animResource =
            if (nightMode) R.animator.day_to_night_background else R.animator.night_to_day_background
        val animatorSet =
            AnimatorInflater.loadAnimator(activity, animResource)
        animatorSet.setTarget(weatherStatusContainer)
        animatorSet.duration = duration

        if (!nightMode) {
            weatherAnimation.setSpeed(-1f)
            weatherAnimation.playAnimation()
        } else {
            weatherAnimation.setSpeed(1f)
            weatherAnimation.playAnimation()
        }

        animatorSet.start()
        isDayMode = !nightMode
    }
}