import React from "react";
import { Heart, MessageCircle, Star } from "lucide-react";
import {
  ProductItem,
  formatInr,
  getDiscountPercent,
  getEffectivePrice,
} from "../data/catalog";
import { openWhatsAppProductEnquiry } from "../utils/whatsapp";

interface ProductCardProps {
  product: ProductItem;
  isWishlisted: boolean;
  onToggleWishlist: (productId: string) => void;
  onSelectProduct: (product: ProductItem) => void;
}

export const ProductCard: React.FC<ProductCardProps> = ({
  product,
  isWishlisted,
  onToggleWishlist,
  onSelectProduct,
}) => {
  const effectivePrice = getEffectivePrice(product);
  const discountPct = getDiscountPercent(product);
  const badgeText =
    product.offerBadge || (discountPct > 0 ? `${discountPct}% OFF` : "");

  return (
    <article className="product-card">
      <div
        className="product-media"
        onClick={() => onSelectProduct(product)}
        role="button"
        tabIndex={0}
        onKeyDown={(e) => {
          if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            onSelectProduct(product);
          }
        }}
        aria-label={`View ${product.name}`}
      >
        <img src={product.images[0]} alt={product.name} loading="lazy" />

        {badgeText && <span className="badge-offer">{badgeText}</span>}

        <button
          type="button"
          className="wishlist-fab"
          onClick={(e) => {
            e.stopPropagation();
            onToggleWishlist(product.id);
          }}
          aria-label={
            isWishlisted
              ? `Remove ${product.name} from wishlist`
              : `Add ${product.name} to wishlist`
          }
        >
          <Heart
            size={18}
            fill={isWishlisted ? "#d4af37" : "none"}
            color={isWishlisted ? "#d4af37" : "#f7f4ee"}
          />
        </button>

        {product.madeToOrder && (
          <span className="badge-mto">Made-to-Order</span>
        )}
      </div>

      <div className="product-body">
        <div
          style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            gap: "6px",
          }}
        >
          <span className="product-cat">{product.category}</span>
          <span
            style={{
              display: "inline-flex",
              alignItems: "center",
              gap: "4px",
              fontSize: "0.75rem",
              color: "var(--gold-champagne)",
            }}
          >
            <Star size={13} fill="#d4af37" color="#d4af37" />
            {product.rating.toFixed(1)}
          </span>
        </div>

        <h3
          className="product-name"
          onClick={() => onSelectProduct(product)}
        >
          {product.name}
        </h3>

        <p className="product-desc-short">{product.description}</p>

        <div className="price-row">
          <span className="price-now">{formatInr(effectivePrice)}</span>
          {discountPct > 0 && (
            <span className="price-mrp">{formatInr(product.price)}</span>
          )}
        </div>

        <div className="card-actions">
          <button
            type="button"
            className="btn-outline-gold"
            style={{ padding: "8px 10px", fontSize: "0.8rem" }}
            onClick={() => onSelectProduct(product)}
          >
            Details
          </button>
          <button
            type="button"
            className="btn-whatsapp"
            style={{ padding: "8px 10px", fontSize: "0.8rem" }}
            onClick={() => openWhatsAppProductEnquiry(product)}
            aria-label={`WhatsApp enquiry for ${product.name}`}
          >
            <MessageCircle size={15} />
            Enquire
          </button>
        </div>
      </div>
    </article>
  );
};
