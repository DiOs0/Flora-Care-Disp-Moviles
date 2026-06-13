package com.uce.floracare.activities.Jhon_AddPlant.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.uce.floracare.R
import com.uce.floracare.databinding.FragmentTestBinding

class TestFragment : Fragment() {

    lateinit var binding : FragmentTestBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()

    }

    private fun initListeners(){

        binding.btnViewDetails.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ShowTestFragment())
                .addToBackStack(null)
                .commit()
        }
    }




}