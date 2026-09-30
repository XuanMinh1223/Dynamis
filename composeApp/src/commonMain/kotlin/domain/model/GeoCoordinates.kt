package domain.model

data class GeoCoordinates(val latitude: Double, val longitude: Double) {
    init {
        require(latitude in -90.0..90.0 && longitude in -180.0..180.0) {
            "Coordinates are outside the valid latitude or longitude range"
        }
    }
}
