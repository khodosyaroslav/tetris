package edu.fpm.tetris.presenters

interface GameModel {
    val FPS: Int
        get() = 60
    val SPEED: Int
        get() = 25

    fun init()
    val gameSize: Int

    fun newGame()
    fun startGame(onGameDrawnListener: (Array<Array<Point>>) -> Unit)
    fun pauseGame()
    fun turn(turn: GameTurn)
    fun setGameOverListener(onGameOverListener: () -> Unit)
    fun setScoreUpdatedListener(onScoreUpdatedListener: (Int) -> Unit)
}