package edu.fpm.tetris.presenters

enum class GameStatus(val value: String) {
    START("START"),
    PLAYING("PLAYING"),
    GAME_OVER("GAME OVER"),
    GAME_PAUSED("GAME PAUSED");
}