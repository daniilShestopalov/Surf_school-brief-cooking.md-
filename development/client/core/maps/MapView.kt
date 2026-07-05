package com.surfschool.core.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Expected UI component for displaying a map in Compose Multiplatform.
 * Android will actualize this with Google Maps (or Yandex),
 * iOS will actualize with MapKit.
 */
@Composable
expect fun MapView(
    address: String,
    lat: Double,
    lng: Double,
    modifier: Modifier = Modifier
)
