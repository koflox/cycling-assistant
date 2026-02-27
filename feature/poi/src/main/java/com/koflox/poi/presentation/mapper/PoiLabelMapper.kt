package com.koflox.poi.presentation.mapper

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.koflox.poi.R
import com.koflox.poi.domain.model.PoiType

@Composable
internal fun PoiType.label(): String = stringResource(
    when (this) {
        PoiType.COFFEE_SHOP -> R.string.poi_label_coffee_shop
        PoiType.TOILET -> R.string.poi_label_toilet
        PoiType.KONBINI -> R.string.poi_label_konbini
        PoiType.VENDING_MACHINE -> R.string.poi_label_vending_machine
        PoiType.WATER_FOUNTAIN -> R.string.poi_label_water_fountain
        PoiType.BICYCLE_SHOP -> R.string.poi_label_bicycle_shop
        PoiType.PARK -> R.string.poi_label_park
        PoiType.PHARMACY -> R.string.poi_label_pharmacy
        PoiType.RESTAURANT -> R.string.poi_label_restaurant
        PoiType.GAS_STATION -> R.string.poi_label_gas_station
    },
)
