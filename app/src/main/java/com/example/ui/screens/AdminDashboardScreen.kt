package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Category
import com.example.data.model.Offer
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.ui.components.RazaProductImage
import com.example.ui.theme.CrimsonVelvet
import com.example.ui.theme.EmeraldSilk
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalGold
import com.example.ui.util.BoutiqueActions
import com.example.ui.viewmodel.StoreUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
  uiState: StoreUiState,
  onSaveProduct: (Product) -> Unit,
  onDeleteProduct: (String) -> Unit,
  onSaveCategory: (Category) -> Unit,
  onSaveOffer: (Offer) -> Unit,
  onUpdateOrderStatus: (orderId: String, status: String, trackingNote: String, paymentStatus: String) -> Unit,
  onBack: () -> Unit,
) {
  BackHandler { onBack() }

  var selectedTab by remember { mutableIntStateOf(0) }
  var editingProduct by remember { mutableStateOf<Product?>(null) }
  var showAddProduct by remember { mutableStateOf(false) }
  var editingCategory by remember { mutableStateOf<Category?>(null) }
  var showAddCategory by remember { mutableStateOf(false) }
  var editingOffer by remember { mutableStateOf<Offer?>(null) }
  var showAddOffer by remember { mutableStateOf(false) }
  var editingOrder by remember { mutableStateOf<Order?>(null) }

  val products = uiState.adminProducts.ifEmpty { uiState.products }
  val categories = uiState.adminCategories.ifEmpty { uiState.categories }
  val offers = uiState.adminOffers.ifEmpty { uiState.offers }
  val totalRevenue = uiState.adminOrders.filter { it.status != "Cancelled" }.sumOf { it.totalAmount }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text("Admin Boutique Studio", style = MaterialTheme.typography.headlineMedium, color = RoyalGold)
            Text("Raza Ethnic Wear Owner Panel", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
      )
    },
    floatingActionButton = {
      when (selectedTab) {
        0 -> FloatingActionButton(
          onClick = { showAddProduct = true },
          containerColor = RoyalGold,
          contentColor = ObsidianBlack,
        ) {
          Icon(Icons.Filled.Add, contentDescription = "Add Product")
        }
        1 -> FloatingActionButton(
          onClick = { showAddCategory = true },
          containerColor = RoyalGold,
          contentColor = ObsidianBlack,
        ) {
          Icon(Icons.Filled.Add, contentDescription = "Add Category")
        }
        3 -> FloatingActionButton(
          onClick = { showAddOffer = true },
          containerColor = RoyalGold,
          contentColor = ObsidianBlack,
        ) {
          Icon(Icons.Filled.Add, contentDescription = "Add Offer")
        }
      }
    },
  ) { innerPadding ->
    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      // KPI Strip
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        AdminKpiCard("Products", "${products.size}", Modifier.weight(1f))
        AdminKpiCard("Orders", "${uiState.adminOrders.size}", Modifier.weight(1f))
        AdminKpiCard("Customers", "${uiState.adminCustomers.size}", Modifier.weight(1f))
        AdminKpiCard("Revenue", BoutiqueActions.formatInr(totalRevenue), Modifier.weight(1.3f))
      }

      val tabs = listOf("Products & Stock", "Categories", "Orders", "Offers", "Customers")
      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = RoyalGold,
        edgePadding = 12.dp,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
            color = RoyalGold,
          )
        },
      ) {
        tabs.forEachIndexed { idx, title ->
          Tab(
            selected = selectedTab == idx,
            onClick = { selectedTab = idx },
            text = { Text(title, style = MaterialTheme.typography.labelLarge) },
          )
        }
      }

      when (selectedTab) {
        0 -> AdminProductsTab(
          products = products,
          onEdit = { editingProduct = it },
          onUpdateStock = { prod, nextStock ->
            onSaveProduct(prod.copy(stockCount = nextStock.coerceAtLeast(0L)))
          },
          onDelete = { onDeleteProduct(it.id) },
        )
        1 -> AdminCategoriesTab(
          categories = categories,
          onEdit = { editingCategory = it },
          onToggleActive = { cat, active -> onSaveCategory(cat.copy(isActive = active)) },
        )
        2 -> AdminOrdersTab(
          orders = uiState.adminOrders,
          onManageOrder = { editingOrder = it },
        )
        3 -> AdminOffersTab(
          offers = offers,
          onEdit = { editingOffer = it },
          onToggleActive = { off, active -> onSaveOffer(off.copy(isActive = active)) },
        )
        4 -> AdminCustomersTab(uiState = uiState)
      }
    }
  }

  if (showAddProduct || editingProduct != null) {
    AdminProductEditorDialog(
      initialProduct = editingProduct,
      categories = categories,
      onDismiss = {
        showAddProduct = false
        editingProduct = null
      },
      onSave = { prod ->
        onSaveProduct(prod)
        showAddProduct = false
        editingProduct = null
      },
    )
  }

  if (showAddCategory || editingCategory != null) {
    AdminCategoryDialog(
      initial = editingCategory,
      onDismiss = {
        showAddCategory = false
        editingCategory = null
      },
      onSave = { cat ->
        onSaveCategory(cat)
        showAddCategory = false
        editingCategory = null
      },
    )
  }

  if (showAddOffer || editingOffer != null) {
    AdminOfferDialog(
      initial = editingOffer,
      onDismiss = {
        showAddOffer = false
        editingOffer = null
      },
      onSave = { off ->
        onSaveOffer(off)
        showAddOffer = false
        editingOffer = null
      },
    )
  }

  if (editingOrder != null) {
    AdminOrderStatusDialog(
      order = editingOrder!!,
      onDismiss = { editingOrder = null },
      onSave = { status, note, paymentStatus ->
        onUpdateOrderStatus(editingOrder!!.id, status, note, paymentStatus)
        editingOrder = null
      },
    )
  }
}

@Composable
private fun AdminKpiCard(label: String, value: String, modifier: Modifier = Modifier) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.45f)),
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(value, style = MaterialTheme.typography.titleMedium, color = RoyalGold, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
  }
}

@Composable
private fun AdminProductsTab(
  products: List<Product>,
  onEdit: (Product) -> Unit,
  onUpdateStock: (Product, Long) -> Unit,
  onDelete: (Product) -> Unit,
) {
  LazyColumn(
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 84.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    items(products, key = { it.id }) { prod ->
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          RazaProductImage(
            imageSource = prod.images.firstOrNull().orEmpty(),
            contentDescription = prod.name,
            modifier = Modifier.size(68.dp).clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop,
          )
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(prod.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${prod.category} • ${BoutiqueActions.formatInr(prod.effectivePrice)} (MRP ${BoutiqueActions.formatInr(prod.price)})", style = MaterialTheme.typography.bodySmall, color = RoyalGold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("Stock: ${prod.stockCount}", style = MaterialTheme.typography.labelSmall)
              IconButton(onClick = { onUpdateStock(prod, prod.stockCount - 1L) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease stock", modifier = Modifier.size(16.dp))
              }
              IconButton(onClick = { onUpdateStock(prod, prod.stockCount + 1L) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Increase stock", modifier = Modifier.size(16.dp))
              }
            }
          }
          Column {
            IconButton(onClick = { onEdit(prod) }) {
              Icon(Icons.Filled.Edit, contentDescription = "Edit product", tint = RoyalGold)
            }
            IconButton(onClick = { onDelete(prod) }) {
              Icon(Icons.Filled.Delete, contentDescription = "Delete product", tint = CrimsonVelvet)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AdminCategoriesTab(
  categories: List<Category>,
  onEdit: (Category) -> Unit,
  onToggleActive: (Category, Boolean) -> Unit,
) {
  LazyColumn(
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 84.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    items(categories, key = { it.id }) { cat ->
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(cat.name, style = MaterialTheme.typography.titleMedium)
            Text("${cat.subtitle} (Order #${cat.sortOrder})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = cat.isActive, onCheckedChange = { onToggleActive(cat, it) })
            IconButton(onClick = { onEdit(cat) }) {
              Icon(Icons.Filled.Edit, contentDescription = "Edit category", tint = RoyalGold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AdminOrdersTab(
  orders: List<Order>,
  onManageOrder: (Order) -> Unit,
) {
  if (orders.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
      Text("No customer orders placed yet.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  } else {
    LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      items(orders, key = { it.id }) { order ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.45f)),
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(order.orderNumber, style = MaterialTheme.typography.titleMedium, color = RoyalGold, fontWeight = FontWeight.Bold)
              Surface(color = if (order.status == "Delivered") EmeraldSilk else RoyalGold, shape = RoundedCornerShape(6.dp)) {
                Text(
                  order.status,
                  style = MaterialTheme.typography.labelSmall,
                  color = ObsidianBlack,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
              }
            }
            Text("${order.customerName} • ${order.customerPhone}", style = MaterialTheme.typography.bodySmall)
            Text(order.shippingAddress, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${order.items.size} items • ${BoutiqueActions.formatInr(order.totalAmount)} • ${order.paymentMethod} (${order.paymentStatus})", style = MaterialTheme.typography.labelMedium)
            Button(
              onClick = { onManageOrder(order) },
              colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianBlack),
              modifier = Modifier.fillMaxWidth().height(42.dp),
            ) {
              Text("Update Order & Tracking Status", fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AdminOffersTab(
  offers: List<Offer>,
  onEdit: (Offer) -> Unit,
  onToggleActive: (Offer, Boolean) -> Unit,
) {
  LazyColumn(
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 84.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    items(offers, key = { it.id }) { offer ->
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("${offer.promoCode} • ${offer.discountPercent}% OFF", style = MaterialTheme.typography.titleMedium, color = RoyalGold, fontWeight = FontWeight.Bold)
            Text(offer.title, style = MaterialTheme.typography.titleSmall)
            Text("${offer.subtitle} (Min ${BoutiqueActions.formatInr(offer.minOrderAmount)})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = offer.isActive, onCheckedChange = { onToggleActive(offer, it) })
            IconButton(onClick = { onEdit(offer) }) {
              Icon(Icons.Filled.Edit, contentDescription = "Edit offer", tint = RoyalGold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AdminCustomersTab(uiState: StoreUiState) {
  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    item {
      Text("Registered Customers (${uiState.adminCustomers.size})", style = MaterialTheme.typography.titleLarge)
    }
    items(uiState.adminCustomers, key = { it.id }) { cust ->
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(cust.displayName, style = MaterialTheme.typography.titleMedium)
          Text(cust.email, style = MaterialTheme.typography.bodySmall, color = RoyalGold)
          if (cust.phone.isNotBlank()) Text("Phone: ${cust.phone}", style = MaterialTheme.typography.bodySmall)
          if (cust.address.isNotBlank()) Text("Address: ${cust.address}, ${cust.city} ${cust.postalCode}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("Orders: ${cust.totalOrders} • Total Spent: ${BoutiqueActions.formatInr(cust.totalSpent)}", style = MaterialTheme.typography.labelMedium)
        }
      }
    }

    if (uiState.adminInquiries.isNotEmpty()) {
      item {
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Customer Inquiries (${uiState.adminInquiries.size})", style = MaterialTheme.typography.titleLarge)
      }
      items(uiState.adminInquiries, key = { it.id }) { inq ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(inq.subject, style = MaterialTheme.typography.titleSmall, color = RoyalGold)
            Text("${inq.name} (${inq.email})", style = MaterialTheme.typography.bodySmall)
            Text(inq.message, style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
    }
  }
}

@Composable
private fun AdminCategoryDialog(
  initial: Category?,
  onDismiss: () -> Unit,
  onSave: (Category) -> Unit,
) {
  var name by remember { mutableStateOf(initial?.name.orEmpty()) }
  var subtitle by remember { mutableStateOf(initial?.subtitle.orEmpty()) }
  var orderText by remember { mutableStateOf(initial?.sortOrder?.toString() ?: "10") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (initial == null) "Add Category" else "Edit Category") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Category Name") })
        OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text("Subtitle") })
        OutlinedTextField(value = orderText, onValueChange = { orderText = it.filter { c -> c.isDigit() } }, label = { Text("Display Order") })
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.trim().length >= 2) {
            onSave(
              Category(
                id = initial?.id.orEmpty(),
                name = name.trim(),
                subtitle = subtitle.trim().ifBlank { "Curated ethnic couture" },
                imageKey = initial?.imageKey ?: "drawable:img_hero_bridal",
                sortOrder = orderText.toLongOrNull() ?: 10L,
                isActive = initial?.isActive ?: true,
              )
            )
          }
        }
      ) { Text("Save") }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}

@Composable
private fun AdminOfferDialog(
  initial: Offer?,
  onDismiss: () -> Unit,
  onSave: (Offer) -> Unit,
) {
  var title by remember { mutableStateOf(initial?.title.orEmpty()) }
  var subtitle by remember { mutableStateOf(initial?.subtitle.orEmpty()) }
  var promoCode by remember { mutableStateOf(initial?.promoCode.orEmpty()) }
  var discountText by remember { mutableStateOf(initial?.discountPercent?.toString() ?: "15") }
  var minOrderText by remember { mutableStateOf(initial?.minOrderAmount?.toLong()?.toString() ?: "10000") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (initial == null) "Create Offer / Discount" else "Edit Offer") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = promoCode, onValueChange = { promoCode = it.uppercase() }, label = { Text("Promo Code (e.g. RAZA20)") })
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Offer Title") })
        OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text("Description") })
        OutlinedTextField(value = discountText, onValueChange = { discountText = it.filter { c -> c.isDigit() } }, label = { Text("Discount % (1-90)") })
        OutlinedTextField(value = minOrderText, onValueChange = { minOrderText = it.filter { c -> c.isDigit() } }, label = { Text("Min Order (₹)") })
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (promoCode.trim().length >= 2 && title.trim().length >= 2) {
            onSave(
              Offer(
                id = initial?.id.orEmpty(),
                title = title.trim(),
                subtitle = subtitle.trim(),
                promoCode = promoCode.trim(),
                discountPercent = (discountText.toLongOrNull() ?: 10L).coerceIn(1L, 90L),
                minOrderAmount = minOrderText.toDoubleOrNull() ?: 0.0,
                isActive = initial?.isActive ?: true,
              )
            )
          }
        }
      ) { Text("Save") }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminOrderStatusDialog(
  order: Order,
  onDismiss: () -> Unit,
  onSave: (status: String, note: String, paymentStatus: String) -> Unit,
) {
  var status by remember { mutableStateOf(order.status) }
  var paymentStatus by remember { mutableStateOf(order.paymentStatus) }
  var trackingNote by remember { mutableStateOf(order.trackingNote) }

  val statuses = listOf("Placed", "Confirmed", "Stitching", "Quality Check", "Shipped", "Delivered", "Cancelled")
  val payStatuses = listOf("Paid", "Pending on Delivery", "Refunded")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Manage ${order.orderNumber}") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Order Status", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          statuses.forEach { s ->
            FilterChip(
              selected = status == s,
              onClick = { status = s },
              label = { Text(s, style = MaterialTheme.typography.labelSmall) },
            )
          }
        }
        Text("Payment Status", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          payStatuses.forEach { ps ->
            FilterChip(
              selected = paymentStatus == ps,
              onClick = { paymentStatus = ps },
              label = { Text(ps, style = MaterialTheme.typography.labelSmall) },
            )
          }
        }
        OutlinedTextField(
          value = trackingNote,
          onValueChange = { trackingNote = it },
          label = { Text("Tracking / Atelier Note") },
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      Button(onClick = { onSave(status, trackingNote, paymentStatus) }) { Text("Update") }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}
