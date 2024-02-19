package com.sunnyweather.android.ui.place

import android.content.Context
import android.os.Bundle
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
import com.sunnyweather.android.databinding.FragmentPlaceBinding

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
                val layoutManager = LinearLayoutManager(activity)
                placeBinding.recyclerView.layoutManager = layoutManager
                adapter = PlaceAdapter(this@PlaceFragment, viewModel.placeList)
                placeBinding.searchPlaceEdit.addTextChangedListener { editable ->
                    val content = editable.toString()
                    if (content.isNotEmpty()) {
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
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _placeBinding = null
    }
}