package com.krizzu.weatherapp.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.krizzu.weatherapp.R


class FragmentError : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_error_container, container!!, false)

        val textErrorView = view?.findViewById<TextView>(R.id.TextView_main_errorMessage)
        val retryButton = view?.findViewById<Button>(R.id.Button_main_retryButton)

        retryButton?.setOnClickListener {
            val act = activity

            if (act is WeatherRequestHandler) {
                act.onWeatherRequestRetry()
            }
        }

        textErrorView?.text = arguments?.getString("error")

        return view
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}

