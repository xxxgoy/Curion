package com.example.konversimatauang

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class ConvertFragment : Fragment() {

    var baseCurrency = "USD"
    var convertedToCurrency = "IDR"
    var conversionRate = 0f

    private lateinit var editFromCurrency: EditText
    private lateinit var editToCurrency: EditText
    private lateinit var btnSwap: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_convert, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editFromCurrency = view.findViewById(R.id.editFromCurrency)
        editToCurrency = view.findViewById(R.id.editToCurrency)
        btnSwap = view.findViewById(R.id.swap_convert)
        spinnerSetup(view)
        textChangedStuff()
        setupSwapButton()
    }

    private fun textChangedStuff() {
        editFromCurrency.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.isNotEmpty() && s.isNotBlank()) {
                    getApiResult()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("Main", "Before Text Changed")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("Main", "On Text Changed")
            }
        })
    }

    private fun getApiResult() {
        if (editFromCurrency.text.isNotEmpty() && editFromCurrency.text.isNotBlank()) {
            val api = "https://v6.exchangerate-api.com/v6/424b60014bc4901886ddb931/latest/$baseCurrency"

            if (baseCurrency == convertedToCurrency) {
                Toast.makeText(context, "Please pick a currency to convert", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val apiResult = URL(api).readText()
                        val jsonObject = JSONObject(apiResult)
                        if (jsonObject.getString("result") == "success") {
                            val conversionRates = jsonObject.getJSONObject("conversion_rates")
                            if (conversionRates.has(convertedToCurrency)) {
                                conversionRate = conversionRates.getString(convertedToCurrency).toFloat()

                                withContext(Dispatchers.Main) {
                                    val text = ((editFromCurrency.text.toString().toFloat()) * conversionRate).toString()
                                    editToCurrency.setText(text)
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Currency not found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "API request failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Please Input Amount", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun spinnerSetup(view: View) {
        val firstSpinner: Spinner = view.findViewById(R.id.spFromCurrency)
        val secondSpinner: Spinner = view.findViewById(R.id.spToCurrency)

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.firstCurrency_code,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            firstSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.secondCurrency_code,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            secondSpinner.adapter = adapter
        }

        firstSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                baseCurrency = parent?.getItemAtPosition(position).toString()
                getApiResult()
            }
        }

        secondSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                convertedToCurrency = parent?.getItemAtPosition(position).toString()
                getApiResult()
            }
        }
    }

    private fun setupSwapButton() {
        btnSwap.setOnClickListener {
            val temp = baseCurrency
            baseCurrency = convertedToCurrency
            convertedToCurrency = temp

            val fromSpinner: Spinner = requireView().findViewById(R.id.spFromCurrency)
            val toSpinner: Spinner = requireView().findViewById(R.id.spToCurrency)

            val fromPosition =
                (fromSpinner.adapter as ArrayAdapter<String>).getPosition(baseCurrency)
            fromSpinner.setSelection(fromPosition)
            val toPosition =
                (toSpinner.adapter as ArrayAdapter<String>).getPosition(convertedToCurrency)
            toSpinner.setSelection(toPosition)

            getApiResult()
        }
    }
}
