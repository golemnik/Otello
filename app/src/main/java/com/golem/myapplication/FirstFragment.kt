package com.golem.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.golem.myapplication.audio.withSound
import com.golem.myapplication.databinding.FragmentFirstBinding

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonStartGame.withSound(requireContext()) {
            findNavController().navigate(R.id.action_FirstFragment_to_gameSetupFragment)
        }

        // Использование расширения для кнопок со звуком
//        binding.buttonStartGame.withSound(requireContext()) {
//            findNavController().navigate(R.id.action_FirstFragment_to_gameSetupFragment)
//        }

        binding.buttonSettings.withSound(requireContext()) {
            findNavController().navigate(R.id.action_FirstFragment_to_settingsFragment)
        }

        binding.buttonHistory.withSound(requireContext()) {
            findNavController().navigate(R.id.action_FirstFragment_to_historyFragment)
        }

        binding.buttonExit.withSound(requireContext()) {
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

