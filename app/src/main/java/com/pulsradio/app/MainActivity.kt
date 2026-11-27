
package com.pulsradio.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.*
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.animation.ValueAnimator
import android.animation.ArgbEvaluator
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class MainActivity : Activity() {

    private val stations = listOf(
        "Европа Плюс",
        "Радио Вера",
        "Коран — перевод на русский",
        "DFM",
        "Like FM",
        "Русское Радио",
        "Радио Рекорд",
        "ENERGY",
        "Авторадио",
        "Новое Радио",
        "Love Radio",
        "Хит FM",
        "Радио Maximum",
        "Наше Радио",
        "Relax FM",
        "Радио Джаз",
        "Ретро FM",
        "Дорожное Радио",
        "Джем FM"
    )

    // Прямые потоки. Некоторые вещатели могут менять адреса эфира;
    // после сборки каждый новый поток нужно проверить на устройстве.
    private val stationUrls = mapOf(
        "Европа Плюс" to
            "https://hls-02-europaplus.emgsound.ru/11/playlist.m3u8",

        "Радио Вера" to
            "http://radiovera.hostingradio.ru:8007/radiovera_128",

        "Коран — перевод на русский" to
            "https://backup.qurango.net/radio/translation_quran_Russia",

        "DFM" to
            "https://dfm.hostingradio.ru/dfm96.aacp",

        "Like FM" to
            "https://hls-01-gpm.hostingradio.ru/likefm495/playlist.m3u8",

        "Русское Радио" to
            "https://rusradio.hostingradio.ru/rusradio96.aacp",

        "Радио Рекорд" to
            "https://hls-01-radiorecord.hostingradio.ru/record-msk/playlist.m3u8",

        "ENERGY" to
            "https://hls-01-gpm.hostingradio.ru/energyfm343/128/playlist.m3u8",

        "Авторадио" to
            "https://hls-01-gpm.hostingradio.ru/avtoradio343/128/playlist.m3u8",

        "Новое Радио" to
            "https://hls.newradio.ru/novoeradio-msk/playlist.m3u8",

        "Love Radio" to
            "http://microit.n340.com:9000/VgMv0WV17ZVx1uuo_12_love_128_reg_60967",

        "Хит FM" to
            "https://hitfm.hostingradio.ru/hitfm96.aacp",

        "Радио Maximum" to
            "http://maximum.hostingradio.ru/maximum128.mp3",

        "Наше Радио" to
            "https://nashe1.hostingradio.ru/nashe-128.mp3",

        "Relax FM" to
            "https://hls-01-gpm.hostingradio.ru/relaxfm7/playlist.m3u8",

        "Радио Джаз" to
            "http://nashe1.hostingradio.ru/jazz-128.mp3",

        "Ретро FM" to
            "https://hls-01-retro.emgsound.ru/12/128/playlist.m3u8",

        "Дорожное Радио" to
            "https://dorognoe.hostingradio.ru:8000/dorognoe",

        "Джем FM" to
            "https://jamfm.ru/jamfm.m3u"
    )

    private val prefs by lazy {
        getSharedPreferences("puls_radio_settings", Context.MODE_PRIVATE)
    }

    private var selectedStation = 0
    private var streamRequestId = 0
    private var currentStreamUrl: String? = null
    private var player: ExoPlayer? = null
    private var currentTab = 0

    private var animationEnabled = true
    private var autoStartEnabled = false
    private var stationTilesEnabled = false

    private lateinit var contentFrame: FrameLayout
    private lateinit var stationTitle: TextView
    private lateinit var stationSubtitle: TextView
    private lateinit var stationIcon: TextView
    private lateinit var playButton: TextView
    private lateinit var statusText: TextView
    private lateinit var playerCard: LinearLayout
    private lateinit var miniPlayer: LinearLayout
    private lateinit var settingsButton: TextView

    private val stationCards = mutableListOf<LinearLayout>()
    private val visualizerBars = mutableListOf<View>()

    private var colorAnimator: ValueAnimator? = null
    private var visualizerAnimator: ValueAnimator? = null

    // Цвета текущей темы. Выбранная тема сохраняется в настройках приложения.
    private var selectedTheme = "blue"

    private var backgroundColor = Color.rgb(5, 12, 24)
    private var panelColor = Color.rgb(17, 34, 52)
    private var accentColor = Color.rgb(0, 195, 255)
    private var textColor = Color.rgb(240, 248, 255)
    private var secondaryColor = Color.rgb(155, 190, 215)

    private fun applyThemeColors() {
        when (selectedTheme) {
            "gray" -> {
                backgroundColor = Color.rgb(43, 47, 53)
                panelColor = Color.rgb(58, 63, 70)
                accentColor = Color.rgb(190, 198, 207)
                textColor = Color.WHITE
                secondaryColor = Color.rgb(205, 210, 216)
            }

            "light_gray" -> {
                backgroundColor = Color.rgb(235, 238, 242)
                panelColor = Color.rgb(215, 220, 226)
                accentColor = Color.rgb(65, 105, 145)
                textColor = Color.rgb(35, 43, 53)
                secondaryColor = Color.rgb(95, 105, 118)
            }

            "blue" -> {
                backgroundColor = Color.rgb(5, 12, 24)
                panelColor = Color.rgb(17, 34, 52)
                accentColor = Color.rgb(0, 195, 255)
                textColor = Color.rgb(240, 248, 255)
                secondaryColor = Color.rgb(155, 190, 215)
            }

            "light_blue" -> {
                backgroundColor = Color.rgb(218, 235, 248)
                panelColor = Color.rgb(195, 220, 240)
                accentColor = Color.rgb(30, 115, 180)
                textColor = Color.rgb(25, 48, 70)
                secondaryColor = Color.rgb(75, 105, 130)
            }

            else -> {
                selectedTheme = "blue"
                backgroundColor = Color.rgb(5, 12, 24)
                panelColor = Color.rgb(17, 34, 52)
                accentColor = Color.rgb(0, 195, 255)
                textColor = Color.rgb(240, 248, 255)
                secondaryColor = Color.rgb(155, 190, 215)
            }
        }
    }

    private fun showThemeDialog() {
        val themeKeys = arrayOf("gray", "light_gray", "blue", "light_blue")
        val themeNames = arrayOf(
            "Серая",
            "Светло-серая",
            "Синяя",
            "Светло-синяя"
        )

        val currentIndex = themeKeys.indexOf(selectedTheme).coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle("Выбери тему оформления")
            .setSingleChoiceItems(themeNames, currentIndex) { dialog, which ->
                selectedTheme = themeKeys[which]

                prefs.edit()
                    .putString("theme", selectedTheme)
                    .apply()

                applyThemeColors()
                window.statusBarColor = backgroundColor
                window.navigationBarColor = backgroundColor

                // Пересоздаём только интерфейс. Проигрыватель продолжает работать.
                createMainLayout()
                showTab(currentTab)

                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        selectedStation = prefs.getInt("last_station", 0)
            .coerceIn(0, stations.lastIndex)

        animationEnabled = prefs.getBoolean("animation", true)
        autoStartEnabled = prefs.getBoolean("auto_start", false)
        stationTilesEnabled = true
        selectedTheme = prefs.getString("theme", "blue") ?: "blue"
        applyThemeColors()

        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor

        createMainLayout()
        showTab(0)
        initializePlayer()

        if (autoStartEnabled) {
            playStation(selectedStation)
        }
    }

    private fun createMainLayout() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
            clipChildren = true
            clipToPadding = true
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(10))
        }

        val logo = TextView(this).apply {
            text = "◉"
            textSize = 31f
            setTextColor(accentColor)
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
        }
        header.addView(logo, LinearLayout.LayoutParams(dp(46), dp(48)))

        val titleColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val appName = TextView(this).apply {
            text = "ПУЛЬС РАДИО"
            textSize = 25f
            setTextColor(textColor)
            setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD), Typeface.BOLD)
            letterSpacing = 0.11f
            setShadowLayer(dp(4).toFloat(), 0f, dp(2).toFloat(), accentColor)
        }
        val accentLine = View(this).apply {
            background = roundedBackground(accentColor, 5)
        }
        titleColumn.addView(appName)
        titleColumn.addView(accentLine, LinearLayout.LayoutParams(dp(82), dp(3)).apply {
            topMargin = dp(3)
        })
        header.addView(titleColumn, LinearLayout.LayoutParams(0, dp(48), 1f))

        settingsButton = TextView(this).apply {
            text = "⚙"
            textSize = 23f
            setTextColor(accentColor)
            gravity = Gravity.CENTER
            background = roundedBackground(panelColor, 16)
            isClickable = true
            isFocusable = true
            setOnClickListener { showTab(if (currentTab == 0) 1 else 0) }
        }
        header.addView(settingsButton, LinearLayout.LayoutParams(dp(46), dp(46)))
        root.addView(header)

        contentFrame = FrameLayout(this)
        root.addView(contentFrame, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        miniPlayer = createMiniPlayer()
        root.addView(miniPlayer, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            leftMargin = dp(12)
            rightMargin = dp(12)
            topMargin = dp(5)
            bottomMargin = dp(7)
        })

        setContentView(root)
        root.setOnApplyWindowInsetsListener { view, insets ->
            val topInset: Int
            val bottomInset: Int
            if (Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars())
                topInset = bars.top
                bottomInset = bars.bottom
            } else {
                @Suppress("DEPRECATION")
                run {
                    topInset = insets.systemWindowInsetTop
                    bottomInset = insets.systemWindowInsetBottom
                }
            }
            view.setPadding(0, topInset, 0, bottomInset)
            insets
        }
        root.requestApplyInsets()
    }

    private fun showTab(tab: Int) {
        currentTab = tab
        stopAnimations()
        contentFrame.removeAllViews()
        miniPlayer.visibility = if (tab == 0) View.VISIBLE else View.GONE
        settingsButton.text = if (tab == 0) "⚙" else "←"
        settingsButton.contentDescription = if (tab == 0) "Открыть параметры" else "Вернуться к радио"

        if (tab == 0) {
            contentFrame.addView(createRadioScreen())
            updatePlayerInformation()
            updateStationCards()
            updatePlayButton()
            if (player?.isPlaying == true && animationEnabled) startAnimations()
        } else {
            contentFrame.addView(createSettingsScreen())
        }
    }

    private fun createRadioScreen(): View {
        stationCards.clear()
        val scroll = ScrollView(this).apply {
            clipToPadding = true
            setPadding(0, 0, 0, dp(4))
        }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(13), dp(5), dp(13), dp(12))
        }

        var index = 0
        while (index < stations.size) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            for (column in 0 until 3) {
                val stationIndex = index + column
                val params = LinearLayout.LayoutParams(0, dp(126), 1f).apply {
                    if (column > 0) marginStart = dp(4)
                    if (column < 2) marginEnd = dp(4)
                }
                if (stationIndex < stations.size) {
                    val tile = createStationTile(stationIndex, stations[stationIndex])
                    stationCards.add(tile)
                    row.addView(tile, params)
                } else {
                    row.addView(View(this), params)
                }
            }
            container.addView(row, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(126)
            ).apply { bottomMargin = dp(8) })
            index += 3
        }
        scroll.addView(container)
        return scroll
    }

    private fun createMiniPlayer(): LinearLayout {
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            clipChildren = true
            clipToPadding = true
            background = playerBackground(
                Color.rgb(8, 48, 77),
                Color.rgb(5, 19, 38)
            )
            setPadding(dp(10), dp(4), dp(10), dp(4))
        }
        if (Build.VERSION.SDK_INT >= 21) panel.clipToOutline = true

        playerCard = panel

        val controlsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(3), dp(10), dp(3))
        }

        stationIcon = TextView(this).apply {
            text = getStationIcon(stations[selectedStation])
            textSize = logoTextSize(stations[selectedStation])
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = roundedBackground(
                getStationLogoColor(stations[selectedStation]),
                12
            )
        }

        controlsRow.addView(
            stationIcon,
            LinearLayout.LayoutParams(dp(44), dp(44)).apply {
                marginEnd = dp(9)
            }
        )

        val textColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }

        stationTitle = TextView(this).apply {
            textSize = 14f
            setTextColor(textColor)
            setTypeface(null, Typeface.BOLD)
            maxLines = 1
        }

        stationSubtitle = TextView(this).apply {
            textSize = 10f
            setTextColor(secondaryColor)
            maxLines = 1
        }

        statusText = TextView(this).apply {
            text = "Готово к воспроизведению"
            textSize = 9f
            setTextColor(secondaryColor)
            maxLines = 1
        }

        textColumn.addView(stationTitle)
        textColumn.addView(stationSubtitle)
        textColumn.addView(statusText)

        controlsRow.addView(
            textColumn,
            LinearLayout.LayoutParams(0, dp(46), 1f)
        )

        val previousButton = createMiniControl("⏮", 20f)
        previousButton.setOnClickListener {
            val previous = if (selectedStation == 0) stations.lastIndex else selectedStation - 1
            playStation(previous)
        }

        playButton = createMiniControl("▶", 19f).apply {
            background = roundAccentBackground()
            setTextColor(Color.WHITE)
        }

        playButton.setOnClickListener {
            togglePlayback()
        }

        val nextButton = createMiniControl("⏭", 20f)
        nextButton.setOnClickListener {
            val next = (selectedStation + 1) % stations.size
            playStation(next)
        }

        controlsRow.addView(
            previousButton,
            LinearLayout.LayoutParams(dp(34), dp(42))
        )
        controlsRow.addView(
            playButton,
            LinearLayout.LayoutParams(dp(42), dp(42)).apply {
                marginStart = dp(3)
                marginEnd = dp(3)
            }
        )
        controlsRow.addView(
            nextButton,
            LinearLayout.LayoutParams(dp(34), dp(42))
        )

        panel.addView(controlsRow)

        val equalizer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            clipChildren = true
            clipToPadding = true
        }

        visualizerBars.clear()
        repeat(64) {
            val bar = View(this).apply {
                background = roundedBackground(accentColor, 2)
            }
            visualizerBars.add(bar)
            equalizer.addView(
                bar,
                LinearLayout.LayoutParams(0, dp(2), 1f).apply {
                    marginStart = dp(1)
                    marginEnd = dp(1)
                }
            )
        }

        panel.addView(
            equalizer,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(7)
            )
        )

        return panel
    }

    private fun createMiniControl(symbol: String, size: Float): TextView {
        return TextView(this).apply {
            text = symbol
            textSize = size
            setTextColor(textColor)
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
        }
    }

    private fun logoTextSize(station: String): Float {
        val length = getStationIcon(station).length
        return when {
            length >= 5 -> 9f
            length >= 4 -> 11f
            length >= 3 -> 14f
            else -> 23f
        }
    }

    private fun createStationCard(
        index: Int,
        station: String
    ): LinearLayout {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = stationBackground(false)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            isClickable = true
            isFocusable = true

            setOnClickListener {
                selectStation(index)
            }
        }

        val icon = TextView(this).apply {
            text = getStationIcon(station)
            textSize = logoTextSize(station)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = roundedBackground(
                getStationLogoColor(station),
                14
            )
        }

        card.addView(
            icon,
            LinearLayout.LayoutParams(dp(54), dp(54)).apply {
                marginEnd = dp(12)
            }
        )

        val column = LinearLayout(this).apply {
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
            setPadding(0, dp(4), 0, 0)
        }

        column.addView(name)
        column.addView(description)

        card.addView(
            column,
            LinearLayout.LayoutParams(0, dp(54), 1f)
        )

        val indicator = TextView(this).apply {
            text = "›"
            textSize = 28f
            setTextColor(secondaryColor)
            gravity = Gravity.CENTER
        }

        card.addView(
            indicator,
            LinearLayout.LayoutParams(dp(28), dp(54))
        )

        return card
    }

    private fun createStationTile(
        index: Int,
        station: String
    ): LinearLayout {
        val tile = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = stationBackground(index == selectedStation)
            setPadding(dp(4), dp(7), dp(4), dp(5))
            isClickable = true
            isFocusable = true
            setOnClickListener {
                selectStation(index)
            }
        }

        val icon = TextView(this).apply {
            text = getStationIcon(station)
            textSize = logoTextSize(station)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = roundedBackground(getStationLogoColor(station), 15)
        }

        tile.addView(
            icon,
            LinearLayout.LayoutParams(dp(44), dp(40)).apply {
                gravity = Gravity.CENTER
                bottomMargin = dp(5)
            }
        )

        val name = TextView(this).apply {
            text = station
            textSize = 10.5f
            setTextColor(textColor)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            maxLines = 2
        }
        tile.addView(
            name,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(34)
            )
        )

        val description = TextView(this).apply {
            text = getStationDescription(station)
            textSize = 7.5f
            setTextColor(secondaryColor)
            gravity = Gravity.CENTER
            maxLines = 1
        }
        tile.addView(
            description,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(15)
            )
        )

        val indicator = TextView(this).apply {
            text = if (index == selectedStation && player?.isPlaying == true) "♫" else "›"
            textSize = 15f
            setTextColor(if (index == selectedStation) accentColor else secondaryColor)
            gravity = Gravity.CENTER
        }
        tile.addView(
            indicator,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(13)
            )
        )

        return tile
    }

    private fun getStationIcon(station: String): String {
        return when (station) {
            "Европа Плюс" -> "EP"
            "Радио Вера" -> "ВЕРА"
            "Коран — перевод на русский" -> "☪"
            "DFM" -> "DFM"
            "Like FM" -> "LIKE"
            "Русское Радио" -> "РР"
            "Радио Рекорд" -> "REC"
            "ENERGY" -> "NRJ"
            "Авторадио" -> "AUTO"
            "Новое Радио" -> "NEW"
            "Love Radio" -> "LOVE"
            "Хит FM" -> "HIT"
            "Радио Maximum" -> "MAX"
            "Наше Радио" -> "НАШЕ"
            "Relax FM" -> "RELAX"
            "Радио Джаз" -> "JAZZ"
            "Ретро FM" -> "RETRO"
            "Дорожное Радио" -> "ДР"
            "Джем FM" -> "JAM"
            else -> "FM"
        }
    }

    private fun getStationLogoColor(station: String): Int {
        return when (station) {
            "Европа Плюс" -> Color.rgb(226, 45, 54)
            "Радио Вера" -> Color.rgb(70, 125, 165)
            "Коран — перевод на русский" -> Color.rgb(35, 145, 105)
            "DFM" -> Color.rgb(235, 75, 35)
            "Like FM" -> Color.rgb(120, 75, 190)
            "Русское Радио" -> Color.rgb(190, 35, 45)
            "Радио Рекорд" -> Color.rgb(35, 105, 205)
            "ENERGY" -> Color.rgb(220, 35, 45)
            "Авторадио" -> Color.rgb(35, 115, 185)
            "Новое Радио" -> Color.rgb(35, 35, 40)
            "Love Radio" -> Color.rgb(220, 55, 125)
            "Хит FM" -> Color.rgb(235, 125, 25)
            "Радио Maximum" -> Color.rgb(35, 35, 35)
            "Наше Радио" -> Color.rgb(40, 100, 65)
            "Relax FM" -> Color.rgb(70, 145, 165)
            "Радио Джаз" -> Color.rgb(95, 75, 125)
            "Ретро FM" -> Color.rgb(185, 115, 35)
            "Дорожное Радио" -> Color.rgb(45, 105, 75)
            "Джем FM" -> Color.rgb(220, 65, 105)
            else -> accentColor
        }
    }

    private fun getStationIconColor(station: String): Int {
        return Color.WHITE
    }

    private fun getStationDescription(station: String): String {
        return when (station) {
            "Коран — перевод на русский" -> "Чтение Корана"
            "Радио Вера" -> "Духовное радио"
            "Европа Плюс" -> "Популярная музыка"
            "Русское Радио" -> "Русская музыка"
            "Like FM" -> "Современные хиты"
            "DFM" -> "Танцевальная музыка"
            "Радио Рекорд" -> "Танцевальная музыка"
            "ENERGY" -> "Хиты и новинки"
            "Авторадио" -> "Популярная музыка"
            "Новое Радио" -> "Современная музыка"
            "Love Radio" -> "Музыка о любви"
            "Хит FM" -> "Зарубежные хиты"
            "Радио Maximum" -> "Рок и альтернативная музыка"
            "Наше Радио" -> "Русский рок"
            "Relax FM" -> "Спокойная музыка"
            "Радио Джаз" -> "Джазовая музыка"
            "Ретро FM" -> "Любимые хиты прошлых лет"
            "Дорожное Радио" -> "Музыка в дороге"
            "Джем FM" -> "Музыка Екатеринбурга"
            else -> "Прямой эфир"
        }
    }

    private fun selectStation(index: Int) {
        if (index == selectedStation) {
            togglePlayback()
        } else {
            playStation(index)
        }
    }

    private fun playStation(index: Int) {
        val currentPlayer = player ?: return
        if (index !in stations.indices) return

        val station = stations[index]
        val url = stationUrls[station]

        if (url.isNullOrBlank()) {
            showStatus("Для станции не задан адрес эфира")
            Toast.makeText(this, "Нет адреса потока: $station", Toast.LENGTH_LONG).show()
            return
        }

        selectedStation = index
        prefs.edit()
            .putInt("last_station", index)
            .apply()

        updatePlayerInformation()
        updateStationCards()
        updatePlayButton()

        val requestId = ++streamRequestId
        showStatus("Подключение: $station")

        if (url.endsWith(".m3u", ignoreCase = true) &&
            !url.endsWith(".m3u8", ignoreCase = true)
        ) {
            Thread {
                var connection: HttpURLConnection? = null
                try {
                    connection = (URL(url).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 10000
                        readTimeout = 10000
                        instanceFollowRedirects = true
                        requestMethod = "GET"
                    }

                    val resolvedUrl = connection.inputStream
                        .bufferedReader()
                        .useLines { lines ->
                            lines.map { it.trim() }
                                .firstOrNull {
                                    it.isNotEmpty() &&
                                        !it.startsWith("#") &&
                                        (it.startsWith("http://") ||
                                            it.startsWith("https://"))
                                }
                        }

                    runOnUiThread {
                        if (requestId != streamRequestId) return@runOnUiThread
                        if (resolvedUrl.isNullOrBlank()) {
                            showStatus("Не удалось получить адрес эфира")
                            return@runOnUiThread
                        }
                        startStationPlayback(currentPlayer, resolvedUrl, station)
                    }
                } catch (error: Throwable) {
                    runOnUiThread {
                        if (requestId == streamRequestId) {
                            reportError("Не удалось получить поток $station", error)
                        }
                    }
                } finally {
                    connection?.disconnect()
                }
            }.start()
        } else {
            startStationPlayback(currentPlayer, url, station)
        }
    }

    private fun startStationPlayback(
        currentPlayer: ExoPlayer,
        url: String,
        station: String
    ) {
        try {
            currentStreamUrl = url
            currentPlayer.stop()
            currentPlayer.setMediaItem(MediaItem.fromUri(url))
            currentPlayer.prepare()
            currentPlayer.play()
            showStatus("Запуск: $station")
        } catch (error: Throwable) {
            reportError("Ошибка запуска станции $station", error)
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
                    val url = currentStreamUrl
                        ?: stationUrls[stations[selectedStation]]
                        ?: return
                    currentPlayer.setMediaItem(MediaItem.fromUri(url))
                    currentPlayer.prepare()
                }

                currentPlayer.play()
                showStatus("Воспроизведение")
            }

            updatePlayButton()
            updateStationCards()

        } catch (error: Throwable) {
            reportError("Ошибка управления эфиром", error)
        }
    }

    private fun updatePlayerInformation() {
        if (!::stationTitle.isInitialized) return

        val station = stations[selectedStation]

        stationTitle.text = station
        stationSubtitle.text = getStationDescription(station)
        stationIcon.text = getStationIcon(station)
        stationIcon.textSize = logoTextSize(station)
        stationIcon.setTextColor(Color.WHITE)
        stationIcon.background = roundedBackground(
            getStationLogoColor(station),
            12
        )
    }

    private fun updatePlayButton() {
        if (::playButton.isInitialized) {
            playButton.text =
                if (player?.isPlaying == true) "Ⅱ" else "▶"
        }
    }

    private fun updateStationCards() {
        stationCards.forEachIndexed { index, card ->
            card.background = stationBackground(
                index == selectedStation
            )

            val indicatorIndex = if (stationTilesEnabled) 3 else 2
            val indicator = card.getChildAt(indicatorIndex) as? TextView

            indicator?.apply {
                text = if (
                    index == selectedStation &&
                    player?.isPlaying == true
                ) "♫" else "›"

                setTextColor(
                    if (index == selectedStation) accentColor
                    else secondaryColor
                )
            }
        }
    }

    private fun createSettingsScreen(): View {
        val scroll = ScrollView(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(13), dp(8), dp(13), dp(20))
        }
        val gridItems = listOf(
            Triple("Анимация", "Плавные эффекты", "animation"),
            Triple("Тема", getThemeDisplayName(), "theme"),
            Triple("Автозапуск", "Последняя станция", "autostart"),
            Triple("Станция", stations[selectedStation], "last"),
            Triple("О приложении", "Пульс Радио · 1.0", "about")
        )
        var index = 0
        while (index < gridItems.size) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            for (column in 0 until 3) {
                val itemIndex = index + column
                val params = LinearLayout.LayoutParams(0, dp(126), 1f).apply {
                    if (column > 0) marginStart = dp(4)
                    if (column < 2) marginEnd = dp(4)
                }
                if (itemIndex < gridItems.size) {
                    val item = gridItems[itemIndex]
                    row.addView(createSettingsTile(item.first, item.second, item.third), params)
                } else {
                    row.addView(View(this), params)
                }
            }
            container.addView(row, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(126)
            ).apply { bottomMargin = dp(8) })
            index += 3
        }
        scroll.addView(container)
        return scroll
    }

    private fun createSettingsTile(title: String, subtitle: String, type: String): LinearLayout {
        val tile = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = stationBackground(false)
            setPadding(dp(5), dp(5), dp(5), dp(4))
            isClickable = type == "theme" || type == "last" || type == "about"
            isFocusable = true
        }
        val symbol = TextView(this).apply {
            text = when (type) {
                "animation" -> "✧"
                "theme" -> "◈"
                "autostart" -> "▶"
                "last" -> "◉"
                else -> "ⓘ"
            }
            textSize = 23f
            setTextColor(accentColor)
            gravity = Gravity.CENTER
        }
        tile.addView(symbol, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(28)
        ))
        val titleView = TextView(this).apply {
            text = title
            textSize = 11f
            setTextColor(textColor)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            maxLines = 2
        }
        tile.addView(titleView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(29)
        ))
        val detailView = TextView(this).apply {
            text = subtitle
            textSize = 8f
            setTextColor(secondaryColor)
            gravity = Gravity.CENTER
            maxLines = 2
        }
        tile.addView(detailView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(23)
        ))

        when (type) {
            "animation" -> {
                val toggle = Switch(this).apply {
                    isChecked = animationEnabled
                    scaleX = 0.78f
                    scaleY = 0.78f
                    setOnCheckedChangeListener { _, enabled ->
                        animationEnabled = enabled
                        prefs.edit().putBoolean("animation", enabled).apply()
                        if (enabled && player?.isPlaying == true && currentTab == 0) startAnimations()
                        else stopAnimations()
                    }
                }
                tile.addView(toggle, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, dp(25)
                ).apply { gravity = Gravity.CENTER })
            }
            "autostart" -> {
                val toggle = Switch(this).apply {
                    isChecked = autoStartEnabled
                    scaleX = 0.78f
                    scaleY = 0.78f
                    setOnCheckedChangeListener { _, enabled ->
                        autoStartEnabled = enabled
                        prefs.edit().putBoolean("auto_start", enabled).apply()
                    }
                }
                tile.addView(toggle, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, dp(25)
                ).apply { gravity = Gravity.CENTER })
            }
            "theme" -> tile.setOnClickListener { showThemeDialog() }
            "last" -> tile.setOnClickListener { showTab(0) }
            "about" -> tile.setOnClickListener {
                Toast.makeText(this, "Пульс Радио · версия 1.0", Toast.LENGTH_SHORT).show()
            }
        }
        return tile
    }

    private fun getThemeDisplayName(): String {
        return when (selectedTheme) {
            "gray" -> "Серая"
            "light_gray" -> "Светло-серая"
            "blue" -> "Синяя"
            "light_blue" -> "Светло-синяя"
            else -> "Синяя"
        }
    }

    private fun sectionTitle(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(accentColor)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(20), 0, dp(10))
        }
    }

    private fun createSwitchSetting(
        title: String,
        description: String,
        initialValue: Boolean,
        onChanged: (Boolean) -> Unit
    ): LinearLayout {

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = stationBackground(false)
            setPadding(dp(14), dp(12), dp(12), dp(12))
        }

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 15f
            setTextColor(textColor)
            setTypeface(null, Typeface.BOLD)
        }

        val descriptionView = TextView(this).apply {
            text = description
            textSize = 11f
            setTextColor(secondaryColor)
            setPadding(0, dp(4), 0, 0)
        }

        column.addView(titleView)
        column.addView(descriptionView)

        row.addView(
            column,
            LinearLayout.LayoutParams(0, dp(48), 1f)
        )

        val switch = Switch(this).apply {
            isChecked = initialValue
            setOnCheckedChangeListener { _, checked ->
                onChanged(checked)
            }
        }

        row.addView(switch)

        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(8)
        }

        return row
    }

    private fun createInfoSetting(
        title: String,
        description: String,
        icon: String
    ): LinearLayout {

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = stationBackground(false)
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }

        val iconView = TextView(this).apply {
            text = icon
            textSize = 24f
            setTextColor(accentColor)
            gravity = Gravity.CENTER
        }

        row.addView(
            iconView,
            LinearLayout.LayoutParams(dp(42), dp(46))
        )

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), 0, 0, 0)
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 15f
            setTextColor(textColor)
            setTypeface(null, Typeface.BOLD)
        }

        val descriptionView = TextView(this).apply {
            text = description
            textSize = 11f
            setTextColor(secondaryColor)
            setPadding(0, dp(4), 0, 0)
        }

        column.addView(titleView)
        column.addView(descriptionView)

        row.addView(
            column,
            LinearLayout.LayoutParams(0, dp(48), 1f)
        )

        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(8)
        }

        return row
    }

    private fun startAnimations() {
        if (!animationEnabled || currentTab != 0) return

        stopAnimations()

        visualizerAnimator = ValueAnimator.ofFloat(0f, 6.28f).apply {
            duration = 1100
            repeatCount = ValueAnimator.INFINITE

            addUpdateListener { animator ->
                val phase = animator.animatedValue as Float

                visualizerBars.forEachIndexed { index, bar ->
                    val wave = kotlin.math.abs(
                        kotlin.math.sin(phase + index * 0.55)
                    )

                    val params = bar.layoutParams
                    params.height = dp((1 + wave * 5).toInt())
                    bar.layoutParams = params
                }
            }

            start()
        }

        colorAnimator = ValueAnimator.ofObject(
            ArgbEvaluator(),
            Color.rgb(8, 39, 65),
            Color.rgb(10, 65, 87),
            Color.rgb(22, 38, 72)
        ).apply {
            duration = 3500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE

            addUpdateListener {
                if (::playerCard.isInitialized && currentTab == 0) {
                    val color = it.animatedValue as Int
                    playerCard.background = playerBackground(
                        color,
                        Color.rgb(7, 20, 38)
                    )
                }
            }

            start()
        }
    }

    private fun stopAnimations() {
        visualizerAnimator?.cancel()
        visualizerAnimator = null

        colorAnimator?.cancel()
        colorAnimator = null

        visualizerBars.forEach { bar ->
            val params = bar.layoutParams
            params.height = dp(2)
            bar.layoutParams = params
        }
    }

    private fun stationBackground(active: Boolean): GradientDrawable {
        val first: Int
        val second: Int
        val third: Int

        when (selectedTheme) {
            "light_gray" -> {
                first = if (active) Color.rgb(224, 231, 238) else panelColor
                second = if (active) Color.rgb(210, 221, 232) else Color.rgb(225, 229, 234)
                third = if (active) Color.rgb(215, 225, 235) else Color.rgb(218, 223, 229)
            }
            "light_blue" -> {
                first = if (active) Color.rgb(185, 216, 240) else panelColor
                second = if (active) Color.rgb(171, 207, 235) else Color.rgb(207, 226, 242)
                third = if (active) Color.rgb(195, 222, 242) else Color.rgb(200, 220, 238)
            }
            "gray" -> {
                first = if (active) Color.rgb(76, 84, 94) else panelColor
                second = if (active) Color.rgb(62, 69, 78) else Color.rgb(49, 54, 61)
                third = if (active) Color.rgb(70, 77, 86) else Color.rgb(54, 60, 68)
            }
            else -> {
                first = if (active) Color.rgb(13, 61, 88) else Color.rgb(24, 43, 61)
                second = if (active) Color.rgb(10, 34, 55) else Color.rgb(12, 27, 43)
                third = if (active) Color.rgb(12, 48, 69) else Color.rgb(19, 37, 54)
            }
        }

        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(first, second, third)
        ).apply {
            cornerRadius = dp(17).toFloat()
            setStroke(
                dp(if (active) 2 else 1),
                if (active) accentColor else secondaryColor
            )
        }
    }

    private fun playerBackground(
        first: Int,
        second: Int
    ): GradientDrawable {
        val endColor = when (selectedTheme) {
            "light_gray" -> Color.rgb(226, 230, 235)
            "light_blue" -> Color.rgb(205, 226, 243)
            "gray" -> Color.rgb(48, 53, 60)
            else -> Color.rgb(9, 24, 43)
        }

        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(first, second, endColor)
        ).apply {
            cornerRadius = dp(25).toFloat()
            setStroke(dp(1), accentColor)
        }
    }

    private fun roundAccentBackground(): GradientDrawable {
        val darkerAccent = when (selectedTheme) {
            "gray" -> Color.rgb(105, 115, 127)
            "light_gray" -> Color.rgb(48, 83, 120)
            "light_blue" -> Color.rgb(35, 105, 165)
            else -> Color.rgb(0, 103, 180)
        }

        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(accentColor, darkerAccent)
        ).apply {
            shape = GradientDrawable.OVAL
            setStroke(dp(2), secondaryColor)
        }
    }

    private fun roundedBackground(
        color: Int,
        radius: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(1), secondaryColor)
        }
    }

    private fun showStatus(message: String) {
        if (::statusText.isInitialized && currentTab == 0) {
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
                    (details + "\n" +
                        Log.getStackTraceString(error)).toByteArray()
                )
            }
        } catch (_: Exception) {
        }
    }

    private fun initializePlayer() {
        try {
            player = ExoPlayer.Builder(this).build()

            player?.addListener(object : Player.Listener {

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updatePlayButton()
                    updateStationCards()

                    if (currentTab == 0) {
                        if (isPlaying && animationEnabled) {
                            startAnimations()
                        } else {
                            stopAnimations()
                        }
                    }
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
                    updateStationCards()
                }

                override fun onPlayerError(error: PlaybackException) {
                    reportError("Ошибка воспроизведения", error)
                    stopAnimations()
                    updateStationCards()
                }
            })

        } catch (error: Throwable) {
            reportError("Ошибка создания проигрывателя", error)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        stopAnimations()

        try {
            player?.release()
            player = null
        } catch (error: Throwable) {
            Log.e("PulsRadio", "Ошибка освобождения проигрывателя", error)
        }

        super.onDestroy()
    }
}
