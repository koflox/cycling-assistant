package com.koflox.session.presentation.route

internal enum class MapLayer {
    DEFAULT,
    SPEED,
    POWER,
    ;

    fun toColorStrategy(): RouteColorStrategy = when (this) {
        DEFAULT -> DefaultRouteColorStrategy()
        SPEED -> SpeedRouteColorStrategy()
        POWER -> PowerRouteColorStrategy()
    }
}
