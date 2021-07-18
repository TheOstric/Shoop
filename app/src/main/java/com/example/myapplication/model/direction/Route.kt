package com.example.myapplication.model.direction

import java.util.*

class Route {
    var bounds: Bounds? = null
    var copyrights: String? = null
    var legs: List<Leg>? = null
    var overview_polyline: OverviewPolyline? = null
    var summary: String? = null
    var warnings: List<Objects>? = null
    var waypoint_order: List<Objects>? = null
}
