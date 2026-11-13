package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.StoreCatalogSeeder
import com.example.data.model.CartItem
import com.example.data.model.Category
import com.example.data.model.Inquiry
import com.example.data.model.Offer
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.Review
import com.example.data.model.UserProfile
import com.example.data.model.WishlistItem
import com.example.data.repository.StoreRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProductSortOption(val label: String) {
  FEATURED("Featured"),
  PRICE_LOW_HIGH("Price: Low to High"),
  PRICE_HIGH_LOW("Price: High to Low"),
  HIGHEST_DISCOUNT("Best Savings"),
}

data class CatalogFilterState(
  val searchQuery: String = "",
  val selectedCategory: String = "All",
  val selectedSection: String = "All",
  val sortOption: ProductSortOption = ProductSortOption.FEATURED,
)

data class StoreUiState(
  val isLoading: Boolean = true,
  val isAdmin: Boolean = false,
  val darkTheme: Boolean = true,
  val userProfile: UserProfile? = null,
  val categories: List<Category> = StoreCatalogSeeder.defaultCategories,
  val products: List<Product> = StoreCatalogSeeder.defaultProducts,
  val offers: List<Offer> = StoreCatalogSeeder.defaultOffers,
  val cartItems: List<CartItem> = emptyList(),
  val wishlistItems: List<WishlistItem> = emptyList(),
  val orders: List<Order> = emptyList(),
  val reviews: List<Review> = StoreCatalogSeeder.defaultReviews,
  val adminProducts: List<Product> = emptyList(),
  val adminCategories: List<Category> = emptyList(),
  val adminOffers: List<Offer> = emptyList(),
  val adminOrders: List<Order> = emptyList(),
  val adminCustomers: List<UserProfile> = emptyList(),
  val adminInquiries: List<Inquiry> = emptyList(),
  val filter: CatalogFilterState = CatalogFilterState(),
  val isBusy: Boolean = false,
  val message: String? = null,
  val errorMessage: String? = null,
) {
  val wishlistProductIds: Set<String>
    get() = wishlistItems.map { it.productId }.toSet()

  val cartCount: Int
    get() = cartItems.sumOf { it.quantity.toInt() }

  val filteredProducts: List<Product>
    get() {
      val q = filter.searchQuery.trim().lowercase()
      val base =
        products.filter { prod ->
          val matchesQuery =
            q.isBlank() ||
              prod.name.lowercase().contains(q) ||
              prod.category.lowercase().contains(q) ||
              prod.fabric.lowercase().contains(q) ||
              prod.description.lowercase().contains(q)
          val matchesCategory =
            filter.selectedCategory == "All" ||
              prod.category.equals(filter.selectedCategory, ignoreCase = true)
          val matchesSection =
            filter.selectedSection == "All" ||
              (filter.selectedSection == "Special Offers" && prod.isSpecialOffer) ||
              prod.collections.any { it.equals(filter.selectedSection, ignoreCase = true) }
          matchesQuery && matchesCategory && matchesSection
        }
      return when (filter.sortOption) {
        ProductSortOption.FEATURED -> base
        ProductSortOption.PRICE_LOW_HIGH -> base.sortedBy { it.effectivePrice }
        ProductSortOption.PRICE_HIGH_LOW -> base.sortedByDescending { it.effectivePrice }
        ProductSortOption.HIGHEST_DISCOUNT -> base.sortedByDescending { it.discountPercent }
      }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class StoreViewModel(
  private val repository: StoreRepository,
  private val currentUserId: String,
  private val userDisplayName: String,
  private val userEmail: String,
  private val isEmailVerified: Boolean,
  private val userPhotoUrl: String,
) : ViewModel() {

  private val _isAdmin = MutableStateFlow(false)
  private val _darkTheme = MutableStateFlow(true)
  private val _filter = MutableStateFlow(CatalogFilterState())
  private val _isBusy = MutableStateFlow(false)
  private val _message = MutableStateFlow<String?>(null)
  private val _errorMessage = MutableStateFlow<String?>(null)

  private val _directBuyItem = MutableStateFlow<CartItem?>(null)
  val directBuyItem: StateFlow<CartItem?> = _directBuyItem.asStateFlow()

  private val productsFlow =
    repository.observePublishedProducts().safeStream("products", emptyList())
  private val categoriesFlow =
    repository.observeActiveCategories().safeStream("categories", emptyList())
  private val offersFlow =
    repository.observeActiveOffers().safeStream("offers", emptyList())
  private val profileFlow =
    repository.observeUserProfile(currentUserId).safeStream("userProfile", null)
  private val cartFlow =
    repository.observeCart(currentUserId).safeStream("cart", emptyList())
  private val wishlistFlow =
    repository.observeWishlist(currentUserId).safeStream("wishlist", emptyList())
  private val ordersFlow =
    repository.observeUserOrders(currentUserId).safeStream("orders", emptyList())
  private val reviewsFlow =
    repository.observeApprovedReviews().safeStream("reviews", emptyList())

  private val adminProductsFlow =
    _isAdmin.flatMapLatest { admin ->
      if (admin) repository.observeAllProductsForAdmin().safeStream("adminProducts", emptyList())
      else flowOf(emptyList())
    }
  private val adminCategoriesFlow =
    _isAdmin.flatMapLatest { admin ->
      if (admin) repository.observeAllCategoriesForAdmin().safeStream("adminCategories", emptyList())
      else flowOf(emptyList())
    }
  private val adminOffersFlow =
    _isAdmin.flatMapLatest { admin ->
      if (admin) repository.observeAllOffersForAdmin().safeStream("adminOffers", emptyList())
      else flowOf(emptyList())
    }
  private val adminOrdersFlow =
    _isAdmin.flatMapLatest { admin ->
      if (admin) repository.observeAllOrdersForAdmin().safeStream("adminOrders", emptyList())
      else flowOf(emptyList())
    }
  private val adminCustomersFlow =
    _isAdmin.flatMapLatest { admin ->
      if (admin) repository.observeAllCustomersForAdmin().safeStream("adminCustomers", emptyList())
      else flowOf(emptyList())
    }
  private val adminInquiriesFlow =
    _isAdmin.flatMapLatest { admin ->
      if (admin) repository.observeAllInquiriesForAdmin().safeStream("adminInquiries", emptyList())
      else flowOf(emptyList())
    }

  val uiState: StateFlow<StoreUiState> =
    combine(
        combine(
          _isAdmin,
          _darkTheme,
          profileFlow,
          categoriesFlow,
          productsFlow,
        ) { isAdmin, darkTheme, profile, categories, products ->
          Tuple5(isAdmin, darkTheme, profile, categories, products)
        },
        combine(
          offersFlow,
          cartFlow,
          wishlistFlow,
          ordersFlow,
          reviewsFlow,
        ) { offers, cart, wishlist, orders, reviews ->
          Tuple5(offers, cart, wishlist, orders, reviews)
        },
        combine(
          adminProductsFlow,
          adminCategoriesFlow,
          adminOffersFlow,
          adminOrdersFlow,
          adminCustomersFlow,
        ) { aProd, aCat, aOffer, aOrder, aCust ->
          Tuple5(aProd, aCat, aOffer, aOrder, aCust)
        },
        combine(
          adminInquiriesFlow,
          _filter,
          _isBusy,
          _message,
          _errorMessage,
        ) { aInq, filter, busy, msg, err ->
          Tuple5(aInq, filter, busy, msg, err)
        },
      ) { base, shopper, adminData, uiMeta ->
        val resolvedCategories = base.d.ifEmpty { StoreCatalogSeeder.defaultCategories }
        val resolvedProducts = base.e.ifEmpty { StoreCatalogSeeder.defaultProducts }
        val resolvedOffers = shopper.a.ifEmpty { StoreCatalogSeeder.defaultOffers }
        val resolvedReviews = shopper.e.ifEmpty { StoreCatalogSeeder.defaultReviews }
        StoreUiState(
          isLoading = false,
          isAdmin = base.a,
          darkTheme = base.b,
          userProfile = base.c,
          categories = resolvedCategories,
          products = resolvedProducts,
          offers = resolvedOffers,
          cartItems = shopper.b,
          wishlistItems = shopper.c,
          orders = shopper.d,
          reviews = resolvedReviews,
          adminProducts = adminData.a,
          adminCategories = adminData.b,
          adminOffers = adminData.c,
          adminOrders = adminData.d,
          adminCustomers = adminData.e,
          adminInquiries = uiMeta.a,
          filter = uiMeta.b,
          isBusy = uiMeta.c,
          message = uiMeta.d,
          errorMessage = uiMeta.e,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = StoreUiState(isLoading = true),
      )

  init {
    bootstrapSession()
  }

  private fun bootstrapSession() {
    viewModelScope.launch {
      repository.ensureUserProfile(
        userId = currentUserId,
        displayName = userDisplayName,
        email = userEmail,
        photoUrl = userPhotoUrl,
      )
      val admin = repository.checkIsAdmin(currentUserId, userEmail, isEmailVerified)
      _isAdmin.value = admin
      if (admin) {
        repository.seedInitialCatalogIfAdmin()
      }
    }
  }

  fun toggleTheme() {
    _darkTheme.update { !it }
  }

  fun updateSearchQuery(query: String) {
    _filter.update { it.copy(searchQuery = query) }
  }

  fun selectCategoryFilter(category: String) {
    _filter.update { it.copy(selectedCategory = category) }
  }

  fun selectSectionFilter(section: String) {
    _filter.update { it.copy(selectedSection = section) }
  }

  fun selectSortOption(sort: ProductSortOption) {
    _filter.update { it.copy(sortOption = sort) }
  }

  fun clearCatalogFilters() {
    _filter.value = CatalogFilterState()
  }

  fun clearMessages() {
    _message.value = null
    _errorMessage.value = null
  }

  fun setDirectBuyItem(
    product: Product,
    size: String,
    color: String,
    customStitching: Boolean,
    madeToOrder: Boolean,
    customMeasurements: String,
  ) {
    _directBuyItem.value =
      CartItem(
        id = "",
        userId = currentUserId,
        productId = product.id,
        productName = product.name,
        productImage = product.images.firstOrNull().orEmpty(),
        category = product.category,
        price = product.price,
        discountPrice = product.discountPrice,
        selectedSize = size,
        selectedColor = color,
        customStitching = customStitching,
        madeToOrder = madeToOrder,
        customMeasurements = customMeasurements,
        estimatedDelivery = product.estimatedDelivery,
        quantity = 1L,
      )
  }

  fun clearDirectBuyItem() {
    _directBuyItem.value = null
  }

  fun addToCart(
    product: Product,
    size: String,
    color: String,
    customStitching: Boolean,
    madeToOrder: Boolean,
    customMeasurements: String,
  ) {
    viewModelScope.launch {
      _isBusy.value = true
      val res =
        repository.addToCart(
          product = product,
          selectedSize = size,
          selectedColor = color,
          customStitching = customStitching,
          madeToOrder = madeToOrder,
          customMeasurements = customMeasurements,
          quantity = 1L,
        )
      _isBusy.value = false
      if (res.isSuccess) {
        _message.value = "Added ${product.name} to your Shopping Bag"
      } else {
        _errorMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Unable to add to bag"
      }
    }
  }

  fun updateCartQuantity(cartItemId: String, newQty: Long) {
    viewModelScope.launch {
      repository.updateCartItemQuantity(cartItemId, newQty)
    }
  }

  fun removeFromCart(cartItemId: String) {
    viewModelScope.launch {
      repository.removeFromCart(cartItemId)
      _message.value = "Item removed from bag"
    }
  }

  fun toggleWishlist(product: Product) {
    val isWishlisted = uiState.value.wishlistProductIds.contains(product.id)
    viewModelScope.launch {
      val res = repository.toggleWishlist(product, isWishlisted)
      if (res.isSuccess) {
        _message.value =
          if (isWishlisted) "Removed from Wishlist" else "Saved to your Couture Wishlist"
      } else {
        _errorMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Wishlist update failed"
      }
    }
  }

  fun moveWishlistItemToBag(item: WishlistItem) {
    val product =
      uiState.value.products.find { it.id == item.productId }
        ?: Product(
          id = item.productId,
          name = item.productName,
          category = item.category,
          images = listOf(item.productImage),
          price = item.price,
          discountPrice = item.discountPrice,
          sizes = listOf("M"),
          colors = listOf("Signature"),
          estimatedDelivery = "7–10 business days",
        )
    addToCart(
      product = product,
      size = product.sizes.firstOrNull() ?: "M",
      color = product.colors.firstOrNull() ?: "Signature",
      customStitching = false,
      madeToOrder = product.madeToOrder,
      customMeasurements = "",
    )
  }

  fun updateProfile(
    displayName: String,
    phone: String,
    address: String,
    city: String,
    postalCode: String,
  ) {
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.updateUserProfile(displayName, phone, address, city, postalCode)
      _isBusy.value = false
      if (res.isSuccess) {
        _message.value = "Profile & shipping details saved"
      } else {
        _errorMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Could not update profile"
      }
    }
  }

  fun placeOrder(
    customerName: String,
    customerPhone: String,
    shippingAddress: String,
    items: List<CartItem>,
    subtotal: Double,
    discountAmount: Double,
    shippingFee: Double,
    totalAmount: Double,
    promoCode: String,
    paymentMethod: String,
    paymentStatus: String,
    transactionId: String,
    clearCart: Boolean,
    onSuccess: (String) -> Unit,
  ) {
    viewModelScope.launch {
      _isBusy.value = true
      val est = items.firstOrNull()?.estimatedDelivery ?: "7–12 business days"
      val res =
        repository.placeOrder(
          customerName = customerName,
          customerEmail = userEmail,
          customerPhone = customerPhone,
          shippingAddress = shippingAddress,
          items = items,
          subtotal = subtotal,
          discountAmount = discountAmount,
          shippingFee = shippingFee,
          totalAmount = totalAmount,
          promoCode = promoCode,
          paymentMethod = paymentMethod,
          paymentStatus = paymentStatus,
          transactionId = transactionId,
          estimatedDelivery = est,
          clearCart = clearCart,
        )
      _isBusy.value = false
      if (res.isSuccess) {
        if (!clearCart) _directBuyItem.value = null
        _message.value = "Order placed successfully! Thank you for choosing Raza Ethnic Wear."
        onSuccess(res.getOrThrow())
      } else {
        _errorMessage.value =
          res.exceptionOrNull()?.localizedMessage ?: "Unable to complete order"
      }
    }
  }

  fun cancelOrder(orderId: String) {
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.cancelOrder(orderId)
      _isBusy.value = false
      if (res.isSuccess) _message.value = "Order cancelled"
      else _errorMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Cancel failed"
    }
  }

  fun submitReview(productId: String, rating: Long, comment: String) {
    val name = uiState.value.userProfile?.displayName?.ifBlank { userDisplayName } ?: userDisplayName
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.submitReview(productId, name, rating, comment)
      _isBusy.value = false
      if (res.isSuccess) _message.value = "Thank you for your review!"
      else _errorMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Review failed"
    }
  }

  fun submitInquiry(name: String, phone: String, subject: String, message: String) {
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.submitInquiry(name, userEmail, phone, subject, message)
      _isBusy.value = false
      if (res.isSuccess) _message.value = "Your inquiry has been sent to our concierge."
      else _errorMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to send inquiry"
    }
  }

  // --- Admin Actions ---

  fun saveProductAsAdmin(product: Product, onDone: () -> Unit = {}) {
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.saveProductAsAdmin(product)
      _isBusy.value = false
      if (res.isSuccess) {
        _message.value = "Product saved to boutique catalog"
        onDone()
      } else {
        _errorMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Product save failed"
      }
    }
  }

  fun deleteProductAsAdmin(productId: String) {
    viewModelScope.launch {
      val res = repository.deleteProductAsAdmin(productId)
      if (res.isSuccess) _message.value = "Product deleted"
      else _errorMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Delete failed"
    }
  }

  fun saveCategoryAsAdmin(category: Category, onDone: () -> Unit = {}) {
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.saveCategoryAsAdmin(category)
      _isBusy.value = false
      if (res.isSuccess) {
        _message.value = "Category saved"
        onDone()
      } else {
        _errorMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Category save failed"
      }
    }
  }

  fun saveOfferAsAdmin(offer: Offer, onDone: () -> Unit = {}) {
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.saveOfferAsAdmin(offer)
      _isBusy.value = false
      if (res.isSuccess) {
        _message.value = "Offer saved"
        onDone()
      } else {
        _errorMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Offer save failed"
      }
    }
  }

  fun updateOrderStatusAsAdmin(
    orderId: String,
    status: String,
    trackingNote: String,
    paymentStatus: String,
  ) {
    viewModelScope.launch {
      _isBusy.value = true
      val res = repository.updateOrderStatusAsAdmin(orderId, status, trackingNote, paymentStatus)
      _isBusy.value = false
      if (res.isSuccess) _message.value = "Order updated to $status"
      else _errorMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Order update failed"
    }
  }

  private fun <T> Flow<T>.safeStream(tag: String, fallback: T): Flow<T> =
    this.map { it }
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val root = firestoreError ?: error
        Log.w(TAG, "Stream error [$tag]: ${root.message}", root)
        emit(fallback)
      }

  private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)

  private companion object {
    const val TAG = "StoreViewModel"
  }
}
