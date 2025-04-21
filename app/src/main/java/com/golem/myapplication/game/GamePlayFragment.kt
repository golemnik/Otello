package com.golem.myapplication.game

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.golem.myapplication.R
import com.golem.myapplication.audio.GameAudioManager
import com.golem.myapplication.audio.withSound
import com.golem.myapplication.databinding.FragmentGamePlayBinding

class GamePlayFragment : Fragment() {

    private var _binding: FragmentGamePlayBinding? = null
    private val binding get() = _binding!!

    private lateinit var gameBoard: GameBoard
    private lateinit var player1Name: String
    private lateinit var player2Name: String
    private var boardSize: Int = 8

    private var gameStartTime: Long = 0
    private var gameTimeSeconds: Int = 0
    private lateinit var timerHandler: Handler
    private val timerRunnable = object : Runnable {
        override fun run() {
            gameTimeSeconds++
            updateTimer()
            timerHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Получаем параметры игры
        arguments?.let {
            player1Name = it.getString("player1Name", "Player 1")
            player2Name = it.getString("player2Name", "Player 2")
            boardSize = it.getInt("boardSize", 8)
        }

        // Инициализируем игровую логику
        gameBoard = GameBoard(boardSize)

        // Инициализируем таймер
        timerHandler = Handler(Looper.getMainLooper())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGamePlayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настраиваем информацию об игроках
        binding.textviewPlayer1Name.text = player1Name
        binding.textviewPlayer2Name.text = player2Name

        // Настраиваем игровое поле
        setupGameBoard()

        // Обновляем информацию о ходе и счете
        updateGameInfo()

        // Кнопка выхода из игры
        binding.buttonExitGame.withSound(requireContext()) {
            // Подтверждение выхода из игры
            AlertDialog.Builder(requireContext())
                .setTitle("Выход из игры")
                .setMessage("Вы уверены, что хотите выйти из игры? Прогресс будет потерян.")
                .setPositiveButton("Выйти") { _, _ ->
                    stopTimer()
                    findNavController().navigateUp()
                }
                .setNegativeButton("Отмена", null)
                .show()
        }

        // Кнопка возврата в меню после окончания игры
        binding.buttonReturnToMenu.withSound(requireContext()) {
            // Сохраняем результат игры в базу данных (в будущем)
            findNavController().navigate(R.id.action_gamePlayFragment_to_FirstFragment)
        }

        // Запускаем таймер
        startTimer()
    }

    private fun setupGameBoard() {
        val gridLayout = binding.gridGameBoard
        gridLayout.rowCount = boardSize
        gridLayout.columnCount = boardSize

        // Вычисляем размер ячейки
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val cellSize = (screenWidth - 32) / boardSize // Учитываем отступы

        // Создаем ячейки игрового поля
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                val cellView = createCellView(row, col, cellSize)
                gridLayout.addView(cellView)
            }
        }

        // Отображаем начальные фишки
        updateBoardUI()
    }

    private fun createCellView(row: Int, col: Int, cellSize: Int): View {
        val cellFrame = FrameLayout(requireContext())
        val params = GridLayout.LayoutParams()
        params.width = cellSize
        params.height = cellSize
        params.rowSpec = GridLayout.spec(row)
        params.columnSpec = GridLayout.spec(col)
        cellFrame.layoutParams = params

        // Фон ячейки
        val cellBackground = FrameLayout(requireContext())
        cellBackground.background = ContextCompat.getDrawable(requireContext(), R.drawable.cell_background)
        cellFrame.addView(cellBackground, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

        // Добавляем фишку (первоначально скрыта)
        val diskView = View(requireContext())
        diskView.id = View.generateViewId() // Генерируем уникальный ID для фишки
        diskView.visibility = View.INVISIBLE // Изначально фишка невидима

        val diskParams = FrameLayout.LayoutParams(cellSize - 10, cellSize - 10)
        diskParams.gravity = Gravity.CENTER
        cellFrame.addView(diskView, diskParams)

        // Обработка нажатия на ячейку
        cellFrame.setOnClickListener {
            if (makeMove(row, col)) {
                // Ход успешен, обновляем интерфейс
                updateBoardUI()
                updateGameInfo()

                // Проверяем окончание игры
                if (gameBoard.isGameOver()) {
                    gameOver()
                }
            }
        }

        return cellFrame
    }

    private fun makeMove(row: Int, col: Int): Boolean {
        // Проверяем, можно ли сделать ход
        if (!gameBoard.isValidMove(row, col)) {
            // Звук ошибки или вибрация
            return false
        }

        // Звук хода
        GameAudioManager.getInstance(requireContext())
            .playInterfaceSound(GameAudioManager.SoundType.BUTTON_CLICK)

        // Делаем ход
        return gameBoard.makeMove(row, col)
    }

    private fun updateBoardUI() {
        val gridLayout = binding.gridGameBoard

        // Обновляем состояние всех фишек на доске
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                val cellIndex = row * boardSize + col
                val cellFrame = gridLayout.getChildAt(cellIndex) as FrameLayout
                val diskView = cellFrame.getChildAt(1) as View

                // Устанавливаем цвет и видимость фишки
                when (gameBoard.board[row][col].color) {
                    DiskColor.BLACK -> {
                        diskView.background = ContextCompat.getDrawable(requireContext(), R.drawable.disk_black)
                        diskView.visibility = View.VISIBLE
                    }
                    DiskColor.WHITE -> {
                        diskView.background = ContextCompat.getDrawable(requireContext(), R.drawable.disk_white)
                        diskView.visibility = View.VISIBLE
                    }
                    DiskColor.NONE -> {
                        diskView.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    private fun updateGameInfo() {
        // Обновляем информацию о текущем ходе
        val currentPlayerName = if (gameBoard.isBlackTurn) player1Name else player2Name
        binding.textviewCurrentTurn.text = "Ход: $currentPlayerName"

        // Обновляем счет
        val (blackScore, whiteScore) = gameBoard.getScore()
        binding.textviewPlayer1Score.text = blackScore.toString()
        binding.textviewPlayer2Score.text = whiteScore.toString()
    }

    private fun startTimer() {
        gameStartTime = System.currentTimeMillis()
        timerHandler.postDelayed(timerRunnable, 1000)
    }

    private fun stopTimer() {
        timerHandler.removeCallbacks(timerRunnable)
    }

    private fun updateTimer() {
        val minutes = gameTimeSeconds / 60
        val seconds = gameTimeSeconds % 60
        binding.textviewTimer.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun gameOver() {
        // Останавливаем таймер
        stopTimer()

        // Определяем победителя
        val winner = gameBoard.getWinner()
        val (blackScore, whiteScore) = gameBoard.getScore()

        // Заполняем информацию о победителе
        val winnerName = when (winner) {
            DiskColor.BLACK -> player1Name
            DiskColor.WHITE -> player2Name
            else -> "Ничья" // Ничья, если winner == null
        }

        binding.textviewWinnerName.text = winnerName
        binding.textviewFinalScore.text = "$blackScore : $whiteScore"

        // Добавляем информацию о времени игры
        val minutes = gameTimeSeconds / 60
        val seconds = gameTimeSeconds % 60
        binding.textviewGameTime.text = "Время: ${String.format("%02d:%02d", minutes, seconds)}"

        // Показываем экран победителя
        binding.overlayWinner.visibility = View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        // Останавливаем таймер при сворачивании приложения
        stopTimer()
    }

    override fun onResume() {
        super.onResume()
        // Возобновляем таймер если игра не окончена
        if (!gameBoard.isGameOver() && gameStartTime > 0) {
            timerHandler.postDelayed(timerRunnable, 1000)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTimer()
        _binding = null
    }
}
