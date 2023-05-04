package edu.fpm.tetris.views

import android.view.View
import android.widget.Button
import android.widget.TextView
import edu.fpm.tetris.presenters.GameStatus
import edu.fpm.tetris.presenters.GameView
import edu.fpm.tetris.presenters.Point

class GameViewImpl(
    private val gameFrame: GameFrame,
    private val gameScoreText: TextView,
    private val gameStatusText: TextView,
    private val gameControlButton: Button
) : GameView {

    override fun init(gameSize: Int) {
        gameFrame.init(gameSize)
    }

    override fun draw(points: Array<Array<Point>>) {
        gameFrame.setPoints(points)
        gameFrame.invalidate()
    }

    override fun setScore(score: Int) {
        gameScoreText.text = "Score: $score"
    }

    override fun setStatus(status: GameStatus) {
        gameStatusText.text = status.value
        gameStatusText.visibility = if (status == GameStatus.PLAYING) View.INVISIBLE else View.VISIBLE
        gameControlButton.text = if (status == GameStatus.PLAYING) "Pause" else "Start"
    }
}