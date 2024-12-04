package com.dudek.dicodingstory.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.LinearLayout
import com.dudek.dicodingstory.R
import com.dudek.dicodingstory.databinding.ViewInputBinding

@SuppressLint("ClickableViewAccessibility")
class PasswordInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewInputBinding =
        ViewInputBinding.inflate(LayoutInflater.from(context), this)

    private var isPasswordVisible = false

    init {
        orientation = VERTICAL
        binding.etInput.apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = context.getString(R.string.password)
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_24, 0, R.drawable.ic_visibility_24, 0)
        }

        binding.etInput.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2
                if (event.rawX >= binding.etInput.right - binding.etInput.compoundDrawables[drawableEnd].bounds.width()) {
                    togglePasswordVisibility()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        binding.etInput.inputType = if (isPasswordVisible) {
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        binding.etInput.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_lock_24,
            0,
            if (isPasswordVisible) R.drawable.ic_visibility_off_24 else R.drawable.ic_visibility_24,
            0
        )
        binding.etInput.setSelection(binding.etInput.text.length)
    }

    fun getPassword(): String = binding.etInput.text.toString().trim()

    fun setPassword(password: String) {
        binding.etInput.setText(password)
    }

    fun setError(error: String?) {
        binding.etInput.error = error
    }
}