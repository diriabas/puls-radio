
package com.pulsradio.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
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
    private lateinit var playButton: TextView
    private lateinit var stationList: LinearLayout

    private val backgroundColor = Color.rgb(30, 34, 42)
    private val panelColor = Color.rgb(43, 49, 60)
    private val buttonColor = Color.rgb(35, 68, 108)
    private val selectedColor = Color.rgb(55, 125, 205)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Системные панели не накладываются на содержимое приложения.
        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
            setPadding(dp(16), dp(8), dp(16), dp(16))
        }

        val header = TextView(this).apply {
            text = "ПУЛЬС РАДИО"
            textSize = 25f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(18))
        }

        root.addView(header)

        val playerPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = roundedBackground(panelColor, 12)
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
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(12), 0, dp(14))
        }

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val previousButton = createControl("НАЗАД").apply {
            setOnClickListener { changeStation(-1) }
        }

        playButton = createControl("▶").apply {
            textSize = 22f
            setOnClickListener { togglePlayback() }
        }

        val nextButton = createControl("ДАЛЕЕ").apply {
            setOnClickListener { changeStation(1) }
        }

        controls.addView(
            previousButton,
            LinearLayout.LayoutParams(0, dp(54), 1f).apply {
                marginEnd = dp(5)
            }
        )

        controls.addView(
            playButton,
            LinearLayout.LayoutParams(0, dp(54), 1f).apply {
                marginStart = dp(3)
                marginEnd = dp(3)
            }
        )

        controls.addView(
            nextButton,
            LinearLayout.LayoutParams(0, dp(54), 1f).apply {
                marginStart = dp(5)
            }
        )

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
            isFillViewport = false
            clipToPadding = false
            setPadding(0, 0, 0, dp(12))
        }

        stationList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        stations.forEachIndexed { index, station ->
            val stationItem = TextView(this).apply {
                text = station
                textSize = 16f
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), 0, dp(12), 0)
                background = roundedBackground(panelColor, 7)

                setOnClickListener {
                    selectedStation = index
                    isPlaying = false
                    updatePlayer()
                    updateStationList()
                }
            }

            stationList.addView(
                stationItem,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(56)
                ).apply {
                    bottomMargin = dp(6)
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
        updateStationList()
    }

    private fun createControl(label: String): TextView {
        return TextView(this).apply {
            text = label
            textSize = 13f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            background = roundedBackground(buttonColor, 9)
            isClickable = true
            isFocusable = true
        }
    }

    private fun roundedBackground(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
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

        // Показываем выбранную станцию в списке.
        stationList.getChildAt(selectedStation)?.let {
            (stationList.parent as? ScrollView)?.smoothScrollTo(
                0,
                it.top
            )
        }
    }

    private fun updatePlayer() {
        stationTitle.text = stations[selectedStation]
        playButton.text = if (isPlaying) "Ⅱ" else "▶"
    }

    private fun updateStationList() {
        for (i in 0 until stationList.childCount) {
            val item = stationList.getChildAt(i) as? TextView ?: continue

            item.background = roundedBackground(
                if (i == selectedStation) selectedColor else panelColor,
                7
            )
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
