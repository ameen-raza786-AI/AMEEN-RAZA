package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import java.io.ByteArrayOutputStream
import kotlin.math.max

@Composable
fun RazaProductImage(
  imageSource: String,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Crop,
) {
  val context = LocalContext.current
  val drawableRes =
    remember(imageSource) {
      when (imageSource) {
        "drawable:img_hero_bridal" -> R.drawable.img_hero_bridal
        "drawable:img_lehenga_emerald" -> R.drawable.img_lehenga_emerald
        "drawable:img_suit_ivory" -> R.drawable.img_suit_ivory
        "drawable:img_saree_obsidian" -> R.drawable.img_saree_obsidian
        else -> null
      }
    }

  if (drawableRes != null) {
    Image(
      painter = painterResource(id = drawableRes),
      contentDescription = contentDescription,
      modifier = modifier,
      contentScale = contentScale,
    )
  } else if (imageSource.startsWith("data:image")) {
    val bytes =
      remember(imageSource) {
        try {
          val base64Part = imageSource.substringAfter("base64,", "")
          if (base64Part.isNotBlank()) Base64.decode(base64Part, Base64.DEFAULT) else null
        } catch (_: Exception) {
          null
        }
      }
    if (bytes != null) {
      AsyncImage(
        model = ImageRequest.Builder(context).data(bytes).crossfade(true).build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
      )
    } else {
      FallbackImageBox(modifier)
    }
  } else if (imageSource.isNotBlank()) {
    AsyncImage(
      model = ImageRequest.Builder(context).data(imageSource).crossfade(true).build(),
      contentDescription = contentDescription,
      modifier = modifier,
      contentScale = contentScale,
    )
  } else {
    FallbackImageBox(modifier)
  }
}

@Composable
private fun FallbackImageBox(modifier: Modifier) {
  Box(
    modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = Icons.Outlined.Checkroom,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
      modifier = Modifier.fillMaxSize(0.35f),
    )
  }
}

fun compressImageUriToDataUri(context: Context, uri: Uri): String? {
  return try {
    val input = context.contentResolver.openInputStream(uri) ?: return null
    val original = input.use { BitmapFactory.decodeStream(it) } ?: return null
    val maxDim = 540
    val scale =
      if (max(original.width, original.height) > maxDim) {
        maxDim.toFloat() / max(original.width, original.height).toFloat()
      } else {
        1f
      }
    val targetW = (original.width * scale).toInt().coerceAtLeast(1)
    val targetH = (original.height * scale).toInt().coerceAtLeast(1)
    val resized =
      if (scale < 1f) Bitmap.createScaledBitmap(original, targetW, targetH, true) else original
    val out = ByteArrayOutputStream()
    resized.compress(Bitmap.CompressFormat.JPEG, 72, out)
    if (resized !== original) resized.recycle()
    original.recycle()
    val encoded = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    "data:image/jpeg;base64,$encoded"
  } catch (_: Exception) {
    null
  }
}
