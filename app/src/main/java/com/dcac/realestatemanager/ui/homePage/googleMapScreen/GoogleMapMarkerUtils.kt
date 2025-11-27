package com.dcac.realestatemanager.ui.homePage.googleMapScreen

import android.content.Context
import android.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptor
import com.dcac.realestatemanager.utils.Utils.getIconForPropertyType
import com.dcac.realestatemanager.utils.Utils.getIconForPoiType
import com.dcac.realestatemanager.utils.Utils.getColorForPoiType
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.core.graphics.createBitmap
import com.dcac.realestatemanager.R


private val propertyMarkerCache = mutableMapOf<String, BitmapDescriptor>()

fun getCustomPropertyMarker(
    context: Context,
    type: String
): BitmapDescriptor {
    val iconRes = getIconForPropertyType(type)
    val backgroundColor = Color.WHITE
    val iconTint = Color.BLACK

    val size = 96
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)

    val backgroundPaint = Paint().apply {
        color = backgroundColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), backgroundPaint)

    val strokePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }
    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), strokePaint)

    val drawable = ContextCompat.getDrawable(context, iconRes)!!.mutate()
    drawable.setTint(iconTint)
    val iconSize = size / 2
    val iconBitmap = drawable.toBitmap(iconSize, iconSize)
    val left = (size - iconBitmap.width) / 2f
    val top = (size - iconBitmap.height) / 2f
    canvas.drawBitmap(iconBitmap, left, top, null)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun getCachedPropertyMarker(
    context: Context,
    type: String
): BitmapDescriptor {
    return propertyMarkerCache.getOrPut(type) {
        getCustomPropertyMarker(context, type)
    }
}

private val poiMarkerCache = mutableMapOf<String, BitmapDescriptor>()

fun getCustomPoiMarker(
    context: Context,
    type: String
): BitmapDescriptor {
    val iconRes = getIconForPoiType(type)
    val backgroundColor = getColorForPoiType(type)
    val iconTint = Color.BLACK

    val size = 96
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)

    val fillPaint = Paint().apply {
        color = backgroundColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, fillPaint)

    val borderPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 1f, borderPaint)

    val drawable = ContextCompat.getDrawable(context, iconRes)!!.mutate()
    drawable.setTint(iconTint)
    val iconBitmap = drawable.toBitmap(size / 2, size / 2)
    val left = (size - iconBitmap.width) / 2f
    val top = (size - iconBitmap.height) / 2f
    canvas.drawBitmap(iconBitmap, left, top, null)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun getCachedPoiMarker(
    context: Context,
    type: String
): BitmapDescriptor {
    return poiMarkerCache.getOrPut(type) {
        getCustomPoiMarker(context, type)
    }
}

private val userMarkerCache = mutableMapOf<String, BitmapDescriptor>()

fun getCustomUserMarker(context: Context): BitmapDescriptor {
    val iconRes = R.drawable.user_24px
    val iconTint = Color.WHITE
    val backgroundColor = Color.BLACK

    val size = 96
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)


    val path = Path().apply {
        moveTo(size / 2f, 0f)
        lineTo(size.toFloat(), size / 2f)
        lineTo(size / 2f, size.toFloat())
        lineTo(0f, size / 2f)
        close()
    }

    val paint = Paint().apply {
        color = backgroundColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawPath(path, paint)

    val drawable = ContextCompat.getDrawable(context, iconRes)!!.mutate()
    drawable.setTint(iconTint)
    val iconSize = size / 2
    val iconBitmap = drawable.toBitmap(iconSize, iconSize)
    val left = (size - iconBitmap.width) / 2f
    val top = (size - iconBitmap.height) / 2f
    canvas.drawBitmap(iconBitmap, left, top, null)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun getCachedUserMarker(
    context: Context
): BitmapDescriptor {
    return userMarkerCache.getOrPut("user") {
        getCustomUserMarker(context)
    }
}
