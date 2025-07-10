package com.example.inkspire.util

import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

fun EditText.onDrawableEndClick(action: () -> Unit) {
    setOnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            val drawableEnd: Drawable? = compoundDrawables[2] // 0: left, 1: top, 2: end, 3: bottom
            if (drawableEnd != null &&
                event.rawX >= (right - drawableEnd.bounds.width() - paddingEnd)
            ) {
                v.performClick()
                action()
                return@setOnTouchListener true
            }
        }
        false
    }

    // Per completare l’accessibilità (soddisfa anche il warning performClick)
    setOnClickListener { /* no-op per performClick */ }
}

//Estensione per legare una LiveData<Boolean> alla visibilità di una View
fun LiveData<Boolean>.bindVisibility(view: View, lifecycleOwner: LifecycleOwner) {
    observe(lifecycleOwner) { isVisible ->
        view.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}