import React, { useEffect, useState } from "react";
import {
  Check,
  Heart,
  MessageCircle,
  Ruler,
  ShoppingBag,
  Sparkles,
  Star,
  Truck,
  X,
} from "lucide-react";
import {
  ProductItem,
  REVIEWS,
  formatInr,
  getDiscountPercent,
  getEffectivePrice,
} from "../data/catalog";
import { openWhatsAppProductEnquiry } from "../utils/whatsapp";

interface ProductModalProps {
  product: ProductItem | null;
  isWishlisted: boolean;
  onClose: () => void;
  onToggleWishlist: (productId: string) => void;
  onAddToCart: (options: {
    product: ProductItem;
    selectedSize: string;
    selectedColor: string;
    customStitching: boolean;
    madeToOrder: boolean;
    customMeasurements: string;
  }) => void;
}

export const ProductModal: React.FC<ProductModalProps> = ({
  product,
  isWishlisted,
  onClose,
  onToggleWishlist,
  onAddToCart,
}) => {
  const [activeImageIdx, setActiveImageIdx] = useState(0);
  const [selectedSize, setSelectedSize] = useState("");
  const [selectedColor, setSelectedColor] = useState("");
  const [customStitching, setCustomStitching] = useState(false);
  const [madeToOrder, setMadeToOrder] = useState(false);
  const [measurements, setMeasurements] = useState("");

  useEffect(() => {
    if (product) {
      setActiveImageIdx(0);
      setSelectedSize(product.sizes[0] || "M");
      setSelectedColor(product.colors[0] || "Signature");
      setCustomStitching(false);
      setMadeToOrder(product.madeToOrder);
      setMeasurements("");
    }
  }, [product]);

  useEffect(() => {
    if (!product) return;
    const handleEsc = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", handleEsc);
    return () => window.removeEventListener("keydown", handleEsc);
  }, [product, onClose]);

  if (!product) return null;

  const effectivePrice = getEffectivePrice(product);
  const discountPct = getDiscountPercent(product);
  const matchingReviews = REVIEWS.filter((r) => r.productId === product.id);
  const displayReviews =
    matchingReviews.length > 0 ? matchingReviews : REVIEWS.slice(0, 2);

  return (
    <div
      className="modal-backdrop"
      onClick={onClose}
      role="dialog"
      aria-modal="true"
      aria-label={product.name}
    >
      <div className="modal-panel" onClick={(e) => e.stopPropagation()}>
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginBottom: "14px",
            gap: "12px",
          }}
        >
          <span className="gold-pill">{product.category}</span>
          <div style={{ display: "flex", gap: "8px" }}>
            <button
              type="button"
              className="icon-btn"
              onClick={() => onToggleWishlist(product.id)}
              aria-label="Toggle Wishlist"
            >
              <Heart
                size={18}
                fill={isWishlisted ? "#d4af37" : "none"}
                color={isWishlisted ? "#d4af37" : "#f7f4ee"}
              />
            </button>
            <button
              type="button"
              className="icon-btn"
              onClick={onClose}
              aria-label="Close Product Details"
            >
              <X size={20} />
            </button>
          </div>
        </div>

        <div className="detail-layout">
          {/* Left: Gallery */}
          <div>
            <div
              style={{
                borderRadius: "16px",
                overflow: "hidden",
                border: "1px solid var(--border-gold)",
                aspectRatio: "4 / 5",
                background: "var(--bg-obsidian)",
              }}
            >
              <img
                src={product.images[activeImageIdx] || product.images[0]}
                alt={product.name}
                style={{ width: "100%", height: "100%", objectFit: "cover" }}
              />
            </div>

            {product.images.length > 1 && (
              <div
                style={{
                  display: "flex",
                  gap: "10px",
                  marginTop: "10px",
                  overflowX: "auto",
                }}
              >
                {product.images.map((imgUrl, idx) => (
                  <button
                    key={`${imgUrl}-${idx}`}
                    type="button"
                    onClick={() => setActiveImageIdx(idx)}
                    style={{
                      width: "68px",
                      height: "84px",
                      padding: 0,
                      borderRadius: "10px",
                      overflow: "hidden",
                      border:
                        activeImageIdx === idx
                          ? "2px solid var(--gold-royal)"
                          : "1px solid var(--border-subtle)",
                      background: "none",
                      flexShrink: 0,
                    }}
                    aria-label={`View image ${idx + 1}`}
                  >
                    <img
                      src={imgUrl}
                      alt={`${product.name} view ${idx + 1}`}
                      style={{ width: "100%", height: "100%", objectFit: "cover" }}
                    />
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Right: Specs & Actions */}
          <div style={{ display: "flex", flexDirection: "column", gap: "14px" }}>
            <div>
              <h2
                className="font-serif"
                style={{
                  margin: "0 0 6px",
                  fontSize: "clamp(1.5rem, 3vw, 2rem)",
                  lineHeight: 1.15,
                }}
              >
                {product.name}
              </h2>
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: "10px",
                  flexWrap: "wrap",
                }}
              >
                <span className="price-now" style={{ fontSize: "1.4rem" }}>
                  {formatInr(effectivePrice)}
                </span>
                {discountPct > 0 && (
                  <>
                    <span className="price-mrp" style={{ fontSize: "0.95rem" }}>
                      {formatInr(product.price)}
                    </span>
                    <span
                      style={{
                        color: "var(--emerald)",
                        fontWeight: 700,
                        fontSize: "0.85rem",
                      }}
                    >
                      Save {discountPct}%
                    </span>
                  </>
                )}
              </div>
            </div>

            <p
              style={{
                margin: 0,
                color: "var(--silver-sterling)",
                fontSize: "0.92rem",
                lineHeight: 1.6,
              }}
            >
              {product.description}
            </p>

            {/* Fabric & Delivery Meta */}
            <div
              style={{
                background: "var(--bg-onyx)",
                border: "1px solid var(--border-subtle)",
                borderRadius: "14px",
                padding: "12px 14px",
                display: "grid",
                gap: "8px",
                fontSize: "0.84rem",
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                <Sparkles size={15} color="#d4af37" />
                <span>
                  <strong>Fabric:</strong> {product.fabric}
                </span>
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                <Truck size={15} color="#d4af37" />
                <span>
                  <strong>Estimated Delivery:</strong> {product.estimatedDelivery}
                </span>
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                <Star size={15} fill="#d4af37" color="#d4af37" />
                <span>
                  <strong>{product.rating.toFixed(1)}</strong> ({product.reviewCount}{" "}
                  verified boutique reviews)
                </span>
              </div>
            </div>

            {/* Sizes */}
            <div>
              <div
                style={{
                  fontSize: "0.8rem",
                  fontWeight: 600,
                  color: "var(--gold-champagne)",
                  marginBottom: "8px",
                }}
              >
                Select Size
              </div>
              <div style={{ display: "flex", flexWrap: "wrap", gap: "8px" }}>
                {product.sizes.map((sz) => (
                  <button
                    key={sz}
                    type="button"
                    className={`filter-chip ${
                      selectedSize === sz ? "active" : ""
                    }`}
                    onClick={() => {
                      setSelectedSize(sz);
                      if (sz.toLowerCase().includes("custom")) {
                        setCustomStitching(true);
                      }
                    }}
                  >
                    {sz}
                  </button>
                ))}
              </div>
            </div>

            {/* Colours */}
            <div>
              <div
                style={{
                  fontSize: "0.8rem",
                  fontWeight: 600,
                  color: "var(--gold-champagne)",
                  marginBottom: "8px",
                }}
              >
                Select Colour
              </div>
              <div style={{ display: "flex", flexWrap: "wrap", gap: "8px" }}>
                {product.colors.map((clr) => (
                  <button
                    key={clr}
                    type="button"
                    className={`filter-chip ${
                      selectedColor === clr ? "active" : ""
                    }`}
                    onClick={() => setSelectedColor(clr)}
                  >
                    {clr}
                  </button>
                ))}
              </div>
            </div>

            {/* Tailoring Options */}
            <div
              style={{
                display: "flex",
                flexWrap: "wrap",
                gap: "10px",
              }}
            >
              {product.customStitchingAvailable && (
                <button
                  type="button"
                  className={`filter-chip ${customStitching ? "active" : ""}`}
                  onClick={() => setCustomStitching((v) => !v)}
                  style={{ display: "inline-flex", alignItems: "center", gap: "6px" }}
                >
                  {customStitching ? <Check size={14} /> : <Ruler size={14} />}
                  Custom Stitching
                </button>
              )}
              <button
                type="button"
                className={`filter-chip ${madeToOrder ? "active" : ""}`}
                onClick={() => setMadeToOrder((v) => !v)}
                style={{ display: "inline-flex", alignItems: "center", gap: "6px" }}
              >
                {madeToOrder && <Check size={14} />}
                Made-to-Order Atelier
              </button>
            </div>

            {(customStitching || madeToOrder) && (
              <input
                type="text"
                className="lux-input"
                placeholder="Optional: Bust / Waist / Height or blouse customization notes"
                value={measurements}
                onChange={(e) => setMeasurements(e.target.value)}
                aria-label="Custom stitching measurements and notes"
              />
            )}

            {/* Primary CTA Buttons */}
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "repeat(auto-fit, minmax(175px, 1fr))",
                gap: "10px",
                marginTop: "4px",
              }}
            >
              <button
                type="button"
                className="btn-gold"
                onClick={() =>
                  onAddToCart({
                    product,
                    selectedSize,
                    selectedColor,
                    customStitching,
                    madeToOrder,
                    customMeasurements: measurements,
                  })
                }
              >
                <ShoppingBag size={18} />
                Add to Bag
              </button>

              <button
                type="button"
                className="btn-whatsapp"
                onClick={() =>
                  openWhatsAppProductEnquiry(product, {
                    size: selectedSize,
                    color: selectedColor,
                    customStitching,
                    madeToOrder,
                    measurements,
                  })
                }
              >
                <MessageCircle size={18} />
                Order on WhatsApp
              </button>
            </div>

            {/* Client Reviews */}
            <div
              style={{
                borderTop: "1px solid var(--border-subtle)",
                paddingTop: "12px",
                marginTop: "4px",
              }}
            >
              <div
                style={{
                  fontSize: "0.82rem",
                  fontWeight: 700,
                  color: "var(--gold-champagne)",
                  marginBottom: "8px",
                }}
              >
                Client Testimonials
              </div>
              <div style={{ display: "grid", gap: "8px" }}>
                {displayReviews.map((rev) => (
                  <div
                    key={rev.id}
                    style={{
                      background: "var(--bg-onyx)",
                      borderRadius: "10px",
                      padding: "10px 12px",
                      fontSize: "0.8rem",
                    }}
                  >
                    <div
                      style={{
                        display: "flex",
                        justifyContent: "space-between",
                        marginBottom: "4px",
                        fontWeight: 600,
                      }}
                    >
                      <span>{rev.reviewerName}</span>
                      <span style={{ color: "var(--gold-royal)" }}>
                        {"★".repeat(rev.rating)}
                      </span>
                    </div>
                    <p style={{ margin: 0, color: "var(--silver-muted)" }}>
                      {rev.comment}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
