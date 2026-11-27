
package com.pulsradio.app

import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

data class Station(
    val name: String,
    val url: String? = null
)

class MainActivity : AppCompatActivity() {

    private lateinit var player: RadioPlayer
    private lateinit var stationTitle: TextView
    private lateinit var statusText: TextView

    private val stations = listOf(
        Station("Европа Плюс", "https://hls-02-europaplus.emgsound.ru/11/playlist.m3u8"),
        Station("Радио Рекорд", "https://radiorecord.hostingradio.ru/rr_main96.aacp"),
        Station("DFM", "https://dfm.hostingradio.ru/dfm96.aacp"),
        Station("Радио ENERGY", "https://pub0202.101.ru:8000/stream/air/aac/64/102"),
        Station("Авторадио"),
        Station("Новое Радио"),
        Station("Love Radio"),
        Station("Русское Радио", "https://rusradio.hostingradio.ru/rusradio96.aacp"),
        Station("Хит FM"),
        Station("Like FM", "https://hls-01-gpm.hostingradio.ru/likefm495/playlist.m3u8"),
        Station("Радио Maximum"),
        Station("Наше Радио", "https://nashe1.hostingradio.ru/nashe-128.mp3"),
        Station("Rock FM"),
        Station("Relax FM", "https://hls-01-gpm.hostingradio.ru/relaxfm7/playlist.m3u8"),
        Station("Монте-Карло", "https://montecarlo.hostingradio.ru/montecarlo96.aacp"),
        Station("Радио Джаз"),
        Station("Kiss FM"),
        Station("NRJ"),
        Station("Radio ONE"),
        Station("Мегаполис FM"),
        Station("Радио Маяк", "https://icecast-vgtrk.cdnvideo.ru/mayakfm_aac_64kbps"),
        Station("Радио России"),
        Station("Вести FM", "https://icecast-vgtrk.cdnvideo.ru/vestifm_aac_32kbps"),
        Station("Комсомольская правда", "https://kpradio.hostingradio.ru:8000/64"),
        Station("Ретро FM", "https://hls-01-retro.emgsound.ru/12/128/playlist.m3u8"),
        Station("Дорожное радио", "http://emgregion.hostingradio.ru:8064/moscow.dorognoe.mp3")
    )

    private val themes = mapOf(
        "Серая" to intArrayOf(0xFF454A50.toInt(), 0xFF60666E.toInt(), Color.WHITE),
        "Светло-серая" to intArrayOf(0xFFE7E9ED.toInt(), Color.WHITE, 0xFF30343A.toInt()),
        "Синяя" to intArrayOf(0xFF174A91.toInt(), 0xFF2865B2.toInt(), Color.WHITE),
        "Светло-синяя" to intArrayOf(0xFFDCEEFF.toInt(), Color.WHITE, 0xFF174A91.toInt())
    )

    private var currentTheme = "Серая"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        player = RadioPlayer(this)

        currentTheme = getSharedPreferences("puls_radio", MODE_PRIVATE)
            .getString("theme", "Серая") ?: "Серая"

        createInterface()
    }

    private fun createInterface() {
        val colors = themes[currentTheme]!!

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(colors[0])
        }

        val scroll = ScrollView(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val title = TextView(this).apply {
            text = "ПУЛЬС РАДИО"
            textSize = 29f
            gravity = Gravity.CENTER
            setTextColor(colors[2])
            setPadding(8, 20, 8, 20)
        }

        content.addView(title)

        stationTitle = TextView(this).apply {
            text = "Выберите радиостанцию"
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(colors[2])
            setPadding(8, 15, 8, 15)
        }

        content.addView(stationTitle)

        statusText = TextView(this).apply {
            text = "Радио остановлено"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(colors[2])
            setPadding(8, 8, 8, 12)
        }

        content.addView(statusText)

        val controls = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
        }

        val playButton = Button(this).apply {
            text = "▶ PLAY"
            setOnClickListener {
                val selected = stations.firstOrNull {
                    it.name == stationTitle.text
                }

                if (selected?.url != null) {
                    try {
                        player.play(selected.url)
                        statusText.text = "Подключение..."
                    } catch (e: Exception) {
                        statusText.text = "Ошибка запуска"
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Поток этой станции добавим следующим этапом",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val pauseButton = Button(this).apply {
            text = "Ⅱ PAUSE"
            setOnClickListener {
                player.pause()
                statusText.text = "Пауза"
            }
        }

        controls.addView(playButton)
        controls.addView(pauseButton)
        content.addView(controls)

        val themeButton = Button(this).apply {
            text = "ОФОРМЛЕНИЕ"
            setOnClickListener { showThemes() }
        }

        content.addView(themeButton)

        val heading = TextView(this).apply {
            text = "РАДИОСТАНЦИИ"
            textSize = 19f
            gravity = Gravity.CENTER
            setTextColor(colors[2])
            setPadding(8, 20, 8, 12)
        }

        content.addView(heading)

        stations.forEach { station ->
            val button = Button(this).apply {
                text = station.name
                textSize = 16f

                setOnClickListener {
                    stationTitle.text = station.name

                    if (station.url != null) {
                        try {
                            player.play(station.url)
                            statusText.text = "Подключение..."
                        } catch (e: Exception) {
                            statusText.text = "Ошибка запуска"
                        }
                    } else {
                        statusText.text = "Поток пока не добавлен"
                    }
                }
            }

            content.addView(button)
        }

        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)

        window.statusBarColor = colors[0]
        window.navigationBarColor = colors[0]
    }

    private fun showThemes() {
        val names = themes.keys.toTypedArray()

        android.app.AlertDialog.Builder(this)
            .setTitle("Выберите тему")
            .setItems(names) { _, which ->
                currentTheme = names[which]

                getSharedPreferences("puls_radio", MODE_PRIVATE)
                    .edit()
                    .putString("theme", currentTheme)
                    .apply()

                createInterface()
            }
            .show()
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}
