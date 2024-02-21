package com.sunnyweather.android.ui.place

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunnyweather.android.LogUtil.LogUtil
import com.sunnyweather.android.MainActivity
import com.sunnyweather.android.databinding.FragmentPlaceBinding
import com.sunnyweather.android.ui.weather.WeatherActivity

class PlaceFragment : Fragment() {
    val viewModel by lazy { ViewModelProvider(this).get(PlaceViewModel::class.java) }
    private lateinit var adapter: PlaceAdapter
    private var _placeBinding: FragmentPlaceBinding? = null
    private val placeBinding get() = _placeBinding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _placeBinding = FragmentPlaceBinding.inflate(inflater, container, false)

        return placeBinding.root
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)

                val layoutManager = LinearLayoutManager(requireActivity())
                placeBinding.recyclerView.layoutManager = layoutManager
                adapter = PlaceAdapter(this@PlaceFragment, viewModel.placeList)
                placeBinding.recyclerView.adapter = adapter
                placeBinding.searchPlaceEdit.addTextChangedListener { editable ->
                    val content = editable.toString()
                    if (content.isNotEmpty()) {
                        LogUtil.d("PlaceFragment", "in MainActivity and place not saved")
                        viewModel.searchPlaces(content)
                    } else {
                        placeBinding.recyclerView.visibility = View.GONE
                        placeBinding.bgImageView.visibility = View.VISIBLE
                        viewModel.placeList.clear()
                        adapter.notifyDataSetChanged()
                    }
                }
                viewModel.placeLiveData.observe(this@PlaceFragment, Observer { result ->
                    val places = result.getOrNull()
                    if (places != null) {
                        placeBinding.recyclerView.visibility = View.VISIBLE
                        placeBinding.bgImageView.visibility = View.GONE
                        viewModel.placeList.clear()
                        viewModel.placeList.addAll(places)
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(activity, "未能查询到任何地点", Toast.LENGTH_SHORT).show()
                        result.exceptionOrNull()?.printStackTrace()
                    }
                })

                if (activity is MainActivity && viewModel.isPlaceSaved()) {
                    LogUtil.d("PlaceFragment", "in MainActivity and place saved")
                    val place = viewModel.getSavedPlace()
                    val intent = Intent(context, WeatherActivity::class.java).apply {
                        putExtra("location_lng", place.location.lng)
                        putExtra("location_lat", place.location.lat)
                        putExtra("place_name", place.name)
                    }
                    startActivity(intent)
                    activity?.finish()
                    return
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _placeBinding = null
    }
}