package com.example.evgramacharge

data class ChargingStation(
    val name: String,
    val location: String,
    val socketType: String,
    val availability: String
)

object StationData {

    val stations = mutableListOf(

        ChargingStation(
            name = "EV Station 1",
            location = "Bengaluru",
            socketType = "15A Socket",
            availability = "Available"
        ),

        ChargingStation(
            name = "Kirana Store Charging",
            location = "BTM Layout",
            socketType = "15A Socket",
            availability = "Busy"
        )
    )
}
