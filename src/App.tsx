import React, { useEffect, useMemo, useState } from "react";
import {
  ArrowRight,
  Copy,
  Instagram,
  MessageCircle,
  Search,
  Sparkles,
} from "lucide-react";
import {
  BOUTIQUE_INSTAGRAM_URL,
  CATEGORIES,
  HOMEPAGE_SECTIONS,
  OFFERS,
  PRODUCTS,
  ProductItem,
  REVIEWS,
  getDiscountPercent,
  getEffectivePrice,
} from "./data/catalog";
import {
  CartEntry,
  loadCartFromStorage,
  loadWishlistFromStorage,
  saveCartToStorage,
  saveWishlistToStorage,
} from "./utils/storage";
import { openWhatsAppConcierge } from "./utils/whatsapp";
import { MainSection, Navbar } from "./components/Navbar";
import { ProductCard } from "./components/ProductCard";
import { ProductModal } from "./components/ProductModal";
import { CartDrawer } from "./components/CartDrawer";
import { InfoPages, InfoSubTab } from "./components/InfoPages";

const HERO_SLIDES = [
  {
    badge: "ROYAL BRIDAL COUTURE",
    title: "Handcrafted Zardozi & Velvet Bridal Lehengas",
    subtitle:
      "Heirloom silhouettes woven with royal gold dabka, sterling silver kasab, and bespoke blouse tailoring.",
    image: "./images/img_hero_bridal.jpg",
    category: "Bridal Lehenga",
  },
  {
    badge: "EXCLUSIVE GHARARA EDIT",
    title: "Nawabi Lucknawi Ghararas & Pakistani Suits",
    subtitle:
      "Ivory, champagne gold, and emerald velvet ensembles curated for nikah, sangeet, and walima nights.",
    image: "./images/img_suit_ivory.jpg",
    category: "Gharara Suits",
  },
  {
    badge: "FESTIVE & SANGEET",
    title: "Emerald Mirrorwork & Designer Party Lehengas",
    subtitle:
      "Luminous mirrorwork and flared shararas crafted for unforgettable celebratory evenings.",
    image: "./images/img_lehenga_emerald.jpg",
    category: "Party Wear Lehenga",
  },
  {
    badge: "MIDNIGHT SILK DRAPES",
    title: "Obsidian Banarasi Sarees & Reception Gowns",
    subtitle:
      "Pure Katan silk sarees and sculpted Indo-western gowns in black, silver, and antique gold.",
    image: "./images/img_saree_obsidian.jpg",
    category: "Sarees",
  },
];

type SortMode = "featured" | "price_asc" | "price_desc" | "discount";

export function App() {
  const [activeSection, setActiveSection] = useState<MainSection>("home");
  const [infoTab, setInfoTab] = useState<InfoSubTab>("about");
  const [activeCategory, setActiveCategory] = useState<string>("All");
  const [activeCollection, setActiveCollection] = useState<string>("All");
  const [searchQuery, setSearchQuery] = useState<string>("");
  const [sortMode, setSortMode] = useState<SortMode>("featured");

  const [heroIndex, setHeroIndex] = useState(0);
  const [selectedProduct, setSelectedProduct] = useState<ProductItem | null>(
    null
  );
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  const [cart, setCart] = useState<CartEntry[]>(() => loadCartFromStorage());
  const [wishlist, setWishlist] = useState<string[]>(() =>
    loadWishlistFromStorage()
  );

  useEffect(() => {
    saveCartToStorage(cart);
  }, [cart]);

  useEffect(() => {
    saveWishlistToStorage(wishlist);
  }, [wishlist]);

  useEffect(() => {
    const timer = window.setInterval(() => {
      setHeroIndex((prev) => (prev + 1) % HERO_SLIDES.length);
    }, 5500);
    return () => window.clearInterval(timer);
  }, []);

  const showToast = (msg: string) => {
    setToast(msg);
    window.setTimeout(() => {
      setToast((current) => (current === msg ? null : current));
    }, 2800);
  };

  const handleNavigate = (section: MainSection, categoryFilter?: string) => {
    setActiveSection(section);
    if (categoryFilter) {
      setActiveCategory(categoryFilter);
      setActiveCollection("All");
    }
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const toggleWishlist = (productId: string) => {
    setWishlist((prev) => {
      const exists = prev.includes(productId);
      const next = exists
        ? prev.filter((id) => id !== productId)
        : [...prev, productId];
      showToast(exists ? "Removed from Wishlist" : "Saved to Wishlist");
      return next;
    });
  };

  const handleAddToCart = ({
    product,
    selectedSize,
    selectedColor,
    customStitching,
    madeToOrder,
    customMeasurements,
  }: {
    product: ProductItem;
    selectedSize: string;
    selectedColor: string;
    customStitching: boolean;
    madeToOrder: boolean;
    customMeasurements: string;
  }) => {
    const cartKey = `${product.id}_${selectedSize}_${selectedColor}_${customStitching}`;
    const effectivePrice = getEffectivePrice(product);

    setCart((prev) => {
      const existing = prev.find((item) => item.cartKey === cartKey);
      if (existing) {
        return prev.map((item) =>
          item.cartKey === cartKey
            ? {
                ...item,
                quantity: item.quantity + 1,
                customMeasurements:
                  customMeasurements || item.customMeasurements,
              }
            : item
        );
      }
      return [
        ...prev,
        {
          cartKey,
          productId: product.id,
          productName: product.name,
          productImage: product.images[0],
          category: product.category,
          unitPrice: effectivePrice,
          mrpPrice: product.price,
          selectedSize,
          selectedColor,
          customStitching,
          madeToOrder,
          customMeasurements,
          estimatedDelivery: product.estimatedDelivery,
          quantity: 1,
        },
      ];
    });

    setSelectedProduct(null);
    setIsCartOpen(true);
    showToast(`${product.name} added to bag`);
  };

  const handleUpdateQty = (cartKey: string, delta: number) => {
    setCart((prev) =>
      prev
        .map((item) =>
          item.cartKey === cartKey
            ? { ...item, quantity: item.quantity + delta }
            : item
        )
        .filter((item) => item.quantity > 0)
    );
  };

  const handleRemoveCartItem = (cartKey: string) => {
    setCart((prev) => prev.filter((item) => item.cartKey !== cartKey));
  };

  const filteredProducts = useMemo(() => {
    const query = searchQuery.trim().toLowerCase();
    const list = PRODUCTS.filter((p) => {
      const matchesCat =
        activeCategory === "All" || p.category === activeCategory;
      const matchesCol =
        activeCollection === "All" ||
        p.collections.includes(activeCollection) ||
        (activeCollection === "Special Offers" && p.isSpecialOffer);
      const matchesQuery =
        !query ||
        p.name.toLowerCase().includes(query) ||
        p.category.toLowerCase().includes(query) ||
        p.description.toLowerCase().includes(query) ||
        p.fabric.toLowerCase().includes(query);
      return matchesCat && matchesCol && matchesQuery;
    });

    if (sortMode === "price_asc") {
      return [...list].sort(
        (a, b) => getEffectivePrice(a) - getEffectivePrice(b)
      );
    }
    if (sortMode === "price_desc") {
      return [...list].sort(
        (a, b) => getEffectivePrice(b) - getEffectivePrice(a)
      );
    }
    if (sortMode === "discount") {
      return [...list].sort(
        (a, b) => getDiscountPercent(b) - getDiscountPercent(a)
      );
    }
    return list;
  }, [activeCategory, activeCollection, searchQuery, sortMode]);

  const wishlistProducts = useMemo(
    () => PRODUCTS.filter((p) => wishlist.includes(p.id)),
    [wishlist]
  );

  const totalBagCount = useMemo(
    () => cart.reduce((sum, item) => sum + item.quantity, 0),
    [cart]
  );

  const activeSlide = HERO_SLIDES[heroIndex];

  const copyPromoCode = (code: string) => {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(code).catch(() => {});
    }
    showToast(`Promo code ${code} copied`);
  };

  return (
    <div>
      <Navbar
        activeSection={activeSection}
        activeCategory={activeCategory}
        cartCount={totalBagCount}
        wishlistCount={wishlist.length}
        onNavigate={handleNavigate}
        onOpenCart={() => setIsCartOpen(true)}
      />

      <main>
        {activeSection === "home" && (
          <>
            {/* Hero Fashion Banner */}
            <section className="hero-section container">
              <div className="hero-card">
                <img
                  src={activeSlide.image}
                  alt={activeSlide.title}
                  className="hero-bg"
                />
                <div className="hero-scrim" />
                <div className="hero-content">
                  <span className="gold-pill">
                    <Sparkles size={13} />
                    {activeSlide.badge}
                  </span>
                  <h1 className="hero-title">{activeSlide.title}</h1>
                  <p className="hero-desc">{activeSlide.subtitle}</p>
                  <div className="hero-actions">
                    <button
                      type="button"
                      className="btn-gold"
                      onClick={() =>
                        handleNavigate("catalogue", activeSlide.category)
                      }
                    >
                      Explore {activeSlide.category}
                      <ArrowRight size={17} />
                    </button>
                    <button
                      type="button"
                      className="btn-outline-gold"
                      onClick={() => handleNavigate("catalogue", "All")}
                    >
                      Full Catalogue
                    </button>
                  </div>
                </div>
              </div>

              <div className="hero-dots" aria-label="Hero Banner Slides">
                {HERO_SLIDES.map((slide, idx) => (
                  <button
                    key={slide.badge}
                    type="button"
                    className={`hero-dot ${idx === heroIndex ? "active" : ""}`}
                    onClick={() => setHeroIndex(idx)}
                    aria-label={`Show slide ${idx + 1}: ${slide.badge}`}
                  />
                ))}
              </div>
            </section>

            {/* 9 Product Categories */}
            <section className="section-block container">
              <div className="section-header">
                <div>
                  <h2 className="section-title">Couture Categories</h2>
                  <p className="section-subtitle">
                    Explore all 9 signature bridal &amp; festive silhouettes
                  </p>
                </div>
                <button
                  type="button"
                  className="btn-outline-gold"
                  style={{ padding: "8px 14px", minHeight: "38px", fontSize: "0.8rem" }}
                  onClick={() => handleNavigate("catalogue", "All")}
                >
                  View All
                </button>
              </div>

              <div className="category-strip">
                {CATEGORIES.map((cat) => (
                  <button
                    key={cat.id}
                    type="button"
                    className="category-tile"
                    onClick={() => handleNavigate("catalogue", cat.name)}
                  >
                    <div className="category-avatar">
                      <img src={cat.image} alt={cat.name} loading="lazy" />
                    </div>
                    <span className="category-name">{cat.name}</span>
                  </button>
                ))}
              </div>
            </section>

            {/* Special Offers */}
            <section className="section-block container">
              <div className="section-header">
                <div>
                  <h2 className="section-title">Special Atelier Offers</h2>
                  <p className="section-subtitle">
                    Apply directly in your shopping bag or on WhatsApp
                  </p>
                </div>
              </div>
              <div className="offers-grid">
                {OFFERS.map((offer) => (
                  <div key={offer.id} className="offer-card">
                    <div>
                      <div
                        style={{
                          fontWeight: 700,
                          color: "var(--gold-champagne)",
                          fontSize: "1.02rem",
                        }}
                      >
                        {offer.title}
                      </div>
                      <div
                        style={{
                          fontSize: "0.82rem",
                          color: "var(--silver-muted)",
                          marginTop: "4px",
                        }}
                      >
                        {offer.subtitle}
                      </div>
                    </div>
                    <button
                      type="button"
                      className="promo-chip"
                      onClick={() => copyPromoCode(offer.promoCode)}
                    >
                      <Copy size={13} />
                      {offer.promoCode}
                    </button>
                  </div>
                ))}
              </div>
            </section>

            {/* Curated Homepage Collections */}
            {HOMEPAGE_SECTIONS.map((collectionName) => {
              const sectionProducts = PRODUCTS.filter(
                (p) =>
                  p.collections.includes(collectionName) ||
                  (collectionName === "Special Offers" && p.isSpecialOffer)
              ).slice(0, 4);

              if (sectionProducts.length === 0) return null;

              return (
                <section key={collectionName} className="section-block container">
                  <div className="section-header">
                    <div>
                      <h2 className="section-title">{collectionName}</h2>
                      <p className="section-subtitle">
                        Curated by RAZA ETHNIC WEAR stylists
                      </p>
                    </div>
                    <button
                      type="button"
                      className="btn-outline-gold"
                      style={{
                        padding: "8px 14px",
                        minHeight: "38px",
                        fontSize: "0.8rem",
                      }}
                      onClick={() => {
                        setActiveSection("catalogue");
                        setActiveCategory("All");
                        setActiveCollection(collectionName);
                        window.scrollTo({ top: 0, behavior: "smooth" });
                      }}
                    >
                      See Collection
                    </button>
                  </div>

                  <div className="product-grid">
                    {sectionProducts.map((product) => (
                      <ProductCard
                        key={`${collectionName}-${product.id}`}
                        product={product}
                        isWishlisted={wishlist.includes(product.id)}
                        onToggleWishlist={toggleWishlist}
                        onSelectProduct={setSelectedProduct}
                      />
                    ))}
                  </div>
                </section>
              );
            })}

            {/* Customer Testimonials */}
            <section className="section-block container">
              <div className="section-header">
                <div>
                  <h2 className="section-title">Client Love</h2>
                  <p className="section-subtitle">
                    Verified brides and patrons wearing RAZA ETHNIC WEAR
                  </p>
                </div>
              </div>
              <div className="offers-grid">
                {REVIEWS.map((rev) => (
                  <div
                    key={rev.id}
                    style={{
                      background: "var(--bg-charcoal)",
                      border: "1px solid var(--border-subtle)",
                      borderRadius: "16px",
                      padding: "16px",
                    }}
                  >
                    <div
                      style={{
                        color: "var(--gold-royal)",
                        marginBottom: "6px",
                        fontSize: "0.9rem",
                      }}
                    >
                      {"★".repeat(rev.rating)}
                    </div>
                    <p
                      style={{
                        margin: "0 0 10px",
                        fontSize: "0.86rem",
                        color: "var(--silver-sterling)",
                        lineHeight: 1.55,
                      }}
                    >
                      &ldquo;{rev.comment}&rdquo;
                    </p>
                    <div
                      style={{
                        fontSize: "0.8rem",
                        fontWeight: 700,
                        color: "var(--gold-champagne)",
                      }}
                    >
                      — {rev.reviewerName}
                    </div>
                  </div>
                ))}
              </div>
            </section>
          </>
        )}

        {activeSection === "catalogue" && (
          <section className="section-block container">
            <div className="section-header">
              <div>
                <span className="gold-pill">PUBLIC COUTURE CATALOGUE</span>
                <h1 className="section-title" style={{ marginTop: "10px" }}>
                  {activeCategory === "All"
                    ? activeCollection === "All"
                      ? "All Royal Collections"
                      : activeCollection
                    : activeCategory}
                </h1>
                <p className="section-subtitle">
                  Browse freely without sign-in • Enquire instantly via WhatsApp
                </p>
              </div>
            </div>

            <div className="filter-controls">
              <div className="search-box">
                <Search
                  size={17}
                  color="#9aa0ae"
                  style={{
                    position: "absolute",
                    left: "14px",
                    top: "50%",
                    transform: "translateY(-50%)",
                  }}
                />
                <input
                  type="search"
                  className="search-input"
                  placeholder="Search Bridal Lehenga, Gharara, Silk Saree, Velvet..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  aria-label="Search garments"
                />
              </div>

              <select
                className="lux-select"
                style={{ width: "auto", minWidth: "200px" }}
                value={sortMode}
                onChange={(e) => setSortMode(e.target.value as SortMode)}
                aria-label="Sort products"
              >
                <option value="featured">Sort: Featured</option>
                <option value="price_asc">Price: Low to High</option>
                <option value="price_desc">Price: High to Low</option>
                <option value="discount">Highest Savings</option>
              </select>
            </div>

            {/* Category Filter Chips */}
            <div className="chip-row" aria-label="Category Filters">
              <button
                type="button"
                className={`filter-chip ${
                  activeCategory === "All" ? "active" : ""
                }`}
                onClick={() => setActiveCategory("All")}
              >
                All Categories
              </button>
              {CATEGORIES.map((cat) => (
                <button
                  key={cat.id}
                  type="button"
                  className={`filter-chip ${
                    activeCategory === cat.name ? "active" : ""
                  }`}
                  onClick={() => setActiveCategory(cat.name)}
                >
                  {cat.name}
                </button>
              ))}
            </div>

            {/* Collection Filter Chips */}
            <div className="chip-row" aria-label="Collection Filters">
              <button
                type="button"
                className={`filter-chip ${
                  activeCollection === "All" ? "active" : ""
                }`}
                onClick={() => setActiveCollection("All")}
              >
                All Edits
              </button>
              {HOMEPAGE_SECTIONS.map((col) => (
                <button
                  key={col}
                  type="button"
                  className={`filter-chip ${
                    activeCollection === col ? "active" : ""
                  }`}
                  onClick={() => setActiveCollection(col)}
                >
                  {col}
                </button>
              ))}
            </div>

            {filteredProducts.length === 0 ? (
              <div
                style={{
                  textAlign: "center",
                  padding: "48px 16px",
                  background: "var(--bg-charcoal)",
                  borderRadius: "18px",
                  border: "1px solid var(--border-subtle)",
                }}
              >
                <p style={{ fontSize: "1rem", margin: "0 0 12px" }}>
                  No garments matched your current filters.
                </p>
                <button
                  type="button"
                  className="btn-gold"
                  onClick={() => {
                    setActiveCategory("All");
                    setActiveCollection("All");
                    setSearchQuery("");
                  }}
                >
                  Reset Filters
                </button>
              </div>
            ) : (
              <div className="product-grid">
                {filteredProducts.map((product) => (
                  <ProductCard
                    key={product.id}
                    product={product}
                    isWishlisted={wishlist.includes(product.id)}
                    onToggleWishlist={toggleWishlist}
                    onSelectProduct={setSelectedProduct}
                  />
                ))}
              </div>
            )}
          </section>
        )}

        {activeSection === "wishlist" && (
          <section className="section-block container">
            <div className="section-header">
              <div>
                <span className="gold-pill">SAVED IN BROWSER</span>
                <h1 className="section-title" style={{ marginTop: "10px" }}>
                  Your Couture Wishlist
                </h1>
              </div>
            </div>

            {wishlistProducts.length === 0 ? (
              <div
                style={{
                  textAlign: "center",
                  padding: "56px 16px",
                  background: "var(--bg-charcoal)",
                  borderRadius: "18px",
                  border: "1px solid var(--border-gold)",
                }}
              >
                <p style={{ margin: "0 0 14px", color: "var(--silver-muted)" }}>
                  You haven&apos;t saved any outfits yet. Tap the heart icon on
                  any garment to shortlist it here.
                </p>
                <button
                  type="button"
                  className="btn-gold"
                  onClick={() => handleNavigate("catalogue", "All")}
                >
                  Explore Catalogue
                </button>
              </div>
            ) : (
              <div className="product-grid">
                {wishlistProducts.map((product) => (
                  <ProductCard
                    key={product.id}
                    product={product}
                    isWishlisted={true}
                    onToggleWishlist={toggleWishlist}
                    onSelectProduct={setSelectedProduct}
                  />
                ))}
              </div>
            )}
          </section>
        )}

        {activeSection === "info" && <InfoPages initialTab={infoTab} />}
      </main>

      {/* Luxury Footer */}
      <footer className="site-footer">
        <div className="container footer-grid">
          <div>
            <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "10px" }}>
              <img
                src="./images/img_app_icon_fg.jpg"
                alt="RAZA ETHNIC WEAR Crest"
                className="brand-crest"
              />
              <div>
                <div className="brand-title">RAZA ETHNIC WEAR</div>
                <div className="brand-subtitle">Royal Indian Couture</div>
              </div>
            </div>
            <p
              style={{
                margin: 0,
                fontSize: "0.84rem",
                color: "var(--silver-muted)",
                lineHeight: 1.6,
              }}
            >
              Hand-embroidered Bridal Lehengas, Ghararas, Pakistani Suits,
              Designer Gowns &amp; Banarasi Silk Sarees.
            </p>
          </div>

          <div>
            <h4 style={{ margin: "0 0 10px", color: "var(--gold-champagne)" }}>
              Collections
            </h4>
            <div style={{ display: "grid", gap: "6px", fontSize: "0.84rem", color: "var(--silver-sterling)" }}>
              <button
                type="button"
                className="nav-link"
                style={{ textAlign: "left", padding: "2px 0" }}
                onClick={() => handleNavigate("catalogue", "Bridal Lehenga")}
              >
                Bridal &amp; Party Lehengas
              </button>
              <button
                type="button"
                className="nav-link"
                style={{ textAlign: "left", padding: "2px 0" }}
                onClick={() => handleNavigate("catalogue", "Gharara Suits")}
              >
                Exclusive Gharara &amp; Sharara Suits
              </button>
              <button
                type="button"
                className="nav-link"
                style={{ textAlign: "left", padding: "2px 0" }}
                onClick={() => handleNavigate("catalogue", "Pakistani Suits")}
              >
                Pakistani &amp; Anarkali Suits
              </button>
              <button
                type="button"
                className="nav-link"
                style={{ textAlign: "left", padding: "2px 0" }}
                onClick={() => handleNavigate("catalogue", "Sarees")}
              >
                Designer Gowns &amp; Banarasi Sarees
              </button>
            </div>
          </div>

          <div>
            <h4 style={{ margin: "0 0 10px", color: "var(--gold-champagne)" }}>
              Atelier Policies
            </h4>
            <div style={{ display: "grid", gap: "6px", fontSize: "0.84rem" }}>
              {(
                [
                  ["about", "About Us"],
                  ["contact", "Contact Us"],
                  ["shipping", "Shipping Policy"],
                  ["returns", "Return & Refund Policy"],
                  ["privacy", "Privacy Policy"],
                ] as [InfoSubTab, string][]
              ).map(([tabKey, label]) => (
                <button
                  key={tabKey}
                  type="button"
                  className="nav-link"
                  style={{ textAlign: "left", padding: "2px 0" }}
                  onClick={() => {
                    setInfoTab(tabKey);
                    handleNavigate("info");
                  }}
                >
                  {label}
                </button>
              ))}
            </div>
          </div>

          <div>
            <h4 style={{ margin: "0 0 10px", color: "var(--gold-champagne)" }}>
              Direct Concierge
            </h4>
            <div style={{ display: "grid", gap: "10px" }}>
              <button
                type="button"
                className="btn-whatsapp"
                onClick={openWhatsAppConcierge}
              >
                <MessageCircle size={17} />
                WhatsApp Atelier
              </button>
              <a
                href={BOUTIQUE_INSTAGRAM_URL}
                target="_blank"
                rel="noopener noreferrer"
                className="btn-outline-gold"
              >
                <Instagram size={17} />
                Instagram @razaethnicwear
              </a>
            </div>
          </div>
        </div>
      </footer>

      <ProductModal
        product={selectedProduct}
        isWishlisted={
          selectedProduct ? wishlist.includes(selectedProduct.id) : false
        }
        onClose={() => setSelectedProduct(null)}
        onToggleWishlist={toggleWishlist}
        onAddToCart={handleAddToCart}
      />

      <CartDrawer
        isOpen={isCartOpen}
        items={cart}
        onClose={() => setIsCartOpen(false)}
        onUpdateQty={handleUpdateQty}
        onRemoveItem={handleRemoveCartItem}
        onClearCart={() => {
          setCart([]);
          showToast("Shopping bag cleared");
        }}
        onNotify={showToast}
      />

      {toast && (
        <div className="toast-banner" role="status" aria-live="polite">
          {toast}
        </div>
      )}
    </div>
  );
}

export default App;
