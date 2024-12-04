package com.dudek.dicodingstory.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.dudek.dicodingstory.R
import com.dudek.dicodingstory.databinding.ViewInputBinding

class NameInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr){
    private val binding: ViewInputBinding =
        ViewInputBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        binding.etInput.apply {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = context.getString(R.string.name)
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_24, 0, 0, 0)
        }
    }

    fun getName(): String = binding.etInput.text.toString().trim()

    fun setName(name: String) {
        binding.etInput.setText(name)
    }

    fun setError(error: String?) {
        binding.etInput.error = error
    }
}