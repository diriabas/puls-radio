
package com.pulsradio.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.content.res.ColorStateList
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : Activity() {

    private val stations = listOf(
        "Европа Плюс",
        "Радио Рекорд",
        "DFM",
        "Радио ENERGY",
        "Авторадио",
        "Новое Радио",
        "Love Radio",
        "Русское Радио",
        "Хит FM",
        "Like FM",
        "Радио Maximum",
        "Наше Радио",
        "Rock FM",
        "Relax FM",
        "Монте-Карло",
        "Радио Джаз",
        "Kiss FM",
        "NRJ",
        "Radio ONE",
        "Мегаполис FM",
        "Радио Маяк",
        "Радио России",
        "Вести FM",
        "Комсомольская правда",
        "Ретро FM",
        "Дорожное радио"
    )

    private var selectedStation = 0
    private var isPlaying = false

    private lateinit var stationTitle: TextView
    private lateinit var playButton: Button
    private lateinit var stationList: LinearLayout

    private val backgroundColor = Color.rgb(30, 34, 42)
    private val panelColor = Color.rgb(43, 49, 60)
    private val buttonColor = Color.rgb(43, 75, 115)
    private val accentColor = Color.rgb(55, 125, 205)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
            clipToPadding = false
        }

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                dp(16) + bars.left,
                dp(12) + bars.top,
                dp(16) + bars.right,
                dp(12) + bars.bottom
            )

            insets
        }

        val header = TextView(this).apply {
            text = "ПУЛЬС РАДИО"
            textSize = 25f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(18))
        }

        root.addView(header)

        val playerPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(panelColor)
            setPadding(dp(12), dp(18), dp(12), dp(18))
        }

        val nowPlaying = TextView(this).apply {
            text = "СЕЙЧАС ИГРАЕТ"
            textSize = 12f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        stationTitle = TextView(this).apply {
            textSize = 23f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(12))
        }

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val previousButton = createControlButton("НАЗАД").apply {
            setOnClickListener { changeStation(-1) }
        }

        playButton = createControlButton("▶").apply {
            setOnClickListener { togglePlayback() }
        }

        val nextButton = createControlButton("ДАЛЕЕ").apply {
            setOnClickListener { changeStation(1) }
        }

        controls.addView(previousButton)
        controls.addView(playButton)
        controls.addView(nextButton)

        playerPanel.addView(nowPlaying)
        playerPanel.addView(stationTitle)
        playerPanel.addView(controls)

        root.addView(
            playerPanel,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val listTitle = TextView(this).apply {
            text = "РАДИОСТАНЦИИ"
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(0, dp(20), 0, dp(10))
        }

        root.addView(listTitle)

        val scrollView = ScrollView(this).apply {
            clipToPadding = false
            isFillViewport = true
            setPadding(0, 0, 0, dp(8))
        }

        stationList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        stations.forEachIndexed { index, station ->
            val stationButton = Button(this).apply {
                text = station
                textSize = 15f
                gravity = Gravity.CENTER_VERTICAL
                setTextColor(Color.WHITE)
                backgroundTintList = ColorStateList.valueOf(panelColor)

                setOnClickListener {
                    selectedStation = index
                    isPlaying = false
                    updatePlayer()
                    updateStationList()
                }
            }

            stationList.addView(
                stationButton,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(52)
                ).apply {
                    bottomMargin = dp(5)
                }
            )
        }

        scrollView.addView(stationList)

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)

        ViewCompat.requestApplyInsets(root)

        updatePlayer()
        updateStationList()
    }

    private fun createControlButton(label: String): Button {
        return Button(this).apply {
            text = label
            textSize = 13f
            setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(buttonColor)
            minWidth = dp(80)
        }
    }

    private fun togglePlayback() {
        isPlaying = !isPlaying
        updatePlayer()
    }

    private fun changeStation(direction: Int) {
        selectedStation =
            (selectedStation + direction + stations.size) % stations.size

        isPlaying = false
        updatePlayer()
        updateStationList()
    }

    private fun updatePlayer() {
        stationTitle.text = stations[selectedStation]
        playButton.text = if (isPlaying) "Ⅱ" else "▶"
    }

    private fun updateStationList() {
        for (i in 0 until stationList.childCount) {
            val button = stationList.getChildAt(i) as? Button ?: continue

            button.backgroundTintList = ColorStateList.valueOf(
                if (i == selectedStation) accentColor else panelColor
            )
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
