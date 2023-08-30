package com.example.drivenmap.feat_splash.presentation.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.drivenmap.R
import com.example.drivenmap.databinding.FragmentLoginBinding
import com.example.drivenmap.databinding.FragmentMapBinding
import com.example.drivenmap.databinding.FragmentSplashBinding
import com.example.drivenmap.feat_core.utils.PermissionManager

class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding by lazy {
        _binding!!
    }
    private var dialog : AlertDialog? = null
    private lateinit var builder: AlertDialog.Builder
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSplashBinding.inflate(inflater)
        setViews()
        return binding.root
    }

    private fun setViews() {

    }
}