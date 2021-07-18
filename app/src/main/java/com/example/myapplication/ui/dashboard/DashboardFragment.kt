package com.example.myapplication.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.database.DataEntity
import com.example.myapplication.databinding.FragmentDashboardBinding
import com.example.myapplication.model.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class DashboardFragment : Fragment(R.layout.fragment_dashboard), CoroutineScope by MainScope() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var mViewModel : ViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView = binding.recyclerviewMain
        val adapter = DataEntityAdapter(this.context)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        mViewModel = activity?.let { ViewModelProvider(it) }?.get(ViewModel::class.java)!!

        val observer = Observer<List<DataEntity>>{
            adapter.setEntities(it)
        }

        mViewModel.getAllData().observe(viewLifecycleOwner, observer)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}