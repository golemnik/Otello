package com.golem.myapplication.game

class GameBoard(val size: Int) {

    // Игровое поле
    val board = Array(size) { Array(size) { Cell() } }

    // Текущий игрок (true - черные, false - белые)
    var isBlackTurn = true

    // Количество ходов
    var moveCount = 0

    // Инициализация начальных фишек
    init {
        val center = size / 2
        board[center-1][center-1].color = DiskColor.WHITE
        board[center][center].color = DiskColor.WHITE
        board[center-1][center].color = DiskColor.BLACK
        board[center][center-1].color = DiskColor.BLACK
    }

    // Проверка возможности хода
    fun isValidMove(row: Int, col: Int): Boolean {
        // Если ячейка уже занята, ход невозможен
        if (board[row][col].color != DiskColor.NONE) {
            return false
        }

        // Проверяем все направления
        val directions = arrayOf(
            Pair(-1, -1), Pair(-1, 0), Pair(-1, 1),
            Pair(0, -1),                Pair(0, 1),
            Pair(1, -1),  Pair(1, 0),  Pair(1, 1)
        )

        val currentColor = if (isBlackTurn) DiskColor.BLACK else DiskColor.WHITE
        val opponentColor = if (isBlackTurn) DiskColor.WHITE else DiskColor.BLACK

        for ((dx, dy) in directions) {
            var x = row + dx
            var y = col + dy
            var foundOpponent = false

            while (x in 0 until size && y in 0 until size && board[x][y].color == opponentColor) {
                x += dx
                y += dy
                foundOpponent = true
            }

            if (foundOpponent && x in 0 until size && y in 0 until size && board[x][y].color == currentColor) {
                return true
            }
        }

        return false
    }

    // Делаем ход и переворачиваем фишки
    fun makeMove(row: Int, col: Int): Boolean {
        if (!isValidMove(row, col)) {
            return false
        }

        val directions = arrayOf(
            Pair(-1, -1), Pair(-1, 0), Pair(-1, 1),
            Pair(0, -1),                Pair(0, 1),
            Pair(1, -1),  Pair(1, 0),  Pair(1, 1)
        )

        val currentColor = if (isBlackTurn) DiskColor.BLACK else DiskColor.WHITE
        val opponentColor = if (isBlackTurn) DiskColor.WHITE else DiskColor.BLACK

        // Ставим фишку текущего игрока
        board[row][col].color = currentColor

        // Переворачиваем фишки противника
        for ((dx, dy) in directions) {
            var x = row + dx
            var y = col + dy
            val disksToFlip = mutableListOf<Pair<Int, Int>>()

            while (x in 0 until size && y in 0 until size && board[x][y].color == opponentColor) {
                disksToFlip.add(Pair(x, y))
                x += dx
                y += dy
            }

            if (disksToFlip.isNotEmpty() && x in 0 until size && y in 0 until size &&
                board[x][y].color == currentColor) {
                for ((flipX, flipY) in disksToFlip) {
                    board[flipX][flipY].color = currentColor
                }
            }
        }

        // Увеличиваем счетчик ходов и меняем игрока
        moveCount++
        isBlackTurn = !isBlackTurn

        // Проверяем, может ли следующий игрок сделать ход
        if (!hasValidMoves()) {
            // Если текущий игрок не может ходить, возвращаем ход предыдущему
            isBlackTurn = !isBlackTurn
            // Если и предыдущий игрок не может ходить, игра окончена
            if (!hasValidMoves()) {
                return true // Игра окончена
            }
        }

        return true
    }

    // Проверка наличия возможных ходов
    fun hasValidMoves(): Boolean {
        for (row in 0 until size) {
            for (col in 0 until size) {
                if (isValidMove(row, col)) {
                    return true
                }
            }
        }
        return false
    }

    // Проверка окончания игры
    fun isGameOver(): Boolean {
        // Игра окончена, если доска заполнена или ни один игрок не может сделать ход
        if (moveCount >= size * size - 4) return true

        val hasCurrentPlayerMoves = hasValidMoves()
        if (!hasCurrentPlayerMoves) {
            isBlackTurn = !isBlackTurn
            val hasOtherPlayerMoves = hasValidMoves()
            isBlackTurn = !isBlackTurn
            return !hasOtherPlayerMoves
        }

        return false
    }

    // Подсчет очков
    fun getScore(): Pair<Int, Int> {
        var blackCount = 0
        var whiteCount = 0

        for (row in 0 until size) {
            for (col in 0 until size) {
                when (board[row][col].color) {
                    DiskColor.BLACK -> blackCount++
                    DiskColor.WHITE -> whiteCount++
                    else -> {}
                }
            }
        }

        return Pair(blackCount, whiteCount)
    }

    // Определение победителя
    fun getWinner(): DiskColor? {
        val (blackCount, whiteCount) = getScore()
        return when {
            blackCount > whiteCount -> DiskColor.BLACK
            whiteCount > blackCount -> DiskColor.WHITE
            else -> null // Ничья
        }
    }
}
