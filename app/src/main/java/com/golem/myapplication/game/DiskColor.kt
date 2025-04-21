package com.golem.myapplication.game

enum class DiskColor {
    NONE, BLACK, WHITE
}

data class Cell(var color: DiskColor = DiskColor.NONE)