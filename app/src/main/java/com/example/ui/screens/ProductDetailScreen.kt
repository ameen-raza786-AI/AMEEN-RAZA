package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.model.Product
import com.example.data.model.Review
import com.example.ui.components.GoldShimmerPill
import com.example.ui.components.RazaProductImage
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonVelvet
import com.example.ui.theme.EmeraldSilk
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.SterlingSilver
import com.example.ui.util.BoutiqueActions

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductDetailScreen(
  product: Product,
  isWishlisted: Boolean,
  reviews: List<Review>,
  onBack: () -> Unit,
  onToggleWishlist: () -> Unit,
  onAddToCart: (size: String, color: String, customStitch: Boolean, madeToOrder: Boolean, notes: String) -> Unit,
  onBuyNow: (size: String, color: String, customStitch: Boolean, madeToOrder: Boolean, notes: String) -> Unit,
  onSubmitReview: (rating: Long, comment: String) -> Unit,
) {
  BackHandler { onBack() }
  val context = LocalContext.current

  val images = remember(product.images) { product.images.ifEmpty { listOf("drawable:img_hero_bridal") } }
  val pagerState = rememberPagerState(pageCount = { images.size })

  var selectedSize by remember(product.id) { mutableStateOf(product.sizes.firstOrNull() ?: "M") }
  var selectedColor by remember(product.id) { mutableStateOf(product.colors.firstOrNull() ?: "Royal Gold") }
  var customStitching by remember(product.id) { mutableStateOf(false) }
  var madeToOrder by remember(product.id) { mutableStateOf(product.madeToOrder) }
  var measurementsNote by remember(product.id) { mutableStateOf("") }
  var showReviewDialog by remember { mutableStateOf(false) }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    bottomBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OutlinedButton(
              onClick = {
                onAddToCart(
                  selectedSize,
                  selectedColor,
                  customStitching,
                  madeToOrder,
                  measurementsNote,
                )
              },
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.2.dp, RoyalGold),
              modifier = Modifier.weight(1f).height(50.dp).testTag("add_to_cart_button"),
            ) {
              Icon(
                imageVector = Icons.Filled.ShoppingBag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Add to Cart",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
              )
            }
            Button(
              onClick = {
                onBuyNow(
                  selectedSize,
                  selectedColor,
                  customStitching,
                  madeToOrder,
                  measurementsNote,
                )
              },
              shape = RoundedCornerShape(12.dp),
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = RoyalGold,
                  contentColor = ObsidianBlack,
                ),
              modifier = Modifier.weight(1f).height(50.dp).testTag("buy_now_button"),
            ) {
              Text("Buy Now", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
          }

          // Dedicated WhatsApp Ordering Button on every product page
          Button(
            onClick = {
              BoutiqueActions.launchWhatsAppProductOrder(
                context = context,
                product = product,
                selectedSize = selectedSize,
                selectedColor = selectedColor,
                customStitching = customStitching,
                madeToOrder = madeToOrder,
                customMeasurements = measurementsNote,
              )
            },
            shape = RoundedCornerShape(12.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = EmeraldSilk,
                contentColor = Color.White,
              ),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("whatsapp_order_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Chat,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Order on WhatsApp with Custom Details",
              style = MaterialTheme.typography.labelLarge,
            )
          }
        }
      }
    },
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentPadding = PaddingValues(bottom = 24.dp),
    ) {
      // Multi-image Swipeable Gallery
      item {
        Box(modifier = Modifier.fillMaxWidth().height(410.dp)) {
          HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { idx ->
            RazaProductImage(
              imageSource = images[idx],
              contentDescription = "${product.name} image ${idx + 1}",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop,
            )
          }

          // Top Back & Wishlist Controls
          Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            IconButton(
              onClick = onBack,
              modifier =
                Modifier.size(48.dp)
                  .clip(CircleShape)
                  .background(ObsidianBlack.copy(alpha = 0.65f))
                  .testTag("product_back_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
              )
            }
            IconButton(
              onClick = onToggleWishlist,
              modifier =
                Modifier.size(48.dp)
                  .clip(CircleShape)
                  .background(ObsidianBlack.copy(alpha = 0.65f))
                  .testTag("product_detail_wishlist"),
            ) {
              Icon(
                imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Wishlist",
                tint = if (isWishlisted) CrimsonVelvet else ChampagneGold,
              )
            }
          }

          // Gallery Dots
          if (images.size > 1) {
            Row(
              modifier =
                Modifier.align(Alignment.BottomCenter)
                  .padding(bottom = 14.dp)
                  .clip(RoundedCornerShape(50))
                  .background(ObsidianBlack.copy(alpha = 0.6f))
                  .padding(horizontal = 10.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              repeat(images.size) { idx ->
                val active = pagerState.currentPage == idx
                Box(
                  modifier =
                    Modifier.size(if (active) 9.dp else 7.dp)
                      .clip(CircleShape)
                      .background(if (active) RoyalGold else SterlingSilver.copy(alpha = 0.5f))
                )
              }
            }
          }
        }
      }

      // Title, Price & Rating
      item {
        Column(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            GoldShimmerPill(text = product.category.uppercase())
            if (product.discountPercent > 0) {
              Surface(
                color = CrimsonVelvet,
                shape = RoundedCornerShape(50),
              ) {
                Text(
                  text = "${product.discountPercent}% OFF",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
              }
            }
          }

          Text(
            text = product.name,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Text(
              text = BoutiqueActions.formatInr(product.effectivePrice),
              style = MaterialTheme.typography.headlineMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
            )
            if (product.discountPrice > 0 && product.discountPrice < product.price) {
              Text(
                text = BoutiqueActions.formatInr(product.price),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough,
              )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = ChampagneGold,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${product.rating} (${reviews.size} reviews)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          Text(
            text = product.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
          )
        }
      }

      // Fabric & Delivery Spec Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(Icons.Filled.Checkroom, contentDescription = null, tint = RoyalGold)
              Column {
                Text(
                  text = "Fabric & Craftsmanship",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                  text = product.fabric,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                  fontWeight = FontWeight.Medium,
                )
              }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = RoyalGold)
              Column {
                Text(
                  text = "Estimated Delivery Time",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                  text = "${product.estimatedDelivery} • Stock: ${if (product.stockCount > 0) "${product.stockCount} available" else "Made-to-Order Only"}",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                  fontWeight = FontWeight.Medium,
                )
              }
            }
          }
        }
      }

      // Size & Colour Selection
      item {
        Column(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Text(
            text = "Select Size",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            product.sizes.forEach { size ->
              val selected = size == selectedSize
              FilterChip(
                selected = selected,
                onClick = {
                  selectedSize = size
                  if (size.equals("Custom", true)) customStitching = true
                },
                label = { Text(size, fontWeight = FontWeight.SemiBold) },
                colors =
                  FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RoyalGold,
                    selectedLabelColor = ObsidianBlack,
                    containerColor = MaterialTheme.colorScheme.surface,
                  ),
              )
            }
          }

          Text(
            text = "Select Colour",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            product.colors.forEach { color ->
              val selected = color == selectedColor
              FilterChip(
                selected = selected,
                onClick = { selectedColor = color },
                label = { Text(color) },
                colors =
                  FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surface,
                  ),
                border =
                  FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    selectedBorderColor = RoyalGold,
                  ),
              )
            }
          }

          // Custom Stitching & Made-to-Order Options
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              if (product.customStitchingAvailable) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "Custom Stitching",
                      style = MaterialTheme.typography.titleSmall,
                      color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                      text = "Tailored to your exact blouse, waist & lehenga length",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                  Switch(
                    checked = customStitching,
                    onCheckedChange = { customStitching = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = RoyalGold),
                  )
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Made-to-Order Couture",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = "Freshly crafted by master artisans for your occasion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Switch(
                  checked = madeToOrder,
                  onCheckedChange = { madeToOrder = it },
                  colors = SwitchDefaults.colors(checkedTrackColor = RoyalGold),
                )
              }

              AnimatedVisibility(visible = customStitching || madeToOrder) {
                OutlinedTextField(
                  value = measurementsNote,
                  onValueChange = { measurementsNote = it },
                  label = { Text("Measurements / Custom Notes (Bust, Waist, Length, Sleeve)") },
                  placeholder = { Text("e.g., Bust 34\", Waist 28\", Length 42\", Sweetheart neck") },
                  minLines = 2,
                  maxLines = 4,
                  modifier = Modifier.fillMaxWidth().testTag("custom_measurements_input"),
                )
              }
            }
          }
        }
      }

      // Customer Reviews Section
      item {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Customer Reviews (${reviews.size})",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
          )
          TextButton(onClick = { showReviewDialog = true }) {
            Icon(
              imageVector = Icons.Outlined.RateReview,
              contentDescription = null,
              tint = RoyalGold,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Write Review", color = RoyalGold)
          }
        }
      }

      if (reviews.isEmpty()) {
        item {
          Text(
            text = "Be the first patron to review this couture piece.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          )
        }
      } else {
        items(reviews, key = { it.id }) { rev ->
          Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = rev.reviewerName,
                  style = MaterialTheme.typography.titleSmall,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Row {
                  repeat(rev.rating.toInt().coerceIn(1, 5)) {
                    Icon(
                      imageVector = Icons.Filled.Star,
                      contentDescription = null,
                      tint = ChampagneGold,
                      modifier = Modifier.size(14.dp),
                    )
                  }
                }
              }
              Text(
                text = rev.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    }
  }

  if (showReviewDialog) {
    var selectedStars by remember { mutableIntStateOf(5) }
    var commentText by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showReviewDialog = false },
      title = { Text("Share Your Experience") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (1..5).forEach { star ->
              Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "$star stars",
                tint = if (star <= selectedStars) RoyalGold else SterlingSilver.copy(alpha = 0.3f),
                modifier = Modifier.size(34.dp).clickable { selectedStars = star },
              )
            }
          }
          OutlinedTextField(
            value = commentText,
            onValueChange = { commentText = it },
            label = { Text("Your Review") },
            placeholder = { Text("Describe the embroidery, fit, and fabric feel...") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (commentText.trim().length >= 2) {
              onSubmitReview(selectedStars.toLong(), commentText)
              showReviewDialog = false
            }
          }
        ) {
          Text("Submit")
        }
      },
      dismissButton = {
        TextButton(onClick = { showReviewDialog = false }) { Text("Cancel") }
      },
    )
  }
}
