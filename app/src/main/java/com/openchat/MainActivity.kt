package com.openchat

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var countryCode: Spinner
    private lateinit var phoneInput: EditText

    private val countries = listOf(
        "+20 Egypt",
        "+1 USA/Canada",
        "+44 UK",
        "+971 UAE",
        "+966 Saudi Arabia"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "OpenChat"
            textSize = 32f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(30, 30, 30))
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Message anyone on WhatsApp"
            textSize = 16f
            setTextColor(Color.rgb(100, 100, 100))
            gravity = Gravity.CENTER
        }

        val instruction = TextView(this).apply {
            text = "Country code"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(60, 60, 60))
        }

        countryCode = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                countries
            )
            setSelection(0)
        }

        val phoneLabel = TextView(this).apply {
            text = "Phone number"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(60, 60, 60))
        }

        phoneInput = EditText(this).apply {
            hint = "Enter number without country code"
            textSize = 17f
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            isSingleLine = true
            imeOptions = EditorInfo.IME_ACTION_GO
            setPadding(dp(12), dp(12), dp(12), dp(12))
            contentDescription = "Phone number"
        }

        val openButton = Button(this).apply {
            text = "Open WhatsApp"
            textSize = 16f
            isAllCaps = false
            setOnClickListener { openWhatsApp() }
        }

        val footer = TextView(this).apply {
            text = "Free · Open source · No ads"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(120, 120, 120))
        }

        fun addView(
            view: View,
            topMargin: Int = 0,
            bottomMargin: Int = 12
        ) {
            root.addView(
                view,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.topMargin = dp(topMargin)
                    this.bottomMargin = dp(bottomMargin)
                }
            )
        }

        addView(title, bottomMargin = 6)
        addView(subtitle, bottomMargin = 32)
        addView(instruction, bottomMargin = 4)
        addView(countryCode, bottomMargin = 20)
        addView(phoneLabel, bottomMargin = 4)
        addView(phoneInput, bottomMargin = 20)
        addView(openButton, bottomMargin = 24)
        addView(footer, bottomMargin = 0)

        setContentView(root)

        phoneInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                openWhatsApp()
                true
            } else {
                false
            }
        }
    }

    private fun openWhatsApp() {
        val rawNumber = phoneInput.text.toString().trim()

        if (rawNumber.isEmpty()) {
            phoneInput.error = "Enter a phone number"
            return
        }

        // Accept digits, spaces, parentheses, hyphens and a leading plus.
        if (!rawNumber.matches(Regex("^\\+?[0-9\\s()\\-]+$"))) {
            phoneInput.error = "Enter a valid phone number"
            return
        }

        val digits = rawNumber.filter { it in '0'..'9' }

        if (digits.length !in 7..15) {
            phoneInput.error = "Check the phone number"
            return
        }

        val selectedCode = countryCode.selectedItem
            .toString()
            .substringBefore(" ")
            .removePrefix("+")

        val normalizedNumber = when {
            rawNumber.startsWith("+") -> digits

            // A number beginning with the selected country code is
            // already international, so don't prepend the code again.
            digits.startsWith(selectedCode) &&
                digits.length > selectedCode.length -> digits

            // Egyptian local numbers commonly start with 0.
            selectedCode == "20" && digits.startsWith("0") ->
                selectedCode + digits.drop(1)

            else -> selectedCode + digits
        }

        if (normalizedNumber.length !in 7..15) {
            phoneInput.error = "Check the phone number"
            return
        }

        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager.hideSoftInputFromWindow(phoneInput.windowToken, 0)

        val uri = Uri.parse("https://wa.me/$normalizedNumber")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(
                this,
                "Unable to open WhatsApp",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
