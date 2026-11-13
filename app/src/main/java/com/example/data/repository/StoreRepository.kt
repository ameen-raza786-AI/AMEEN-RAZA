package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.StoreCatalogSeeder
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.CartItem
import com.example.data.model.Category
import com.example.data.model.Inquiry
import com.example.data.model.Offer
import com.example.data.model.Order
import com.example.data.model.OrderLineItem
import com.example.data.model.Product
import com.example.data.model.Review
import com.example.data.model.UserProfile
import com.example.data.model.WishlistItem
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class StoreRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(
    context: Context
  ) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    ),
    Firebase.auth,
  )

  private fun requireUserId(): String =
    auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

  suspend fun checkIsAdmin(userId: String, email: String?, emailVerified: Boolean): Boolean {
    if (emailVerified && email?.equals(StoreCatalogSeeder.ADMIN_EMAIL, ignoreCase = true) == true) {
      return true
    }
    return try {
      val doc = db.collection("admins").document(userId).get().await()
      doc.exists()
    } catch (_: Exception) {
      false
    }
  }

  suspend fun ensureUserProfile(
    userId: String,
    displayName: String,
    email: String,
    photoUrl: String,
  ): Result<Unit> {
    val docRef = db.collection("users").document(userId)
    return try {
      val existing = docRef.get().await()
      if (!existing.exists()) {
        val payload =
          mapOf(
            "userId" to userId,
            "displayName" to displayName.ifBlank { "Valued Patron" },
            "email" to email.ifBlank { "patron@razaethnicwear.com" },
            "photoUrl" to photoUrl,
            "totalOrders" to 0L,
            "totalSpent" to 0.0,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        docRef.set(payload).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun seedInitialCatalogIfAdmin(): Result<Unit> {
    return try {
      val existingProducts = db.collection("products").limit(1).get().await()
      if (existingProducts.isEmpty) {
        val batch = db.batch()
        StoreCatalogSeeder.defaultCategories.forEach { cat ->
          val ref = db.collection("categories").document(cat.id)
          batch.set(
            ref,
            mapOf(
              "name" to cat.name,
              "subtitle" to cat.subtitle,
              "imageKey" to cat.imageKey,
              "sortOrder" to cat.sortOrder,
              "isActive" to cat.isActive,
              "createdAt" to FieldValue.serverTimestamp(),
              "updatedAt" to FieldValue.serverTimestamp(),
            ),
          )
        }
        StoreCatalogSeeder.defaultProducts.forEach { prod ->
          val ref = db.collection("products").document(prod.id)
          batch.set(
            ref,
            mapOf(
              "name" to prod.name,
              "description" to prod.description,
              "category" to prod.category,
              "collections" to prod.collections,
              "images" to prod.images,
              "price" to prod.price,
              "discountPrice" to prod.discountPrice,
              "isSpecialOffer" to prod.isSpecialOffer,
              "offerBadge" to prod.offerBadge,
              "sizes" to prod.sizes,
              "colors" to prod.colors,
              "fabric" to prod.fabric,
              "customStitchingAvailable" to prod.customStitchingAvailable,
              "madeToOrder" to prod.madeToOrder,
              "estimatedDelivery" to prod.estimatedDelivery,
              "stockCount" to prod.stockCount,
              "rating" to prod.rating,
              "reviewCount" to prod.reviewCount,
              "isPublished" to prod.isPublished,
              "createdAt" to FieldValue.serverTimestamp(),
              "updatedAt" to FieldValue.serverTimestamp(),
            ),
          )
        }
        StoreCatalogSeeder.defaultOffers.forEach { offer ->
          val ref = db.collection("offers").document(offer.id)
          batch.set(
            ref,
            mapOf(
              "title" to offer.title,
              "subtitle" to offer.subtitle,
              "promoCode" to offer.promoCode,
              "discountPercent" to offer.discountPercent,
              "minOrderAmount" to offer.minOrderAmount,
              "isActive" to offer.isActive,
              "createdAt" to FieldValue.serverTimestamp(),
              "updatedAt" to FieldValue.serverTimestamp(),
            ),
          )
        }
        StoreCatalogSeeder.defaultReviews.forEach { review ->
          val ref = db.collection("reviews").document(review.id)
          batch.set(
            ref,
            mapOf(
              "productId" to review.productId,
              "userId" to review.userId,
              "reviewerName" to review.reviewerName,
              "rating" to review.rating,
              "comment" to review.comment,
              "isApproved" to review.isApproved,
              "createdAt" to FieldValue.serverTimestamp(),
            ),
          )
        }
        batch.commit().await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, "products", auth)
      Result.failure(e)
    }
  }

  // --- Customer Read Streams (Query Aligned with Rules) ---

  fun observePublishedProducts(): Flow<List<Product>> =
    db.collection("products")
      .whereEqualTo("isPublished", true)
      .snapshots()
      .map { snap -> snap.toObjects(Product::class.java, ServerTimestampBehavior.ESTIMATE) }
      .logAndRethrow(OperationType.LIST, "products")

  fun observeActiveCategories(): Flow<List<Category>> =
    db.collection("categories")
      .whereEqualTo("isActive", true)
      .snapshots()
      .map { snap ->
        snap
          .toObjects(Category::class.java, ServerTimestampBehavior.ESTIMATE)
          .sortedBy { it.sortOrder }
      }
      .logAndRethrow(OperationType.LIST, "categories")

  fun observeActiveOffers(): Flow<List<Offer>> =
    db.collection("offers")
      .whereEqualTo("isActive", true)
      .snapshots()
      .map { snap -> snap.toObjects(Offer::class.java, ServerTimestampBehavior.ESTIMATE) }
      .logAndRethrow(OperationType.LIST, "offers")

  fun observeUserProfile(userId: String): Flow<UserProfile?> =
    db.collection("users")
      .document(userId)
      .snapshots()
      .map { snap ->
        if (snap.exists()) snap.toObject(UserProfile::class.java, ServerTimestampBehavior.ESTIMATE)
        else null
      }
      .logAndRethrow(OperationType.GET, "users/$userId")

  fun observeCart(userId: String): Flow<List<CartItem>> =
    db.collection("users")
      .document(userId)
      .collection("cart")
      .snapshots()
      .map { snap -> snap.toObjects(CartItem::class.java, ServerTimestampBehavior.ESTIMATE) }
      .logAndRethrow(OperationType.LIST, "users/$userId/cart")

  fun observeWishlist(userId: String): Flow<List<WishlistItem>> =
    db.collection("users")
      .document(userId)
      .collection("wishlist")
      .snapshots()
      .map { snap -> snap.toObjects(WishlistItem::class.java, ServerTimestampBehavior.ESTIMATE) }
      .logAndRethrow(OperationType.LIST, "users/$userId/wishlist")

  fun observeUserOrders(userId: String): Flow<List<Order>> =
    db.collection("orders")
      .whereEqualTo("userId", userId)
      .snapshots()
      .map { snap ->
        snap
          .toObjects(Order::class.java, ServerTimestampBehavior.ESTIMATE)
          .sortedByDescending { it.createdAt?.seconds ?: 0L }
      }
      .logAndRethrow(OperationType.LIST, "orders")

  fun observeApprovedReviews(): Flow<List<Review>> =
    db.collection("reviews")
      .whereEqualTo("isApproved", true)
      .snapshots()
      .map { snap ->
        snap
          .toObjects(Review::class.java, ServerTimestampBehavior.ESTIMATE)
          .sortedByDescending { it.createdAt?.seconds ?: 0L }
      }
      .logAndRethrow(OperationType.LIST, "reviews")

  // --- Admin Read Streams ---

  fun observeAllProductsForAdmin(): Flow<List<Product>> =
    db.collection("products")
      .snapshots()
      .map { snap -> snap.toObjects(Product::class.java, ServerTimestampBehavior.ESTIMATE) }
      .logAndRethrow(OperationType.LIST, "products")

  fun observeAllCategoriesForAdmin(): Flow<List<Category>> =
    db.collection("categories")
      .snapshots()
      .map { snap ->
        snap
          .toObjects(Category::class.java, ServerTimestampBehavior.ESTIMATE)
          .sortedBy { it.sortOrder }
      }
      .logAndRethrow(OperationType.LIST, "categories")

  fun observeAllOffersForAdmin(): Flow<List<Offer>> =
    db.collection("offers")
      .snapshots()
      .map { snap -> snap.toObjects(Offer::class.java, ServerTimestampBehavior.ESTIMATE) }
      .logAndRethrow(OperationType.LIST, "offers")

  fun observeAllOrdersForAdmin(): Flow<List<Order>> =
    db.collection("orders")
      .snapshots()
      .map { snap ->
        snap
          .toObjects(Order::class.java, ServerTimestampBehavior.ESTIMATE)
          .sortedByDescending { it.createdAt?.seconds ?: 0L }
      }
      .logAndRethrow(OperationType.LIST, "orders")

  fun observeAllCustomersForAdmin(): Flow<List<UserProfile>> =
    db.collection("users")
      .snapshots()
      .map { snap -> snap.toObjects(UserProfile::class.java, ServerTimestampBehavior.ESTIMATE) }
      .logAndRethrow(OperationType.LIST, "users")

  fun observeAllInquiriesForAdmin(): Flow<List<Inquiry>> =
    db.collection("inquiries")
      .snapshots()
      .map { snap ->
        snap
          .toObjects(Inquiry::class.java, ServerTimestampBehavior.ESTIMATE)
          .sortedByDescending { it.createdAt?.seconds ?: 0L }
      }
      .logAndRethrow(OperationType.LIST, "inquiries")

  // --- Customer Mutations ---

  suspend fun updateUserProfile(
    displayName: String,
    phone: String,
    address: String,
    city: String,
    postalCode: String,
  ): Result<Unit> {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    return try {
      val payload =
        mapOf(
          "displayName" to displayName.trim().ifBlank { "Valued Patron" },
          "phone" to phone.trim(),
          "address" to address.trim(),
          "city" to city.trim(),
          "postalCode" to postalCode.trim(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      docRef.set(payload, SetOptions.merge()).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun addToCart(
    product: Product,
    selectedSize: String,
    selectedColor: String,
    customStitching: Boolean,
    madeToOrder: Boolean,
    customMeasurements: String,
    quantity: Long = 1L,
  ): Result<String> {
    val uid = requireUserId()
    val cartCollection = db.collection("users").document(uid).collection("cart")
    val docRef = cartCollection.document()
    return try {
      val primaryImage = product.images.firstOrNull() ?: "drawable:img_hero_bridal"
      val payload =
        mapOf(
          "userId" to uid,
          "productId" to product.id,
          "productName" to product.name,
          "productImage" to primaryImage,
          "category" to product.category,
          "price" to product.price,
          "discountPrice" to product.discountPrice,
          "selectedSize" to selectedSize,
          "selectedColor" to selectedColor,
          "customStitching" to customStitching,
          "madeToOrder" to madeToOrder,
          "customMeasurements" to customMeasurements.trim(),
          "estimatedDelivery" to product.estimatedDelivery,
          "quantity" to quantity.coerceIn(1L, 20L),
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      docRef.set(payload).await()
      Result.success(docRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun updateCartItemQuantity(cartItemId: String, newQuantity: Long): Result<Unit> {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("cart").document(cartItemId)
    return try {
      if (newQuantity <= 0L) {
        docRef.delete().await()
      } else {
        docRef
          .update(
            mapOf(
              "quantity" to newQuantity.coerceIn(1L, 20L),
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun removeFromCart(cartItemId: String): Result<Unit> {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("cart").document(cartItemId)
    return try {
      docRef.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun toggleWishlist(product: Product, currentlyWishlisted: Boolean): Result<Unit> {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("wishlist").document(product.id)
    return try {
      if (currentlyWishlisted) {
        docRef.delete().await()
      } else {
        val payload =
          mapOf(
            "userId" to uid,
            "productId" to product.id,
            "productName" to product.name,
            "productImage" to (product.images.firstOrNull() ?: "drawable:img_hero_bridal"),
            "category" to product.category,
            "price" to product.price,
            "discountPrice" to product.discountPrice,
            "createdAt" to FieldValue.serverTimestamp(),
          )
        docRef.set(payload).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun placeOrder(
    customerName: String,
    customerEmail: String,
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
    estimatedDelivery: String,
    clearCart: Boolean,
  ): Result<String> {
    val uid = requireUserId()
    val orderRef = db.collection("orders").document()
    val orderNumber = "REW-${(100000..999999).random()}"
    return try {
      val lineItems =
        items.map { item ->
          mapOf(
            "productId" to item.productId,
            "productName" to item.productName,
            "productImage" to item.productImage,
            "selectedSize" to item.selectedSize,
            "selectedColor" to item.selectedColor,
            "customStitching" to item.customStitching,
            "madeToOrder" to item.madeToOrder,
            "customMeasurements" to item.customMeasurements,
            "quantity" to item.quantity,
            "unitPrice" to item.unitPrice,
          )
        }

      val payload =
        mapOf(
          "userId" to uid,
          "orderNumber" to orderNumber,
          "customerName" to customerName.trim().ifBlank { "Valued Patron" },
          "customerEmail" to customerEmail.trim(),
          "customerPhone" to customerPhone.trim(),
          "shippingAddress" to shippingAddress.trim(),
          "items" to lineItems,
          "subtotal" to subtotal,
          "discountAmount" to discountAmount,
          "shippingFee" to shippingFee,
          "totalAmount" to totalAmount,
          "promoCode" to promoCode.trim(),
          "paymentMethod" to paymentMethod,
          "paymentStatus" to paymentStatus,
          "transactionId" to transactionId.trim(),
          "status" to "Placed",
          "trackingNote" to "Order received at Raza Ethnic Wear Atelier",
          "estimatedDelivery" to estimatedDelivery.ifBlank { "7–12 business days" },
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      orderRef.set(payload).await()

      // Update user stats
      val userRef = db.collection("users").document(uid)
      try {
        userRef
          .update(
            mapOf(
              "totalOrders" to FieldValue.increment(1L),
              "totalSpent" to FieldValue.increment(totalAmount),
              "phone" to customerPhone.trim(),
              "address" to shippingAddress.trim(),
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      } catch (_: Exception) {
        // Non-fatal if profile doc is created concurrently
      }

      // Decrement stock in Firestore if product exists there
      for (cartItem in items) {
        try {
          val prodRef = db.collection("products").document(cartItem.productId)
          val prodSnap = prodRef.get().await()
          if (prodSnap.exists()) {
            val currentStock = prodSnap.getLong("stockCount") ?: 0L
            val nextStock = (currentStock - cartItem.quantity).coerceAtLeast(0L)
            prodRef
              .update(
                mapOf(
                  "stockCount" to nextStock,
                  "updatedAt" to FieldValue.serverTimestamp(),
                )
              )
              .await()
          }
        } catch (_: Exception) {
          // Non-fatal if catalog item was local fallback
        }
      }

      if (clearCart) {
        val batch = db.batch()
        items.filter { it.id.isNotBlank() }.forEach { item ->
          val ref = db.collection("users").document(uid).collection("cart").document(item.id)
          batch.delete(ref)
        }
        batch.commit().await()
      }

      Result.success(orderRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, orderRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun cancelOrder(orderId: String): Result<Unit> {
    requireUserId()
    val docRef = db.collection("orders").document(orderId)
    return try {
      docRef
        .update(
          mapOf(
            "status" to "Cancelled",
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun submitReview(
    productId: String,
    reviewerName: String,
    rating: Long,
    comment: String,
  ): Result<Unit> {
    val uid = requireUserId()
    val docRef = db.collection("reviews").document()
    return try {
      val payload =
        mapOf(
          "productId" to productId,
          "userId" to uid,
          "reviewerName" to reviewerName.trim().ifBlank { "Patron" },
          "rating" to rating.coerceIn(1L, 5L),
          "comment" to comment.trim(),
          "isApproved" to true,
          "createdAt" to FieldValue.serverTimestamp(),
        )
      docRef.set(payload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun submitInquiry(
    name: String,
    email: String,
    phone: String,
    subject: String,
    message: String,
  ): Result<Unit> {
    val uid = requireUserId()
    val docRef = db.collection("inquiries").document()
    return try {
      val payload =
        mapOf(
          "userId" to uid,
          "name" to name.trim().ifBlank { "Guest" },
          "email" to email.trim(),
          "phone" to phone.trim(),
          "subject" to subject.trim(),
          "message" to message.trim(),
          "status" to "Open",
          "createdAt" to FieldValue.serverTimestamp(),
        )
      docRef.set(payload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
      Result.failure(e)
    }
  }

  // --- Admin Mutations ---

  suspend fun saveProductAsAdmin(product: Product): Result<String> {
    requireUserId()
    val isNew = product.id.isBlank()
    val docRef =
      if (isNew) db.collection("products").document()
      else db.collection("products").document(product.id)
    return try {
      if (isNew) {
        val payload =
          mapOf(
            "name" to product.name.trim(),
            "description" to product.description.trim(),
            "category" to product.category,
            "collections" to product.collections,
            "images" to product.images.ifEmpty { listOf("drawable:img_hero_bridal") },
            "price" to product.price,
            "discountPrice" to product.discountPrice.coerceAtMost(product.price),
            "isSpecialOffer" to product.isSpecialOffer,
            "offerBadge" to product.offerBadge,
            "sizes" to product.sizes.ifEmpty { listOf("S", "M", "L", "XL", "Custom") },
            "colors" to product.colors.ifEmpty { listOf("Royal Gold") },
            "fabric" to product.fabric.trim(),
            "customStitchingAvailable" to product.customStitchingAvailable,
            "madeToOrder" to product.madeToOrder,
            "estimatedDelivery" to product.estimatedDelivery.ifBlank { "7–10 business days" },
            "stockCount" to product.stockCount.coerceAtLeast(0L),
            "rating" to product.rating.coerceIn(0.0, 5.0),
            "reviewCount" to product.reviewCount.coerceAtLeast(0L),
            "isPublished" to product.isPublished,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        docRef.set(payload).await()
      } else {
        val updates =
          mapOf(
            "name" to product.name.trim(),
            "description" to product.description.trim(),
            "category" to product.category,
            "collections" to product.collections,
            "images" to product.images.ifEmpty { listOf("drawable:img_hero_bridal") },
            "price" to product.price,
            "discountPrice" to product.discountPrice.coerceAtMost(product.price),
            "isSpecialOffer" to product.isSpecialOffer,
            "offerBadge" to product.offerBadge,
            "sizes" to product.sizes.ifEmpty { listOf("S", "M", "L", "XL", "Custom") },
            "colors" to product.colors.ifEmpty { listOf("Royal Gold") },
            "fabric" to product.fabric.trim(),
            "customStitchingAvailable" to product.customStitchingAvailable,
            "madeToOrder" to product.madeToOrder,
            "estimatedDelivery" to product.estimatedDelivery.ifBlank { "7–10 business days" },
            "stockCount" to product.stockCount.coerceAtLeast(0L),
            "isPublished" to product.isPublished,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        docRef.update(updates).await()
      }
      Result.success(docRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun deleteProductAsAdmin(productId: String): Result<Unit> {
    requireUserId()
    val docRef = db.collection("products").document(productId)
    return try {
      docRef.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun saveCategoryAsAdmin(category: Category): Result<Unit> {
    requireUserId()
    val isNew = category.id.isBlank()
    val docRef =
      if (isNew) db.collection("categories").document()
      else db.collection("categories").document(category.id)
    return try {
      if (isNew) {
        docRef
          .set(
            mapOf(
              "name" to category.name.trim(),
              "subtitle" to category.subtitle.trim(),
              "imageKey" to category.imageKey.ifBlank { "drawable:img_hero_bridal" },
              "sortOrder" to category.sortOrder,
              "isActive" to category.isActive,
              "createdAt" to FieldValue.serverTimestamp(),
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      } else {
        docRef
          .update(
            mapOf(
              "name" to category.name.trim(),
              "subtitle" to category.subtitle.trim(),
              "imageKey" to category.imageKey.ifBlank { "drawable:img_hero_bridal" },
              "sortOrder" to category.sortOrder,
              "isActive" to category.isActive,
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun saveOfferAsAdmin(offer: Offer): Result<Unit> {
    requireUserId()
    val isNew = offer.id.isBlank()
    val docRef =
      if (isNew) db.collection("offers").document()
      else db.collection("offers").document(offer.id)
    return try {
      if (isNew) {
        docRef
          .set(
            mapOf(
              "title" to offer.title.trim(),
              "subtitle" to offer.subtitle.trim(),
              "promoCode" to offer.promoCode.trim().uppercase(),
              "discountPercent" to offer.discountPercent.coerceIn(1L, 90L),
              "minOrderAmount" to offer.minOrderAmount.coerceAtLeast(0.0),
              "isActive" to offer.isActive,
              "createdAt" to FieldValue.serverTimestamp(),
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      } else {
        docRef
          .update(
            mapOf(
              "title" to offer.title.trim(),
              "subtitle" to offer.subtitle.trim(),
              "promoCode" to offer.promoCode.trim().uppercase(),
              "discountPercent" to offer.discountPercent.coerceIn(1L, 90L),
              "minOrderAmount" to offer.minOrderAmount.coerceAtLeast(0.0),
              "isActive" to offer.isActive,
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun updateOrderStatusAsAdmin(
    orderId: String,
    status: String,
    trackingNote: String,
    paymentStatus: String,
  ): Result<Unit> {
    requireUserId()
    val docRef = db.collection("orders").document(orderId)
    return try {
      docRef
        .update(
          mapOf(
            "status" to status,
            "trackingNote" to trackingNote.trim(),
            "paymentStatus" to paymentStatus,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
      Result.failure(e)
    }
  }

  private fun <T> Flow<T>.logAndRethrow(op: OperationType, path: String): Flow<T> = flow {
    this@logAndRethrow.catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val ex = firestoreError ?: (error as? Exception)
        if (ex != null && auth.currentUser != null) {
          handleFirestoreError(ex, op, path, auth)
        }
        throw error
      }
      .collect { emit(it) }
  }
}
