package com.koflox.session.presentation.route

enum class MapLayer {
    DEFAULT,
    SPEED,
    POWER,
    ;

    internal fun toColorStrategy(): RouteColorStrategy = when (this) {
        DEFAULT -> DefaultRouteColorStrategy()
        SPEED -> SpeedRouteColorStrategy()
        POWER -> PowerRouteColorStrategy()
    }
}
