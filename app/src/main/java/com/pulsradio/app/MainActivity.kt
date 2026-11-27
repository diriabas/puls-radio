
package com.pulsradio.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

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

    private val backgroundColor = Color.rgb(32, 36, 42)
    private val panelColor = Color.rgb(48, 53, 62)
    private val accentColor = Color.rgb(75, 145, 230)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
            setPadding(dp(16), dp(12), dp(16), dp(12))
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

        val previousButton = Button(this).apply {
            text = "Назад"
            setOnClickListener { changeStation(-1) }
        }

        playButton = Button(this).apply {
            text = "▶"
            setOnClickListener { togglePlayback() }
        }

        val nextButton = Button(this).apply {
            text = "Далее"
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

        val scrollView = ScrollView(this)

        val stationList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        stations.forEachIndexed { index, station ->
            val stationButton = Button(this).apply {
                text = station
                textSize = 15f
                gravity = Gravity.CENTER_VERTICAL
                setTextColor(Color.WHITE)
                setBackgroundColor(panelColor)
                setOnClickListener {
                    selectedStation = index
                    isPlaying = false
                    updatePlayer()
                    updateStationList(stationList)
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

        updatePlayer()
        updateStationList(stationList)
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
    }

    private fun updatePlayer() {
        stationTitle.text = stations[selectedStation]
        playButton.text = if (isPlaying) "Ⅱ" else "▶"
    }

    private fun updateStationList(list: LinearLayout) {
        for (i in 0 until list.childCount) {
            val button = list.getChildAt(i) as? Button ?: continue

            button.setBackgroundColor(
                if (i == selectedStation) accentColor else panelColor
            )
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
