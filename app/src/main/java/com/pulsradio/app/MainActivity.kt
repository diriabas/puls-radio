
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
        "Русское Радио"
    )

    private val stationUrls = mapOf(
        "Европа Плюс" to
            "https://hls-01-regions.emgsound.ru/11_msk/96/playlist.m3u8",

        "Радио Вера" to
            "https://radiovera.hostingradio.ru:8007/radiovera_128",

        "Коран — перевод на русский" to
            "https://backup.qurango.net/radio/translation_quran_Russia",

        "DFM" to
            "https://dfm.hostingradio.ru/dfm96.aacp",

        "Like FM" to
            "https://hls-01-gpm.hostingradio.ru/likefm495/playlist.m3u8",

        "Русское Радио" to
            "https://rusradio.hostingradio.ru/rusradio96.aacp"
    )

    private val prefs by lazy {
        getSharedPreferences("puls_radio_settings", Context.MODE_PRIVATE)
    }

    private var selectedStation = 0
    private var player: ExoPlayer? = null
    private var currentTab = 0

    private var animationEnabled = true
    private var autoStartEnabled = false

    private lateinit var contentFrame: FrameLayout
    private lateinit var stationTitle: TextView
    private lateinit var stationSubtitle: TextView
    private lateinit var stationIcon: TextView
    private lateinit var playButton: TextView
    private lateinit var statusText: TextView
    private lateinit var playerCard: LinearLayout
    private lateinit var radioTab: TextView
    private lateinit var settingsTab: TextView

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
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(8), dp(18), dp(8))
        }

        val logo = TextView(this).apply {
            text = "〰"
            textSize = 34f
            setTextColor(accentColor)
            gravity = Gravity.CENTER
        }

        val appName = TextView(this).apply {
            text = "ПУЛЬС РАДИО"
            textSize = 23f
            setTextColor(textColor)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
            letterSpacing = 0.06f
        }

        header.addView(
            logo,
            LinearLayout.LayoutParams(dp(48), dp(48))
        )

        header.addView(
            appName,
            LinearLayout.LayoutParams(0, dp(48), 1f)
        )

        root.addView(header)

        contentFrame = FrameLayout(this)

        root.addView(
            contentFrame,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val bottomBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(5), dp(12), dp(5))
            background = roundedBackground(
                Color.rgb(12, 26, 42),
                18
            )
        }

        radioTab = createTabButton("◉", "РАДИО")
        settingsTab = createTabButton("⚙", "НАСТРОЙКИ")

        radioTab.setOnClickListener {
            showTab(0)
        }

        settingsTab.setOnClickListener {
            showTab(1)
        }

        bottomBar.addView(
            radioTab,
            LinearLayout.LayoutParams(0, dp(56), 1f)
        )

        bottomBar.addView(
            settingsTab,
            LinearLayout.LayoutParams(0, dp(56), 1f)
        )

        root.addView(
            bottomBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(66)
            )
        )

        setContentView(root)

        // Учитываем системные панели Android.
        // Нижнее меню будет находиться выше панели навигации телефона.
        root.setOnApplyWindowInsetsListener { view, insets ->

            val topInset: Int
            val bottomInset: Int

            if (Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(
                    WindowInsets.Type.systemBars()
                )
                topInset = bars.top
                bottomInset = bars.bottom
            } else {
                @Suppress("DEPRECATION")
                run {
                    topInset = insets.systemWindowInsetTop
                    bottomInset = insets.systemWindowInsetBottom
                }
            }

            view.setPadding(
                0,
                topInset,
                0,
                bottomInset
            )

            insets
        }

        root.requestApplyInsets()
    }

    private fun createTabButton(
        symbol: String,
        label: String
    ): TextView {
        return TextView(this).apply {
            text = "$symbol\n$label"
            textSize = 12f
            setTextColor(secondaryColor)
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            isClickable = true
            isFocusable = true
        }
    }

    private fun showTab(tab: Int) {
        currentTab = tab
        stopAnimations()

        contentFrame.removeAllViews()

        if (tab == 0) {
            contentFrame.addView(createRadioScreen())
            updatePlayerInformation()
            updateStationCards()
            updatePlayButton()

            if (player?.isPlaying == true && animationEnabled) {
                startAnimations()
            }
        } else {
            contentFrame.addView(createSettingsScreen())
        }

        radioTab.setTextColor(
            if (tab == 0) accentColor else secondaryColor
        )

        settingsTab.setTextColor(
            if (tab == 1) accentColor else secondaryColor
        )
    }

    private fun createRadioScreen(): View {
        stationCards.clear()
        visualizerBars.clear()

        val scroll = ScrollView(this).apply {
            clipToPadding = false
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(5), dp(16), dp(18))
        }

        playerCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = playerBackground(
                Color.rgb(8, 48, 77),
                Color.rgb(5, 19, 38)
            )
            setPadding(dp(18), dp(20), dp(18), dp(20))
        }

        val liveLabel = TextView(this).apply {
            text = "●   ПРЯМОЙ ЭФИР"
            textSize = 12f
            setTextColor(Color.rgb(85, 225, 255))
            gravity = Gravity.CENTER
            letterSpacing = 0.16f
        }

        playerCard.addView(liveLabel)

        // Большая декоративная область с ореолом и значком станции.
        val artwork = FrameLayout(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(12, 69, 100),
                    Color.rgb(9, 36, 67),
                    Color.rgb(15, 25, 55)
                )
            ).apply {
                cornerRadius = dp(90).toFloat()
                setStroke(dp(1), Color.rgb(57, 159, 202))
            }
        }

        val glow = View(this).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(16, 105, 145))
                setStroke(dp(2), Color.rgb(74, 205, 255))
            }
            alpha = 0.48f
        }

        artwork.addView(
            glow,
            FrameLayout.LayoutParams(dp(112), dp(112)).apply {
                gravity = Gravity.CENTER
            }
        )

        stationIcon = TextView(this).apply {
            text = getStationIcon(stations[selectedStation])
            textSize = 57f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        artwork.addView(
            stationIcon,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        playerCard.addView(
            artwork,
            LinearLayout.LayoutParams(dp(154), dp(154)).apply {
                gravity = Gravity.CENTER
                topMargin = dp(17)
                bottomMargin = dp(10)
            }
        )

        stationTitle = TextView(this).apply {
            textSize = 25f
            setTextColor(textColor)
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(8), 0, dp(4))
            maxLines = 2
        }

        stationSubtitle = TextView(this).apply {
            textSize = 13f
            setTextColor(secondaryColor)
            gravity = Gravity.CENTER
        }

        playerCard.addView(stationTitle)
        playerCard.addView(stationSubtitle)

        // Визуализатор
        val visualizer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(17), 0, dp(17))
        }

        repeat(23) {
            val bar = View(this).apply {
                background = roundedBackground(accentColor, 5)
            }

            visualizerBars.add(bar)

            visualizer.addView(
                bar,
                LinearLayout.LayoutParams(dp(5), dp(9)).apply {
                    marginStart = dp(2)
                    marginEnd = dp(2)
                }
            )
        }

        playerCard.addView(visualizer)

        playButton = TextView(this).apply {
            text = "▶"
            textSize = 27f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = roundAccentBackground()
            setOnClickListener {
                togglePlayback()
            }
        }

        playerCard.addView(
            playButton,
            LinearLayout.LayoutParams(dp(70), dp(70)).apply {
                gravity = Gravity.CENTER
            }
        )

        statusText = TextView(this).apply {
            textSize = 12f
            setTextColor(secondaryColor)
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(2))
            text = "Выбери радиостанцию"
        }

        playerCard.addView(statusText)

        container.addView(
            playerCard,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(20)
            }
        )

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

        val count = TextView(this).apply {
            text = "${stations.size} станций"
            textSize = 12f
            setTextColor(secondaryColor)
            gravity = Gravity.CENTER
            background = roundedBackground(panelColor, 25)
            setPadding(dp(12), dp(7), dp(12), dp(7))
        }

        titleRow.addView(
            listTitle,
            LinearLayout.LayoutParams(0, dp(40), 1f)
        )

        titleRow.addView(count)
        container.addView(titleRow)

        stations.forEachIndexed { index, station ->
            val card = createStationCard(index, station)
            stationCards.add(card)

            container.addView(
                card,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(76)
                ).apply {
                    bottomMargin = dp(8)
                }
            )
        }

        scroll.addView(container)
        return scroll
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
            textSize = 24f
            setTextColor(getStationIconColor(station))
            gravity = Gravity.CENTER
            background = roundedBackground(
                Color.rgb(11, 42, 62),
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

    private fun getStationIcon(station: String): String {
        return when (station) {
            "Европа Плюс" -> "♫"
            "Радио Вера" -> "✧"
            "Коран — перевод на русский" -> "☪"
            "DFM" -> "D"
            "Like FM" -> "L"
            "Русское Радио" -> "R"
            else -> "♪"
        }
    }

    private fun getStationIconColor(station: String): Int {
        return when (station) {
            "Радио Вера" -> Color.rgb(120, 195, 255)
            "Коран — перевод на русский" -> Color.rgb(76, 222, 164)
            else -> accentColor
        }
    }

    private fun getStationDescription(station: String): String {
        return when (station) {
            "Коран — перевод на русский" -> "Чтение Корана"
            "Радио Вера" -> "Духовное радио"
            "Европа Плюс" -> "Популярная музыка"
            "Русское Радио" -> "Русская музыка"
            "Like FM" -> "Современные хиты"
            "DFM" -> "Танцевальная музыка"
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
        val station = stations[index]
        val url = stationUrls[station] ?: return

        try {
            selectedStation = index

            currentPlayer.stop()
            currentPlayer.setMediaItem(MediaItem.fromUri(url))
            currentPlayer.prepare()
            currentPlayer.play()

            prefs.edit()
                .putInt("last_station", index)
                .apply()

            updatePlayerInformation()
            updateStationCards()
            updatePlayButton()

            showStatus("Запуск: $station")

        } catch (error: Throwable) {
            reportError("Ошибка запуска станции", error)
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
        stationIcon.setTextColor(getStationIconColor(station))
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

            val indicator = card.getChildAt(2) as? TextView

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
            setPadding(dp(18), dp(12), dp(18), dp(24))
        }

        val title = TextView(this).apply {
            text = "НАСТРОЙКИ"
            textSize = 23f
            setTextColor(textColor)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(8))
        }

        val subtitle = TextView(this).apply {
            text = "Настрой приложение под себя"
            textSize = 13f
            setTextColor(secondaryColor)
            setPadding(0, 0, 0, dp(20))
        }

        container.addView(title)
        container.addView(subtitle)
        container.addView(sectionTitle("ВНЕШНИЙ ВИД"))

        container.addView(
            createSwitchSetting(
                "Плавная анимация",
                "Переливы карточки и визуализатор",
                animationEnabled
            ) { enabled ->
                animationEnabled = enabled

                prefs.edit()
                    .putBoolean("animation", enabled)
                    .apply()

                if (currentTab == 0) {
                    if (enabled && player?.isPlaying == true) {
                        startAnimations()
                    } else {
                        stopAnimations()
                    }
                }
            }
        )

        val themeSetting = createInfoSetting(
            "Тема приложения",
            getThemeDisplayName(),
            "◈"
        ).apply {
            isClickable = true
            isFocusable = true
            setOnClickListener {
                showThemeDialog()
            }
        }
        container.addView(themeSetting)

        container.addView(sectionTitle("ВОСПРОИЗВЕДЕНИЕ"))

        container.addView(
            createSwitchSetting(
                "Автозапуск последней станции",
                "Запускать радио при открытии приложения",
                autoStartEnabled
            ) { enabled ->
                autoStartEnabled = enabled

                prefs.edit()
                    .putBoolean("auto_start", enabled)
                    .apply()
            }
        )

        container.addView(
            createInfoSetting(
                "Последняя станция",
                stations[selectedStation],
                "◉"
            )
        )

        container.addView(sectionTitle("О ПРИЛОЖЕНИИ"))

        container.addView(
            createInfoSetting(
                "Пульс Радио",
                "Версия приложения 1.0",
                "〰"
            )
        )

        val footer = TextView(this).apply {
            text = "Пульс Радио\nТвоя музыка и любимые эфиры"
            textSize = 12f
            setTextColor(secondaryColor)
            gravity = Gravity.CENTER
            setPadding(0, dp(28), 0, dp(10))
        }

        container.addView(footer)
        scroll.addView(container)

        return scroll
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
                    params.height = dp((8 + wave * 30).toInt())
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
            params.height = dp(9)
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
