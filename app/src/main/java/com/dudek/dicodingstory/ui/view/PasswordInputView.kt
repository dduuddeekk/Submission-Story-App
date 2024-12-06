package com.dudek.dicodingstory.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
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

        binding.etInput.apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = context.getString(R.string.password)
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_24, 0, R.drawable.ic_visibility_24, 0)

            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val drawableEndIndex = 2
                    if (compoundDrawables[drawableEndIndex] != null &&
                        event.rawX >= right - compoundDrawables[drawableEndIndex].bounds.width()
                    ) {
                        togglePasswordVisibility()
                        return@setOnTouchListener true
                    }
                }
                false
            }

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    validatePasswordLength()
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        setOnClickListener {
            binding.etInput.requestFocus()
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
        binding.etInput.setSelection(binding.etInput.text?.length ?: 0)
    }

    private fun validatePasswordLength() {
        binding.etInput.error = if ((binding.etInput.text?.length ?: 0) < 8) {
            context.getString(R.string.error_password_to_short)
        } else {
            null
        }
    }

    fun getPassword(): String = binding.etInput.text?.toString()?.trim() ?: ""

    fun setPassword(password: String) {
        binding.etInput.setText(password)
    }

    fun setErrorMessage(errorMessage: String?) {
        binding.etInput.error = errorMessage
    }
}