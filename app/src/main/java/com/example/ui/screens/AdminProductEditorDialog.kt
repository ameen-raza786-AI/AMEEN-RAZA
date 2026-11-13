package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.firebase.StoreCatalogSeeder
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.ui.components.RazaProductImage
import com.example.ui.components.compressImageUriToDataUri
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalGold

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminProductEditorDialog(
  initialProduct: Product?,
  categories: List<Category>,
  onDismiss: () -> Unit,
  onSave: (Product) -> Unit,
) {
  val context = LocalContext.current

  var name by remember { mutableStateOf(initialProduct?.name.orEmpty()) }
  var description by remember { mutableStateOf(initialProduct?.description.orEmpty()) }
  var selectedCategory by remember {
    mutableStateOf(
      initialProduct?.category ?: categories.firstOrNull()?.name ?: "Bridal Lehenga"
    )
  }
  val selectedCollections = remember {
    mutableStateListOf<String>().apply {
      addAll(initialProduct?.collections ?: listOf("New Arrivals"))
    }
  }
  val images = remember {
    mutableStateListOf<String>().apply {
      addAll(initialProduct?.images ?: listOf("drawable:img_hero_bridal"))
    }
  }
  var priceText by remember { mutableStateOf(initialProduct?.price?.toLong()?.toString() ?: "") }
  var discountPriceText by remember {
    mutableStateOf(initialProduct?.discountPrice?.toLong()?.toString() ?: "")
  }
  var offerBadge by remember { mutableStateOf(initialProduct?.offerBadge.orEmpty()) }
  var isSpecialOffer by remember { mutableStateOf(initialProduct?.isSpecialOffer ?: false) }
  var sizesText by remember {
    mutableStateOf(
      initialProduct?.sizes?.joinToString(", ") ?: "XS, S, M, L, XL, Custom"
    )
  }
  var colorsText by remember {
    mutableStateOf(
      initialProduct?.colors?.joinToString(", ") ?: "Royal Crimson, Champagne Gold"
    )
  }
  var fabric by remember { mutableStateOf(initialProduct?.fabric ?: "Pure Raw Silk & Zardozi") }
  var estimatedDelivery by remember {
    mutableStateOf(initialProduct?.estimatedDelivery ?: "7–12 business days")
  }
  var stockText by remember {
    mutableStateOf(initialProduct?.stockCount?.toString() ?: "10")
  }
  var customStitching by remember {
    mutableStateOf(initialProduct?.customStitchingAvailable ?: true)
  }
  var madeToOrder by remember { mutableStateOf(initialProduct?.madeToOrder ?: false) }
  var isPublished by remember { mutableStateOf(initialProduct?.isPublished ?: true) }
  var imageUrlInput by remember { mutableStateOf("") }
  var validationError by remember { mutableStateOf<String?>(null) }

  // Zero-permission Android Photo Picker (Play Policy Compliant)
  val photoPickerLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4),
      onResult = { uris ->
        uris.forEach { uri ->
          val dataUri = compressImageUriToDataUri(context, uri)
          if (dataUri != null && images.size < 6) {
            images.add(0, dataUri)
          }
        }
      },
    )

  val studioPresets = listOf(
    "drawable:img_hero_bridal" to "Bridal",
    "drawable:img_lehenga_emerald" to "Emerald",
    "drawable:img_suit_ivory" to "Ivory Suit",
    "drawable:img_saree_obsidian" to "Obsidian",
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier = Modifier.fillMaxWidth(0.95f).padding(vertical = 24.dp),
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.6f)),
    ) {
      Column(
        modifier = Modifier.padding(18.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = if (initialProduct == null) "Add New Couture Piece" else "Edit Product",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Filled.Close, contentDescription = "Close")
          }
        }

        // Images Upload & Preview
        Text("Product Images (${images.size}/6)", style = MaterialTheme.typography.titleSmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(images) { img ->
            Box(modifier = Modifier.size(84.dp)) {
              RazaProductImage(
                imageSource = img,
                contentDescription = "Product photo",
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
              )
              if (images.size > 1) {
                Box(
                  modifier =
                    Modifier.align(Alignment.TopEnd)
                      .padding(3.dp)
                      .size(22.dp)
                      .clip(CircleShape)
                      .background(ObsidianBlack.copy(alpha = 0.8f))
                      .clickable { images.remove(img) },
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remove image",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                  )
                }
              }
            }
          }
        }

        OutlinedButton(
          onClick = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          border = BorderStroke(1.dp, RoyalGold),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, tint = RoyalGold)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Upload Photos from Gallery", color = RoyalGold)
        }

        // Studio presets + optional URL
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          studioPresets.forEach { (key, label) ->
            FilterChip(
              selected = images.contains(key),
              onClick = {
                if (images.contains(key) && images.size > 1) images.remove(key)
                else if (!images.contains(key) && images.size < 6) images.add(key)
              },
              label = { Text(label, style = MaterialTheme.typography.labelSmall) },
            )
          }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
          OutlinedTextField(
            value = imageUrlInput,
            onValueChange = { imageUrlInput = it },
            label = { Text("Or paste HTTPS image URL") },
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          TextButton(
            onClick = {
              val trimmed = imageUrlInput.trim()
              if (trimmed.startsWith("https://") && images.size < 6) {
                images.add(0, trimmed)
                imageUrlInput = ""
              }
            }
          ) {
            Text("Add URL")
          }
        }

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Product Name*") },
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description*") },
          minLines = 2,
          modifier = Modifier.fillMaxWidth(),
        )

        // Category Selection
        Text("Category", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          categories.forEach { cat ->
            FilterChip(
              selected = selectedCategory == cat.name,
              onClick = { selectedCategory = cat.name },
              label = { Text(cat.name, style = MaterialTheme.typography.labelSmall) },
            )
          }
        }

        // Homepage Collection Tags
        Text("Homepage Sections", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          StoreCatalogSeeder.homepageSections.forEach { sec ->
            val checked = selectedCollections.contains(sec)
            FilterChip(
              selected = checked,
              onClick = {
                if (checked) selectedCollections.remove(sec) else selectedCollections.add(sec)
              },
              label = { Text(sec, style = MaterialTheme.typography.labelSmall) },
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = priceText,
            onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
            label = { Text("MRP (₹)*") },
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = discountPriceText,
            onValueChange = { discountPriceText = it.filter { ch -> ch.isDigit() } },
            label = { Text("Offer Price (₹)") },
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = stockText,
            onValueChange = { stockText = it.filter { ch -> ch.isDigit() } },
            label = { Text("Stock Count") },
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = offerBadge,
            onValueChange = { offerBadge = it },
            label = { Text("Offer Badge") },
            placeholder = { Text("e.g. 20% OFF") },
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
        }

        OutlinedTextField(
          value = fabric,
          onValueChange = { fabric = it },
          label = { Text("Fabric Details") },
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = sizesText,
          onValueChange = { sizesText = it },
          label = { Text("Sizes (comma separated)") },
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = colorsText,
          onValueChange = { colorsText = it },
          label = { Text("Colours (comma separated)") },
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = estimatedDelivery,
          onValueChange = { estimatedDelivery = it },
          label = { Text("Estimated Delivery") },
          modifier = Modifier.fillMaxWidth(),
        )

        ToggleOption("Custom Stitching Available", customStitching) { customStitching = it }
        ToggleOption("Made-to-Order Couture", madeToOrder) { madeToOrder = it }
        ToggleOption("Highlight in Special Offers", isSpecialOffer) { isSpecialOffer = it }
        ToggleOption("Published on Storefront", isPublished) { isPublished = it }

        if (!validationError.isNullOrBlank()) {
          Text(validationError ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Button(
          onClick = {
            val price = priceText.toDoubleOrNull() ?: 0.0
            val discount = discountPriceText.toDoubleOrNull() ?: price
            val stock = stockText.toLongOrNull() ?: 0L
            when {
              name.trim().length < 2 -> validationError = "Enter a valid product name"
              description.trim().length < 5 -> validationError = "Enter a short product description"
              price <= 0 -> validationError = "Price must be greater than ₹0"
              discount > price -> validationError = "Offer price cannot exceed MRP"
              else -> {
                validationError = null
                val sizes = sizesText.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { listOf("M", "L", "Custom") }
                val colors = colorsText.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { listOf("Royal Gold") }
                onSave(
                  Product(
                    id = initialProduct?.id.orEmpty(),
                    name = name.trim(),
                    description = description.trim(),
                    category = selectedCategory,
                    collections = selectedCollections.toList(),
                    images = images.toList(),
                    price = price,
                    discountPrice = discount,
                    isSpecialOffer = isSpecialOffer,
                    offerBadge = offerBadge.trim(),
                    sizes = sizes,
                    colors = colors,
                    fabric = fabric.trim().ifBlank { "Pure Ethnic Silk" },
                    customStitchingAvailable = customStitching,
                    madeToOrder = madeToOrder,
                    estimatedDelivery = estimatedDelivery.trim().ifBlank { "7–10 business days" },
                    stockCount = stock,
                    rating = initialProduct?.rating ?: 4.9,
                    reviewCount = initialProduct?.reviewCount ?: 0L,
                    isPublished = isPublished,
                  )
                )
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianBlack),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
          Text("Save Product", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun ToggleOption(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(label, style = MaterialTheme.typography.bodyMedium)
    Switch(
      checked = checked,
      onCheckedChange = onChange,
      colors = SwitchDefaults.colors(checkedTrackColor = RoyalGold),
    )
  }
}
