package com.honsl.petfeeder
import android.app.Application
import android.content.Context
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable
import java.lang.reflect.Type
import java.time.LocalTime
import java.util.UUID

data class Schedule(val id: UUID, var side:String, var amount:Int, var time: String): Serializable
data class Feeder(val name:String, var status:String, val ipAddress:String, var levelLeft:Float, var levelRight:Float, var schedule: MutableList<Schedule>): Serializable

class GlobalFeeder : Application() {
   // private val jsonManager by lazy { JsonManager(this) }
 //   private var _wifiManager: WifiManager? = null
 //   val wifiManager: WifiManager?
 //       get() = _wifiManager
//Anytime the object is updated, auto save to the json file
    var feeder: Feeder? = null
   /*     set(value) {
            field = value
            value?.let {
                jsonManager.saveToJSON(it)
                if (_wifiManager == null || _wifiManager?.ipAddress != it.ipAddress) {
                    _wifiManager = WifiManager(this, "http://${it.ipAddress}")
                }
            }
        }
    fun sendJsonToDevice(){
        CoroutineScope(Dispatchers.IO).launch {
            try{

                  val info = _wifiManager?.apiService?.getFeeder()
            }catch (e: Exception){

            }
        }
    }
*/}

class JsonManager(val context: Context) {
    private val fileName = "feeders.json"
    private val gson = Gson()
    private val itemType: Type = object : TypeToken<List<Feeder>>() {}.type

    fun getFeeders(): MutableList<Feeder> {
        val existingJson = try {
            context.openFileInput(fileName).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "[]" // default to empty list
        }
        return gson.fromJson(existingJson, itemType) ?: mutableListOf()
    }
    fun updateFeeder(updatedFeeder: Feeder): Boolean {
        return try {
            val feeders = getFeeders()
            val index = feeders.indexOfFirst { it.name == updatedFeeder.name }

            if (index != -1) {
                feeders[index] = updatedFeeder
            } else {
                // Optionally add the feeder if it doesn't exist
                feeders.add(updatedFeeder)
            }

            val updatedJson = gson.toJson(feeders)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(updatedJson.toByteArray())
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getFeederByName(name:String): Feeder?{
        val feeder = getFeeders().find{it.name == name}

        return feeder
    }
    fun saveToJSON(newFeeder: Feeder): Boolean {
        return try {
            val feeders = getFeeders().toMutableList()

            val index = feeders.indexOfFirst { it.name == newFeeder.name }
            if (index != -1) {
                feeders[index] = newFeeder
            } else {
                feeders.add(newFeeder)
            }

            val updatedJson = gson.toJson(feeders)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(updatedJson.toByteArray())
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun sendJsonToDevice(feeder: Feeder){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val wifi = WifiManager(context,"http://${feeder.ipAddress}")
              //  val info = wifi.apiService.getFeeder("feed","123")
            }catch (e: Exception){

            }
        }
    }


}

