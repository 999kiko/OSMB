package refiner

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object VersionChecker {

    fun getVersion(): Double? {
        var connection: HttpURLConnection? = null

        return try {
            connection = URL(VERSION_URI).openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return null
            }

           val version = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                reader.readText().trim()
            }

            version.toDoubleOrNull()
        } catch (_: Exception) {
            return null
        } finally {
            connection?.disconnect()
        }
    }
}