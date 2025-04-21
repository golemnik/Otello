package com.golem.myapplication.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.golem.myapplication.R
import com.golem.myapplication.audio.withSound
import com.golem.myapplication.databinding.FragmentGameSetupBinding

class GameSetupFragment : Fragment() {

    private var _binding: FragmentGameSetupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Кнопка "Начать игру"
        binding.buttonStartGame.withSound(requireContext()) {
            startGame()
        }

        // Кнопка "Назад"
        binding.buttonBack.withSound(requireContext()) {
            findNavController().navigateUp()
        }
    }

    private fun startGame() {
        // Получаем имена игроков (с проверкой на пустые строки)
        val player1Name = binding.edittextPlayer1.text.toString().takeIf { it.isNotBlank() } ?: "Player 1"
        val player2Name = binding.edittextPlayer2.text.toString().takeIf { it.isNotBlank() } ?: "Player 2"

        // Определяем выбранный размер поля
        val boardSize = when {
            binding.radiobutton6x6.isChecked -> 6
            binding.radiobutton10x10.isChecked -> 10
            else -> 8 // По умолчанию 8x8
        }

        // Создаем Bundle с параметрами игры
        val args = Bundle().apply {
            putString("player1Name", player1Name)
            putString("player2Name", player2Name)
            putInt("boardSize", boardSize)
        }

        // Переходим к игровому экрану
        findNavController().navigate(R.id.action_gameSetupFragment_to_gamePlayFragment, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
