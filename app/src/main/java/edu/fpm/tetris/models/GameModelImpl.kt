package edu.fpm.tetris.models

import android.os.Handler
import edu.fpm.tetris.presenters.GameModel
import edu.fpm.tetris.presenters.GameTurn
import edu.fpm.tetris.presenters.Point
import edu.fpm.tetris.presenters.PointType
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

class GameModelImpl : GameModel {
    private lateinit var points: Array<Array<Point>>
    private lateinit var playingPoints: Array<Array<Point?>>
    private lateinit var upcomingPoints: Array<Array<Point?>>

    private var score: Int = 0
    private val isGamePaused = AtomicBoolean()
    private val isTurning = AtomicBoolean()
    private val fallingPoints = LinkedList<Point?>()

    private var gameOverObserver: (() -> Unit)? = null
    private var scoreUpdatedObserver: ((Int) -> Unit)? = null
    private val handler: Handler = Handler()

    private enum class BrickType(val value: Int) {
        L(0), T(1), S(2), I(3), O(4);

        companion object {
            fun fromValue(value: Int): BrickType {
                return when (value) {
                    0 -> L
                    1 -> T
                    2 -> S
                    3 -> I
                    4 -> O
                    else -> L
                }
            }

            fun random(): BrickType = fromValue(Random.nextInt(5))
        }
    }

    override fun init() {
        points = Array(GAME_SIZE) { row -> Array(GAME_SIZE) { col -> Point(row, col) } }
        for (i in 0 until GAME_SIZE) {
            for (j in 0 until GAME_SIZE) {
                points[i][j] = Point(j, i)
            }
        }

        playingPoints = Array(PLAYING_AREA_HEIGHT) { arrayOfNulls(PLAYING_AREA_WIDTH) }
        for (i in 0 until PLAYING_AREA_HEIGHT) {
            System.arraycopy(points[i], 0, playingPoints[i], 0, PLAYING_AREA_WIDTH)
        }

        upcomingPoints = Array(UPCOMING_AREA_SIZE) { arrayOfNulls(UPCOMING_AREA_SIZE) }
        for (i in 0 until UPCOMING_AREA_SIZE) {
            for (j in 0 until UPCOMING_AREA_SIZE) {
                upcomingPoints[i][j] = points[i + 1][j + 1 + PLAYING_AREA_WIDTH]
            }
        }

        for (i in 0 until PLAYING_AREA_HEIGHT) {
            points[i][PLAYING_AREA_WIDTH].type = PointType.VERTICAL_LINE
        }

        newGame()
    }

    override val gameSize: Int
        get() = GAME_SIZE

    override fun newGame() {
        score = 0
        for (i in 0 until PLAYING_AREA_HEIGHT) {
            for (j in 0 until PLAYING_AREA_WIDTH) {
                playingPoints[i][j]!!.type = PointType.EMPTY
            }
        }
        fallingPoints.clear()
        generateUpcomingBrick()
    }

    private fun generateUpcomingBrick() {
        val upcomingBrick: BrickType = BrickType.random()
        for (i in 0 until UPCOMING_AREA_SIZE) {
            for (j in 0 until UPCOMING_AREA_SIZE) {
                upcomingPoints[i][j]!!.type = PointType.EMPTY
            }
        }
        when (upcomingBrick) {
            BrickType.L -> {
                upcomingPoints[1][1]!!.type = PointType.BOX
                upcomingPoints[2][1]!!.type = PointType.BOX
                upcomingPoints[3][1]!!.type = PointType.BOX
                upcomingPoints[3][2]!!.type = PointType.BOX
            }

            BrickType.T -> {
                upcomingPoints[1][1]!!.type = PointType.BOX
                upcomingPoints[2][1]!!.type = PointType.BOX
                upcomingPoints[2][2]!!.type = PointType.BOX
                upcomingPoints[3][1]!!.type = PointType.BOX
            }

            BrickType.S -> {
                upcomingPoints[1][1]!!.type = PointType.BOX
                upcomingPoints[2][1]!!.type = PointType.BOX
                upcomingPoints[2][2]!!.type = PointType.BOX
                upcomingPoints[3][2]!!.type = PointType.BOX
            }

            BrickType.I -> {
                upcomingPoints[0][1]!!.type = PointType.BOX
                upcomingPoints[1][1]!!.type = PointType.BOX
                upcomingPoints[2][1]!!.type = PointType.BOX
                upcomingPoints[3][1]!!.type = PointType.BOX
            }

            BrickType.O -> {
                upcomingPoints[1][1]!!.type = PointType.BOX
                upcomingPoints[2][1]!!.type = PointType.BOX
                upcomingPoints[1][2]!!.type = PointType.BOX
                upcomingPoints[2][2]!!.type = PointType.BOX
            }
        }
    }

    override fun startGame(onGameDrawnListener: (Array<Array<Point>>) -> Unit) {
        isGamePaused.set(false)
        val sleepTime = (1000 / GameModel.FPS).toLong()
        Thread {
            var count: Long = 0
            while (!isGamePaused.get()) {
                try {
                    Thread.sleep(sleepTime)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                if (count % GameModel.SPEED == 0L) {
                    if (isTurning.get()) {
                        continue
                    }
                    next()
                    handler.post { onGameDrawnListener(points) }
                }
                count++
            }
        }.start()
    }

    @Synchronized
    private operator fun next() {
        updateFallingPoints()

        if (isNextMerged) {
            if (isOutSide) {
                if (gameOverObserver != null) {
                    handler.post { gameOverObserver!!.invoke() }
                }
                isGamePaused.set(true)
                return
            }

            var y: Int = fallingPoints.stream().mapToInt { p -> p!!.y }.max().orElse(-1)
            while (y >= 0) {
                var isScored = true
                for (i in 0 until PLAYING_AREA_WIDTH) {
                    val point = getPlayingPoint(i, y)
                    if (point?.type == PointType.EMPTY) {
                        isScored = false
                        break
                    }
                }
                if (isScored) {
                    score++
                    if (scoreUpdatedObserver != null) {
                        handler.post { scoreUpdatedObserver!!.invoke(score) }
                    }

                    val tmPoints = LinkedList<Point>()
                    for (i in 0..y) {
                        for (j in 0 until PLAYING_AREA_WIDTH) {
                            val point = getPlayingPoint(j, i)
                            if (point?.type == PointType.BOX) {
                                point.type = PointType.EMPTY
                                if (i != y) {
                                    tmPoints.add(Point(point.x, point.y + 1, false, PointType.BOX))
                                }
                            }
                        }
                    }
                    tmPoints.forEach(this::updatePlayingPoint)
                } else {
                    y--
                }
            }
            fallingPoints.forEach { p -> p!!.isFallingPoint = false }
            fallingPoints.clear()
        } else {
            val tmPoints = LinkedList<Point>()
            for (fallingPoint in fallingPoints) {
                fallingPoint!!.type = PointType.EMPTY
                fallingPoint.isFallingPoint = false
                tmPoints.add(Point(fallingPoint.x, fallingPoint.y + 1, true, PointType.BOX))
            }
            fallingPoints.clear()
            fallingPoints.addAll(tmPoints)
            fallingPoints.forEach { point -> updatePlayingPoint(point) }
        }
    }

    private val isNextMerged: Boolean
        get() {
            for (fallingPoint in fallingPoints) {
                if (fallingPoint!!.y + 1 >= 0 && (fallingPoint.y == PLAYING_AREA_HEIGHT - 1 ||
                            getPlayingPoint(fallingPoint.x, fallingPoint.y + 1)?.isStablePoint!!)
                )
                    return true
            }
            return false
        }

    private val isOutSide: Boolean
        get() {
            for (fallingPoint in fallingPoints) {
                if (fallingPoint!!.y < 0) {
                    return true
                }
            }
            return false
        }

    private fun updatePlayingPoint(point: Point?) {
        if (point!!.x in 0 until PLAYING_AREA_WIDTH && point.y in 0 until PLAYING_AREA_HEIGHT) {
            points[point.y][point.x] = point
            playingPoints[point.y][point.x] = point
        }
    }

    private fun getPlayingPoint(x: Int, y: Int): Point? {
        return if (x >= 0 && y >= 0 && x < PLAYING_AREA_WIDTH && y < PLAYING_AREA_HEIGHT) {
            points[y][x]
        } else null
    }

    private fun updateFallingPoints() {
        if (fallingPoints.isEmpty()) {
            for (i in 0 until UPCOMING_AREA_SIZE) {
                for (j in 0 until UPCOMING_AREA_SIZE) {
                    if (upcomingPoints[i][j]!!.type == PointType.BOX) {
                        fallingPoints.add(Point(j + 3, i - 4, true, PointType.BOX))
                    }
                }
            }
            generateUpcomingBrick()
        }
    }

    override fun pauseGame() {
        isGamePaused.set(true)
    }

    override fun turn(turn: GameTurn) {

    }

    override fun setGameOverListener(onGameOverListener: () -> Unit) {
        gameOverObserver = onGameOverListener
    }

    override fun setScoreUpdatedListener(onScoreUpdatedListener: (Int) -> Unit) {
        scoreUpdatedObserver = onScoreUpdatedListener
    }

    companion object {
        private const val GAME_SIZE = 15
        private const val PLAYING_AREA_WIDTH = 10
        private const val PLAYING_AREA_HEIGHT = GAME_SIZE
        private const val UPCOMING_AREA_SIZE = 4
    }
}