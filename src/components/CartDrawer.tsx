import React, { useMemo, useState } from "react";
import {
  MessageCircle,
  Minus,
  Plus,
  ShoppingBag,
  Tag,
  Trash2,
  X,
} from "lucide-react";
import { OFFERS, formatInr } from "../data/catalog";
import { CartEntry } from "../utils/storage";
import { openWhatsAppCartEnquiry } from "../utils/whatsapp";

interface CartDrawerProps {
  isOpen: boolean;
  items: CartEntry[];
  onClose: () => void;
  onUpdateQty: (cartKey: string, delta: number) => void;
  onRemoveItem: (cartKey: string) => void;
  onClearCart: () => void;
  onNotify: (message: string) => void;
}

export const CartDrawer: React.FC<CartDrawerProps> = ({
  isOpen,
  items,
  onClose,
  onUpdateQty,
  onRemoveItem,
  onClearCart,
  onNotify,
}) => {
  const [promoCode, setPromoCode] = useState("");
  const [appliedCode, setAppliedCode] = useState("");
  const [customerName, setCustomerName] = useState("");
  const [customerPhone, setCustomerPhone] = useState("");
  const [shippingAddress, setShippingAddress] = useState("");

  const subtotal = useMemo(
    () => items.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0),
    [items]
  );

  const matchedOffer = useMemo(
    () =>
      OFFERS.find(
        (o) =>
          o.promoCode.toUpperCase() === appliedCode.trim().toUpperCase() &&
          subtotal >= o.minOrderAmount
      ),
    [appliedCode, subtotal]
  );

  const discount = matchedOffer
    ? Math.round((subtotal * matchedOffer.discountPercent) / 100)
    : 0;
  const finalTotal = Math.max(0, subtotal - discount);

  if (!isOpen) return null;

  const handleApplyPromo = (codeToApply: string) => {
    const clean = codeToApply.trim().toUpperCase();
    const offer = OFFERS.find((o) => o.promoCode.toUpperCase() === clean);
    if (!offer) {
      onNotify("Invalid promo code. Try RAZA15 or BRIDAL10.");
      return;
    }
    if (subtotal < offer.minOrderAmount) {
      onNotify(
        `${offer.promoCode} applies on orders above ${formatInr(
          offer.minOrderAmount
        )}.`
      );
      return;
    }
    setPromoCode(offer.promoCode);
    setAppliedCode(offer.promoCode);
    onNotify(`Code ${offer.promoCode} applied (${offer.discountPercent}% off)!`);
  };

  return (
    <div
      className="drawer-backdrop"
      onClick={onClose}
      role="dialog"
      aria-modal="true"
      aria-label="Shopping Bag"
    >
      <div className="drawer-panel" onClick={(e) => e.stopPropagation()}>
        <div
          style={{
            padding: "16px",
            borderBottom: "1px solid var(--border-gold)",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
          }}
        >
          <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
            <ShoppingBag size={20} color="#d4af37" />
            <div>
              <h2 className="font-serif" style={{ margin: 0, fontSize: "1.35rem" }}>
                Your Couture Bag
              </h2>
              <span style={{ fontSize: "0.74rem", color: "var(--silver-muted)" }}>
                Saved automatically in your browser
              </span>
            </div>
          </div>
          <button
            type="button"
            className="icon-btn"
            onClick={onClose}
            aria-label="Close Bag"
          >
            <X size={19} />
          </button>
        </div>

        <div className="drawer-scroll">
          {items.length === 0 ? (
            <div
              style={{
                textAlign: "center",
                padding: "48px 16px",
                color: "var(--silver-muted)",
              }}
            >
              <ShoppingBag
                size={42}
                color="#d4af37"
                style={{ marginBottom: "12px", opacity: 0.8 }}
              />
              <p style={{ color: "var(--ivory-text)", fontWeight: 600 }}>
                Your shopping bag is empty
              </p>
              <p style={{ fontSize: "0.85rem", lineHeight: 1.5 }}>
                Explore our Bridal Lehengas, Lucknawi Ghararas, Pakistani Suits,
                and Banarasi Sarees to curate your wardrobe.
              </p>
              <button
                type="button"
                className="btn-gold"
                style={{ marginTop: "10px" }}
                onClick={onClose}
              >
                Browse Couture
              </button>
            </div>
          ) : (
            <>
              {items.map((item) => (
                <div
                  key={item.cartKey}
                  style={{
                    background: "var(--bg-onyx)",
                    border: "1px solid var(--border-subtle)",
                    borderRadius: "14px",
                    padding: "12px",
                    display: "flex",
                    gap: "12px",
                  }}
                >
                  <img
                    src={item.productImage}
                    alt={item.productName}
                    style={{
                      width: "76px",
                      height: "96px",
                      borderRadius: "10px",
                      objectFit: "cover",
                      flexShrink: 0,
                    }}
                  />
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div
                      style={{
                        display: "flex",
                        justifyContent: "space-between",
                        gap: "8px",
                      }}
                    >
                      <h4
                        style={{
                          margin: "0 0 4px",
                          fontSize: "0.9rem",
                          lineHeight: 1.3,
                        }}
                      >
                        {item.productName}
                      </h4>
                      <button
                        type="button"
                        onClick={() => onRemoveItem(item.cartKey)}
                        aria-label={`Remove ${item.productName}`}
                        style={{
                          background: "none",
                          border: "none",
                          color: "var(--silver-muted)",
                          padding: "2px",
                        }}
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                    <div
                      style={{
                        fontSize: "0.75rem",
                        color: "var(--silver-muted)",
                        marginBottom: "6px",
                      }}
                    >
                      Size: {item.selectedSize} • {item.selectedColor}
                      {item.customStitching ? " • Custom Stitched" : ""}
                    </div>
                    {item.customMeasurements && (
                      <div
                        style={{
                          fontSize: "0.72rem",
                          color: "var(--gold-champagne)",
                          marginBottom: "6px",
                        }}
                      >
                        Notes: {item.customMeasurements}
                      </div>
                    )}
                    <div
                      style={{
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "space-between",
                        marginTop: "6px",
                      }}
                    >
                      <span
                        style={{
                          fontWeight: 700,
                          color: "var(--gold-champagne)",
                          fontSize: "0.95rem",
                        }}
                      >
                        {formatInr(item.unitPrice * item.quantity)}
                      </span>
                      <div
                        style={{
                          display: "inline-flex",
                          alignItems: "center",
                          gap: "8px",
                          background: "var(--bg-charcoal)",
                          borderRadius: "10px",
                          padding: "4px 8px",
                          border: "1px solid var(--border-subtle)",
                        }}
                      >
                        <button
                          type="button"
                          onClick={() => onUpdateQty(item.cartKey, -1)}
                          aria-label="Decrease quantity"
                          style={{
                            background: "none",
                            border: "none",
                            color: "var(--ivory-text)",
                            display: "flex",
                          }}
                        >
                          <Minus size={14} />
                        </button>
                        <span style={{ fontSize: "0.85rem", fontWeight: 700 }}>
                          {item.quantity}
                        </span>
                        <button
                          type="button"
                          onClick={() => onUpdateQty(item.cartKey, 1)}
                          aria-label="Increase quantity"
                          style={{
                            background: "none",
                            border: "none",
                            color: "var(--ivory-text)",
                            display: "flex",
                          }}
                        >
                          <Plus size={14} />
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              ))}

              {/* Promo Code */}
              <div
                style={{
                  background: "var(--bg-onyx)",
                  borderRadius: "14px",
                  padding: "12px",
                  border: "1px solid var(--border-subtle)",
                }}
              >
                <div
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: "6px",
                    fontSize: "0.8rem",
                    fontWeight: 600,
                    color: "var(--gold-champagne)",
                    marginBottom: "8px",
                  }}
                >
                  <Tag size={14} />
                  Apply Couture Offer Code
                </div>
                <div style={{ display: "flex", gap: "8px" }}>
                  <input
                    type="text"
                    className="lux-input"
                    placeholder="RAZA15 or BRIDAL10"
                    value={promoCode}
                    onChange={(e) => setPromoCode(e.target.value)}
                    style={{ minHeight: "40px" }}
                  />
                  <button
                    type="button"
                    className="btn-outline-gold"
                    style={{ minHeight: "40px", padding: "8px 14px" }}
                    onClick={() => handleApplyPromo(promoCode)}
                  >
                    Apply
                  </button>
                </div>
                <div style={{ display: "flex", gap: "6px", marginTop: "8px", flexWrap: "wrap" }}>
                  {OFFERS.map((offer) => (
                    <button
                      key={offer.id}
                      type="button"
                      className="filter-chip"
                      style={{ fontSize: "0.72rem", padding: "4px 10px" }}
                      onClick={() => handleApplyPromo(offer.promoCode)}
                    >
                      Use {offer.promoCode} ({offer.discountPercent}% off)
                    </button>
                  ))}
                </div>
              </div>

              {/* Optional Enquiry Details for WhatsApp / COD */}
              <div
                style={{
                  background: "var(--bg-onyx)",
                  borderRadius: "14px",
                  padding: "12px",
                  border: "1px solid var(--border-subtle)",
                  display: "grid",
                  gap: "8px",
                }}
              >
                <div
                  style={{
                    fontSize: "0.8rem",
                    fontWeight: 600,
                    color: "var(--gold-champagne)",
                  }}
                >
                  Optional Delivery Details (for WhatsApp / COD Enquiry)
                </div>
                <input
                  type="text"
                  className="lux-input"
                  placeholder="Your Name"
                  value={customerName}
                  onChange={(e) => setCustomerName(e.target.value)}
                  style={{ minHeight: "40px" }}
                />
                <input
                  type="tel"
                  className="lux-input"
                  placeholder="WhatsApp / Mobile Number"
                  value={customerPhone}
                  onChange={(e) => setCustomerPhone(e.target.value)}
                  style={{ minHeight: "40px" }}
                />
                <input
                  type="text"
                  className="lux-input"
                  placeholder="City & Delivery Address"
                  value={shippingAddress}
                  onChange={(e) => setShippingAddress(e.target.value)}
                  style={{ minHeight: "40px" }}
                />
              </div>
            </>
          )}
        </div>

        {items.length > 0 && (
          <div
            style={{
              padding: "14px 16px",
              borderTop: "1px solid var(--border-gold)",
              background: "var(--bg-obsidian)",
              display: "grid",
              gap: "10px",
            }}
          >
            <div style={{ display: "grid", gap: "4px", fontSize: "0.86rem" }}>
              <div style={{ display: "flex", justifyContent: "space-between" }}>
                <span style={{ color: "var(--silver-muted)" }}>Subtotal</span>
                <span>{formatInr(subtotal)}</span>
              </div>
              {discount > 0 && (
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    color: "var(--emerald)",
                  }}
                >
                  <span>Offer ({appliedCode})</span>
                  <span>-{formatInr(discount)}</span>
                </div>
              )}
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  fontWeight: 700,
                  fontSize: "1.05rem",
                  color: "var(--gold-champagne)",
                  paddingTop: "4px",
                }}
              >
                <span>Estimated Total</span>
                <span>{formatInr(finalTotal)}</span>
              </div>
            </div>

            <button
              type="button"
              className="btn-whatsapp"
              style={{ width: "100%" }}
              onClick={() =>
                openWhatsAppCartEnquiry(
                  items,
                  subtotal,
                  discount,
                  appliedCode,
                  finalTotal,
                  customerName,
                  customerPhone,
                  shippingAddress
                )
              }
            >
              <MessageCircle size={18} />
              Order &amp; Enquire on WhatsApp (COD Available)
            </button>

            <button
              type="button"
              onClick={onClearCart}
              style={{
                background: "none",
                border: "none",
                color: "var(--silver-muted)",
                fontSize: "0.78rem",
                textDecoration: "underline",
              }}
            >
              Clear Shopping Bag
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
