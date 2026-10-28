package com.openchat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
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

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import java.util.Locale

private data class Country(
    val region: String,
    val name: String,
    val callingCode: Int
) {
    override fun toString(): String = "$name (+$callingCode)"
}

class MainActivity : Activity() {

    private lateinit var countrySpinner: Spinner
    private lateinit var phoneInput: EditText

    private val phoneUtil: PhoneNumberUtil =
        PhoneNumberUtil.getInstance()

    private lateinit var countries: List<Country>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        countries = phoneUtil.getSupportedRegions()
            .filter { it.length == 2 }
            .mapNotNull { region ->
                val name = Locale("", region).displayCountry
                val callingCode =
                    phoneUtil.getCountryCodeForRegion(region)

                if (name.isBlank() || callingCode == 0) {
                    null
                } else {
                    Country(region, name, callingCode)
                }
            }
            .distinctBy { it.region }
            .sortedBy { it.name.lowercase(Locale.ROOT) }

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
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
        }

        val countryLabel = TextView(this).apply {
            text = "Country / region"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.DKGRAY)
        }

        countrySpinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_item,
                countries
            ).also {
                it.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
                )
            }

            val egyptIndex =
                countries.indexOfFirst { it.region == "EG" }

            if (egyptIndex >= 0) {
                setSelection(egyptIndex)
            }
        }

        val phoneLabel = TextView(this).apply {
            text = "Phone number"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.DKGRAY)
        }

        phoneInput = EditText(this).apply {
            hint = "Enter phone number"
            textSize = 17f
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            isSingleLine = true
            imeOptions = EditorInfo.IME_ACTION_GO
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        val openButton = Button(this).apply {
            text = "Open WhatsApp"
            isAllCaps = false
            setOnClickListener { openWhatsApp() }
        }

        val footer = TextView(this).apply {
            text = "Free · Open source · No ads"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.GRAY)
        }

        fun addView(view: View, bottomMargin: Int = 12) {
            root.addView(
                view,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.bottomMargin = dp(bottomMargin)
                }
            )
        }

        addView(title, 6)
        addView(subtitle, 32)
        addView(countryLabel, 4)
        addView(countrySpinner, 20)
        addView(phoneLabel, 4)
        addView(phoneInput, 20)
        addView(openButton, 24)
        addView(footer, 0)

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

        val selectedCountry =
            countrySpinner.selectedItem as? Country

        if (selectedCountry == null) {
            showMessage("Select a country or region")
            return
        }

        try {
            // libphonenumber handles local and international formats.
            // The selected region is used for numbers without a country prefix.
            val parsedNumber = phoneUtil.parse(
                rawNumber,
                selectedCountry.region
            )

            if (!phoneUtil.isValidNumber(parsedNumber)) {
                phoneInput.error = "Check the phone number"
                return
            }

            val e164 = phoneUtil.format(
                parsedNumber,
                PhoneNumberFormat.E164
            )

            // wa.me expects the international number without "+".
            val digitsOnly = e164.removePrefix("+")

            val inputMethodManager =
                getSystemService(
                    Context.INPUT_METHOD_SERVICE
                ) as InputMethodManager

            inputMethodManager.hideSoftInputFromWindow(
                phoneInput.windowToken,
                0
            )

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://wa.me/$digitsOnly")
            )

            startActivity(intent)

        } catch (_: NumberParseException) {
            phoneInput.error = "Enter a valid phone number"
        } catch (_: Exception) {
            showMessage("Unable to open WhatsApp or a browser")
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}
