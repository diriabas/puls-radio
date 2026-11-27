
package com.pulsradio.app

import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var root: LinearLayout
    private lateinit var stationTitle: TextView

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

    private val themes = mapOf(
        "Серая" to intArrayOf(
            0xFF454A50.toInt(),
            0xFF60666E.toInt(),
            Color.WHITE
        ),
        "Светло-серая" to intArrayOf(
            0xFFE7E9ED.toInt(),
            Color.WHITE,
            0xFF30343A.toInt()
        ),
        "Синяя" to intArrayOf(
            0xFF174A91.toInt(),
            0xFF2865B2.toInt(),
            Color.WHITE
        ),
        "Светло-синяя" to intArrayOf(
            0xFFDCEEFF.toInt(),
            Color.WHITE,
            0xFF174A91.toInt()
        )
    )

    private var currentTheme = "Серая"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentTheme = getSharedPreferences(
            "puls_radio",
            MODE_PRIVATE
        ).getString("theme", "Серая") ?: "Серая"

        createInterface()
    }

    private fun createInterface() {
        val colors = themes[currentTheme]!!

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
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
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(colors[2])
            setPadding(8, 12, 8, 12)
        }

        content.addView(stationTitle)

        val themeButton = Button(this).apply {
            text = "ОФОРМЛЕНИЕ"
            setOnClickListener {
                showThemes()
            }
        }

        content.addView(themeButton)

        val separator = TextView(this).apply {
            text = "РАДИОСТАНЦИИ"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(colors[2])
            setPadding(8, 20, 8, 12)
        }

        content.addView(separator)

        stations.forEach { station ->
            val button = Button(this).apply {
                text = station
                textSize = 16f

                setOnClickListener {
                    stationTitle.text = station
                }
            }

            content.addView(button)
        }

        root.addView(
            content,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        scroll.addView(root)
        setContentView(scroll)

        window.statusBarColor = colors[0]
        window.navigationBarColor = colors[0]
    }

    private fun showThemes() {
        val names = themes.keys.toTypedArray()

        android.app.AlertDialog.Builder(this)
            .setTitle("Выберите тему")
            .setItems(names) { _, which ->
                currentTheme = names[which]

                getSharedPreferences(
                    "puls_radio",
                    MODE_PRIVATE
                ).edit()
                    .putString("theme", currentTheme)
                    .apply()

                createInterface()
            }
            .show()
    }
}
