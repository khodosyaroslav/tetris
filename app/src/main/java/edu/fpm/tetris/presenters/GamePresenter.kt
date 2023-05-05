package edu.fpm.tetris.presenters

class GamePresenter {
    private lateinit var gameModel: GameModel
    private lateinit var gameView: GameView
    private var status: GameStatus = GameStatus.START

    fun init() {
        gameModel.init()
        gameView.init(gameModel.gameSize)
        gameModel.setGameOverListener { setStatus(GameStatus.GAME_OVER) }
        gameModel.setScoreUpdatedListener(gameView::setScore)
        setStatus(GameStatus.START)
    }

    fun turn(turn: GameTurn) {
        gameModel.turn(turn)
    }

    fun changeStatus() {
        if (status == GameStatus.PLAYING) {
            pauseGame()
        } else {
            startGame()
        }
    }

    fun setGameModel(gameModel: GameModel) {
        this.gameModel = gameModel
    }

    fun setGameView(gameView: GameView) {
        this.gameView = gameView
    }

    private fun startGame() {
        setStatus(GameStatus.PLAYING)
        gameModel.startGame(gameView::draw)
    }

    private fun pauseGame() {
        setStatus(GameStatus.GAME_PAUSED)
        gameModel.pauseGame()
    }

    private fun setStatus(status: GameStatus) {
        if (this.status == GameStatus.GAME_OVER || this.status == GameStatus.START) {
            gameModel.newGame()
        }
        this.status = status
        gameView.setStatus(status)
    }
}