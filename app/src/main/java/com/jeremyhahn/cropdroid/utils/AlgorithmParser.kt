package com.jeremyhahn.cropdroid.utils

import android.util.Log
import com.jeremyhahn.cropdroid.model.Algorithm
import org.json.JSONArray

class AlgorithmParser {

    companion object {
        fun parse(json: String): ArrayList<Algorithm> {
            return parse(JSONArray(json))
        }

        fun parse(jsonAlgorithms : JSONArray) : ArrayList<Algorithm> {
            var algorithms = ArrayList<Algorithm>(jsonAlgorithms.length())
            for (i in 0..jsonAlgorithms.length() - 1) {
                val jsonAlgorithm = jsonAlgorithms.getJSONObject(i)

                Log.d("AlgorithmParser.parse", jsonAlgorithm.toString())

                val id = jsonAlgorithm.getInt("id")
                val name = jsonAlgorithm.getString("name")
                algorithms.add(Algorithm(id, name))
            }
            return algorithms
        }
    }
}