package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Order
import com.example.ui.components.OrderTrackingTimeline
import com.example.ui.components.RazaProductImage
import com.example.ui.theme.CrimsonVelvet
import com.example.ui.theme.EmeraldSilk
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalGold
import com.example.ui.util.BoutiqueActions
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun OrdersScreen(
  orders: List<Order>,
  onCancelOrder: (String) -> Unit,
  onExploreStore: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
      Text(
        text = "Orders & Live Tracking",
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onBackground,
      )
      Text(
        text = "Track stitching, quality check & insured dispatch",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    if (orders.isEmpty()) {
      EmptyStateView(
        icon = Icons.Outlined.ReceiptLong,
        title = "No Orders Yet",
        subtitle = "Your couture orders, tailoring updates & order history will appear here.",
        ctaLabel = "Browse Collection",
        onCta = onExploreStore,
      )
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        items(orders, key = { it.id }) { order ->
          OrderHistoryCard(order = order, onCancelOrder = { onCancelOrder(order.id) })
        }
      }
    }
  }
}

@Composable
private fun OrderHistoryCard(
  order: Order,
  onCancelOrder: () -> Unit,
) {
  val context = LocalContext.current
  var expanded by remember { mutableStateOf(true) }
  val dateLabel =
    remember(order.createdAt) {
      val date = order.createdAt?.toDate()
      if (date != null) SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(date)
      else "Just now"
    }

  val badgeColor =
    when (order.status) {
      "Delivered" -> EmeraldSilk
      "Cancelled" -> CrimsonVelvet
      else -> RoyalGold
    }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = order.orderNumber,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = dateLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Surface(color = badgeColor, shape = RoundedCornerShape(8.dp)) {
            Text(
              text = order.status,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = if (badgeColor == RoyalGold) ObsidianBlack else androidx.compose.ui.graphics.Color.White,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            )
          }
          Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = "Toggle tracking details",
          )
        }
      }

      // Items Preview
      order.items.forEach { item ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          RazaProductImage(
            imageSource = item.productImage,
            contentDescription = item.productName,
            modifier = Modifier.size(54.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(item.productName, style = MaterialTheme.typography.titleSmall, maxLines = 1)
            Text(
              text = "Size ${item.selectedSize} • ${item.selectedColor} • Qty ${item.quantity}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Text(
            text = BoutiqueActions.formatInr(item.unitPrice * item.quantity),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
          text = "${order.paymentMethod} (${order.paymentStatus})",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = "Total: ${BoutiqueActions.formatInr(order.totalAmount)}",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold,
        )
      }

      AnimatedVisibility(visible = expanded) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          if (order.trackingNote.isNotBlank()) {
            Text(
              text = "Atelier Update: ${order.trackingNote}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary,
            )
          }
          Text(
            text = "Delivery to: ${order.shippingAddress} • Est. ${order.estimatedDelivery}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          OrderTrackingTimeline(currentStatus = order.status)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OutlinedButton(
              onClick = { BoutiqueActions.launchWhatsAppOrderSupport(context, order) },
              border = BorderStroke(1.dp, EmeraldSilk),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldSilk),
              modifier = Modifier.weight(1f),
            ) {
              Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("WhatsApp Support", style = MaterialTheme.typography.labelMedium)
            }
            if (order.status == "Placed") {
              TextButton(
                onClick = onCancelOrder,
                colors = ButtonDefaults.textButtonColors(contentColor = CrimsonVelvet),
              ) {
                Text("Cancel Order")
              }
            }
          }
        }
      }
    }
  }
}
