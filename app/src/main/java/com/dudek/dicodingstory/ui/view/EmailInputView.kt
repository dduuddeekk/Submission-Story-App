package com.dudek.dicodingstory.ui.view

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.dudek.dicodingstory.R
import com.dudek.dicodingstory.databinding.ViewInputBinding

class EmailInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewInputBinding =
        ViewInputBinding.inflate(LayoutInflater.from(context), this)

    init {

        binding.etInput.apply {
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            hint = context.getString(R.string.e_mail)
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email_24, 0, 0, 0)

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    validateEmail()
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        setOnClickListener {
            binding.etInput.requestFocus()
        }
    }

    private fun validateEmail() {
        val email = binding.etInput.text?.toString()?.trim()
        binding.etInput.error = if (!isValidEmail(email)) {
            context.getString(R.string.error_invalid_email)
        } else {
            null
        }
    }

    private fun isValidEmail(email: String?): Boolean {
        return !email.isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun getEmail(): String = binding.etInput.text?.toString()?.trim() ?: ""

    fun setEmail(email: String) {
        binding.etInput.setText(email)
    }

    fun setError(errorMessage: String?) {
        binding.etInput.error = errorMessage
    }
}