package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Product
import com.example.ui.theme.AntiqueBronze
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonVelvet
import com.example.ui.theme.EmeraldSilk
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.SterlingSilver
import com.example.ui.util.BoutiqueActions

@Composable
fun LuxurySectionHeader(
  title: String,
  subtitle: String? = null,
  actionLabel: String? = "View All",
  onActionClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Bottom,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (!subtitle.isNullOrBlank()) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
    if (actionLabel != null && onActionClick != null) {
      TextButton(onClick = onActionClick) {
        Text(
          text = actionLabel,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = actionLabel,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(16.dp),
        )
      }
    }
  }
}

@Composable
fun LuxuryProductCard(
  product: Product,
  isWishlisted: Boolean,
  onProductClick: () -> Unit,
  onToggleWishlist: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by
    animateFloatAsState(
      targetValue = if (isPressed) 0.97f else 1f,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "product_card_scale",
    )

  Card(
    modifier =
      modifier
        .scale(scale)
        .clip(RoundedCornerShape(16.dp))
        .clickable(interactionSource = interactionSource, indication = null) { onProductClick() }
        .testTag("product_card_${product.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
  ) {
    Column {
      Box(modifier = Modifier.fillMaxWidth().aspectRatio(0.84f)) {
        RazaProductImage(
          imageSource = product.images.firstOrNull().orEmpty(),
          contentDescription = product.name,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop,
        )

        // Subtle bottom gradient for badge legibility
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .height(64.dp)
              .align(Alignment.BottomCenter)
              .background(
                Brush.verticalGradient(
                  listOf(Color.Transparent, ObsidianBlack.copy(alpha = 0.72f))
                )
              )
        )

        // Discount / Offer Badge
        val badgeText =
          when {
            product.offerBadge.isNotBlank() -> product.offerBadge
            product.discountPercent > 0 -> "${product.discountPercent}% OFF"
            else -> ""
          }
        if (badgeText.isNotBlank()) {
          Surface(
            color = RoyalGold,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
          ) {
            Text(
              text = badgeText,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = ObsidianBlack,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
          }
        }

        // Wishlist button with 48dp touch target
        IconButton(
          onClick = onToggleWishlist,
          modifier =
            Modifier.align(Alignment.TopEnd)
              .padding(4.dp)
              .size(48.dp)
              .testTag("wishlist_toggle_${product.id}"),
        ) {
          Box(
            modifier =
              Modifier.size(34.dp)
                .clip(CircleShape)
                .background(ObsidianBlack.copy(alpha = 0.65f))
                .border(1.dp, SterlingSilver.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector =
                if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
              contentDescription = if (isWishlisted) "Remove from wishlist" else "Save to wishlist",
              tint = if (isWishlisted) CrimsonVelvet else ChampagneGold,
              modifier = Modifier.size(18.dp),
            )
          }
        }

        if (product.madeToOrder || product.stockCount <= 0L) {
          Surface(
            color =
              if (product.stockCount <= 0L) CrimsonVelvet.copy(alpha = 0.9f)
              else ObsidianBlack.copy(alpha = 0.78f),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(0.8.dp, RoyalGold.copy(alpha = 0.6f)),
            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
          ) {
            Text(
              text = if (product.stockCount <= 0L) "Sold Out" else "Made to Order",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            )
          }
        }
      }

      Column(modifier = Modifier.padding(12.dp)) {
        Text(
          text = product.category.uppercase(),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = product.name,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          minLines = 2,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = BoutiqueActions.formatInr(product.effectivePrice),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
          )
          if (product.discountPrice > 0 && product.discountPrice < product.price) {
            Text(
              text = BoutiqueActions.formatInr(product.price),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textDecoration = TextDecoration.LineThrough,
            )
          }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = ChampagneGold,
            modifier = Modifier.size(13.dp),
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "${product.rating} (${product.reviewCount})",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@Composable
fun GoldShimmerPill(
  text: String,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .clip(RoundedCornerShape(50))
        .background(Brush.horizontalGradient(listOf(AntiqueBronze, RoyalGold, ChampagneGold)))
        .padding(horizontal = 12.dp, vertical = 5.dp)
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = ObsidianBlack,
    )
  }
}

@Composable
fun OrderTrackingTimeline(
  currentStatus: String,
  modifier: Modifier = Modifier,
) {
  val steps = listOf("Placed", "Confirmed", "Stitching", "Quality Check", "Shipped", "Delivered")
  if (currentStatus == "Cancelled") {
    Surface(
      color = CrimsonVelvet.copy(alpha = 0.14f),
      shape = RoundedCornerShape(10.dp),
      border = BorderStroke(1.dp, CrimsonVelvet.copy(alpha = 0.5f)),
      modifier = modifier.fillMaxWidth(),
    ) {
      Text(
        text = "Order Cancelled",
        style = MaterialTheme.typography.titleSmall,
        color = CrimsonVelvet,
        modifier = Modifier.padding(12.dp),
      )
    }
    return
  }

  val currentIndex = steps.indexOf(currentStatus).coerceAtLeast(0)
  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    steps.forEachIndexed { idx, step ->
      val reached = idx <= currentIndex
      val isCurrent = idx == currentIndex
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Icon(
          imageVector =
            if (reached) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
          contentDescription = step,
          tint =
            when {
              currentStatus == "Delivered" && reached -> EmeraldSilk
              reached -> MaterialTheme.colorScheme.primary
              else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            },
          modifier = Modifier.size(18.dp),
        )
        Text(
          text = step,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
          color =
            if (reached) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
        if (isCurrent) {
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Current",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }
  }
}
