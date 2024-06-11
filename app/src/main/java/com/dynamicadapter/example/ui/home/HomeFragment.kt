package com.dynamicadapter.example.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.DEFAULT_SPAN_COUNT
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.dynamicadapter.example.databinding.FragmentHomeBinding
import com.dynamicadapter.example.ui.home.vm.HomeViewModel
import com.dynamicadapter.lib.DynamicDiffAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = requireNotNull(_binding)
    private val viewModel: HomeViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(aClass: Class<T>): T = HomeViewModel() as T
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initViewModel()
        viewModel.loadInit()
    }

    private fun initRecyclerView() {
        with(binding.rvItems) {
            layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false).apply {
                spanSizeLookup = object : SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        var spanItem = (adapter as? DynamicDiffAdapter)?.getSpanItem(position)
                                ?: DEFAULT_SPAN_COUNT
                        if (spanItem < 0 || spanItem > spanCount) {
                            spanItem = spanCount
                        }
                        return spanItem
                    }
                }
            }
            adapter = DynamicDiffAdapter()
        }
    }

    private fun initViewModel() {
        viewModel.dataList.observe(viewLifecycleOwner) { dataPair ->
            val dataList = dataPair.first
            val runnable = dataPair.second ?: Runnable { }

            (binding.rvItems.adapter as? DynamicDiffAdapter)?.submitList(dataList, runnable)
        }
    }

    fun onChangeOrientation(isVertical: Boolean) {
        binding.rvItems.scrollToPosition(0)
        (binding.rvItems.adapter as? DynamicDiffAdapter)?.stickyHelper?.reset()
        viewModel.onChangeOrientation(isVertical) {
            binding.rvItems.updateLayoutParams {
                height = if (isVertical) {
                    ViewGroup.LayoutParams.MATCH_PARENT
                } else {
                    requireView().height / 2
                }
            }
            (binding.rvItems.layoutManager as? GridLayoutManager)?.orientation = if (isVertical) {
                GridLayoutManager.VERTICAL
            } else {
                GridLayoutManager.HORIZONTAL
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}