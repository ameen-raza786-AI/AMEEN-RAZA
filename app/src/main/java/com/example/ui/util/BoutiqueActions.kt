package com.example.ui.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.firebase.StoreCatalogSeeder
import com.example.data.model.Order
import com.example.data.model.Product
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object BoutiqueActions {
  private val inrFormat =
    DecimalFormat("#,##,##0", DecimalFormatSymbols(Locale.forLanguageTag("en-IN")))

  fun formatInr(amount: Double): String = "₹${inrFormat.format(amount.toLong())}"

  fun launchWhatsAppProductOrder(
    context: Context,
    product: Product,
    selectedSize: String,
    selectedColor: String,
    customStitching: Boolean,
    madeToOrder: Boolean,
    customMeasurements: String,
  ) {
    val imageRef = product.images.firstOrNull() ?: "Studio Lookbook"
    val imageLabel =
      when {
        imageRef.startsWith("https://") || imageRef.startsWith("http://") -> imageRef
        imageRef.startsWith("data:image") -> "Uploaded Boutique Photo (${product.name})"
        else -> "Raza Atelier Catalog (${imageRef.removePrefix("drawable:")})"
      }

    val message =
      buildString {
        appendLine("Hello *RAZA ETHNIC WEAR* Couture Concierge,")
        appendLine("I would like to place an order / inquire about:")
        appendLine("• *Product:* ${product.name}")
        appendLine("• *Category:* ${product.category}")
        appendLine("• *Price:* ${formatInr(product.effectivePrice)} (MRP ${formatInr(product.price)})")
        appendLine("• *Size:* $selectedSize")
        appendLine("• *Colour:* $selectedColor")
        appendLine("• *Custom Stitching:* ${if (customStitching) "Yes" else "Standard Fit"}")
        appendLine("• *Made-to-Order:* ${if (madeToOrder) "Yes" else "Ready Collection"}")
        if (customMeasurements.isNotBlank()) {
          appendLine("• *Customization / Measurements:* ${customMeasurements.trim()}")
        }
        appendLine("• *Image Reference:* $imageLabel")
        append("Please confirm availability and delivery timeline.")
      }

    openWhatsAppWithFallback(context, message)
  }

  fun launchWhatsAppOrderSupport(context: Context, order: Order) {
    val message =
      buildString {
        appendLine("Hello *RAZA ETHNIC WEAR* Support,")
        appendLine("I am inquiring about my order *${order.orderNumber}*:")
        appendLine("• *Status:* ${order.status}")
        appendLine("• *Total:* ${formatInr(order.totalAmount)}")
        appendLine("• *Payment:* ${order.paymentMethod} (${order.paymentStatus})")
        append("Kindly share the latest update.")
      }
    openWhatsAppWithFallback(context, message)
  }

  fun launchWhatsAppConcierge(context: Context) {
    openWhatsAppWithFallback(
      context,
      "Hello RAZA ETHNIC WEAR! I would like styling & bridal couture assistance.",
    )
  }

  private fun openWhatsAppWithFallback(context: Context, message: String) {
    val encoded = Uri.encode(message)
    val waUri = Uri.parse("https://wa.me/${StoreCatalogSeeder.BOUTIQUE_WHATSAPP_NUMBER}?text=$encoded")
    val waIntent =
      Intent(Intent.ACTION_VIEW, waUri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

    try {
      context.startActivity(waIntent)
    } catch (_: Exception) {
      copyToClipboard(context, "WhatsApp Order", message)
      val shareIntent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, message)
        }
      try {
        context.startActivity(
          Intent.createChooser(shareIntent, "Send Order to WhatsApp")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
      } catch (_: Exception) {
        Toast.makeText(
          context,
          "Order details copied! Paste in WhatsApp (+91 98765 43210).",
          Toast.LENGTH_LONG,
        ).show()
      }
    }
  }

  fun launchInstagram(context: Context) {
    val intent =
      Intent(Intent.ACTION_VIEW, Uri.parse(StoreCatalogSeeder.BOUTIQUE_INSTAGRAM_URL)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    try {
      context.startActivity(intent)
    } catch (_: Exception) {
      Toast.makeText(context, "Follow us @razaethnicwear on Instagram", Toast.LENGTH_SHORT).show()
    }
  }

  fun tryLaunchUpiApp(context: Context, amount: Double, orderNote: String): Boolean {
    val uri =
      Uri.parse(
        "upi://pay?pa=razaethnicwear@okicici&pn=Raza%20Ethnic%20Wear&am=${amount.toInt()}&cu=INR&tn=${Uri.encode(orderNote)}"
      )
    val intent = Intent(Intent.ACTION_VIEW, uri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    return try {
      context.startActivity(intent)
      true
    } catch (_: Exception) {
      false
    }
  }

  fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
  }
}
