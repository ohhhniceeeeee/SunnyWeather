package com.sunnyweather.android.ui.weather

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.LogUtil.LogUtil
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.ActivityWeatherBinding
import com.sunnyweather.android.databinding.ForecastItemBinding
import com.sunnyweather.android.logic.Repository.refreshWeather
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.Locale

class WeatherActivity : AppCompatActivity() {
    private lateinit var weatherBinding: ActivityWeatherBinding
    val _weatherBinding get() = weatherBinding

    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        weatherBinding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(weatherBinding.root)


        //加载天气信息
        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            weatherBinding.swipeRefresh.isRefreshing = false
        })

        //刷新功能
        weatherBinding.swipeRefresh.setColorSchemeResources(com.google.android.material.R.color.design_default_color_primary)
        refreshWeather()
        weatherBinding.swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }
        LogUtil.d("WeatherActivity", "lng:${viewModel.locationLng},lat:${viewModel.locationLat}")
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)


        //滑动菜单处理
        weatherBinding.nowLayout.navBtn.setOnClickListener {
            weatherBinding.drawerLayout.openDrawer(GravityCompat.START)
        }
        weatherBinding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {
            }

            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(
                    drawerView.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        })
    }

    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        weatherBinding.swipeRefresh.isRefreshing = true
    }


    private fun showWeatherInfo(weather: Weather) {
        weatherBinding.nowLayout.placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily
        //填充now布局
        val currentTempText = "${realtime.temperature.toInt()} ℃"
        weatherBinding.nowLayout.currentTemp.text = currentTempText
        weatherBinding.nowLayout.currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        weatherBinding.nowLayout.currentAQI.text = currentPM25Text
        weatherBinding.nowLayout.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)
        //填充forecast布局
        weatherBinding.forecastLayout.forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val forecastItemBinding = ForecastItemBinding.inflate(
                LayoutInflater.from(this),
                weatherBinding.forecastLayout.forecastLayoutParent,
                false
            )
//            val view = LayoutInflater.from(this).inflate(
//                R.layout.forecast_item,
//                weatherBinding.forecastLayout.forecastLayout,
//                false
//            )

//            val dateInfo = view.findViewById<TextView>(R.id.dateInfo)
//            val skyIcon = view.findViewById<ImageView>(R.id.skyIcon)
//            val skyInfo = view.findViewById<TextView>(R.id.skyInfo)
//            val temperatureInfo = view.findViewById<TextView>(R.id.temperatureInfo)
//
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            dateInfo.text =
//                simpleDateFormat.format(simpleDateFormat.parse(skycon.date)!!)//skycon.date
//            val sky = getSky(skycon.value)
//            skyIcon.setImageResource(sky.icon)
//            skyInfo.text = sky.info
//            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
//            temperatureInfo.text = tempText
//            weatherBinding.forecastLayout.forecastLayout.addView(view)


            forecastItemBinding.dateInfo.text =
                simpleDateFormat.format(simpleDateFormat.parse(skycon.date)!!)
            val sky = getSky(skycon.value)
            forecastItemBinding.skyIcon.setImageResource(sky.icon)
            forecastItemBinding.skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            forecastItemBinding.temperatureInfo.text = tempText
            weatherBinding.forecastLayout.forecastLayout.addView(forecastItemBinding.root)

        }
        //填充life_index.xml
        val lifeIndex = daily.lifeIndex
        weatherBinding.lifeIndex.coldRiskText.text = lifeIndex.coldRisk[0].desc
        weatherBinding.lifeIndex.dressingText.text = lifeIndex.dressing[0].desc
        weatherBinding.lifeIndex.ultrovioletText.text = lifeIndex.ultraviolet[0].desc
        weatherBinding.lifeIndex.carWashingText.text = lifeIndex.carWashing[0].desc
        weatherBinding.weatherLayout.visibility = View.VISIBLE
    }
}