package com.labs.fleamarketapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.labs.fleamarketapp.databinding.FragmentProfileSectionBinding

class ProfileSectionFragment : Fragment() {

    private var _binding: FragmentProfileSectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileSectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.sectionTitle.text = arguments?.getString(ARG_TITLE).orEmpty()
        binding.sectionSubtitle.text = arguments?.getString(ARG_DESC).orEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_DESC = "arg_desc"

        fun newInstance(title: String, description: String): ProfileSectionFragment =
            ProfileSectionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_DESC, description)
                }
            }
    }
}

