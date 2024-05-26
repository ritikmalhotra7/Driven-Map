package com.example.drivenmap.feat_map.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.example.drivenmap.databinding.FragmentAddMemberDialogBinding
import com.example.drivenmap.feat_map.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddMemberDialogFragment : DialogFragment() {
    private var _binding:FragmentAddMemberDialogBinding? = null
    private val binding get() = _binding!!

    private var addedMembers:ArrayList<String> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddMemberDialogBinding.inflate(inflater)
        setViews()
        return binding.root
    }

    private fun setViews() {
        binding.apply {
            btAdd.setOnClickListener {
                addedMembers.add(tietId.text.toString())
                llAddedMembers.addView(TextView(requireContext()).apply {
                    text = tietId.text.toString()
                })
            }
            btCreate.setOnClickListener {
                setFragmentResult(Constants.ADD_MEMBER_ID_FRAGMENT_RESULT_KEY,Bundle().apply {
                    putStringArrayList(Constants.ADD_MEMBER_IDs_KEY,addedMembers)
                })
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}