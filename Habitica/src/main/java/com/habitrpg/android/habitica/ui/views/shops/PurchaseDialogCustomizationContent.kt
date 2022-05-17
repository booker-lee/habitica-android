package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.widget.TextView
import com.habitrpg.android.habitica.databinding.DialogPurchaseCustomizationBinding
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.views.PixelArtView

class PurchaseDialogCustomizationContent(context: Context) : PurchaseDialogContent(context) {
    val binding = DialogPurchaseCustomizationBinding.inflate(context.layoutInflater, this)
    override val imageView: PixelArtView
        get() = binding.imageView
    override val titleTextView: TextView
        get() = binding.titleTextView
}
