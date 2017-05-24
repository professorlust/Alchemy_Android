package com.etbrady.alchemy.apis

import android.content.Context
import android.content.res.Resources
import com.etbrady.alchemy.R
import com.etbrady.alchemy.models.Class
import com.github.salomonbrys.kotson.DeserializerArg
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class AlchemyAPIFactory {
    companion object {
        fun createAlchemyAPIInstance(context: Context): AlchemyAPI {
            val gson = GsonBuilder()
                    .registerTypeAdapter<List<Class>> {
                        deserialize({
                            deserializeClassList(it, context)
                        })
                    }
                    .create()

            val retrofit = Retrofit.Builder()
                    .baseUrl(context.getString(R.string.alchemy_base_url))
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

            return retrofit.create(AlchemyAPI::class.java)
        }

        private fun deserializeClassList(deserializerArg: DeserializerArg, context: Context): List<Class> {
            val dateFormatter = SimpleDateFormat(context.getString(R.string.alchemy_date_format), Locale.US)
            return deserializerArg.json["event_occurrences"].asJsonArray.map {
                val name = it["name"].asString
                val startDate = dateFormatter.parse(it["start_at"].asString)
                val endDate = dateFormatter.parse(it["end_at"].asString)
                val locationId = it["location_id"].asInt
                val instructors = it["staff_members"].asJsonArray
                val instructorName = if (instructors.size() > 0) {
                    instructors[0]["name"].asString
                } else {
                    ""
                }
                Class(name, startDate, endDate, locationId, instructorName)
            }
        }
    }
}
