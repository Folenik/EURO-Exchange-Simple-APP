package com.danielhenel.basicapiedited

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URL

class MainActivity : AppCompatActivity() {
    lateinit var edittext1: EditText
    lateinit var textview2: TextView
    lateinit var spinner2: Spinner
    lateinit var adapter2: ArrayAdapter<String>
    lateinit var button: Button
    lateinit var progressBar: ProgressBar
    lateinit var currencyhistory: TextView
    lateinit var sharedPref: SharedPreferences

    var currencyConvert: Double = 0.0
    var baseCurrency: String = ""
    var currencies = arrayOf("PLN", "USD", "GBP", "EUR", "AUD", "CAD", "CHF", "CNH", "SEK", "NZD")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        edittext1 = findViewById(R.id.currency1)
        textview2 = findViewById(R.id.currency2)
        spinner2 = findViewById(R.id.spinner2)
        button = findViewById(R.id.button)

        adapter2 =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, currencies)
        spinner2.setAdapter(adapter2)

        button.setOnClickListener() {
            DownloadCurrency().execute()
        }

        progressBar = findViewById(R.id.progressBar)
        progressBar.setVisibility(View.INVISIBLE)

        checkPermission()

        currencyhistory = findViewById(R.id.currencyhistory)
        sharedPref = getSharedPreferences("Preferencje", 0)
    }


    inner class DownloadCurrency() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()

            progressBar.setVisibility(View.VISIBLE)
            button.visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            try {
                response =
                    URL("http://data.fixer.io/api/latest?access_key=d8c0ea979334afd2932867824aeb8fcc&symbols=PLN,USD,GBP,EUR,AUD,CAD,CHF,CNH,SEK,NZD&format=1")
                        .readText(Charsets.UTF_8)
                Log.i("response: ", response)
            } catch (e: Exception) {
                response = null
                Log.i("response error: ", e.toString())
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            try {
                val jsonObj = JSONObject(result)

                baseCurrency =
                    jsonObj.getJSONObject("rates").getString(spinner2.selectedItem.toString())
                convert(baseCurrency.toDouble())

            } catch (e: java.lang.Exception) {
                Toast.makeText(applicationContext, "Coś poszło nie tak", LENGTH_LONG).show()
                Log.i("Błąd: ", e.toString())
            }

            progressBar.setVisibility(View.GONE)
            button.visibility = View.VISIBLE
        }

        fun convert(currency: Double) {
            if (edittext1.text != null) {
                currencyConvert = currency * edittext1.text.toString().toDouble()

                val currencyConverted = BigDecimal(currencyConvert).setScale(2,RoundingMode.HALF_EVEN)
                textview2.text = currencyConverted.toString()
            }

            val historia: String = "Zamieniono " + edittext1.text + "EUR na " + textview2.text + spinner2.selectedItem.toString()
            sharedPref.edit().putString("zamiany",historia).commit()

            currencyhistory.text = sharedPref.getString("zamiany","null")
        }
    }

    fun checkPermission() {
        val builder = AlertDialog.Builder(this@MainActivity)

        builder.setTitle("Dostęp do internetu")
        builder.setMessage("Ta aplikacja chce użyć internetu. Czy wyrażasz zgodę?")
        builder.setPositiveButton("TAK"){dialog, which ->
            Toast.makeText(applicationContext,"Zgoda udzielona.",Toast.LENGTH_SHORT).show()

        }
        builder.setNegativeButton("NIE"){dialog,which ->
            finishAffinity()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}

