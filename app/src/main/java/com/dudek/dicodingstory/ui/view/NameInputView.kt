package com.dudek.dicodingstory.ui.view

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.dudek.dicodingstory.R
import com.dudek.dicodingstory.databinding.ViewInputBinding

class NameInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewInputBinding =
        ViewInputBinding.inflate(LayoutInflater.from(context), this)

    init {

        binding.etInput.apply {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = context.getString(R.string.name)
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_24, 0, 0, 0)

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    validateName()
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        setOnClickListener {
            binding.etInput.requestFocus()
        }
    }

    private fun validateName() {
        val name = binding.etInput.text?.toString()?.trim()
        binding.etInput.error = if (!isValidName(name)) {
            context.getString(R.string.error_invalid_name)
        } else {
            null
        }
    }

    private fun isValidName(name: String?): Boolean {
        return !name.isNullOrEmpty() && name.matches(Regex("^[a-zA-Z\\s]+$"))
    }

    fun getName(): String = binding.etInput.text?.toString()?.trim() ?: ""

    fun setName(name: String) {
        binding.etInput.setText(name)
    }

    fun setError(errorMessage: String?) {
        binding.etInput.error = errorMessage
    }
}