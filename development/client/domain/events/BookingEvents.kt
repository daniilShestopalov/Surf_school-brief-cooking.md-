package com.surfschool.domain.events

import kotlinx.coroutines.flow.MutableSharedFlow

object BookingEvents {
    val bookingCancelled = MutableSharedFlow<String>(extraBufferCapacity = 1)
}
