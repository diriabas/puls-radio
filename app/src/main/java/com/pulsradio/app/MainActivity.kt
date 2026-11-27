
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
import android.widget.Toast
import android.util.Log

import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class MainActivity : Activity() {

    private val stations = listOf(
        "Европа Плюс",
        "Радио Вера",
        "Коран — Абдул Басит",
        "Коран — Мишари Аль-Афаси",
        "Коран — Махер Аль-Муэкли",
        "Коран — перевод на русский",
        "Коран — Мухаммад аль-Миншави",
        "Коран — Сауд аш-Шурайм",
        "DFM",
        "Like FM",
        "Русское Радио"
    )

    private val stationUrls = mapOf(
        "Европа Плюс" to
            "https://hls-01-regions.emgsound.ru/11_msk/96/playlist.m3u8",
        "Радио Вера" to
            "https://radiovera.hostingradio.ru:8007/radiovera_128",
        "Коран — Абдул Басит" to
            "https://backup.qurango.net/radio/abdulbasit_abdulsamad",
        "Коран — Мишари Аль-Афаси" to
            "https://backup.qurango.net/radio/mishary_alafasi",
        "Коран — Махер Аль-Муэкли" to
            "https://backup.qurango.net/radio/maher",
        "Коран — перевод на русский" to
            "https://backup.qurango.net/radio/translation_quran_Russia",
        "Коран — Мухаммад аль-Миншави" to
            "https://radio.mp3islam.com/listen/minshawi/radio.mp3",
        "Коран — Сауд аш-Шурайм" to
            "https://radio.mp3islam.com/listen/alshuraim/radio.mp3",
        "DFM" to
            "https://dfm.hostingradio.ru/dfm96.aacp",
        "Like FM" to
            "https://hls-01-gpm.hostingradio.ru/likefm495/playlist.m3u8",
        "Русское Радио" to
            "https://rusradio.hostingradio.ru/rusradio96.aacp"
    )

    private var selectedStation = 0
    private var player: ExoPlayer? = null

    private lateinit var stationTitle: TextView
    private lateinit var playButton: TextView
    private lateinit var stationList: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var stationCount: TextView

    private val backgroundColor = Color.rgb(5, 14, 26)
    private val panelColor = Color.rgb(17, 34, 52)
    private val selectedColor = Color.rgb(12, 57, 86)
    private val accentColor = Color.rgb(0, 195, 255)
    private val textColor = Color.rgb(238, 247, 255)
    private val secondaryColor = Color.rgb(155, 190, 215)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor

        createInterface()
        initializePlayer()
    }

    private fun initializePlayer() {
        try {
            player = ExoPlayer.Builder(this).build()

            player?.addListener(object : Player.Listener {

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updatePlayButton()
                    updateStationList()
                }

                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING ->
                            showStatus("Подключение к эфиру...")

                        Player.STATE_READY ->
                            showStatus("Эфир подключён")

                        Player.STATE_ENDED ->
                            showStatus("Эфир завершён")

                        Player.STATE_IDLE ->
                            showStatus("Ожидание")
                    }

                    updatePlayButton()
                    updateStationList()
                }

                override fun onPlayerError(error: PlaybackException) {
                    reportError("Ошибка воспроизведения", error)
                    updateStationList()
                }
            })

            showStatus("Выбери радиостанцию")

        } catch (error: Throwable) {
            reportError("Ошибка проигрывателя", error)
        }
    }

    private fun createInterface() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
            setPadding(dp(16), dp(8), dp(16), dp(12))
        }

        val header = TextView(this).apply {
            text = "〰  ПУЛЬС РАДИО"
            textSize = 26f
            setTextColor(textColor)
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(12), 0, dp(18))
        }

        root.addView(header)

        val playerPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = glassBackground(true)
            setPadding(dp(16), dp(18), dp(16), dp(18))
        }

        val nowPlaying = TextView(this).apply {
            text = "◉  СЕЙЧАС ИГРАЕТ"
            textSize = 12f
            setTextColor(accentColor)
            gravity = Gravity.CENTER
        }

        stationTitle = TextView(this).apply {
            textSize = 24f
            setTextColor(textColor)
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(14), 0, dp(12))
        }

        val equalizer = TextView(this).apply {
            text = "▂ ▄ ▆ ▇ ▅ ▃ ▆ ▇ ▄ ▂ ▅ ▇ ▆ ▃"
            textSize = 23f
            setTextColor(accentColor)
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, dp(12))
        }

        playButton = TextView(this).apply {
            text = "▶"
            textSize = 25f
            setTextColor(textColor)
            gravity = Gravity.CENTER
            background = roundBackground(accentColor, 100)
            setOnClickListener {
                togglePlayback()
            }
        }

        playerPanel.addView(nowPlaying)
        playerPanel.addView(stationTitle)
        playerPanel.addView(equalizer)

        playerPanel.addView(
            playButton,
            LinearLayout.LayoutParams(dp(64), dp(64)).apply {
                gravity = Gravity.CENTER
            }
        )

        root.addView(
            playerPanel,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        statusText = TextView(this).apply {
            textSize = 12f
            setTextColor(secondaryColor)
            gravity = Gravity.CENTER
            setPadding(dp(5), dp(12), dp(5), dp(8))
            text = "Выбери радиостанцию"
        }

        root.addView(statusText)

        val titleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val listTitle = TextView(this).apply {
            text = "РАДИОСТАНЦИИ"
            textSize = 16f
            setTextColor(textColor)
            setTypeface(null, Typeface.BOLD)
        }

        stationCount = TextView(this).apply {
            text = "${stations.size} станций"
            textSize = 12f
            setTextColor(secondaryColor)
            gravity = Gravity.CENTER
            background = roundBackground(panelColor, 30)
            setPadding(dp(12), dp(7), dp(12), dp(7))
        }

        titleRow.addView(
            listTitle,
            LinearLayout.LayoutParams(0, dp(38), 1f)
        )

        titleRow.addView(stationCount)

        root.addView(titleRow)

        val scrollView = ScrollView(this).apply {
            clipToPadding = false
            setPadding(0, dp(8), 0, dp(8))
        }

        stationList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        stations.forEachIndexed { index, station ->
            val card = createStationCard(index, station)

            stationList.addView(
                card,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(72)
                ).apply {
                    bottomMargin = dp(8)
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

        updatePlayerTitle()
        updateStationList()
    }

    private fun createStationCard(
        index: Int,
        station: String
    ): LinearLayout {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = glassBackground(false)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            isClickable = true
            isFocusable = true

            setOnClickListener {
                selectStation(index)
            }
        }

        val icon = TextView(this).apply {
            text = getStationIcon(station)
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(accentColor)
            background = roundBackground(
                Color.rgb(10, 39, 57),
                12
            )
        }

        card.addView(
            icon,
            LinearLayout.LayoutParams(dp(52), dp(52)).apply {
                marginEnd = dp(12)
            }
        )

        val textColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val name = TextView(this).apply {
            text = station
            textSize = 15f
            setTextColor(textColor)
            setTypeface(null, Typeface.BOLD)
            maxLines = 1
        }

        val description = TextView(this).apply {
            text = getStationDescription(station)
            textSize = 12f
            setTextColor(secondaryColor)
            setPadding(0, dp(3), 0, 0)
        }

        textColumn.addView(name)
        textColumn.addView(description)

        card.addView(
            textColumn,
            LinearLayout.LayoutParams(0, dp(52), 1f)
        )

        val indicator = TextView(this).apply {
            text = "›"
            textSize = 27f
            setTextColor(secondaryColor)
            gravity = Gravity.CENTER
            tag = "indicator"
        }

        card.addView(
            indicator,
            LinearLayout.LayoutParams(dp(28), dp(52))
        )

        card.tag = "station_$index"

        return card
    }

    private fun getStationIcon(station: String): String {
        return when {
            station == "Европа Плюс" -> "♫"
            station == "Радио Вера" -> "✧"
            station.startsWith("Коран") -> "☪"
            station == "DFM" -> "D"
            station == "Like FM" -> "L"
            station == "Русское Радио" -> "R"
            else -> "♪"
        }
    }

    private fun getStationDescription(station: String): String {
        return when {
            station.startsWith("Коран") -> "Чтение Корана"
            station == "Радио Вера" -> "Духовное радио"
            station == "Европа Плюс" -> "Популярная музыка"
            station == "Русское Радио" -> "Русская музыка"
            station == "Like FM" -> "Современные хиты"
            station == "DFM" -> "Танцевальная музыка"
            else -> "Прямой эфир"
        }
    }

    private fun selectStation(index: Int) {
        val currentPlayer = player ?: return
        val station = stations[index]
        val url = stationUrls[station] ?: return

        try {
            if (selectedStation == index) {
                if (currentPlayer.isPlaying) {
                    currentPlayer.pause()
                    showStatus("Пауза: $station")
                } else {
                    currentPlayer.play()
                    showStatus("Продолжаем: $station")
                }
            } else {
                selectedStation = index

                currentPlayer.stop()
                currentPlayer.setMediaItem(MediaItem.fromUri(url))
                currentPlayer.prepare()
                currentPlayer.play()

                showStatus("Запуск: $station")
            }

            updatePlayerTitle()
            updateStationList()
            updatePlayButton()

        } catch (error: Throwable) {
            reportError("Ошибка переключения станции", error)
        }
    }

    private fun togglePlayback() {
        val currentPlayer = player ?: return

        try {
            if (currentPlayer.isPlaying) {
                currentPlayer.pause()
                showStatus("Пауза")
            } else {
                if (currentPlayer.playbackState == Player.STATE_IDLE) {
                    val url = stationUrls[stations[selectedStation]] ?: return
                    currentPlayer.setMediaItem(MediaItem.fromUri(url))
                    currentPlayer.prepare()
                }

                currentPlayer.play()
                showStatus("Воспроизведение")
            }

            updatePlayButton()
            updateStationList()

        } catch (error: Throwable) {
            reportError("Ошибка управления эфиром", error)
        }
    }

    private fun updatePlayerTitle() {
        stationTitle.text = stations[selectedStation]
    }

    private fun updatePlayButton() {
        if (::playButton.isInitialized) {
            playButton.text =
                if (player?.isPlaying == true) "Ⅱ" else "▶"
        }
    }

    private fun updateStationList() {
        if (!::stationList.isInitialized) return

        for (i in 0 until stationList.childCount) {
            val card = stationList.getChildAt(i) as? LinearLayout ?: continue
            val active = i == selectedStation
            card.background = glassBackground(active)

            val indicator = card.findViewWithTag<TextView>("indicator")
            indicator?.apply {
                text = if (active && player?.isPlaying == true) "♫" else "›"
                setTextColor(if (active) accentColor else secondaryColor)
            }
        }
    }

    private fun glassBackground(active: Boolean): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            if (active) {
                intArrayOf(
                    Color.rgb(15, 65, 91),
                    Color.rgb(10, 35, 57),
                    Color.rgb(13, 45, 67)
                )
            } else {
                intArrayOf(
                    Color.rgb(24, 43, 61),
                    Color.rgb(13, 29, 45),
                    Color.rgb(20, 38, 55)
                )
            }
        ).apply {
            cornerRadius = dp(18).toFloat()
            setStroke(
                dp(if (active) 2 else 1),
                if (active) accentColor else Color.rgb(65, 105, 135)
            )
        }
    }

    private fun roundBackground(
        color: Int,
        radius: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(1), Color.rgb(65, 115, 150))
        }
    }

    private fun showStatus(message: String) {
        if (::statusText.isInitialized) {
            statusText.text = message
        }
    }

    private fun reportError(message: String, error: Throwable) {
        val details =
            "$message: ${error.javaClass.simpleName}: ${error.message}"

        Log.e("PulsRadio", details, error)
        showStatus(details)

        Toast.makeText(
            this,
            details,
            Toast.LENGTH_LONG
        ).show()

        try {
            openFileOutput("puls-radio-error.txt", MODE_PRIVATE).use {
                it.write(
                    (details + "\n" + Log.getStackTraceString(error))
                        .toByteArray()
                )
            }
        } catch (_: Exception) {
        }
    }

    private fun createControl(label: String): TextView {
        return TextView(this).apply {
            text = label
            textSize = 14f
            setTextColor(textColor)
            gravity = Gravity.CENTER
            background = roundBackground(panelColor, 12)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        try {
            player?.release()
            player = null
        } catch (error: Throwable) {
            Log.e("PulsRadio", "Ошибка освобождения проигрывателя", error)
        }

        super.onDestroy()
    }
}
