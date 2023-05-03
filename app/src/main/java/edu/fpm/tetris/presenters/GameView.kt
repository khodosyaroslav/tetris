package edu.fpm.tetris.presenters

interface GameView {
    fun init(gameSize: Int)
    fun draw(points: Array<Array<Point>>)
    fun setScore(score: Int)
    fun setStatus(status: GameStatus)
}