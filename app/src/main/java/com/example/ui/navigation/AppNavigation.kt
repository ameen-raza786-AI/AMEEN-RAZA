package com.example.ui.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.firebase.authStateFlow
import com.example.data.firebase.signOutUser
import com.example.data.repository.StoreRepository
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BoutiqueInfoScreen
import com.example.ui.screens.CartAndWishlistScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RazaEthnicWearTheme
import com.example.ui.theme.RoyalGold
import com.example.ui.viewmodel.StoreViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

object Routes {
  const val HOME = "home"
  const val CATALOG = "catalog"
  const val BAG = "bag"
  const val ORDERS = "orders"
  const val PROFILE = "profile"
  const val PRODUCT_DETAIL = "product/{productId}"
  const val CHECKOUT = "checkout/{directBuy}"
  const val ADMIN = "admin"
  const val INFO = "info/{pageType}"

  fun productDetail(productId: String) = "product/$productId"
  fun checkout(directBuy: Boolean) = "checkout/$directBuy"
  fun info(pageType: String) = "info/$pageType"
}

private data class BottomNavDest(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
  val currentUser by
    auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

  val user = currentUser
  if (user == null) {
    RazaEthnicWearTheme(darkTheme = true) {
      AuthScreen(onAuthSuccess = {})
    }
  } else {
    key(user.uid) {
      AuthenticatedStoreShell(user = user)
    }
  }
}

@Composable
private fun AuthenticatedStoreShell(user: FirebaseUser) {
  val storeViewModel: StoreViewModel =
    viewModel(
      key = user.uid,
      factory =
        viewModelFactory {
          initializer {
            val app =
              checkNotNull(this[APPLICATION_KEY]) {
                "APPLICATION_KEY missing from CreationExtras"
              }
            val databaseId = app.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(databaseId)
            StoreViewModel(
              repository = StoreRepository(db, Firebase.auth),
              currentUserId = user.uid,
              userDisplayName = user.displayName ?: "Valued Patron",
              userEmail = user.email ?: "",
              isEmailVerified = user.isEmailVerified,
              userPhotoUrl = user.photoUrl?.toString().orEmpty(),
            )
          }
        },
    )

  val uiState by storeViewModel.uiState.collectAsStateWithLifecycle()
  val directBuyItem by storeViewModel.directBuyItem.collectAsStateWithLifecycle()
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val snackbarHostState = remember { SnackbarHostState() }
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  var bagInitialTab by remember { mutableIntStateOf(0) }
  var pendingPromoCode by remember { mutableStateOf("") }
  var pendingDiscountAmount by remember { mutableDoubleStateOf(0.0) }

  LaunchedEffect(uiState.message, uiState.errorMessage) {
    val msg = uiState.errorMessage ?: uiState.message
    if (!msg.isNullOrBlank()) {
      snackbarHostState.showSnackbar(msg)
      storeViewModel.clearMessages()
    }
  }

  val navItems = remember {
    listOf(
      BottomNavDest(Routes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
      BottomNavDest(Routes.CATALOG, "Shop", Icons.Filled.GridView, Icons.Outlined.GridView),
      BottomNavDest(Routes.BAG, "Bag", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
      BottomNavDest(Routes.ORDERS, "Orders", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
      BottomNavDest(Routes.PROFILE, "Account", Icons.Filled.Person, Icons.Outlined.Person),
    )
  }

  val showTopLevelBar =
    currentRoute in setOf(Routes.HOME, Routes.CATALOG, Routes.BAG, Routes.ORDERS, Routes.PROFILE)

  fun navigateTopLevel(route: String) {
    navController.navigate(route) {
      popUpTo(Routes.HOME) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  RazaEthnicWearTheme(darkTheme = uiState.darkTheme) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val useRail = maxWidth >= 600.dp

      Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
          if (showTopLevelBar && !useRail) {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surface,
              tonalElevation = 8.dp,
            ) {
              navItems.forEach { dest ->
                val selected = currentRoute == dest.route
                NavigationBarItem(
                  selected = selected,
                  onClick = {
                    if (dest.route == Routes.BAG) bagInitialTab = 0
                    navigateTopLevel(dest.route)
                  },
                  icon = {
                    val icon = if (selected) dest.selectedIcon else dest.unselectedIcon
                    if (dest.route == Routes.BAG && uiState.cartCount > 0) {
                      BadgedBox(
                        badge = {
                          Badge(containerColor = RoyalGold, contentColor = ObsidianBlack) {
                            Text("${uiState.cartCount}")
                          }
                        }
                      ) {
                        Icon(icon, contentDescription = dest.label)
                      }
                    } else {
                      Icon(icon, contentDescription = dest.label)
                    }
                  },
                  label = { Text(dest.label, style = MaterialTheme.typography.labelSmall) },
                  colors =
                    NavigationBarItemDefaults.colors(
                      selectedIconColor = ObsidianBlack,
                      selectedTextColor = RoyalGold,
                      indicatorColor = RoyalGold,
                    ),
                )
              }
            }
          }
        },
      ) { innerPadding ->
        Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
          if (showTopLevelBar && useRail) {
            NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
              navItems.forEach { dest ->
                val selected = currentRoute == dest.route
                NavigationRailItem(
                  selected = selected,
                  onClick = {
                    if (dest.route == Routes.BAG) bagInitialTab = 0
                    navigateTopLevel(dest.route)
                  },
                  icon = {
                    Icon(
                      if (selected) dest.selectedIcon else dest.unselectedIcon,
                      contentDescription = dest.label,
                    )
                  },
                  label = { Text(dest.label) },
                )
              }
            }
          }

          NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.weight(1f).fillMaxSize(),
          ) {
            composable(Routes.HOME) {
              HomeScreen(
                uiState = uiState,
                onOpenSearch = {
                  storeViewModel.clearCatalogFilters()
                  navigateTopLevel(Routes.CATALOG)
                },
                onOpenWishlist = {
                  bagInitialTab = 1
                  navigateTopLevel(Routes.BAG)
                },
                onOpenCart = {
                  bagInitialTab = 0
                  navigateTopLevel(Routes.BAG)
                },
                onOpenCategory = { category ->
                  storeViewModel.clearCatalogFilters()
                  storeViewModel.selectCategoryFilter(category)
                  navigateTopLevel(Routes.CATALOG)
                },
                onOpenSection = { section ->
                  storeViewModel.clearCatalogFilters()
                  storeViewModel.selectSectionFilter(section)
                  navigateTopLevel(Routes.CATALOG)
                },
                onOpenProduct = { id -> navController.navigate(Routes.productDetail(id)) },
                onToggleWishlist = { storeViewModel.toggleWishlist(it) },
                onCopyPromoCode = { code ->
                  scope.launch {
                    snackbarHostState.showSnackbar("Copied promo code $code")
                  }
                },
                onOpenInfoPage = { page -> navController.navigate(Routes.info(page)) },
              )
            }

            composable(Routes.CATALOG) {
              CatalogScreen(
                uiState = uiState,
                onSearchQueryChange = storeViewModel::updateSearchQuery,
                onCategorySelect = storeViewModel::selectCategoryFilter,
                onSectionSelect = storeViewModel::selectSectionFilter,
                onSortSelect = storeViewModel::selectSortOption,
                onClearFilters = storeViewModel::clearCatalogFilters,
                onProductClick = { id -> navController.navigate(Routes.productDetail(id)) },
                onToggleWishlist = storeViewModel::toggleWishlist,
              )
            }

            composable(Routes.BAG) {
              CartAndWishlistScreen(
                uiState = uiState,
                initialTab = bagInitialTab,
                onUpdateQuantity = storeViewModel::updateCartQuantity,
                onRemoveCartItem = storeViewModel::removeFromCart,
                onProceedToCheckout = { promo, discount ->
                  pendingPromoCode = promo
                  pendingDiscountAmount = discount
                  navController.navigate(Routes.checkout(directBuy = false))
                },
                onMoveWishlistToBag = storeViewModel::moveWishlistItemToBag,
                onRemoveWishlistItem = storeViewModel::toggleWishlist,
                onOpenProduct = { id -> navController.navigate(Routes.productDetail(id)) },
                onExploreStore = { navigateTopLevel(Routes.CATALOG) },
              )
            }

            composable(Routes.ORDERS) {
              OrdersScreen(
                orders = uiState.orders,
                onCancelOrder = storeViewModel::cancelOrder,
                onExploreStore = { navigateTopLevel(Routes.CATALOG) },
              )
            }

            composable(Routes.PROFILE) {
              ProfileScreen(
                uiState = uiState,
                onSaveProfile = storeViewModel::updateProfile,
                onToggleTheme = storeViewModel::toggleTheme,
                onOpenWishlist = {
                  bagInitialTab = 1
                  navigateTopLevel(Routes.BAG)
                },
                onOpenOrders = { navigateTopLevel(Routes.ORDERS) },
                onOpenAdminDashboard = { navController.navigate(Routes.ADMIN) },
                onOpenInfoPage = { page -> navController.navigate(Routes.info(page)) },
                onSignOut = {
                  signOutUser(credentialManager = credentialManager, scope = scope)
                },
              )
            }

            composable(
              route = Routes.PRODUCT_DETAIL,
              arguments = listOf(navArgument("productId") { type = NavType.StringType }),
            ) { backStackEntry ->
              val productId = backStackEntry.arguments?.getString("productId").orEmpty()
              val product =
                uiState.products.firstOrNull { it.id == productId }
                  ?: uiState.products.firstOrNull()
              if (product != null) {
                val productReviews = uiState.reviews.filter { it.productId == product.id }
                ProductDetailScreen(
                  product = product,
                  isWishlisted = uiState.wishlistProductIds.contains(product.id),
                  reviews = productReviews,
                  onBack = { navController.popBackStack() },
                  onToggleWishlist = { storeViewModel.toggleWishlist(product) },
                  onAddToCart = { size, color, customStitch, mto, notes ->
                    storeViewModel.addToCart(product, size, color, customStitch, mto, notes)
                  },
                  onBuyNow = { size, color, customStitch, mto, notes ->
                    storeViewModel.setDirectBuyItem(product, size, color, customStitch, mto, notes)
                    pendingPromoCode = ""
                    pendingDiscountAmount = 0.0
                    navController.navigate(Routes.checkout(directBuy = true))
                  },
                  onSubmitReview = { rating, comment ->
                    storeViewModel.submitReview(product.id, rating, comment)
                  },
                )
              }
            }

            composable(
              route = Routes.CHECKOUT,
              arguments = listOf(navArgument("directBuy") { type = NavType.BoolType }),
            ) { backStackEntry ->
              val isDirectBuy = backStackEntry.arguments?.getBoolean("directBuy") ?: false
              val items =
                if (isDirectBuy) listOfNotNull(directBuyItem) else uiState.cartItems
              val subtotal = items.sumOf { it.unitPrice * it.quantity }
              val shipping = if (subtotal >= 10000.0 || subtotal == 0.0) 0.0 else 499.0
              val discount = if (isDirectBuy) 0.0 else pendingDiscountAmount
              val total = (subtotal - discount + shipping).coerceAtLeast(0.0)

              CheckoutScreen(
                uiState = uiState,
                itemsToCheckout = items,
                promoCode = if (isDirectBuy) "" else pendingPromoCode,
                discountAmount = discount,
                isDirectBuy = isDirectBuy,
                onBack = { navController.popBackStack() },
                onConfirmOrder = { name, phone, address, payMethod, payStatus, txnId ->
                  storeViewModel.placeOrder(
                    customerName = name,
                    customerPhone = phone,
                    shippingAddress = address,
                    items = items,
                    subtotal = subtotal,
                    discountAmount = discount,
                    shippingFee = shipping,
                    totalAmount = total,
                    promoCode = if (isDirectBuy) "" else pendingPromoCode,
                    paymentMethod = payMethod,
                    paymentStatus = payStatus,
                    transactionId = txnId,
                    clearCart = !isDirectBuy,
                    onSuccess = {
                      navController.navigate(Routes.ORDERS) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                      }
                    },
                  )
                },
              )
            }

            composable(Routes.ADMIN) {
              AdminDashboardScreen(
                uiState = uiState,
                onSaveProduct = { storeViewModel.saveProductAsAdmin(it) },
                onDeleteProduct = storeViewModel::deleteProductAsAdmin,
                onSaveCategory = { storeViewModel.saveCategoryAsAdmin(it) },
                onSaveOffer = { storeViewModel.saveOfferAsAdmin(it) },
                onUpdateOrderStatus = storeViewModel::updateOrderStatusAsAdmin,
                onBack = { navController.popBackStack() },
              )
            }

            composable(
              route = Routes.INFO,
              arguments = listOf(navArgument("pageType") { type = NavType.StringType }),
            ) { backStackEntry ->
              val pageType = backStackEntry.arguments?.getString("pageType") ?: "about"
              BoutiqueInfoScreen(
                pageType = pageType,
                uiState = uiState,
                onSubmitInquiry = storeViewModel::submitInquiry,
                onBack = { navController.popBackStack() },
              )
            }
          }
        }
      }
    }
  }
}
