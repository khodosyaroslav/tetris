package edu.fpm.tetris

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import edu.fpm.tetris.databinding.ActivityMainBinding
import edu.fpm.tetris.models.GameModelImpl
import edu.fpm.tetris.presenters.GamePresenter
import edu.fpm.tetris.presenters.GameTurn
import edu.fpm.tetris.views.GameFrame
import edu.fpm.tetris.views.GameViewImpl

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        val gameFrame: GameFrame = binding.gameContainer
        val scoreText: TextView = binding.score
        val statusText: TextView = binding.status
        val controlButton: Button = binding.btnControl

        val gamePresenter = GamePresenter()
        gamePresenter.setGameModel(GameModelImpl())
        gamePresenter.setGameView(
            GameViewImpl.newInstance(
                gameFrame,
                scoreText,
                statusText,
                controlButton
            )
        )

        val downButton = binding.btnDown
        downButton.setOnClickListener { gamePresenter.turn(GameTurn.DOWN) }

        val leftButton = binding.btnLeft
        leftButton.setOnClickListener { gamePresenter.turn(GameTurn.LEFT) }

        val rightButton = binding.btnRight
        rightButton.setOnClickListener { gamePresenter.turn(GameTurn.RIGHT) }

        val rotateButton = binding.btnRotate
        rotateButton.setOnClickListener { gamePresenter.turn(GameTurn.ROTATE) }

        controlButton.setOnClickListener { gamePresenter.changeStatus() }

        gamePresenter.init()

        setContentView(binding.root)
    }
}