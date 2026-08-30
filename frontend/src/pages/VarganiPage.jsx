import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { Download, Search, Settings, Share2, Filter, Plus, Edit2, X } from "lucide-react";
import { AppShell } from "@/components/AppShell";
import * as api from "@/api/index";

import { useAuth } from "@/context/AuthContext";
import { useLang } from "@/context/LangContext";

const inr = (n) => "₹" + Number(n).toLocaleString("en-IN", { maximumFractionDigits: 0 });

const methodLabel = {
  CASH: "Cash",
  UPI: "UPI",
  BANK_TRANSFER: "Bank",
  CHEQUE: "Cheque",
};

export default function VarganiPage() {
  const { user, mandalName } = useAuth();
  const { t } = useLang();
  const location = useLocation();
  const [contributions, setContributions] = useState([]);
  const [search, setSearch] = useState("");
  const [showAddForm, setShowAddForm] = useState(location.state?.openForm || false);
  const [editItem, setEditItem] = useState(null);
  const [selectedReceipt, setSelectedReceipt] = useState(null);
  const [loading, setLoading] = useState(true);

  // Clear state so it doesn't reopen on refresh
  useEffect(() => {
    if (location.state?.openForm) {
      window.history.replaceState({}, document.title);
    }
  }, [location]);

  const loadContributions = () => {
    api.getContributions()
      .then((res) => setContributions(res.data || res || []))
      .catch((err) => console.error("Failed to load contributions:", err))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadContributions();
  }, []);

  const filtered = contributions.filter(
    (c) =>
      c.memberName?.toLowerCase().includes(search.toLowerCase()) ||
      c.receiptNo?.toLowerCase().includes(search.toLowerCase())
  );

  const total = filtered.reduce((s, c) => s + Number(c.amount), 0);
  const canEdit = user?.role === "ADMIN" || user?.role === "KARYAKARTA";

  return (
    <AppShell title={t("vargani")}>
      <div className="flex h-full flex-col p-4 pt-6">
        <div className="mb-6 rounded-3xl bg-black p-6 text-white shadow-xl">
          <p className="text-sm font-medium text-white/70 uppercase tracking-widest">{mandalName || t("mandalName")}</p>
          <h1 className="mt-2 font-display text-4xl font-bold tracking-tight">{t("vargani")}</h1>
          <p className="mt-2 font-medium text-white/80">{filtered.length} {t("entries")} · {inr(total)} {t("collected")}</p>
        </div>

        <div className="mb-6 flex gap-3">
          <div className="relative flex-1">
            <Search className="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
            <input
              type="text"
              placeholder={t("searchNameOrReceipt")}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full rounded-2xl border border-input bg-background/50 py-3.5 pl-12 pr-4 text-sm outline-none transition-colors focus:border-primary"
            />
          </div>
          <button className="flex h-[50px] w-[50px] shrink-0 items-center justify-center rounded-2xl border border-input bg-background/50 text-muted-foreground transition-colors hover:text-foreground">
            <Filter className="h-5 w-5" />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto pb-24">
          {loading ? (
            <div className="flex h-40 items-center justify-center">
              <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-r-transparent" />
            </div>
          ) : (
            <div className="space-y-3">
              {filtered.map((c) => (
                <div key={c.id} className="surface-lift flex items-center justify-between rounded-3xl p-4">
                  <div
                    className="flex flex-1 items-center gap-4 cursor-pointer"
                    onClick={() => setSelectedReceipt(c)}
                  >
                    <div className="flex h-12 w-12 items-center justify-center rounded-full bg-primary/10 font-display font-semibold text-primary">
                      {c.memberName?.charAt(0) || "M"}
                    </div>
                    <div>
                      <p className="font-semibold text-foreground">{c.memberName}</p>
                      <p className="text-xs text-muted-foreground">{c.receiptNo} · {methodLabel[c.paymentMethod] || c.paymentMethod}</p>
                      {c.collectedByName && <p className="text-[10px] text-muted-foreground mt-0.5">{t("collectedBy") || "Collected by"}: {c.collectedByName}</p>}
                    </div>
                  </div>
                  <div className="text-right flex flex-col items-end gap-1">
                    <div className="flex items-center gap-2">
                      <p className="font-display font-semibold text-emerald-600">+{inr(c.amount)}</p>
                      {canEdit && (
                        <button onClick={(e) => { e.stopPropagation(); setEditItem(c); }} className="text-muted-foreground hover:text-primary transition-colors">
                          <Edit2 className="h-4 w-4" />
                        </button>
                      )}
                    </div>
                    <p className="text-[11px] text-muted-foreground">{c.contributionDate || c.date}</p>
                  </div>
                </div>
              ))}
              
              {filtered.length === 0 && (
                <div className="mt-12 text-center text-muted-foreground">
                  <p>No vargani found.</p>
                </div>
              )}
            </div>
          )}
        </div>

        {canEdit && (
          <div className="fixed inset-x-0 bottom-24 z-40 pointer-events-none">
            <div className="relative mx-auto w-full max-w-xl px-5 sm:max-w-2xl">
              <button
                onClick={() => setShowAddForm(true)}
                className="accent-gradient absolute right-5 bottom-0 flex h-14 w-14 items-center justify-center rounded-full text-white shadow-lg shadow-primary/30 transition-transform active:scale-95 pointer-events-auto"
                aria-label="Add Vargani"
              >
                <Plus className="h-6 w-6" />
              </button>
            </div>
          </div>
        )}
      </div>

      {(showAddForm || editItem) && (
        <AddVarganiSheet
          initialData={editItem}
          onClose={() => { setShowAddForm(false); setEditItem(null); }}
          onSuccess={() => {
            setShowAddForm(false);
            setEditItem(null);
            loadContributions();
          }}
        />
      )}

      {selectedReceipt && (
        <ReceiptSheet c={selectedReceipt} onClose={() => setSelectedReceipt(null)} />
      )}
    </AppShell>
  );
}

function AddVarganiSheet({ onClose, onSuccess, initialData }) {
  const { user } = useAuth();
  const { t } = useLang();
  const isEdit = !!initialData;
  const [formData, setFormData] = useState({
    memberName: initialData?.memberName || "",
    amount: initialData?.amount || "",
    paymentMethod: initialData?.paymentMethod || "CASH",
    collectedByName: initialData?.collectedByName || "",
    note: initialData?.note || "",
    contributionDate: initialData?.contributionDate || initialData?.date || new Date().toISOString().split("T")[0],
    roomNumber: initialData?.roomNumber || "",
    floorNumber: initialData?.floorNumber || "",
    phone: initialData?.phone || "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const payload = {
        ...formData,
        amount: Number(formData.amount),
        collectedBy: user?.id,
        memberId: initialData?.memberId || null,
        roomNumber: formData.roomNumber?.trim() || null,
        floorNumber: formData.floorNumber ? Number(formData.floorNumber) : null,
      };
      
      let res;
      if (isEdit) {
        res = await api.updateContribution(initialData.id, payload);
      } else {
        res = await api.addContribution(payload);
      }
      onSuccess(res?.data || res);
    } catch (err) {
      setError(err.message || "Failed to add vargani");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[60] flex items-end justify-center">
      <button
        aria-label="Close form"
        onClick={onClose}
        className="absolute inset-0 bg-foreground/45 backdrop-blur-sm animate-in fade-in"
      />
      <div className="relative w-full max-w-xl animate-in slide-in-from-bottom-6 duration-300">
        <div className="glass rounded-t-3xl px-5 pb-8 pt-4">
          <div className="mx-auto mb-4 h-1 w-10 rounded-full bg-muted-foreground/40" />
          <div className="mb-4 flex items-center justify-between">
            <h2 className="font-display text-lg font-semibold">{isEdit ? t("editVargani") : t("newVargani")}</h2>
            <button
              onClick={onClose}
              className="flex h-9 w-9 items-center justify-center rounded-full bg-secondary"
              aria-label="Close"
            >
              <X className="h-4 w-4" />
            </button>
          </div>

          {error && (
            <div className="mb-4 rounded-xl bg-destructive/10 p-3 text-sm font-medium text-destructive">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-foreground">{t("memberName")}</label>
              <input
                required
                type="text"
                value={formData.memberName}
                onChange={e => setFormData({...formData, memberName: e.target.value})}
                className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                placeholder="Ramesh Jadhav"
              />
            </div>
            
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-foreground">
                Phone Number
                <span className="ml-1 text-[10px] text-muted-foreground font-normal">(optional)</span>
              </label>
              <input
                type="tel"
                value={formData.phone}
                onChange={e => setFormData({...formData, phone: e.target.value})}
                className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                placeholder="9876543210"
              />
            </div>

            {/* Room info — optional, syncs with room tracker */}
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-foreground">
                  {t("roomNumber")}
                  <span className="ml-1 text-[10px] text-muted-foreground font-normal">(optional)</span>
                </label>
                <input
                  type="text"
                  value={formData.roomNumber}
                  onChange={e => setFormData({...formData, roomNumber: e.target.value})}
                  className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                  placeholder="4"
                />
              </div>
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-foreground">
                  {t("floor")}
                  <span className="ml-1 text-[10px] text-muted-foreground font-normal">(0 = Owner)</span>
                </label>
                <input
                  type="number"
                  min="0"
                  value={formData.floorNumber}
                  onChange={e => setFormData({...formData, floorNumber: e.target.value})}
                  className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                  placeholder="0 = Owner, 1-3 = Floor"
                />
              </div>
            </div>
            
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-foreground">{t("amount")} (₹)</label>
                <input
                  required
                  type="number"
                  min="1"
                  value={formData.amount}
                  onChange={e => setFormData({...formData, amount: e.target.value})}
                  className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                  placeholder="501"
                />
              </div>
              <div className="space-y-1.5">
                <label className="text-sm font-medium text-foreground">{t("date")}</label>
                <input
                  required
                  type="date"
                  value={formData.contributionDate}
                  onChange={e => setFormData({...formData, contributionDate: e.target.value})}
                  className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <label className="text-sm font-medium text-foreground">{t("collectedBy") || "Collected By"}</label>
              <input
                type="text"
                value={formData.collectedByName}
                onChange={e => setFormData({...formData, collectedByName: e.target.value})}
                className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
                placeholder="Optional (e.g., Harshad)"
              />
            </div>

            <div className="space-y-1.5">
              <label className="text-sm font-medium text-foreground">{t("paymentMethod")}</label>
              <select
                value={formData.paymentMethod}
                onChange={e => setFormData({...formData, paymentMethod: e.target.value})}
                className="w-full rounded-2xl border border-input bg-background/50 px-4 py-3 text-sm outline-none focus:border-primary"
              >
                <option value="CASH">{t("cash")}</option>
                <option value="UPI">{t("upi")}</option>
                <option value="BANK_TRANSFER">{t("bankTransfer")}</option>
                <option value="CHEQUE">{t("cheque")}</option>
              </select>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="accent-gradient mt-4 flex min-h-[48px] w-full items-center justify-center rounded-2xl text-sm font-semibold text-primary-foreground disabled:opacity-70"
            >
              {loading ? t("saving") : isEdit ? t("saveChanges") : t("saveContribution")}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

function ReceiptSheet({ c, onClose }) {
  if (!c) return null;
  return (
    <div className="fixed inset-0 z-[70] flex items-end justify-center">
      <button onClick={onClose} className="absolute inset-0 bg-foreground/45 backdrop-blur-sm animate-in fade-in" />
      <div className="relative w-full max-w-xl animate-in slide-in-from-bottom-6 duration-300">
        <div className="glass rounded-t-3xl px-6 pb-12 pt-6">
          <div className="mx-auto mb-6 h-1.5 w-12 rounded-full bg-muted-foreground/30" />
          
          <div className="rounded-2xl border border-border bg-card p-6 shadow-sm">
            <div className="text-center">
              <div className="mx-auto mb-3 flex h-14 w-14 items-center justify-center rounded-full bg-primary/10">
                <span className="font-display text-xl font-bold text-primary">₹</span>
              </div>
              <h3 className="font-display text-2xl font-bold">{inr(c.amount)}</h3>
              <p className="text-sm font-medium text-emerald-600">Successfully Received</p>
            </div>

            <dl className="mt-6 space-y-2 border-t border-dashed border-border pt-4 text-sm">
              <Row k="Received from" v={c.memberName} />
              <Row k="Receipt no." v={c.receiptNo} />
              <Row k="Method" v={methodLabel[c.paymentMethod] || c.paymentMethod} />
              <Row k="Date" v={c.contributionDate || c.date} />
              {c.collectedByName && <Row k="Collected by" v={c.collectedByName} />}
              {c.roomNumber && (
                <Row k="Room / Floor" v={`Room ${c.roomNumber} · ${c.floorNumber === 0 ? "Owner" : `Floor ${c.floorNumber ?? "-"}`}`} />
              )}
            </dl>
            <p className="mt-5 text-center text-[11px] text-muted-foreground">
              श्री गणेशाय नमः · Thank you for your contribution
            </p>
          </div>

          <div className="mt-6 flex gap-3">
            <button 
              className="flex h-12 flex-1 items-center justify-center gap-2 rounded-xl bg-[#25D366] text-sm font-semibold text-white hover:bg-[#1ea952] shadow-sm shadow-[#25D366]/20"
              onClick={() => {
                if (!c.receiptPdfUrl) {
                  alert("Receipt PDF not found. Please re-generate or check older records.");
                  return;
                }
                const text = `*Contribution Receipt: ${c.receiptNo}*\nReceived from: ${c.memberName}\nAmount: ${inr(c.amount)}\n\nThank you for your contribution!\n\nDownload Receipt: ${api.getMediaUrl(c.receiptPdfUrl)}`;
                window.open(`https://wa.me/?text=${encodeURIComponent(text)}`, '_blank');
              }}
            >
              <svg viewBox="0 0 24 24" fill="currentColor" className="h-5 w-5">
                <path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347m-5.421 7.403h-.004a9.87 9.87 0 01-5.031-1.378l-.361-.214-3.741.982.998-3.648-.235-.374a9.86 9.86 0 01-1.51-5.26c.001-5.45 4.436-9.884 9.888-9.884 2.64 0 5.122 1.03 6.988 2.898a9.825 9.825 0 012.893 6.994c-.003 5.45-4.437 9.884-9.885 9.884m8.413-18.297A11.815 11.815 0 0012.05 0C5.495 0 .16 5.335.157 11.892c0 2.096.547 4.142 1.588 5.945L.057 24l6.305-1.654a11.882 11.882 0 005.683 1.448h.005c6.554 0 11.89-5.335 11.893-11.893a11.821 11.821 0 00-3.48-8.413z"/>
              </svg>
              WhatsApp
            </button>
            <button 
              className="flex h-12 flex-1 items-center justify-center gap-2 rounded-xl bg-primary text-sm font-semibold text-primary-foreground hover:bg-primary/90"
              onClick={() => {
                if (c.receiptPdfUrl) {
                  api.downloadReceipt(c.receiptPdfUrl).catch(err => alert("Download failed: " + err.message));
                } else {
                  alert("Receipt PDF not found for this contribution.");
                }
              }}
            >
              <Download className="h-4 w-4" /> Download PDF
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

function Row({ k, v }) {
  return (
    <div className="flex justify-between py-1">
      <dt className="text-muted-foreground">{k}</dt>
      <dd className="font-medium text-foreground text-right">{v}</dd>
    </div>
  );
}
