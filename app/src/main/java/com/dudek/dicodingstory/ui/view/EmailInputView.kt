package com.dudek.dicodingstory.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.dudek.dicodingstory.R
import com.dudek.dicodingstory.databinding.ViewInputBinding

class EmailInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr){
    private val binding: ViewInputBinding =
        ViewInputBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        binding.etInput.apply {
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            hint = context.getString(R.string.e_mail)
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email_24, 0, 0, 0)
        }
    }

    fun getEmail(): String = binding.etInput.text.toString().trim()

    fun setEmail(email: String) {
        binding.etInput.setText(email)
    }

    fun setError(error: String?) {
        binding.etInput.error = error
    }
}