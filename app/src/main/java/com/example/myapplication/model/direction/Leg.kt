package com.example.myapplication.model.direction

import java.util.*

class Leg {
    var distance: Distance? = null
    var duration: Duration? = null
    var end_address: String? = null
    var end_location: EndLocation? = null
    var start_address: String? = null
    var start_location: StartLocation? = null
    var steps: List<Step>? = null
    var traffic_speed_entry: List<Objects>? = null
    var via_waypoint: List<Objects>? = null
}
